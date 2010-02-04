/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker. 
 *
 *  Copyright (C) 2007-2008  Dirk Beyer and Erkan Keremoglu.
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
 *    http://www.cs.sfu.ca/~dbeyer/CPAchecker/
 */
package cpa.common.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;


import common.Pair;

import cpa.common.CPAchecker;
import cpa.common.ReachedElements;
import cpa.common.interfaces.AbstractElement;
import cpa.common.interfaces.ConfigurableProgramAnalysis;
import cpa.common.interfaces.MergeOperator;
import cpa.common.interfaces.Precision;
import cpa.common.interfaces.PrecisionAdjustment;
import cpa.common.interfaces.StopOperator;
import cpa.common.interfaces.TransferRelation;
import exceptions.CPAException;
import exceptions.TransferTimeOutException;

public class CPAAlgorithm implements Algorithm {

  private long chooseTime = 0;
  private long transferTime = 0;
  private long mergeTime = 0;
  private long stopTime = 0;
  
  private final ConfigurableProgramAnalysis cpa;

  public CPAAlgorithm(ConfigurableProgramAnalysis cpa) {
    this.cpa = cpa;
  }

  @Override
  public void run(final ReachedElements reachedElements, boolean stopAfterError) throws CPAException, TransferTimeOutException {
    final TransferRelation transferRelation = cpa.getTransferRelation();
    final MergeOperator mergeOperator = cpa.getMergeOperator();
    final StopOperator stopOperator = cpa.getStopOperator();
    PrecisionAdjustment precisionAdjustment = cpa.getPrecisionAdjustment();

    while (reachedElements.hasWaitingElement()) {
      
      // Pick next element using strategy
      // BFS, DFS or top sort according to the configuration
      long start = System.currentTimeMillis();
      Pair<AbstractElement,Precision> e = reachedElements.popFromWaitlist();
      long end = System.currentTimeMillis();
      chooseTime += (end - start);
      Pair<AbstractElement, Precision> tempPair;
      tempPair = precisionAdjustment.prec(e.getFirst(), e.getSecond(), reachedElements.getReachedWithPrecision());
      if(tempPair != null){
        e = tempPair;
      }
      AbstractElement element = e.getFirst();
      Precision precision = e.getSecond();

      CPAchecker.logger.log(Level.FINER, "Retrieved element from waitlist");
      CPAchecker.logger.log(Level.ALL, "Current element is", element, "with precision", precision);

      start = System.currentTimeMillis();
      Collection<? extends AbstractElement> successors = transferRelation.getAbstractSuccessors (element, precision, null);
      end = System.currentTimeMillis();
      transferTime += (end - start);
      // TODO When we have a nice way to mark the analysis result as incomplete, we could continue analysis on a CPATransferException with the next element from waitlist.
      
      CPAchecker.logger.log(Level.FINER, "Current element has", successors.size(), "successors");
      
      for (AbstractElement successor : successors) {
        CPAchecker.logger.log(Level.FINER, "Considering successor of current element");
        CPAchecker.logger.log(Level.ALL, "Successor of", element, "\nis", successor);
        
        Collection<AbstractElement> reached = reachedElements.getReached(successor);

        // AG as an optimization, we allow the mergeOperator to be null,
        // as a synonym of a trivial operator that never merges
        if (mergeOperator != null && !reached.isEmpty()) {
          start = System.currentTimeMillis();

          List<AbstractElement> toRemove = new ArrayList<AbstractElement>();
          List<Pair<AbstractElement, Precision>> toAdd = new ArrayList<Pair<AbstractElement, Precision>>();
          
          CPAchecker.logger.log(Level.FINER, "Considering", reached.size(), "elements from reached set for merge");
          for (AbstractElement reachedElement : reached) {
            AbstractElement mergedElement = mergeOperator.merge( successor, reachedElement, precision);

            if (!mergedElement.equals(reachedElement)) {
              CPAchecker.logger.log(Level.FINER, "Successor was merged with element from reached set");
              CPAchecker.logger.log(Level.ALL,
                  "Merged", successor, "\nand", reachedElement, "\n-->", mergedElement);
              
              toRemove.add(reachedElement);
              toAdd.add(new Pair<AbstractElement, Precision>(mergedElement, precision));
            }
          }
          reachedElements.removeAll(toRemove);
          reachedElements.addAll(toAdd);
          
          end = System.currentTimeMillis();
          mergeTime += (end - start);
        }
        
        start = System.currentTimeMillis();

        if (stopOperator.stop(successor, reached, precision)) {
          CPAchecker.logger.log(Level.FINER, "Successor is covered or unreachable, not adding to waitlist");
        
        } else {
          CPAchecker.logger.log(Level.FINER, "No need to stop, adding successor to waitlist");

          reachedElements.add(successor, precision);
          
          if(stopAfterError && successor.isError()) {
            end = System.currentTimeMillis();
            stopTime += (end - start);
            return;
          }
        }
        end = System.currentTimeMillis();
        stopTime += (end - start);
      }
    }
  }

  @Override
  public ConfigurableProgramAnalysis getCPA() {
    return cpa;
  }
}