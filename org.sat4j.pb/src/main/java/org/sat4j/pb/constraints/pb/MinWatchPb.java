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

/**
 * Data structure for pseudo-boolean constraint with watched literals.
 * 
 * All literals are watched. The sum of the literals satisfied or unvalued is
 * always memorized, to detect conflict.
 * 
 * @author anne
 * 
 */
public class MinWatchPb extends WatchPb {

	private static final long serialVersionUID = 1L;

	/**
	 * sum of the coefficients of the literals satisfied or unvalued
	 */
	protected BigInteger watchCumul = BigInteger.ZERO;

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
	protected MinWatchPb(ILits voc, IDataStructurePB mpb) {

		super(mpb);
		this.voc = voc;

		watching = new int[this.coefs.length];
		watched = new boolean[this.coefs.length];
		activity = 0;
		watchCumul = BigInteger.ZERO;
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
	protected MinWatchPb(ILits voc, int[] lits, BigInteger[] coefs, // NOPMD
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
	 * This method initialize the watched literals.
	 * 
	 * This method is only called in the factory methods.
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
				// update the initial value for watchCumul (poss)
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
		// looking for literals to be watched,
		// ordered by decreasing level
		int free = 1;
		int maxlevel, maxi, level;

		while ((watchCumul.subtract(coefs[0]).compareTo(degree) < 0)
				&& (free > 0)) {
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
				watchCumul = watchCumul.add(coefs[maxi]);
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
				&& watchCumul.subtract(coefs[watching[ind]]).compareTo(degree) < 0) {
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
	public static MinWatchPb normalizedMinWatchPbNew(UnitPropagationListener s,
			ILits voc, int[] lits, BigInteger[] coefs, BigInteger degree)
			throws ContradictionException {
		// Parameters must not be modified
		MinWatchPb outclause = new MinWatchPb(voc, lits, coefs, degree);

		if (outclause.degree.signum() <= 0) {
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
		BigInteger maxCoef = maximalCoefficient(pIndice);

		// update watching and watched w.r.t. to the propogation of p
		// new literals will be watched, maxCoef could be changed
		maxCoef = updateWatched(maxCoef, pIndice);

		BigInteger upWatchCumul = watchCumul.subtract(coefs[pIndice]);
		assert nbOfWatched() == watchingCount;

		// if a conflict has been detected, return false
		if (upWatchCumul.compareTo(degree) < 0) {
			// conflit
			voc.watch(p, this);
			assert watched[pIndice];
			assert !isSatisfiable();
			return false;
		} else if (upWatchCumul.compareTo(degree.add(maxCoef)) < 0) {
			// some literals must be assigned to true and then propagated
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
			// if the constraint is added to the undos of p (by propagation),
			// then p should be preserved.
			voc.undos(p).push(this);
		}

		// else p is no more watched
		watched[pIndice] = false;
		watchCumul = upWatchCumul;
		watching[pIndiceWatching] = watching[--watchingCount];

		assert watchingCount != 0;
		assert nbOfWatched() == watchingCount;

		return true;
	}

	/**
	 * Remove the constraint from the solver
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
	 * this method is called during backtrack
	 * 
	 * @param p
	 *            un unassigned literal
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
	public static WatchPb normalizedWatchPbNew(ILits voc, IDataStructurePB mpb) {
		return new MinWatchPb(voc, mpb);
	}

	/**
	 * the maximal coefficient for the watched literals
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
	protected BigInteger updateWatched(BigInteger mc, int pIndice) {
		BigInteger maxCoef = mc;
		// if not all the literals are watched
		if (watchingCount < size()) {
			// the watchCumul sum will have to be updated
			BigInteger upWatchCumul = watchCumul.subtract(coefs[pIndice]);

			// we must obtain upWatchCumul such that
			// upWatchCumul = degree + maxCoef
			BigInteger degreePlusMaxCoef = degree.add(maxCoef); // dvh
			for (int ind = 0; ind < lits.length; ind++) {
				if (upWatchCumul.compareTo(degreePlusMaxCoef) >= 0) {
					// nothing more to watch
					// note: logic negated to old version // dvh
					break;
				}
				// while upWatchCumul does not contain enough
				if (!voc.isFalsified(lits[ind]) && !watched[ind]) {
					// watch one more
					upWatchCumul = upWatchCumul.add(coefs[ind]);
					// update arrays watched and watching
					watched[ind] = true;
					assert watchingCount < size();
					watching[watchingCount++] = ind;
					voc.watch(lits[ind] ^ 1, this);
					// this new watched literal could change the maximal
					// coefficient
					if (coefs[ind].compareTo(maxCoef) > 0) {
						maxCoef = coefs[ind];
						degreePlusMaxCoef = degree.add(maxCoef); // update
						// that one
						// too
					}
				}
			}
			// update watchCumul
			watchCumul = upWatchCumul.add(coefs[pIndice]);
		}
		return maxCoef;
	}

}
