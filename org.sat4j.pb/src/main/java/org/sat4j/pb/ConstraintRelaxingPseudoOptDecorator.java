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
 * Based on the pseudo boolean algorithms described in:
 * A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
 * Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
 * Volume 24, Issue 3, March 2005 Page(s): 305 - 317
 * 
 * and 
 * Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
 * Framework. Ph.D. Dissertation, University of Oregon.
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
 * 
 * @author lonca
 * 
 */

public class ConstraintRelaxingPseudoOptDecorator extends PBSolverDecorator
		implements IOptimizationProblem {

	private static final long serialVersionUID = 1L;
	private int[] bestModel;
	private boolean[] bestFullModel;
	private IConstr previousPBConstr;
	private IConstr addedConstr = null;
	private int maxValue = 0;
	private Number objectiveValue;
	private boolean optimumFound = false;

	public ConstraintRelaxingPseudoOptDecorator(IPBSolver solver) {
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
			bestModel = super.model();
			bestFullModel = new boolean[nVars()];
			for (int i = 0; i < nVars(); i++) {
				bestFullModel[i] = decorated().model(i + 1);
			}
			calculateObjective();
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

	public boolean admitABetterSolution() throws TimeoutException {
		return admitABetterSolution(VecInt.EMPTY);
	}

	public boolean admitABetterSolution(IVecInt assumps)
			throws TimeoutException {
		boolean isSatisfiable;

		if (this.optimumFound) {
			return false;
		}
		maxValue = getObjectiveFunction().minValue().intValue();
		while (true) {
			if (addedConstr != null) {
				this.decorated().removeConstr(addedConstr);
			}
			try {
				forceObjectiveValueTo(this.maxValue++);
			} catch (ContradictionException e) {
				System.out.println(decorated().getLogPrefix()
						+ " no solution for objective value "
						+ (this.maxValue - 1));
				continue;
			}
			isSatisfiable = super.isSatisfiable(assumps, true);
			if (isSatisfiable) {
				optimumFound = true;
				bestModel = super.model();
				bestFullModel = new boolean[nVars()];
				for (int i = 0; i < nVars(); i++) {
					bestFullModel[i] = decorated().model(i + 1);
				}
				if (getObjectiveFunction() != null) {
					calculateObjective();
				}
				this.decorated().removeConstr(addedConstr);
				return true;
			}
			System.out.println(decorated().getLogPrefix()
					+ "no solution for objective value " + (this.maxValue - 1));
		}
	}

	public boolean hasNoObjectiveFunction() {
		return getObjectiveFunction() == null;
	}

	public boolean nonOptimalMeansSatisfiable() {
		return false;
	}

	@Deprecated
	public Number calculateObjective() {
		if (getObjectiveFunction() == null) {
			throw new UnsupportedOperationException(
					"The problem does not contain an objective function");
		}
		objectiveValue = getObjectiveFunction().calculateDegree(bestModel);
		return objectiveValue;
	}

	public Number getObjectiveValue() {
		return objectiveValue;
	}

	public void forceObjectiveValueTo(Number forcedValue)
			throws ContradictionException {
		addedConstr = super.addPseudoBoolean(getObjectiveFunction().getVars(),
				getObjectiveFunction().getCoeffs(), false,
				BigInteger.valueOf(forcedValue.longValue()));
	}

	@Deprecated
	public void discard() {
		discardCurrentSolution();
	}

	public void discardCurrentSolution() {
		// nothing to do here
	}

	public boolean isOptimal() {
		return this.optimumFound;
	}
}