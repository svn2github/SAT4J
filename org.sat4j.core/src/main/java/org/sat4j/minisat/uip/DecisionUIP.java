/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
* 
* Based on the original MiniSat specification from:
* 
* An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
* Sixth International Conference on Theory and Applications of Satisfiability
* Testing, LNCS 2919, pp 502-518, 2003.
*
* See www.minisat.se for the original solver in C++.
* 
*******************************************************************************/
package org.sat4j.minisat.uip;

import java.io.Serializable;

import org.sat4j.minisat.core.AssertingClauseGenerator;
import org.sat4j.specs.IConstr;

/**
 * Decision UIP scheme for building an asserting clause. This is one of the
 * simplest way to build an asserting clause: the generator stops when it meets
 * a decision variable (a literal with no reason). Note that this scheme cannot
 * be used for general constraints, since decision variables are not necessarily
 * UIP in the pseudo boolean case.
 * 
 * @author leberre
 */
public class DecisionUIP implements AssertingClauseGenerator, Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.AnalysisScheme#initAnalyse()
     */
    public void initAnalyze() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.AnalysisScheme#onCurrentDecisionLevelLiteral()
     */
    public void onCurrentDecisionLevelLiteral(int p) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.AnalysisScheme#continueResolution(org.sat4j.datatype.Lit)
     */
    public boolean clauseNonAssertive(IConstr reason) {
        return reason != null;
    }

    @Override
    public String toString() {
        return "Stops conflict analysis at the last decision variable";
    }

}
