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
package cpa.assumptions.collector.progressobserver;

import cfa.objectmodel.CFAEdge;
import cfa.objectmodel.CFANode;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import cpa.common.CPAConfiguration;
import cpa.common.LogManager;

/**
 * @author g.theoduloz
 */
public class RepetitionsInPathHeuristics
  implements StopHeuristics<RepetitionsInPathHeuristicsData>
{
  private final Function<? super CFAEdge, Integer> thresholdFunction;
  
  public RepetitionsInPathHeuristics(CPAConfiguration config, LogManager logger)
  {
    int configThreshold = Integer.parseInt(config.getProperty("threshold", "-1"));
    thresholdFunction = Functions.constant((configThreshold <= 0) ? null : configThreshold);
  }

  @Override
  public RepetitionsInPathHeuristicsData getBottom() {
    return RepetitionsInPathHeuristicsData.BOTTOM;
  }

  @Override
  public RepetitionsInPathHeuristicsData getInitialData(CFANode pNode) {
    return new RepetitionsInPathHeuristicsData();
  }

  @Override
  public RepetitionsInPathHeuristicsData getTop() {
    return RepetitionsInPathHeuristicsData.TOP;
  }

  @Override
  public RepetitionsInPathHeuristicsData collectData(StopHeuristicsData pData,
      ReachedHeuristicsDataSetView pReached) {
    return (RepetitionsInPathHeuristicsData)pData;
  }

  @Override
  public RepetitionsInPathHeuristicsData processEdge(StopHeuristicsData pData,
      CFAEdge pEdge) {
    return ((RepetitionsInPathHeuristicsData)pData).updateForEdge(pEdge, thresholdFunction);
  }
  
}
