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

public class MinWatchPb extends WatchPb {

	private static final long serialVersionUID = 1L;

	/**
	 * Liste des indices des litt???raux regardant la contrainte
	 */
	protected boolean[] watched;

	/**
	 * Sert ??? d???terminer si la clause est watched par le litt???ral
	 */
	protected int[] watching;

	/**
	 * Liste des indices des litt???raux regardant la contrainte
	 */
	protected int watchingCount = 0;

	/**
	 * Constructeur de base des contraintes
	 * 
	 * @param voc
	 *            Informations sur le vocabulaire employ???
	 * @param mpb
	 *            a mutable PB constraint
	 */
	protected MinWatchPb(ILits voc, IDataStructurePB mpb) {

		super(mpb);
		this.voc = voc;

		watching = new int[this.coefs.length];
		watched = new boolean[this.coefs.length];
		activity = 0;
		watchCumul = BigInteger.ZERO;
		watchingCount = 0;

	}

	protected MinWatchPb(ILits voc, int[] lits, BigInteger[] coefs,
			BigInteger degree) {

		super(lits, coefs, degree);
		this.voc = voc;

		watching = new int[this.coefs.length];
		watched = new boolean[this.coefs.length];
		activity = 0;
		watchCumul = BigInteger.ZERO;
		watchingCount = 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.minisat.constraints.WatchPb#computeWatches()
	 */
	@Override
	protected void computeWatches() throws ContradictionException {
		assert watchCumul.signum() == 0;
		assert watchingCount == 0;
		for (int i = 0; i < lits.length
				&& watchCumul.subtract(coefs[0]).compareTo(degree) < 0; i++) {
			if (!voc.isFalsified(lits[i])) {
				voc.watch(lits[i] ^ 1, this);
				watching[watchingCount++] = i;
				watched[i] = true;
				// Mise ??? jour de la possibilit??? initiale
				watchCumul = watchCumul.add(coefs[i]);
			}
		}

		if (learnt)
			watchMoreForLearntConstraint();

		if (watchCumul.compareTo(degree) < 0) {
			throw new ContradictionException("non satisfiable constraint");
		}
		assert nbOfWatched() == watchingCount;
	}

	private void watchMoreForLearntConstraint() {
		// chercher tous les litteraux a regarder
		// par ordre de niveau decroissant
		int free = 1;
		int maxlevel, maxi, level;

		while ((watchCumul.subtract(coefs[0]).compareTo(degree) < 0)
				&& (free > 0)) {
			free = 0;
			// regarder le litteral falsifie au plus bas niveau
			maxlevel = -1;
			maxi = -1;
			for (int i = 0; i < lits.length; i++) {
				if (voc.isFalsified(lits[i]) && !watched[i]) {
					free++;
					level = voc.getLevel(lits[i]);
					if (level > maxlevel) {
						maxi = i;
						maxlevel = level;
					}
				}
			}

			if (free > 0) {
				assert maxi >= 0;
				voc.watch(lits[maxi] ^ 1, this);
				watching[watchingCount++] = maxi;
				watched[maxi] = true;
				// Mise ??? jour de la possibilit??? initiale
				watchCumul = watchCumul.add(coefs[maxi]);
				free--;
				assert free >= 0;
			}
		}
		assert lits.length == 1 || watchingCount > 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sat4j.minisat.constraints.WatchPb#computePropagation(org.sat4j.minisat
	 * .UnitPropagationListener)
	 */
	@Override
	protected void computePropagation(UnitPropagationListener s)
			throws ContradictionException {
		// On propage si n???cessaire
		int ind = 0;
		while (ind < lits.length
				&& watchCumul.subtract(coefs[watching[ind]]).compareTo(degree) < 0) {
			if (voc.isUnassigned(lits[ind]) && !s.enqueue(lits[ind], this)) {
				throw new ContradictionException("non satisfiable constraint");
			}
			ind++;
		}
	}

	/**
	 * @param s
	 *            a unit propagation listener
	 * @param voc
	 *            the vocabulary
	 * @param lits
	 *            the literals
	 * @param coefs
	 *            the coefficients
	 * @param degree
	 *            the degree of the constraint to normalize.
	 * @return a new PB constraint or null if a trivial inconsistency is
	 *         detected.
	 */
	public static MinWatchPb normalizedMinWatchPbNew(UnitPropagationListener s,
			ILits voc, int[] lits, BigInteger[] coefs, BigInteger degree)
			throws ContradictionException {
		// Il ne faut pas modifier les param?tres
		MinWatchPb outclause = new MinWatchPb(voc, lits, coefs, degree);

		if (outclause.degree.signum() <= 0) {
			return null;
		}

		outclause.computeWatches();

		outclause.computePropagation(s);

		return outclause;

	}

	/**
	 * Nombre de litt???raux actuellement observ???
	 * 
	 * @return nombre de litt???raux regard???s
	 */
	protected int nbOfWatched() {
		int retour = 0;
		for (int ind = 0; ind < this.watched.length; ind++) {
			for (int i = 0; i < watchingCount; i++)
				if (watching[i] == ind)
					assert watched[ind];
			retour += (this.watched[ind]) ? 1 : 0;
		}
		return retour;
	}

	/**
	 * Propagation de la valeur de v???rit??? d'un litt???ral falsifi???
	 * 
	 * @param s
	 *            un prouveur
	 * @param p
	 *            le litt???ral propag??? (il doit etre falsifie)
	 * @return false ssi une inconsistance est d???t???ct???e
	 */
	public boolean propagate(UnitPropagationListener s, int p) {
		assert nbOfWatched() == watchingCount;
		assert watchingCount > 1;

		// Recherche de l'indice du litt???ral p
		int pIndiceWatching = 0;
		while (pIndiceWatching < watchingCount
				&& (lits[watching[pIndiceWatching]] ^ 1) != p)
			pIndiceWatching++;
		int pIndice = watching[pIndiceWatching];

		assert p == (lits[pIndice] ^ 1);
		assert watched[pIndice];

		// Recherche du coefficient maximal parmi ceux des litt???raux
		// observ???s
		BigInteger maxCoef = maximalCoefficient(pIndice);

		// Recherche de la compensation
		maxCoef = updateWatched(maxCoef, pIndice);

		BigInteger upWatchCumul = watchCumul.subtract(coefs[pIndice]);
		assert nbOfWatched() == watchingCount;

		// Effectuer les propagations, return si l'une est impossible
		if (upWatchCumul.compareTo(degree) < 0) {
			// conflit
			voc.watch(p, this);
			assert watched[pIndice];
			assert !isSatisfiable();
			return false;
		} else if (upWatchCumul.compareTo(degree.add(maxCoef)) < 0) {

			assert watchingCount != 0;
			BigInteger limit = upWatchCumul.subtract(degree);
			for (int i = 0; i < watchingCount; i++) {
				if (limit.compareTo(coefs[watching[i]]) < 0
						&& i != pIndiceWatching
						&& !voc.isSatisfied(lits[watching[i]])
						&& !s.enqueue(lits[watching[i]], this)) {
					voc.watch(p, this);
					assert !isSatisfiable();
					return false;
				}
			}
			// Si propagation ajoute la contrainte aux undos de p, conserver p
			voc.undos(p).push(this);
		}

		// sinon p peut sortir de la liste de watched
		watched[pIndice] = false;
		watchCumul = upWatchCumul;
		watching[pIndiceWatching] = watching[--watchingCount];

		assert watchingCount != 0;
		assert nbOfWatched() == watchingCount;

		return true;
	}

	/**
	 * Enl???ve une contrainte du prouveur
	 */
	public void remove(UnitPropagationListener upl) {
		for (int i = 0; i < watchingCount; i++) {
			voc.watches(lits[watching[i]] ^ 1).remove(this);
			this.watched[this.watching[i]] = false;
		}
		watchingCount = 0;
		assert nbOfWatched() == watchingCount;
	}

	/**
	 * M???thode appel???e lors du backtrack
	 * 
	 * @param p
	 *            un litt???ral d???saffect???
	 */
	public void undo(int p) {
		voc.watch(p, this);
		int pIndice = 0;
		while ((lits[pIndice] ^ 1) != p)
			pIndice++;

		assert pIndice < lits.length;

		watchCumul = watchCumul.add(coefs[pIndice]);

		assert watchingCount == nbOfWatched();

		watched[pIndice] = true;
		watching[watchingCount++] = pIndice;

		assert watchingCount == nbOfWatched();
	}

	/**
     * 
     */
	public static WatchPb normalizedWatchPbNew(ILits voc, IDataStructurePB mpb) {
		return new MinWatchPb(voc, mpb);
	}

	/**
	 * 
	 * @param pIndice
	 *            propagated literal : its coefficient is excluded from the
	 *            search of the maximal coefficient
	 * @return the maximal coefficient for the watched literals
	 */
	protected BigInteger maximalCoefficient(int pIndice) {
		BigInteger maxCoef = BigInteger.ZERO;
		for (int i = 0; i < watchingCount; i++)
			if (coefs[watching[i]].compareTo(maxCoef) > 0
					&& watching[i] != pIndice) {
				maxCoef = coefs[watching[i]];
			}

		assert learnt || maxCoef.signum() != 0;
		// DLB assert maxCoef!=0;
		return maxCoef;
	}

	protected BigInteger updateWatched(BigInteger mc, int pIndice) {
		BigInteger maxCoef = mc;
		if (watchingCount < size()) {
			BigInteger upWatchCumul = watchCumul.subtract(coefs[pIndice]);

			BigInteger degreePlusMaxCoef = degree.add(maxCoef); // dvh
			for (int ind = 0; ind < lits.length; ind++) {
				if (upWatchCumul.compareTo(degreePlusMaxCoef) >= 0) {
					// note: logic negated to old version // dvh
					break;
				}

				if (!voc.isFalsified(lits[ind]) && !watched[ind]) {
					upWatchCumul = upWatchCumul.add(coefs[ind]);
					watched[ind] = true;
					assert watchingCount < size();
					watching[watchingCount++] = ind;
					voc.watch(lits[ind] ^ 1, this);
					// Si on obtient un nouveau coefficient maximum
					if (coefs[ind].compareTo(maxCoef) > 0) {
						maxCoef = coefs[ind];
						degreePlusMaxCoef = degree.add(maxCoef); // update
						// that one
						// too
					}
				}
			}
			watchCumul = upWatchCumul.add(coefs[pIndice]);
		}
		return maxCoef;
	}

}
