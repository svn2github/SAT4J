/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;

/**
 * Data structure for pseudo-boolean constraint with watched literals.
 * 
 * All literals are watched. The sum of the literals satisfied or unvalued is
 * always memorized, to detect conflict.
 * 
 * 
 */

public class MinWatchPbLongLimit extends WatchPbLong {
	private static final long serialVersionUID = 1L;

	/**
	 * sum of the coefficients of the literals satisfied or unvalued
	 */
	protected long watchCumul = 0;

	/**
	 * if watchCumul is at Long.MAX_VALUE, contains the complement to the sum of
	 * the coefficients of the literals satisfied or unvalued
	 */
	protected long compWatchCumul = 0;

	/**
	 * is the literal of index i watching the constraint ?
	 */
	protected boolean[] watched;

	/**
	 * indexes of literals watching the constraint
	 */
	protected int[] watching;

	/**
	 * number of literals watching the constraint.
	 * 
	 * This is the real size of the array watching
	 */
	protected int watchingCount = 0;

	/**
	 * Basic constructor for pb constraint a0.x0 + a1.x1 + ... + an.xn >= k
	 * 
	 * This constructor is called for learnt pseudo boolean constraints.
	 * 
	 * @param voc
	 *            all the possible variables (vocabulary)
	 * @param mpb
	 *            a mutable PB constraint
	 */
	protected MinWatchPbLongLimit(ILits voc, IDataStructurePB mpb) {

		super(mpb);
		this.voc = voc;

		watching = new int[this.coefs.length];
		watched = new boolean[this.coefs.length];
		activity = 0;
		watchCumul = 0;
		watchingCount = 0;

	}

	/**
	 * Basic constructor for PB constraint a0.x0 + a1.x1 + ... + an.xn >= k
	 * 
	 * @param voc
	 *            all the possible variables (vocabulary)
	 * @param lits
	 *            literals of the constraint (x0,x1, ... xn)
	 * @param coefs
	 *            coefficients of the left side of the constraint (a0, a1, ...
	 *            an)
	 * @param degree
	 *            degree of the constraint (k)
	 */
	protected MinWatchPbLongLimit(ILits voc, int[] lits, BigInteger[] coefs, // NOPMD
			BigInteger degree, BigInteger sumCoefs) {

		super(lits, coefs, degree, sumCoefs);
		this.voc = voc;

		watching = new int[this.coefs.length];
		watched = new boolean[this.coefs.length];
		activity = 0;
		watchCumul = 0;
		watchingCount = 0;

	}

	/*
	 * This method initialize the watched literals.
	 * 
	 * This method is only called in the factory methods.
	 * 
	 * @see org.sat4j.minisat.constraints.WatchPb#computeWatches()
	 */
	@Override
	protected void computeWatches() throws ContradictionException {
		assert watchCumul == 0;
		assert watchingCount == 0;
		for (int i = 0; i < lits.length
				&& watchCumulMinusValueIsLessThanDegree(coefs[0]); i++) {
			if (!voc.isFalsified(lits[i])) {
				voc.watch(lits[i] ^ 1, this);
				watching[watchingCount++] = i;
				watched[i] = true;
				// update the initial value for watchCumul (poss)
				addToWC(coefs[i]);
			}
		}

		if (learnt)
			watchMoreForLearntConstraint();

		if (watchCumul < degree) {
			throw new ContradictionException("non satisfiable constraint");
		}
		assert nbOfWatched() == watchingCount;
	}

