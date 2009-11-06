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
package cpa.common.defaults;

import cpa.common.interfaces.AbstractElement;
import cpa.common.interfaces.AbstractElementWithLocation;
import cpa.common.interfaces.JoinOperator;
import cpa.common.interfaces.MergeOperator;
import cpa.common.interfaces.Precision;
import exceptions.CPAException;

/**
 * Standard merge-join operator
 * @author g.theoduloz
 */
public class MergeJoinOperator implements MergeOperator {

  private final JoinOperator joinOperator;
  
  /**
   * Creates a merge-join operator, based on the given join
   * operator
   */
  public MergeJoinOperator(JoinOperator op)
  {
    joinOperator = op;
  }
  
  @Override
  public AbstractElement merge(AbstractElement el1, AbstractElement el2, Precision p)
    throws CPAException
  {
    return joinOperator.join(el1, el2);
  }

  @Override
  public AbstractElementWithLocation merge(
      AbstractElementWithLocation el1,
      AbstractElementWithLocation el2, Precision p)
    throws CPAException
  {
    return (AbstractElementWithLocation) joinOperator.join(el1, el2);
  }

}
