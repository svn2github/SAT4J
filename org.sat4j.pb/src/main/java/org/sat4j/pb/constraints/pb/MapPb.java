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
package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.VarActivityListener;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author parrain
 * 
 */
public class MapPb implements IDataStructurePB {

	/*
	 * During the process of cutting planes, pseudo-boolean constraints are
	 * coded with a HashMap <literal, coefficient> and a BigInteger for the
	 * degree.
	 */
	protected InternalMapPBStructure weightedLits;

	protected BigInteger degree;

	protected int assertiveLiteral = -1;

	MapPb(PBConstr cpb) {
		weightedLits = new InternalMapPBStructure(cpb);
		degree = cpb.getDegree();
	}

	MapPb(int size) {
		weightedLits = new InternalMapPBStructure(size);
		degree = BigInteger.ZERO;
	}

	public boolean isCardinality() {
		for (int i = 0; i < size(); i++)
			if (!(weightedLits.getCoef(i).equals(BigInteger.ONE)))
				return false;
		return true;
	}

	public int getAssertiveLiteral() {
		return assertiveLiteral;
	}

	public BigInteger saturation() {
		assert degree.signum() > 0;
		BigInteger minimum = degree;
		for (int ind = 0; ind < size(); ind++) {
			assert weightedLits.getCoef(ind).signum() > 0;
			if (degree.compareTo(weightedLits.getCoef(ind)) < 0)
				changeCoef(ind, degree);
			assert weightedLits.getCoef(ind).signum() > 0;
			minimum = minimum.min(weightedLits.getCoef(ind));
		}
		// a clause has been learned
		if (minimum.equals(degree) && minimum.compareTo(BigInteger.ONE) > 0) {
			degree = BigInteger.ONE;
			for (int ind = 0; ind < size(); ind++)
				changeCoef(ind, BigInteger.ONE);
		}

		return degree;
	}

	public BigInteger cuttingPlane(PBConstr cpb, BigInteger deg,
			BigInteger[] reducedCoefs, VarActivityListener val) {
		return cuttingPlane(cpb, deg, reducedCoefs, BigInteger.ONE, val);
	}

	public BigInteger cuttingPlane(PBConstr cpb, BigInteger degreeCons,
			BigInteger[] reducedCoefs, BigInteger coefMult,
			VarActivityListener val) {
		degree = degree.add(degreeCons);
		assert degree.signum() > 0;

		if (reducedCoefs == null)
			for (int i = 0; i < cpb.size(); i++) {
				val.varBumpActivity(cpb.get(i));
				cuttingPlaneStep(cpb.get(i), multiplyCoefficient(
						cpb.getCoef(i), coefMult));
			}
		else
			for (int i = 0; i < cpb.size(); i++) {
				val.varBumpActivity(cpb.get(i));
				cuttingPlaneStep(cpb.get(i), multiplyCoefficient(
						reducedCoefs[i], coefMult));
			}

		return degree;
	}

	public BigInteger cuttingPlane(int[] lits, BigInteger[] reducedCoefs,
			BigInteger deg) {
		return cuttingPlane(lits, reducedCoefs, deg, BigInteger.ONE);
	}

	public BigInteger cuttingPlane(int lits[], BigInteger[] reducedCoefs,
			BigInteger degreeCons, BigInteger coefMult) {
		degree = degree.add(degreeCons);
		assert degree.signum() > 0;

		for (int i = 0; i < lits.length; i++)
			cuttingPlaneStep(lits[i], reducedCoefs[i].multiply(coefMult));

		return degree;
	}

	private void cuttingPlaneStep(final int lit, final BigInteger coef) {
		assert coef.signum() >= 0;
		int nlit = lit ^ 1;
		if (coef.signum() > 0) {
			if (weightedLits.containsKey(nlit)) {
				assert !weightedLits.containsKey(lit);
				assert weightedLits.get(nlit) != null;
				if (weightedLits.get(nlit).compareTo(coef) < 0) {
					BigInteger tmp = weightedLits.get(nlit);
					setCoef(lit, coef.subtract(tmp));
					assert weightedLits.get(lit).signum() > 0;
					degree = degree.subtract(tmp);
					removeCoef(nlit);
				} else {
					if (weightedLits.get(nlit).equals(coef)) {
						degree = degree.subtract(coef);
						removeCoef(nlit);
					} else {
						decreaseCoef(nlit, coef);
						assert weightedLits.get(nlit).signum() > 0;
						degree = degree.subtract(coef);
					}
				}
			} else {
				assert (!weightedLits.containsKey(lit))
						|| (weightedLits.get(lit).signum() > 0);
				if (weightedLits.containsKey(lit))
					increaseCoef(lit, coef);
				else
					setCoef(lit, coef);
				assert weightedLits.get(lit).signum() > 0;
			}
		}
		assert (!weightedLits.containsKey(nlit))
				|| (!weightedLits.containsKey(lit));
	}

	public void buildConstraintFromConflict(IVecInt resLits,
			IVec<BigInteger> resCoefs) {
		resLits.clear();
		resCoefs.clear();
		weightedLits.copyCoefs(resCoefs);
		weightedLits.copyLits(resLits);
	};

	public void buildConstraintFromMapPb(int[] resLits, BigInteger[] resCoefs) {
		// On recherche tous les litt?raux concern?s
		assert resLits.length == resCoefs.length;
		assert resLits.length == size();
		weightedLits.copyCoefs(resCoefs);
		weightedLits.copyLits(resLits);
	};

	public BigInteger getDegree() {
		return degree;
	}

	public int size() {
		return weightedLits.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer stb = new StringBuffer();
		for (int ind = 0; ind < size(); ind++) {
			stb.append(weightedLits.getCoef(ind));
			stb.append(".");
			stb.append(Lits.toString(weightedLits.getLit(ind)));
			stb.append(" ");
		}
		return stb.toString() + " >= " + degree; //$NON-NLS-1$
	}

	private BigInteger multiplyCoefficient(BigInteger coef, BigInteger mult) {
		if (coef.equals(BigInteger.ONE))
			return mult;
		return coef.multiply(mult);
	}

	void increaseCoef(int lit, BigInteger incCoef) {
		weightedLits.put(lit, weightedLits.get(lit).add(incCoef));
	}

	void decreaseCoef(int lit, BigInteger decCoef) {
		weightedLits.put(lit, weightedLits.get(lit).subtract(decCoef));
	}

	void setCoef(int lit, BigInteger newValue) {
		weightedLits.put(lit, newValue);
	}

	void changeCoef(int indLit, BigInteger newValue) {
		weightedLits.changeCoef(indLit, newValue);
	}

	void removeCoef(int lit) {
		weightedLits.remove(lit);
	}

}
