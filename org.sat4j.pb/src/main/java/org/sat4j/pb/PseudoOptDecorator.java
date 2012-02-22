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
package org.sat4j.pb;

import java.math.BigInteger;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * A decorator that computes minimal pseudo boolean models.
 * 
 * @author daniel
 * 
 */
public class PseudoOptDecorator extends PBSolverDecorator implements
		IOptimizationProblem {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	private BigInteger objectiveValue;

	private int[] prevmodel;
	private int[] prevmodelwithadditionalvars;

	private boolean[] prevfullmodel;

	private IConstr previousPBConstr;

	private boolean isSolutionOptimal;

	public PseudoOptDecorator(IPBSolver solver) {
		super(solver);
	}

	@Override
	public boolean isSatisfiable() throws TimeoutException {
		return isSatisfiable(VecInt.EMPTY);
	}

	@Override
	public boolean isSatisfiable(boolean global) throws TimeoutException {
		return isSatisfiable(VecInt.EMPTY, global);
	}

	@Override
	public boolean isSatisfiable(IVecInt assumps, boolean global)
			throws TimeoutException {
		boolean result = super.isSatisfiable(assumps, true);
		if (result) {
			prevmodel = super.model();
			prevmodelwithadditionalvars = super.modelWithInternalVariables();
			prevfullmodel = new boolean[nVars()];
			for (int i = 0; i < nVars(); i++) {
				prevfullmodel[i] = decorated().model(i + 1);
			}
		} else {
			if (previousPBConstr != null) {
				decorated().removeConstr(previousPBConstr);
				previousPBConstr = null;
			}
		}
		return result;
	}

	@Override
	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
		return isSatisfiable(assumps, true);
	}

	@Override
	public void setObjectiveFunction(ObjectiveFunction objf) {
		decorated().setObjectiveFunction(objf);
	}

	public boolean admitABetterSolution() throws TimeoutException {
		return admitABetterSolution(VecInt.EMPTY);
	}

	public boolean admitABetterSolution(IVecInt assumps)
			throws TimeoutException {
		try {
			isSolutionOptimal = false;
			boolean result = super.isSatisfiable(assumps, true);
			if (result) {
				prevmodel = super.model();
				prevmodelwithadditionalvars = super
						.modelWithInternalVariables();
				prevfullmodel = new boolean[nVars()];
				for (int i = 0; i < nVars(); i++) {
					prevfullmodel[i] = decorated().model(i + 1);
				}
				if (decorated().getObjectiveFunction() != null) {
					calculateObjective();
				}
			} else {
				isSolutionOptimal = true;
				if (previousPBConstr != null) {
					decorated().removeConstr(previousPBConstr);
					previousPBConstr = null;
				}
			}
			return result;
		} catch (TimeoutException te) {
			if (previousPBConstr != null) {
				decorated().removeConstr(previousPBConstr);
				previousPBConstr = null;
			}
			throw te;
		}
	}

	public boolean hasNoObjectiveFunction() {
		return decorated().getObjectiveFunction() == null;
	}

	public boolean nonOptimalMeansSatisfiable() {
		return true;
	}

	public Number calculateObjective() {
		if (decorated().getObjectiveFunction() == null) {
			throw new UnsupportedOperationException(
					"The problem does not contain an objective function");
		}
		objectiveValue = decorated().getObjectiveFunction().calculateDegree(
				decorated());
		return getObjectiveValue();
	}

	public void discardCurrentSolution() throws ContradictionException {
		if (previousPBConstr != null) {
			super.removeSubsumedConstr(previousPBConstr);
		}
		if (decorated().getObjectiveFunction() != null
				&& objectiveValue != null) {
			previousPBConstr = super.addPseudoBoolean(decorated()
					.getObjectiveFunction().getVars(), decorated()
					.getObjectiveFunction().getCoeffs(), false, objectiveValue
					.subtract(BigInteger.ONE));
		}
	}

	@Override
	public void reset() {
		previousPBConstr = null;
		super.reset();
	}

	@Override
	public int[] model() {
		// DLB findbugs ok
		return prevmodel;
	}

	@Override
	public boolean model(int var) {
		return prevfullmodel[var - 1];
	}

	@Override
	public String toString(String prefix) {
		return prefix + "Pseudo Boolean Optimization by upper bound\n"
				+ super.toString(prefix);
	}

	public Number getObjectiveValue() {
		return objectiveValue.add(decorated().getObjectiveFunction()
				.getCorrection());
	}

	public void discard() throws ContradictionException {
		discardCurrentSolution();
	}

	public void forceObjectiveValueTo(Number forcedValue)
			throws ContradictionException {
		super.addPseudoBoolean(decorated().getObjectiveFunction().getVars(),
				decorated().getObjectiveFunction().getCoeffs(), false,
				(BigInteger) forcedValue);
	}

	public boolean isOptimal() {
		return isSolutionOptimal;
	}

	@Override
	public int[] modelWithInternalVariables() {
		return prevmodelwithadditionalvars;
	}
}
