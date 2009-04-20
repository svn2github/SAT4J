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

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * A solver able to deal with pseudo boolean constraints.
 * 
 * @author daniel
 * 
 */
public interface IPBSolver extends ISolver {

	/**
	 * Create a Pseudo-Boolean constraint of the type "at least n of those
	 * literals must be satisfied"
	 * 
	 * @param lits
	 *            a set of literals. The vector can be reused since the solver
	 *            is not supposed to keep a reference to that vector.
	 * @param coeffs
	 *            the coefficients of the literals. The vector can be reused
	 *            since the solver is not supposed to keep a reference to that
	 *            vector.
	 * @param moreThan
	 *            true if it is a constraint >= degree
	 * @param d
	 *            the degree of the cardinality constraint
	 * @return a reference to the constraint added in the solver, to use in
	 *         removeConstr().
	 * @throws ContradictionException
	 *             iff the vector of literals is empty or if the constraint is
	 *             falsified after unit propagation
	 * @see #removeConstr(IConstr)
	 */
	IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
			boolean moreThan, BigInteger d) throws ContradictionException;

	public void setObjectiveFunction(ObjectiveFunction obj);

	public ObjectiveFunction getObjectiveFunction();
}
