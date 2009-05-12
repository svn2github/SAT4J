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

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;

public class MaxWatchPb extends WatchPb {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur de base cr?ant des contraintes vides
	 * 
	 * @param voc
	 *            Informations sur le vocabulaire employ?
	 * @param ps
	 *            Liste des litt?raux
	 * @param weightedLits
	 *            Liste des coefficients
	 * @param moreThan
	 *            Indication sur le comparateur
	 * @param degree
	 *            Stockage du degr? de la contrainte
	 */
	private MaxWatchPb(ILits voc, IDataStructurePB mpb) {

		super(mpb);
		this.voc = voc;

		activity = 0;
		watchCumul = BigInteger.ZERO;
	}

	private MaxWatchPb(ILits voc, int[] lits, BigInteger[] coefs,
			BigInteger degree) {

		super(lits, coefs, degree);
		this.voc = voc;

		activity = 0;
		watchCumul = BigInteger.ZERO;
	}

	/**
	 * Permet l'observation de tous les litt???raux
	 * 
	 * @see org.sat4j.pb.constraints.pb.WatchPb#computeWatches()
	 */
	@Override
	protected void computeWatches() throws ContradictionException {
		assert watchCumul.equals(BigInteger.ZERO);
		for (int i = 0; i < lits.length; i++) {
			if (voc.isFalsified(lits[i])) {
				if (learnt) {
					voc.undos(lits[i] ^ 1).push(this);
					voc.watch(lits[i] ^ 1, this);
				}
			} else {
				// Mise ? jour de la possibilit? initiale
				voc.watch(lits[i] ^ 1, this);
				watchCumul = watchCumul.add(coefs[i]);
			}
		}

		assert watchCumul.compareTo(recalcLeftSide()) >= 0;
		if (!learnt && watchCumul.compareTo(degree) < 0) {
			throw new ContradictionException("non satisfiable constraint");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.minisat.constraints.WatchPb#computePropagation()
	 */
	@Override
	protected void computePropagation(UnitPropagationListener s)
			throws ContradictionException {
		// On propage
		int ind = 0;
		while (ind < coefs.length
				&& watchCumul.subtract(coefs[ind]).compareTo(degree) < 0) {
			if (voc.isUnassigned(lits[ind]) && !s.enqueue(lits[ind], this))
				throw new ContradictionException("non satisfiable constraint");
			ind++;
		}
		assert watchCumul.compareTo(recalcLeftSide()) >= 0;
	}

	/**
	 * Propagation de la valeur de v?rit? d'un litt?ral falsifi?
	 * 
	 * @param s
	 *            un prouveur
	 * @param p
	 *            le litt?ral propag? (il doit etre falsifie)
	 * @return false ssi une inconsistance est d?tect?e
	 */
	public boolean propagate(UnitPropagationListener s, int p) {
		voc.watch(p, this);

		assert watchCumul.compareTo(recalcLeftSide()) >= 0 : "" + watchCumul
				+ "/" + recalcLeftSide() + ":" + learnt;

		// Si le litt?ral est impliqu? il y a un conflit
		int indiceP = 0;
		while ((lits[indiceP] ^ 1) != p)
			indiceP++;

		BigInteger coefP = coefs[indiceP];

		BigInteger newcumul = watchCumul.subtract(coefP);
		if (newcumul.compareTo(degree) < 0) {
			// System.out.println(this.analyse(new ConstrHandle()));

			assert !isSatisfiable();
			return false;
		}

		// On met en place la mise ? jour du compteur
		voc.undos(p).push(this);
		watchCumul = newcumul;

		// On propage
		int ind = 0;
		BigInteger limit = watchCumul.subtract(degree);
		while (ind < coefs.length && limit.compareTo(coefs[ind]) < 0) {
			if (voc.isUnassigned(lits[ind]) && (!s.enqueue(lits[ind], this))) {
				assert !isSatisfiable();
				return false;
			}
			ind++;
		}

		assert learnt || watchCumul.compareTo(recalcLeftSide()) >= 0;
		assert watchCumul.compareTo(recalcLeftSide()) >= 0;
		return true;
	}

	/**
	 * Enl???ve une contrainte du prouveur
	 */
	public void remove(UnitPropagationListener upl) {
		for (int i = 0; i < lits.length; i++) {
			if (!voc.isFalsified(lits[i]))
				voc.watches(lits[i] ^ 1).remove(this);
		}
	}

	/**
	 * M?thode appel?e lors du backtrack
	 * 
	 * @param p
	 *            un litt?ral d?saffect?
	 */
	public void undo(int p) {
		int indiceP = 0;
		while ((lits[indiceP] ^ 1) != p)
			indiceP++;

		assert coefs[indiceP].signum() > 0;

		watchCumul = watchCumul.add(coefs[indiceP]);
	}

	/**
	 * @param s
	 *            a unit propagation listener
	 * @param voc
	 *            the vocabulary
	 * @param lits
	 *            the literals of the constraint
	 * @param coefs
	 *            the coefficients of the constraint
	 * @param degree
	 *            the degree of the constraint
	 * @return a new PB constraint or null if a trivial inconsistency is
	 *         detected.
	 */
	public static MaxWatchPb normalizedMaxWatchPbNew(UnitPropagationListener s,
			ILits voc, int[] lits, BigInteger[] coefs, BigInteger degree)
			throws ContradictionException {
		// Il ne faut pas modifier les param?tres
		MaxWatchPb outclause = new MaxWatchPb(voc, lits, coefs, degree);

		if (outclause.degree.signum() <= 0) {
			return null;
		}

		outclause.computeWatches();

		outclause.computePropagation(s);

		return outclause;

	}

	/**
     * 
     */
	public static WatchPb normalizedWatchPbNew(ILits voc, IDataStructurePB mpb) {
		return new MaxWatchPb(voc, mpb);
	}

}
