/*
 * CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.policyiteration;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.sosy_lab.cpachecker.cfa.blocks.Block;
import org.sosy_lab.cpachecker.cfa.blocks.ReferencedVariable;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.FunctionExitNode;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.Reducer;
import org.sosy_lab.cpachecker.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BAM reduction for LPI.
 */
public class PolicyReducer implements Reducer {

  /**
   * Remove all information from the {@code expandedState} which is not
   * relevant to {@code context}.
   */
  @Override
  public AbstractState getVariableReducedState(
      AbstractState expandedState, Block context, CFANode callNode) {
    PolicyState pState = (PolicyState) expandedState;

    if (!pState.isAbstract()) {
      // Intermediate states stay as-is.
      return pState;
    }

    PolicyAbstractedState aState = pState.asAbstracted();
    Set<String> blockVars = getBlockVariables(context);

    // TODO: what about congruence?
    // Also can't we finally move it to a different CPA?
    // problem : might require second abstraction, which is somewhat
    // undesirable.
    Map<Template, PolicyBound> newAbstraction = Maps.filterKeys(
        aState.getAbstraction(),
        template -> !Sets.intersection(
            template.getUsedVars().collect(Collectors.toSet()), blockVars
        ).isEmpty()
    );
    return aState.withNewAbstraction(newAbstraction);
  }

  @Override
  public AbstractState getVariableExpandedState(
      AbstractState rootState,
      Block reducedContext,
      AbstractState reducedState) {
    PolicyState pRootState = (PolicyState) rootState;
    PolicyState pReducedState = (PolicyState) reducedState;

    // TODO: perform the calculation for the congruence as well.
    // (makes considerably more sense if in a separate CPA).
    if (!pReducedState.isAbstract()) {

      // Intermediate states stay as-is.
      return pReducedState;
    }

    // Enrich the {@code pReducedState} with bounds obtained from {@code
    // pRootState} which were dropped during the reduction.
    Map<Template, PolicyBound> rootAbstraction =
        pRootState.asAbstracted().getAbstraction();
    Map<Template, PolicyBound> reducedAbstraction =
        new HashMap<>(pReducedState.asAbstracted().getAbstraction());
    rootAbstraction.forEach(reducedAbstraction::putIfAbsent);

    return pReducedState.asAbstracted().withNewAbstraction(reducedAbstraction);
  }

  @Override
  public Precision getVariableReducedPrecision(
      Precision precision, Block context) {
    // todo?
    return precision;
  }

  @Override
  public Precision getVariableExpandedPrecision(
      Precision rootPrecision, Block rootContext, Precision reducedPrecision) {
    return rootPrecision;
  }

  @Override
  public Object getHashCodeForState(
      AbstractState stateKey, Precision precisionKey) {
    return Pair.of(stateKey, precisionKey);
  }

  @Override
  public int measurePrecisionDifference(
      Precision pPrecision, Precision pOtherPrecision) {
    // TODO? seems not strictly necessary.
    return 0;
  }

  @Override
  public AbstractState getVariableReducedStateForProofChecking(
      AbstractState expandedState, Block context, CFANode callNode) {
    return getVariableReducedState(expandedState, context, callNode);
  }

  @Override
  public AbstractState getVariableExpandedStateForProofChecking(
      AbstractState rootState,
      Block reducedContext,
      AbstractState reducedState) {
    return getVariableExpandedState(rootState, reducedContext, reducedState);
  }

  /**
   * Take root state,
   * remove all bounds associated with global variables,
   * add all globals from the expandedState,
   * add assignment to return function value from expandedState.
   */
  @Override
  public AbstractState rebuildStateAfterFunctionCall(
      AbstractState rootState,
      AbstractState entryState,
      AbstractState expandedState,
      FunctionExitNode exitLocation) {
    PolicyState pRootState = (PolicyState) rootState;
    PolicyState pExpandedState = (PolicyState) expandedState;

    if (!pExpandedState.isAbstract()) {
      return pExpandedState;
    }

    // Remove all global values from root state.
    Map<Template, PolicyBound> rootAbstraction
        = new HashMap<>(pRootState.asAbstracted().getAbstraction());
    Map<Template, PolicyBound> expandedAbstraction
        = new HashMap<>(pExpandedState.asAbstracted().getAbstraction());
    Map<Template, PolicyBound> noGlobals = Maps.filterKeys(
        rootAbstraction,
        t -> !t.hasGlobals()
    );

    // Re-add globals from expanded state.
    noGlobals.putAll(
      Maps.filterKeys(
          expandedAbstraction,
          t -> !t.getUsedVars()
              .filter(s -> !s.contains("::")).findAny().isPresent()
      ));

    Optional<String> retName = exitLocation.getEntryNode().getReturnVariable()
        .flatMap(t -> Optional.of(t.getQualifiedName()));

    Map<Template, PolicyBound> out;
    if (retName.isPresent()) {
      String retVarName = retName.get();

      // Drop all templates which contain the return variable
      // name (TODO: prob need to call simplex at this point to figure out the
      // new bounds).
      Map<Template, PolicyBound> noRetVar = Maps.filterKeys(
          noGlobals,
          t -> t.getUsedVars()
                .filter(v -> v.equals(retVarName)).findAny().isPresent()
      );

      // Re-add the template length 1 from {@code expandedState} if exists.
      expandedAbstraction.keySet().stream().filter(
          t -> t.getLinearExpression().size() == 1
            && t.getUsedVars().filter(v -> v.equals(retVarName)).findAny().isPresent()
      ).forEach(
          t -> noRetVar.put(t, expandedAbstraction.get(t))
      );
      out = noRetVar;
    } else {
      out = noGlobals;
    }
    return pExpandedState.asAbstracted().withNewAbstraction(out);

  }

  private Set<String> getBlockVariables(Block pBlock) {
    return pBlock.getReferencedVariables().stream()
        .map(ReferencedVariable::getName).collect(Collectors.toSet());
  }
}