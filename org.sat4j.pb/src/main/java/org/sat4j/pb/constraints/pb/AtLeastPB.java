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

import org.sat4j.minisat.constraints.card.AtLeast;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public final class AtLeastPB extends AtLeast implements PBConstr {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	private final BigInteger degree;

	private AtLeastPB(ILits voc, IVecInt ps, int degree) {
		super(voc, ps, degree);
		this.degree = BigInteger.valueOf(degree);
	}

	public static AtLeastPB atLeastNew(UnitPropagationListener s, ILits voc,
			IVecInt ps, int n) throws ContradictionException {
		int degree = niceParameters(s, voc, ps, n);
		if (degree == 0)
			return null;
		return new AtLeastPB(voc, ps, degree);
	}

	public static AtLeastPB atLeastNew(ILits voc, IVecInt ps, int n) {
		return new AtLeastPB(voc, ps, n);
	}

	public BigInteger getCoef(int literal) {
		return BigInteger.ONE;
	}

	public BigInteger getDegree() {
		return degree;
	}

	public ILits getVocabulary() {
		return voc;
	}

	public int[] getLits() {
		int[] tmp = new int[size()];
		System.arraycopy(lits, 0, tmp, 0, size());
		return tmp;
	}

	public BigInteger[] getCoefs() {
		BigInteger[] tmp = new BigInteger[size()];
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = BigInteger.ONE;
		return tmp;
	}

	/**
     * 
     */
	private boolean learnt = false;

	/**
	 * D?termine si la contrainte est apprise
	 * 
	 * @return true si la contrainte est apprise, false sinon
	 * @see org.sat4j.specs.IConstr#learnt()
	 */
	@Override
	public boolean learnt() {
		return learnt;
	}

	@Override
	public void setLearnt() {
		learnt = true;
	}

	@Override
	public void register() {
		assert learnt;
		// countFalsified();
	}

	@Override
	public void assertConstraint(UnitPropagationListener s) {
		for (int i = 0; i < size(); i++) {
			if (getVocabulary().isUnassigned(get(i))) {
				boolean ret = s.enqueue(get(i), this);
				assert ret;
			}
		}
	}

	public IVecInt computeAnImpliedClause() {
		return null;
	}

}
