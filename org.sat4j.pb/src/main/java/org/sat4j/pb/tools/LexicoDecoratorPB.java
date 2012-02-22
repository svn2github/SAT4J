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
package org.sat4j.pb.tools;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.core.Vec;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.LexicoDecorator;

public class LexicoDecoratorPB extends LexicoDecorator<IPBSolver> implements
		IPBSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final List<ObjectiveFunction> objs = new ArrayList<ObjectiveFunction>();
	private BigInteger bigCurrentValue;

	public LexicoDecoratorPB(IPBSolver solver) {
		super(solver);
	}

	public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
			boolean moreThan, BigInteger d) throws ContradictionException {
		return decorated().addPseudoBoolean(lits, coeffs, moreThan, d);
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		throw new UnsupportedOperationException();

	}

	public ObjectiveFunction getObjectiveFunction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean admitABetterSolution(IVecInt assumps)
			throws TimeoutException {
		decorated().setObjectiveFunction(objs.get(currentCriterion));
		return super.admitABetterSolution(assumps);
	}

	@Override
	public void addCriterion(IVecInt literals) {
		objs.add(new ObjectiveFunction(literals, new Vec<BigInteger>(literals
				.size(), BigInteger.ONE)));
	}

	public void addCriterion(IVecInt literals, IVec<BigInteger> coefs) {
		objs.add(new ObjectiveFunction(literals, coefs));
	}

	@Override
	protected Number evaluate() {
		bigCurrentValue = objs.get(currentCriterion).calculateDegree(this);
		return bigCurrentValue;
	}

	@Override
	protected void fixCriterionValue() throws ContradictionException {
		addPseudoBoolean(objs.get(currentCriterion).getVars(),
				objs.get(currentCriterion).getCoeffs(), true, bigCurrentValue);
		addPseudoBoolean(objs.get(currentCriterion).getVars(),
				objs.get(currentCriterion).getCoeffs(), false, bigCurrentValue);
	}

	@Override
	protected IConstr discardSolutionsForOptimizing()
			throws ContradictionException {
		return addPseudoBoolean(objs.get(currentCriterion).getVars(),
				objs.get(currentCriterion).getCoeffs(), false,
				bigCurrentValue.subtract(BigInteger.ONE));
	}

	@Override
	protected int numberOfCriteria() {
		return objs.size();
	}

	public IConstr addAtMost(IVecInt literals, IVecInt coeffs, int degree)
			throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	public IConstr addAtMost(IVecInt literals, IVec<BigInteger> coeffs,
			BigInteger degree) throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	public IConstr addAtLeast(IVecInt literals, IVecInt coeffs, int degree)
			throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	public IConstr addAtLeast(IVecInt literals, IVec<BigInteger> coeffs,
			BigInteger degree) throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	public IConstr addExactly(IVecInt literals, IVecInt coeffs, int weight)
			throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	public IConstr addExactly(IVecInt literals, IVec<BigInteger> coeffs,
			BigInteger weight) throws ContradictionException {
		throw new UnsupportedOperationException();
	}

}
