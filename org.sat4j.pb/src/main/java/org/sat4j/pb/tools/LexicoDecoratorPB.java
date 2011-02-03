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
				.size(), BigInteger.TEN)));
		super.addCriterion(literals);
	}

}
