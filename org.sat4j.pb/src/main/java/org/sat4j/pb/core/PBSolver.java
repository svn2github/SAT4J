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
package org.sat4j.pb.core;

import java.math.BigInteger;

import org.sat4j.minisat.core.AssertingClauseGenerator;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public abstract class PBSolver extends Solver<PBDataStructureFactory> implements
		IPBSolver {

	private ObjectiveFunction objf;

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	protected PBSolverStats stats;

	public PBSolver(AssertingClauseGenerator acg,
			LearningStrategy<PBDataStructureFactory> learner,
			PBDataStructureFactory dsf, IOrder order, RestartStrategy restarter) {
		super(acg, learner, dsf, order, restarter);
		stats = new PBSolverStats();
		initStats(stats);
	}

	public PBSolver(AssertingClauseGenerator acg,
			LearningStrategy<PBDataStructureFactory> learner,
			PBDataStructureFactory dsf, SearchParams params, IOrder order,
			RestartStrategy restarter) {
		super(acg, learner, dsf, params, order, restarter);
		stats = new PBSolverStats();
		initStats(stats);
	}

	public IConstr addPseudoBoolean(IVecInt literals, IVec<BigInteger> coeffs,
			boolean moreThan, BigInteger degree) throws ContradictionException {
		IVecInt vlits = dimacs2internal(literals);
		assert vlits.size() == literals.size();
		assert literals.size() == coeffs.size();
		return addConstr(dsfactory.createPseudoBooleanConstraint(vlits, coeffs,
				moreThan, degree));
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		objf = obj;
		IOrder order = getOrder();
		if (order instanceof VarOrderHeapObjective) {
			((VarOrderHeapObjective) order).setObjectiveFunction(obj);
		}
	}

	public ObjectiveFunction getObjectiveFunction() {
		return objf;
	}
}
