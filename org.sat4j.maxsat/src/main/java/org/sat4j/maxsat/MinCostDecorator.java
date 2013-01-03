/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.maxsat;

import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.PBSolverDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * A decorator that computes minimal cost models. That problem is also known as
 * binate covering problem.
 * 
 * Please make sure that newVar(howmany) is called first to setup the decorator.
 * 
 * @author daniel
 * 
 */
public class MinCostDecorator extends PBSolverDecorator implements
        IOptimizationProblem {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int[] costs;

    private int[] prevmodel;

    private final IVecInt vars = new VecInt();

    private final IVec<BigInteger> coeffs = new Vec<BigInteger>();

    private int objectivevalue;

    private IConstr prevConstr;

    private boolean isSolutionOptimal;

    public MinCostDecorator(IPBSolver solver) {
        super(solver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.tools.SolverDecorator#newVar()
     */
    @Override
    public int newVar() {
        throw new UnsupportedOperationException();
    }

    /**
     * Setup the number of variables to use inside the solver.
     * 
     * It is mandatory to call that method before setting the cost of the
     * variables.
     * 
     * @param howmany
     *            the maximum number of variables in the solver.
     */
    @Override
    public int newVar(int howmany) {
        this.costs = new int[howmany + 1];
        // Arrays.fill(costs, 1);
        this.vars.clear();
        this.coeffs.clear();
        for (int i = 1; i <= howmany; i++) {
            this.vars.push(i);
            this.coeffs.push(BigInteger.ZERO);
        }
        // should the default cost be 1????
        // here it is 0
        return super.newVar(howmany);
    }

    /**
     * to know the cost of a given var.
     * 
     * @param var
     *            a variable in dimacs format
     * @return the cost of that variable when assigned to true
     */
    public int costOf(int var) {
        return this.costs[var];
    }

    /**
     * to set the cost of a given var.
     * 
     * @param var
     *            a variable in dimacs format
     * @param cost
     *            the cost of var when assigned to true
     */
    public void setCost(int var, int cost) {
        this.costs[var] = cost;
        this.coeffs.set(var - 1, BigInteger.valueOf(cost));
    }

    public boolean admitABetterSolution() throws TimeoutException {
        return admitABetterSolution(VecInt.EMPTY);
    }

    public boolean admitABetterSolution(IVecInt assumps)
            throws TimeoutException {
        this.isSolutionOptimal = false;
        boolean result = super.isSatisfiable(assumps, true);
        if (result) {
            this.prevmodel = super.model();
            calculateObjective();
        } else {
            this.isSolutionOptimal = true;
        }
        return result;
    }

    public boolean hasNoObjectiveFunction() {
        return false;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return true;
    }

    public Number calculateObjective() {
        this.objectivevalue = calculateDegree(this.prevmodel);
        return this.objectivevalue;
    }

    private int calculateDegree(int[] prevmodel2) {
        int tmpcost = 0;
        for (int i = 1; i < this.costs.length; i++) {
            if (prevmodel2[i - 1] > 0) {
                tmpcost += this.costs[i];
            }
        }
        return tmpcost;
    }

    public void discardCurrentSolution() throws ContradictionException {
        if (this.prevConstr != null) {
            super.removeSubsumedConstr(this.prevConstr);
        }
        try {
            this.prevConstr = super.addPseudoBoolean(this.vars, this.coeffs,
                    false, BigInteger.valueOf(this.objectivevalue - 1));
        } catch (ContradictionException e) {
            this.isSolutionOptimal = true;
            throw e;
        }
    }

    @Override
    public void reset() {
        this.prevConstr = null;
        super.reset();
    }

    @Override
    public int[] model() {
        // DLB findbugs ok
        return this.prevmodel;
    }

    public Number getObjectiveValue() {
        return this.objectivevalue;
    }

    public void discard() throws ContradictionException {
        discardCurrentSolution();
    }

    public void forceObjectiveValueTo(Number forcedValue)
            throws ContradictionException {
        super.addPseudoBoolean(this.vars, this.coeffs, false,
                (BigInteger) forcedValue);
    }

    public boolean isOptimal() {
        return this.isSolutionOptimal;
    }

    public void setTimeoutForFindingBetterSolution(int seconds) {
        // TODO
        throw new UnsupportedOperationException("No implemented yet");
    }
}
