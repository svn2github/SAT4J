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

package org.sat4j.pb.tools;

import java.math.BigInteger;
import java.util.Iterator;

import org.sat4j.core.Vec;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.tools.xplain.Xplain;

public class XplainPB extends Xplain<IPBSolver> implements IPBSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XplainPB(IPBSolver solver) {
		super(solver);
	}

	@Override
	public IConstr addAtLeast(IVecInt literals, int degree)
			throws ContradictionException {
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.growTo(literals.size(), BigInteger.ONE);
		int newvar = createNewVar(literals);
		literals.push(newvar);
		BigInteger coef = BigInteger.valueOf(coeffs.size() - degree);
		coeffs.push(coef);
		IConstr constr = decorated().addPseudoBoolean(literals, coeffs, true,
				BigInteger.valueOf(degree));
		if (constr == null) {
			// constraint trivially satisfied
			discardLastestVar();
			// System.err.println(lits.toString()+"/"+coeffs+"/"+(moreThan?">=":"<=")+d);
		} else {
			constrs.put(newvar, constr);
		}
		return constr;
	}

	@Override
	public IConstr addAtMost(IVecInt literals, int degree)
			throws ContradictionException {
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.growTo(literals.size(), BigInteger.ONE);
		int newvar = createNewVar(literals);
		literals.push(newvar);
		BigInteger coef = BigInteger.valueOf(degree - coeffs.size());
		coeffs.push(coef);
		IConstr constr = decorated().addPseudoBoolean(literals, coeffs, false,
				BigInteger.valueOf(degree));
		if (constr == null) {
			// constraint trivially satisfied
			discardLastestVar();
			// System.err.println(lits.toString()+"/"+coeffs+"/"+(moreThan?">=":"<=")+d);
		} else {
			constrs.put(newvar, constr);
		}
		return constr;
	}

	public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
			boolean moreThan, BigInteger d) throws ContradictionException {
		int newvar = createNewVar(lits);
		lits.push(newvar);
		if (moreThan && d.signum() >= 0) {
			coeffs.push(d);
		} else {
			BigInteger sum = BigInteger.ZERO;
			for (Iterator<BigInteger> ite = coeffs.iterator(); ite.hasNext();)
				sum = sum.add(ite.next());
			sum = sum.subtract(d);
			coeffs.push(sum.negate());
		}
		IConstr constr = decorated()
				.addPseudoBoolean(lits, coeffs, moreThan, d);
		if (constr == null) {
			// constraint trivially satisfied
			discardLastestVar();
			// System.err.println(lits.toString()+"/"+coeffs+"/"+(moreThan?">=":"<=")+d);
		} else {
			constrs.put(newvar, constr);
		}
		return constr;
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		decorated().setObjectiveFunction(obj);
	}

	public ObjectiveFunction getObjectiveFunction() {
		return decorated().getObjectiveFunction();
	}
}
