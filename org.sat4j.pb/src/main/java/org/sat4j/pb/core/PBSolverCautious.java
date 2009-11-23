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

import org.sat4j.minisat.core.AssertingClauseGenerator;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.pb.constraints.pb.ConflictMapSwitchToClause;
import org.sat4j.pb.constraints.pb.IConflict;
import org.sat4j.pb.constraints.pb.PBConstr;

public class PBSolverCautious extends PBSolverCP {

	private static final long serialVersionUID = 1L;
	public static final int BOUND = 10;

	public PBSolverCautious(AssertingClauseGenerator acg,
			LearningStrategy<PBDataStructureFactory> learner,
			PBDataStructureFactory dsf, IOrder order) {
		super(acg, learner, dsf, order);
		ConflictMapSwitchToClause.UPPERBOUND = BOUND;
	}

	public PBSolverCautious(AssertingClauseGenerator acg,
			LearningStrategy<PBDataStructureFactory> learner,
			PBDataStructureFactory dsf, IOrder order, int bound) {
		super(acg, learner, dsf, order);
		ConflictMapSwitchToClause.UPPERBOUND = bound;
	}

	@Override
	IConflict chooseConflict(PBConstr myconfl, int level) {
		return ConflictMapSwitchToClause.createConflict(myconfl, level);
	}

	@Override
	public String toString(String prefix) {
		return super.toString(prefix)
				+ "\n"
				+ prefix
				+ "When dealing with too large coefficients, simplify asserted PB constraints to clauses";
	}

	@Override
	protected void updateNumberOfReductions(IConflict confl) {
		stats.numberOfReductions += ((ConflictMapSwitchToClause) confl)
				.getNumberOfReductions();
	}

	@Override
	protected void updateNumberOfReducedLearnedConstraints(IConflict confl) {
		if (((ConflictMapSwitchToClause) confl).hasBeenReduced())
			stats.numberOfLearnedConstraintsReduced++;
	}

}
