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

import org.sat4j.minisat.constraints.card.MinWatchCard;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public final class MinWatchCardPB extends MinWatchCard implements PBConstr {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	private final BigInteger bigDegree;

	public MinWatchCardPB(ILits voc, IVecInt ps, boolean moreThan, int degree) {
		super(voc, ps, moreThan, degree);
		// this.degree has been computed in the superclass constructor.
		this.bigDegree = BigInteger.valueOf(this.degree);
	}

	public MinWatchCardPB(ILits voc, IVecInt ps, int degree) {
		super(voc, ps, degree);
		this.bigDegree = BigInteger.valueOf(this.degree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.minisat.constraints.pb.PBConstr#getCoefficient(int)
	 */
	public BigInteger getCoef(int literal) {
		return BigInteger.ONE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.minisat.constraints.pb.PBConstr#getDegree()
	 */
	public BigInteger getDegree() {
		return bigDegree;
	}

	public BigInteger[] getCoefs() {
		BigInteger[] tmp = new BigInteger[size()];
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = BigInteger.ONE;
		return tmp;
	}

	/**
	 * Permet la cr?ation de contrainte de cardinalit? ? observation minimale
	 * 
	 * @param s
	 *            outil pour la propagation des litt?raux
	 * @param voc
	 *            vocabulaire utilis? par la contrainte
	 * @param ps
	 *            liste des litt?raux de la nouvelle contrainte
	 * @param degree
	 *            fournit le degr? de la contrainte
	 * @return une nouvelle clause si tout va bien, null sinon
	 * @throws ContradictionException
	 */
	public static MinWatchCardPB normalizedMinWatchCardPBNew(
			UnitPropagationListener s, ILits voc, IVecInt ps, int degree)
			throws ContradictionException {
		return minWatchCardPBNew(s, voc, ps, ATLEAST, degree, true);
	}

	/**
	 * Permet la cr?ation de contrainte de cardinalit? ? observation minimale
	 * 
	 * @param s
	 *            outil pour la propagation des litt?raux
	 * @param voc
	 *            vocabulaire utilis? par la contrainte
	 * @param ps
	 *            liste des litt?raux de la nouvelle contrainte
	 * @param moreThan
	 *            d?termine si c'est une sup?rieure ou ?gal ? l'origine
	 * @param degree
	 *            fournit le degr? de la contrainte
	 * @return une nouvelle clause si tout va bien, null sinon
	 * @throws ContradictionException
	 */
	public static MinWatchCardPB minWatchCardPBNew(UnitPropagationListener s,
			ILits voc, IVecInt ps, boolean moreThan, int degree)
			throws ContradictionException {
		return minWatchCardPBNew(s, voc, ps, moreThan, degree, false);
	}

	private static MinWatchCardPB minWatchCardPBNew(UnitPropagationListener s,
			ILits voc, IVecInt ps, boolean moreThan, int degree,
			boolean normalized) throws ContradictionException {
		int mydegree = degree + linearisation(voc, ps);

		if (ps.size() == 0 && mydegree > 0) {
			throw new ContradictionException();
		} else if (ps.size() == mydegree || ps.size() <= 0) {
			for (int i = 0; i < ps.size(); i++)
				if (!s.enqueue(ps.get(i))) {
					throw new ContradictionException();
				}
			return null;
		}

		// constraint is now instanciated
		MinWatchCardPB retour = null;
		if (normalized)
			retour = new MinWatchCardPB(voc, ps, mydegree);
		else
			retour = new MinWatchCardPB(voc, ps, moreThan, mydegree);

		if (retour.bigDegree.signum() <= 0)
			return null;

		retour.computeWatches();

		return ((MinWatchCardPB) retour.computePropagation(s));
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
		computeWatches();
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
