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
package cpa.symbpredabs.summary;

import java.util.Collection;
import java.util.logging.Level;

import symbpredabstraction.interfaces.AbstractFormulaManager;
import cmdline.CPAMain;
import cpa.common.interfaces.AbstractElement;
import cpa.common.interfaces.Precision;
import cpa.common.interfaces.StopOperator;
import exceptions.CPAException;

/**
 * coverage check for symbolic lazy abstraction with summaries
 *
 * @author Alberto Griggio <alberto.griggio@disi.unitn.it>
 */
public class SummaryStopOperator implements StopOperator {

  private final SummaryAbstractDomain domain;

  public SummaryStopOperator(SummaryAbstractDomain d) {
    domain = d;
  }

  public <AE extends AbstractElement> boolean stop(AE element,
                                                   Collection<AE> reached, Precision prec) throws CPAException {
    for (AbstractElement e : reached) {
      if (stop(element, e)) {
        return true;
      }
    }
    return false;
  }


  public boolean stop(AbstractElement element, AbstractElement reachedElement)
  throws CPAException {

    SummaryAbstractElement e1 = (SummaryAbstractElement)element;
    SummaryAbstractElement e2 = (SummaryAbstractElement)reachedElement;

    if (e1.getLocation().equals(e2.getLocation())) {
      CPAMain.logManager.log(Level.ALL, "DEBUG_4",
          "Checking Coverage of element: ", element);

      if (!e1.sameContext(e2)) {
        CPAMain.logManager.log(Level.FINEST,
            "NO, not covered: context differs");
        return false;
      }

      SummaryCPA cpa = domain.getCPA();
      AbstractFormulaManager amgr = cpa.getAbstractFormulaManager();

      assert(e1.getAbstraction() != null);
      assert(e2.getAbstraction() != null);

      boolean ok = amgr.entails(e1.getAbstraction(), e2.getAbstraction());

      if (ok) {
        CPAMain.logManager.log(Level.FINEST,
            "Element: ", element, " COVERED by: ", e2);
        cpa.setCovered(e1);
        e1.setCovered(true);
      } else {
        CPAMain.logManager.log(Level.FINEST,
            "NO, not covered");
      }

      return ok;
    } else {
      return false;
    }
  }

}
