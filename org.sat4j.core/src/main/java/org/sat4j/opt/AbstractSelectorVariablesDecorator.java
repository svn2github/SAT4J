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

import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolverDecorator;

/**
 * Abstract class which adds a new "selector" variable for each clause entered
 * in the solver.
 * 
 * As a consequence, an original problem with n variables and m clauses will end
 * up with n+m variables.
 * 
 * @author daniel
 *
 */
public abstract class AbstractSelectorVariablesDecorator extends
        SolverDecorator<ISolver> implements IOptimizationProblem {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int nborigvars;

    private int nbexpectedclauses;

    protected int nbnewvar;

    protected int[] prevfullmodel;

    public AbstractSelectorVariablesDecorator(ISolver solver) {
        super(solver);
    }
    
    @Override
    public int[] model() {
        int end = nborigvars - 1;
        while (Math.abs(prevfullmodel[end]) > nborigvars)
            end--;
        int[] shortmodel = new int[end + 1];
        for (int i = 0; i <= end; i++) {
            shortmodel[i] = prevfullmodel[i];
        }
        return shortmodel;
    }

    @Override
    public int newVar(int howmany) {
        nborigvars = super.newVar(howmany);
        return nborigvars;
    }

    @Override
    public void setExpectedNumberOfClauses(int nb) {
        nbexpectedclauses = nb;
        super.setExpectedNumberOfClauses(nb);
        super.newVar(nborigvars + nbexpectedclauses);
    }
    
    public int getExpectedNumberOfClauses() {
        return nbexpectedclauses;
    }

    @Override
    public void reset() {
        super.reset();
        nbnewvar = 0;
    }

    public boolean admitABetterSolution() throws TimeoutException {
        boolean result = super.isSatisfiable(true);
        if (result) {
            prevfullmodel = super.model();
            calculateObjectiveValue();
        }
        return result;
    }

    abstract void calculateObjectiveValue();
}