	private void watchMoreForLearntConstraint() {
		// looking for literals to be watched,
		// ordered by decreasing level
		int free = 1;
		int maxlevel, maxi, level;

		while (watchCumulMinusValueIsLessThanDegree(coefs[0]) && (free > 0)) {
			free = 0;
			// looking for the literal falsified
			// at the least (lowest ?) level
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
				// update of the watchCumul value
				addToWC(coefs[maxi]);
				free--;
				assert free >= 0;
			}
		}
		assert lits.length == 1 || watchingCount > 1;
	}

	/*
	 * This method propagates any possible value.
	 * 
	 * This method is only called in the factory methods.
	 * 
	 * @see
	 * org.sat4j.minisat.constraints.WatchPb#computePropagation(org.sat4j.minisat
	 * .UnitPropagationListener)
	 */
	@Override
	protected void computePropagation(UnitPropagationListener s)
			throws ContradictionException {
		// propagate any possible value
		int ind = 0;
		while (ind < lits.length
				&& watchCumulMinusValueIsLessThanDegree(coefs[watching[ind]])) {
			// (watchCumul - coefs[watching[ind]]) < degree) {
			if (voc.isUnassigned(lits[ind]) && !s.enqueue(lits[ind], this)) {
				throw new ContradictionException("non satisfiable constraint");
			}
			ind++;
		}
	}

	/**
	 * build a pseudo boolean constraint. Coefficients are positive integers
	 * less than or equal to the degree (this is called a normalized
	 * constraint).
	 * 
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
	public static MinWatchPbLongLimit normalizedMinWatchPbNew(
			UnitPropagationListener s, ILits voc, int[] lits,
			BigInteger[] coefs, BigInteger degree, BigInteger sumCoefs)
			throws ContradictionException {
		// Parameters must not be modified
		MinWatchPbLongLimit outclause = new MinWatchPbLongLimit(voc, lits,
				coefs, degree, sumCoefs);

		if (outclause.degree <= 0) {
			return null;
		}

		outclause.computeWatches();

		outclause.computePropagation(s);

		return outclause;

	}

	/**
	 * Number of really watched literals. It should return the same value as
	 * watchingCount.
	 * 
	 * This method must only be called for assertions.
	 * 
	 * @return number of watched literals.
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
	 * Propagation of a falsified literal
	 * 
	 * @param s
	 *            the solver
	 * @param p
	 *            the propagated literal (it must be falsified)
	 * @return false iff there is a conflict
	 */
	@Override
	public boolean propagate(UnitPropagationListener s, int p) {
		assert nbOfWatched() == watchingCount;
		assert watchingCount > 1;

		// finding the index for p in the array of literals (pIndice)
		// and in the array of watching (pIndiceWatching)
		int pIndiceWatching = 0;
		while (pIndiceWatching < watchingCount
				&& (lits[watching[pIndiceWatching]] ^ 1) != p)
			pIndiceWatching++;
		int pIndice = watching[pIndiceWatching];

		assert p == (lits[pIndice] ^ 1);
		assert watched[pIndice];

		// the greatest coefficient of the watched literals is necessary
		// (pIndice excluded)
		long maxCoef = maximalCoefficient(pIndice);

		// update watching and watched w.r.t. to the propagation of p
		// new literals will be watched, maxCoef could be changed
		maxCoef = updateWatched(maxCoef, pIndice);

		// long upWatchCumul = watchCumul - coefs[pIndice];
		assert nbOfWatched() == watchingCount;

		// if a conflict has been detected, return false
		if (watchCumulMinusValueIsLessThanDegree(coefs[pIndice])) {
			// if (watchCumul - coefs[pIndice] < degree) {
			// conflit
			voc.watch(p, this);
			assert watched[pIndice];
			assert !isSatisfiable();
			return false;
		} else {
			assert Long.MAX_VALUE - coefs[pIndice] > maxCoef;
			if (watchCumulMinusValueIsLessThanDegree(coefs[pIndice] + maxCoef)) {
				// some literals must be assigned to true and then propagated
				assert watchingCount != 0;
				// long limit = upWatchCumul - degree;
				// A REVOIR A PARTIR D'ICI
				for (int i = 0; i < watchingCount; i++) {
					assert Long.MAX_VALUE - coefs[pIndice] > coefs[watching[i]];
					// if (watchCumul - coefs[pIndice] - degree <
					// coefs[watching[i]]
					if (watchCumulMinusValueIsLessThanDegree(coefs[pIndice]
							+ coefs[watching[i]])
							&& i != pIndiceWatching
							&& !voc.isSatisfied(lits[watching[i]])
							&& !s.enqueue(lits[watching[i]], this)) {
						voc.watch(p, this);
						assert !isSatisfiable();
						return false;
					}
				}
			}
			// if the constraint is added to the undos of p (by propagation),
			// then p should be preserved.
			voc.undos(p).push(this);
		}

		// else p is no more watched
		watched[pIndice] = false;
		substractToWC(coefs[pIndice]);
		watching[pIndiceWatching] = watching[--watchingCount];

		assert watchingCount != 0;
		assert nbOfWatched() == watchingCount;

		return true;
	}

	/**
	 * Remove the constraint from the solver
	 */
	@Override
	public void remove(UnitPropagationListener upl) {
		for (int i = 0; i < watchingCount; i++) {
			voc.watches(lits[watching[i]] ^ 1).remove(this);
			this.watched[this.watching[i]] = false;
		}
		watchingCount = 0;
		assert nbOfWatched() == watchingCount;
		int ind = 0;
		while (ind < coefs.length
				&& watchCumulMinusValueIsLessThanDegree(coefs[ind])) {
			// (watchCumul - coefs[ind]) < degree) {
			upl.unset(lits[ind]);
			ind++;
		}
	}

	/**
	 * this method is called during backtrack
	 * 
	 * @param p
	 *            un unassigned literal
	 */
	@Override
	public void undo(int p) {
		voc.watch(p, this);
		int pIndice = 0;
		while ((lits[pIndice] ^ 1) != p)
			pIndice++;

		assert pIndice < lits.length;

		// watchCumul = watchCumul + coefs[pIndice];
		addToWC(coefs[pIndice]);

		assert watchingCount == nbOfWatched();

		watched[pIndice] = true;
		watching[watchingCount++] = pIndice;

		assert watchingCount == nbOfWatched();
	}

	/**
	 * build a pseudo boolean constraint from a specific data structure. For
	 * learnt constraints.
	 * 
	 * @param s
	 *            a unit propagation listener (usually the solver)
	 * @param mpb
	 *            data structure which contains literals of the constraint,
	 *            coefficients (a0, a1, ... an), and the degree of the
	 *            constraint (k). The constraint is a "more than" constraint.
	 * @return a new PB constraint or null if a trivial inconsistency is
	 *         detected.
	 */
	public static WatchPbLong normalizedWatchPbNew(ILits voc,
			IDataStructurePB mpb) {
		return new MinWatchPbLong(voc, mpb);
	}

	/**
	 * the maximal coefficient for the watched literals
	 * 
	 * @param pIndice
	 *            propagated literal : its coefficient is excluded from the
	 *            search of the maximal coefficient
	 * @return the maximal coefficient for the watched literals
	 */
	protected long maximalCoefficient(int pIndice) {
		long maxCoef = 0;
		for (int i = 0; i < watchingCount; i++)
			if (coefs[watching[i]] > maxCoef && watching[i] != pIndice) {
				maxCoef = coefs[watching[i]];
			}

		assert learnt || maxCoef != 0;
		// DLB assert maxCoef!=0;
		return maxCoef;
	}

	/**
	 * update arrays watched and watching w.r.t. the propagation of a literal.
	 * 
	 * return the maximal coefficient of the watched literals (could have been
	 * changed).
	 * 
	 * @param mc
	 *            the current maximal coefficient of the watched literals
	 * @param pIndice
	 *            the literal propagated (falsified)
	 * @return the new maximal coefficient of the watched literals
	 */
	protected long updateWatched(long mc, int pIndice) {
		long maxCoef = mc;
		// if not all the literals are watched
		if (watchingCount < size()) {
			// the watchCumul sum will have to be updated
			// long upWatchCumul = watchCumul - coefs[pIndice];
			long upWatchCumul = 0;
			long compUpWatchCumul = 0;
			if (compWatchCumul > 0) {
				assert watchCumul == Long.MAX_VALUE;
				compUpWatchCumul = compWatchCumul - coefs[pIndice];
				if (compUpWatchCumul < 0) {
					upWatchCumul = Long.MAX_VALUE - compUpWatchCumul;
					compUpWatchCumul = 0;
				}
			} else {
				upWatchCumul = watchCumul - coefs[pIndice];
			}
			// we must obtain upWatchCumul such that
			// upWatchCumul = degree + maxCoef
			// long degreePlusMaxCoef = degree + maxCoef; // dvh
			long degreePlusMaxCoef = 0;
			long compDegreePlusMaxCoef = 0;
			if (Long.MAX_VALUE - degree < maxCoef) {
				degreePlusMaxCoef = Long.MAX_VALUE;
				compDegreePlusMaxCoef = maxCoef - (Long.MAX_VALUE - degree);
			} else
				degreePlusMaxCoef = degree + maxCoef;
			for (int ind = 0; ind < lits.length; ind++) {
				// if (upWatchCumul >= degreePlusMaxCoef) {
				if ((compUpWatchCumul == 0 && compDegreePlusMaxCoef == 0 && upWatchCumul >= degreePlusMaxCoef)
						|| (compUpWatchCumul > compDegreePlusMaxCoef)
						|| (compUpWatchCumul == compDegreePlusMaxCoef && upWatchCumul == degreePlusMaxCoef)) {
					// nothing more to watch
					// note: logic negated to old version // dvh
					break;
				}
				// while upWatchCumul does not contain enough
				if (!voc.isFalsified(lits[ind]) && !watched[ind]) {
					// watch one more
					// upWatchCumul = upWatchCumul + coefs[ind];
					if (Long.MAX_VALUE - coefs[ind] >= upWatchCumul)
						upWatchCumul = upWatchCumul + coefs[ind];
					else {
						compUpWatchCumul = coefs[ind]
								- (Long.MAX_VALUE - upWatchCumul);
						upWatchCumul = Long.MAX_VALUE;
					}

					// update arrays watched and watching
					watched[ind] = true;
					assert watchingCount < size();
					watching[watchingCount++] = ind;
					voc.watch(lits[ind] ^ 1, this);
					// this new watched literal could change the maximal
					// coefficient
					if (coefs[ind] > maxCoef) {
						maxCoef = coefs[ind];
						// degreePlusMaxCoef = degree + maxCoef; // update
						compDegreePlusMaxCoef = 0;
						if (Long.MAX_VALUE - degree < maxCoef) {
							degreePlusMaxCoef = Long.MAX_VALUE;
							compDegreePlusMaxCoef = maxCoef
									- (Long.MAX_VALUE - degree);
						} else
							degreePlusMaxCoef = degree + maxCoef;
						// that one
						// too
					}
				}
			}
			// update watchCumul
			// watchCumul = upWatchCumul + coefs[pIndice];
			watchCumul = upWatchCumul;
			compWatchCumul = compUpWatchCumul;
			addToWC(coefs[pIndice]);
		}
		return maxCoef;
	}

	private boolean watchCumulMinusValueIsLessThanDegree(long coef) {
		return ((watchCumul - coef) < degree && watchCumul != Long.MAX_VALUE)
				|| (compWatchCumul - coef) > 0
				|| ((watchCumul - coef) + compWatchCumul) < degree;
	}

	private void addToWC(long coef) {
		if (Long.MAX_VALUE - coef >= watchCumul)
			watchCumul = watchCumul + coef;
		else {
			compWatchCumul = coef - (Long.MAX_VALUE - watchCumul);
			watchCumul = Long.MAX_VALUE;
		}
	}

	private void substractToWC(long coef) {
		if (compWatchCumul == 0)
			watchCumul = watchCumul - coef;
		else {
			compWatchCumul = compWatchCumul - coef;
			if (compWatchCumul < 0) {
				watchCumul = watchCumul - compWatchCumul;
				compWatchCumul = 0;
			}
		}
	}

}
