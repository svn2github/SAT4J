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
		costs = new int[howmany + 1];
		// Arrays.fill(costs, 1);
		vars.clear();
		coeffs.clear();
		for (int i = 1; i <= howmany; i++) {
			vars.push(i);
			coeffs.push(BigInteger.ZERO);
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
		return costs[var];
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
		costs[var] = cost;
		coeffs.set(var - 1, BigInteger.valueOf(cost));
	}

	public boolean admitABetterSolution() throws TimeoutException {
		return admitABetterSolution(VecInt.EMPTY);
	}

	public boolean admitABetterSolution(IVecInt assumps)
			throws TimeoutException {
		boolean result = super.isSatisfiable(assumps, true);
		if (result) {
			prevmodel = super.model();
			calculateObjective();
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
		objectivevalue = calculateDegree(prevmodel);
		return new Integer(objectivevalue);
	}

	private int calculateDegree(int[] prevmodel2) {
		int tmpcost = 0;
		for (int i = 1; i < costs.length; i++) {
			if (prevmodel2[i - 1] > 0) {
				tmpcost += costs[i];
			}
		}
		return tmpcost;
	}

	public void discardCurrentSolution() throws ContradictionException {
		if (prevConstr!=null) {
			super.removeSubsumedConstr(prevConstr);
		}
		prevConstr = super.addPseudoBoolean(vars, coeffs, false, BigInteger
				.valueOf(objectivevalue - 1));
	}

	@Override
	public void reset() {
		prevConstr = null;
		super.reset();
	}

	@Override
	public int[] model() {
		// DLB findbugs ok
		return prevmodel;
	}

	public Number getObjectiveValue() {
		return objectivevalue;
	}

	public void discard() throws ContradictionException {
		discardCurrentSolution();
	}

	public void forceObjectiveValueTo(Number forcedValue)
			throws ContradictionException {
		super.addPseudoBoolean(vars, coeffs, false, (BigInteger)
				forcedValue);
	}
}
