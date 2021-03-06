/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2014  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.cpa.arg;

import static org.sosy_lab.cpachecker.util.AbstractStates.getOutgoingEdges;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import java.util.Collection;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.blocks.BlockPartitioning;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.core.Specification;
import org.sosy_lab.cpachecker.core.defaults.AbstractSingleWrapperCPA;
import org.sosy_lab.cpachecker.core.defaults.AutomaticCPAFactory;
import org.sosy_lab.cpachecker.core.defaults.FlatLatticeDomain;
import org.sosy_lab.cpachecker.core.defaults.MergeSepOperator;
import org.sosy_lab.cpachecker.core.defaults.SimplePrecisionAdjustment;
import org.sosy_lab.cpachecker.core.interfaces.AbstractDomain;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.CPAFactory;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysisWithBAM;
import org.sosy_lab.cpachecker.core.interfaces.ForcedCoveringStopOperator;
import org.sosy_lab.cpachecker.core.interfaces.MergeOperator;
import org.sosy_lab.cpachecker.core.interfaces.PrecisionAdjustment;
import org.sosy_lab.cpachecker.core.interfaces.Reducer;
import org.sosy_lab.cpachecker.core.interfaces.StateSpacePartition;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.interfaces.TransferRelation;
import org.sosy_lab.cpachecker.core.interfaces.pcc.ProofChecker;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;

