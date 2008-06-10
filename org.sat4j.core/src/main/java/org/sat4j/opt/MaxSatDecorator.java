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
package org.sat4j.opt;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * Computes a solution that satisfies the maximum of clauses.
 * 
 * @author daniel
 *
 */
public class MaxSatDecorator extends AbstractSelectorVariablesDecorator
        implements IOptimizationProblem {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public MaxSatDecorator(ISolver solver) {
        super(solver);
    }

    
    @Override
    public void setExpectedNumberOfClauses(int nb) {
        super.setExpectedNumberOfClauses(nb);
        lits.ensure(nb);
    }


    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {
        int newvar = nborigvars + ++nbnewvar;
        lits.push(newvar);
        literals.push(newvar);
        return super.addClause(literals);
    }

    @Override
    public void reset() {
        nbnewvar = 0;
        lits.clear();
        super.reset();
    }

    public boolean hasNoObjectiveFunction() {
        return false;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return false;
    }

    public Number calculateObjective() {
        counter = 0;
        for (int q : prevfullmodel) {
            if (q > nborigvars) {
                counter++;
            }
        }
        return counter;
    }

    private final IVecInt lits = new VecInt();

    private int counter;

    public void discard() throws ContradictionException {
         super.addAtMost(lits, counter - 1);
    }

}
