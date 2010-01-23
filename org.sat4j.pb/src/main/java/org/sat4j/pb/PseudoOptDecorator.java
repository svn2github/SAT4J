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

	private ObjectiveFunction objfct;

	private BigInteger objectiveValue;

	private int[] prevmodel;
	private boolean[] prevfullmodel;

	private IConstr previousPBConstr;

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
		objfct = objf;
		decorated().setObjectiveFunction(objf);
	}

	public boolean admitABetterSolution() throws TimeoutException {
		return admitABetterSolution(VecInt.EMPTY);
	}

	public boolean admitABetterSolution(IVecInt assumps)
			throws TimeoutException {
		try {
			boolean result = super.isSatisfiable(assumps, true);
			if (result) {
				prevmodel = super.model();
				prevfullmodel = new boolean[nVars()];
				for (int i = 0; i < nVars(); i++) {
					prevfullmodel[i] = decorated().model(i + 1);
				}
				if (objfct != null) {
					calculateObjective();
				}
			} else {
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
		return objfct == null;
	}

	public boolean nonOptimalMeansSatisfiable() {
		return true;
	}

	public Number calculateObjective() {
		if (objfct == null) {
			throw new UnsupportedOperationException(
					"The problem does not contain an objective function");
		}
		objectiveValue = objfct.calculateDegree(prevmodel);
		return objectiveValue;
	}

	public void discardCurrentSolution() throws ContradictionException {
		if (previousPBConstr != null) {
			super.removeSubsumedConstr(previousPBConstr);
		}
		if (objfct != null && objectiveValue != null) {
			previousPBConstr = super.addPseudoBoolean(objfct.getVars(), objfct
					.getCoeffs(), false, objectiveValue
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
		return prefix + "Pseudo Boolean Optimization\n"
				+ super.toString(prefix);
	}

	public Number getObjectiveValue() {
		return objectiveValue;
	}

	public void discard() throws ContradictionException {
		discardCurrentSolution();
	}

	public void forceObjectiveValueTo(Number forcedValue)
			throws ContradictionException {
		super.addPseudoBoolean(objfct.getVars(), objfct.getCoeffs(), false,
				(BigInteger) forcedValue);
	}
}