@Options
public class ARGCPA extends AbstractSingleWrapperCPA implements
    ConfigurableProgramAnalysisWithBAM, ProofChecker {

  public static CPAFactory factory() {
    return AutomaticCPAFactory.forType(ARGCPA.class);
  }

  @Option(secure=true, name="cpa.arg.inCPAEnabledAnalysis",
  description="inform ARG CPA if it is run in an analysis with enabler CPA because then it must "
    + "behave differently during merge.")
  private boolean inCPAEnabledAnalysis = false;

  @Option(secure=true, name="cpa.arg.deleteInCPAEnabledAnalysis",
      description="inform merge operator in CPA enabled analysis that it should delete the subgraph of the merged node "
        + "which is required to get at most one successor per CFA edge.")
      private boolean deleteInCPAEnabledAnalysis = false;

  @Option(
    secure = true,
    name = "cpa.arg.keepCoveredStatesInReached",
    description =
        "whether to keep covered states in the reached set as addition to keeping them in the ARG"
  )
  private boolean keepCoveredStatesInReached = false;

  private final MergeOperator merge;

  private final LogManager logger;

  private final ARGStatistics stats;

  private ARGCPA(
      ConfigurableProgramAnalysis cpa,
      Configuration config,
      LogManager logger,
      Specification pSpecification,
      CFA cfa)
      throws InvalidConfigurationException {
    super(cpa);
    config.inject(this);
    this.logger = logger;

    MergeOperator wrappedMergeOperator = getWrappedCpa().getMergeOperator();
    if (wrappedMergeOperator == MergeSepOperator.getInstance()) {
      merge =  MergeSepOperator.getInstance();
    } else if (inCPAEnabledAnalysis) {
      merge =  new ARGMergeJoinCPAEnabledAnalysis(wrappedMergeOperator, deleteInCPAEnabledAnalysis);
    } else {
      merge = new ARGMergeJoin(wrappedMergeOperator, cpa.getAbstractDomain(), config);
    }

    stats = new ARGStatistics(config, logger, this, pSpecification, cfa);
  }

  @Override
  public AbstractDomain getAbstractDomain() {
    return new FlatLatticeDomain();
  }

  @Override
  public TransferRelation getTransferRelation() {
    return new ARGTransferRelation(getWrappedCpa().getTransferRelation());
  }

  @Override
  public MergeOperator getMergeOperator() {
    return merge;
  }

  @Override
  public ForcedCoveringStopOperator getStopOperator() {
    return new ARGStopSep(
        getWrappedCpa().getStopOperator(),
        logger,
        inCPAEnabledAnalysis,
        keepCoveredStatesInReached);
  }

  @Override
  public PrecisionAdjustment getPrecisionAdjustment() {
    PrecisionAdjustment wrappedPrec = getWrappedCpa().getPrecisionAdjustment();
    if (wrappedPrec instanceof SimplePrecisionAdjustment) {
      return new ARGSimplePrecisionAdjustment((SimplePrecisionAdjustment) wrappedPrec);
    } else {
      return new ARGPrecisionAdjustment(wrappedPrec, inCPAEnabledAnalysis, stats);
    }
  }

  @Override
  public Reducer getReducer() throws InvalidConfigurationException {
    ConfigurableProgramAnalysis cpa = getWrappedCpa();
    Preconditions.checkState(
        cpa instanceof ConfigurableProgramAnalysisWithBAM,
        "wrapped CPA does not support BAM: " + cpa.getClass().getCanonicalName());
    return new ARGReducer(((ConfigurableProgramAnalysisWithBAM) cpa).getReducer());
  }

  @Override
  public AbstractState getInitialState(CFANode pNode, StateSpacePartition pPartition) throws InterruptedException {
    // TODO some code relies on the fact that this method is called only one and the result is the root of the ARG
    return new ARGState(getWrappedCpa().getInitialState(pNode, pPartition), null);
  }

  protected LogManager getLogger() {
    return logger;
  }

  @Override
  public void collectStatistics(Collection<Statistics> pStatsCollection) {
    if (!Iterables.any(pStatsCollection, Predicates.instanceOf(ARGStatistics.class))) {
      // we do not want to add ARGStatistics twice, if a wrapping CPA also uses it.
      // This would result in overriding the output-files due to equal file names.
      // Info: this case is one of the reasons to first collect our own statistics
      // and afterwards call super.collectStatistics().
      pStatsCollection.add(stats);
    }
    super.collectStatistics(pStatsCollection);
  }

  ARGStatistics getARGExporter() {
    return stats;
  }

  @Override
  public boolean areAbstractSuccessors(AbstractState pElement, CFAEdge pCfaEdge,
      Collection<? extends AbstractState> pSuccessors) throws CPATransferException, InterruptedException {
    Preconditions.checkState(
        getWrappedCpa() instanceof ProofChecker,
        "Wrapped CPA has to implement ProofChecker interface");
    ProofChecker wrappedProofChecker = (ProofChecker)getWrappedCpa();
    ARGState element = (ARGState) pElement;

    assert Iterables.elementsEqual(element.getChildren(), pSuccessors);

    AbstractState wrappedState = element.getWrappedState();
    Multimap<CFAEdge, AbstractState> wrappedSuccessors = HashMultimap.create();
    for (AbstractState absElement : pSuccessors) {
      ARGState successorElem = (ARGState) absElement;
      wrappedSuccessors.put(element.getEdgeToChild(successorElem), successorElem.getWrappedState());
    }

    if (pCfaEdge != null) {
      return wrappedProofChecker.areAbstractSuccessors(
          wrappedState, pCfaEdge, wrappedSuccessors.get(pCfaEdge));
    }

    for (CFAEdge edge : getOutgoingEdges(element)) {
      if (!wrappedProofChecker.areAbstractSuccessors(
          wrappedState, edge, wrappedSuccessors.get(edge))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isCoveredBy(AbstractState pElement, AbstractState pOtherElement) throws CPAException, InterruptedException {
    Preconditions.checkState(
        getWrappedCpa() instanceof ProofChecker,
        "Wrapped CPA has to implement ProofChecker interface");
    ProofChecker wrappedProofChecker = (ProofChecker)getWrappedCpa();
    AbstractState wrappedState = ((ARGState) pElement).getWrappedState();
    AbstractState wrappedOtherElement = ((ARGState) pOtherElement).getWrappedState();
    return wrappedProofChecker.isCoveredBy(wrappedState, wrappedOtherElement);
  }

  @Override
  public void setPartitioning(BlockPartitioning partitioning) {
    ConfigurableProgramAnalysis cpa = getWrappedCpa();
    assert cpa instanceof ConfigurableProgramAnalysisWithBAM;
    ((ConfigurableProgramAnalysisWithBAM) cpa).setPartitioning(partitioning);
  }

  @Override
  public boolean isCoveredByRecursiveState(AbstractState state1, AbstractState state2)
      throws CPAException, InterruptedException {
    return ((ConfigurableProgramAnalysisWithBAM) getWrappedCpa())
        .isCoveredByRecursiveState(
            ((ARGState) state1).getWrappedState(), ((ARGState) state2).getWrappedState());
  }
}
