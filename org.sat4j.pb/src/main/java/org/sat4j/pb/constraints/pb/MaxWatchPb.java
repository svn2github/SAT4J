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
public final class MaxWatchPb extends WatchPb {

	private static final long serialVersionUID = 1L;

	/**
	 * sum of the coefficients of the literals satisfied or unvalued
	 */
	private BigInteger watchCumul = BigInteger.ZERO;

	/**
	 * Builds a PB constraint for a0.x0 + a1.x1 + ... + an.xn >= k
	 * 
	 * This constructor is called for learnt pseudo boolean constraints.
	 * 
	 * @param voc
	 *            all the possible variables (vocabulary)
	 * @param mpb
	 *            data structure which contains literals of the constraint,
	 *            coefficients (a0, a1, ... an), and the degree of the
	 *            constraint (k). The constraint is a "more than" constraint.
	 */
	private MaxWatchPb(ILits voc, IDataStructurePB mpb) {

		super(mpb);
		this.voc = voc;

		activity = 0;
		watchCumul = BigInteger.ZERO;
	}

	/**
	 * Builds a PB constraint for a0.x0 + a1.x1 + ... + an.xn >= k
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
	private MaxWatchPb(ILits voc, int[] lits, BigInteger[] coefs,
			BigInteger degree) {

		super(lits, coefs, degree);
		this.voc = voc;

		activity = 0;
		watchCumul = BigInteger.ZERO;
	}

	/**
	 * All the literals are watched.
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
				// updating of the initial value for the counter
				voc.watch(lits[i] ^ 1, this);
				watchCumul = watchCumul.add(coefs[i]);
			}
		}

		assert watchCumul.compareTo(computeLeftSide()) >= 0;
		if (!learnt && watchCumul.compareTo(degree) < 0) {
			throw new ContradictionException("non satisfiable constraint");
		}
	}

	/*
	 * This method propagates any possible value.
	 * 
	 * This method is only called in the factory methods.
	 * 
	 * @see org.sat4j.minisat.constraints.WatchPb#computePropagation()
	 */
	@Override
	protected void computePropagation(UnitPropagationListener s)
			throws ContradictionException {
		// propagate any possible value
		int ind = 0;
		while (ind < coefs.length
				&& watchCumul.subtract(coefs[ind]).compareTo(degree) < 0) {
			if (voc.isUnassigned(lits[ind]) && !s.enqueue(lits[ind], this))
				// because this happens during the building of a constraint.
				throw new ContradictionException("non satisfiable constraint");
			ind++;
		}
		assert watchCumul.compareTo(computeLeftSide()) >= 0;
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
		voc.watch(p, this);

		assert watchCumul.compareTo(computeLeftSide()) >= 0 : "" + watchCumul
				+ "/" + computeLeftSide() + ":" + learnt;

		// finding the index for p in the array of literals
		int indiceP = 0;
		while ((lits[indiceP] ^ 1) != p)
			indiceP++;

		// compute the new value for watchCumul
		BigInteger coefP = coefs[indiceP];
		BigInteger newcumul = watchCumul.subtract(coefP);

		if (newcumul.compareTo(degree) < 0) {
			// there is a conflict
			assert !isSatisfiable();
			return false;
		}

		// if no conflict, not(p) can be propagated
		// allow a later un-assignation
		voc.undos(p).push(this);
		// really update watchCumul
		watchCumul = newcumul;

		// propagation
		int ind = 0;
		// limit is the margin between the sum of the coefficients of the
		// satisfied+unassigned literals
		// and the degree of the constraint
		BigInteger limit = watchCumul.subtract(degree);
		// for each coefficient greater than limit
		while (ind < coefs.length && limit.compareTo(coefs[ind]) < 0) {
			// its corresponding literal is implied
			if (voc.isUnassigned(lits[ind]) && (!s.enqueue(lits[ind], this))) {
				// if it is not possible then there is a conflict
				assert !isSatisfiable();
				return false;
			}
			ind++;
		}

		assert learnt || watchCumul.compareTo(computeLeftSide()) >= 0;
		assert watchCumul.compareTo(computeLeftSide()) >= 0;
		return true;
	}

	/**
	 * Remove a constraint from the solver
	 */
	public void remove(UnitPropagationListener upl) {
		for (int i = 0; i < lits.length; i++) {
			if (!voc.isFalsified(lits[i]))
				voc.watches(lits[i] ^ 1).remove(this);
		}
	}

	/**
	 * this method is called during backtrack
	 * 
	 * @param p
	 *            an unassigned literal
	 */
	public void undo(int p) {
		int indiceP = 0;
		while ((lits[indiceP] ^ 1) != p)
			indiceP++;

		assert coefs[indiceP].signum() > 0;

		watchCumul = watchCumul.add(coefs[indiceP]);
	}

	/**
	 * build a pseudo boolean constraint. Coefficients are positive integers
	 * less than or equal to the degree (this is called a normalized
	 * constraint).
	 * 
	 * @param s
	 *            a unit propagation listener (usually the solver)
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
		// Parameters must not be modified
		MaxWatchPb outclause = new MaxWatchPb(voc, lits, coefs, degree);

		if (outclause.degree.signum() <= 0) {
			return null;
		}

		outclause.computeWatches();

		outclause.computePropagation(s);

		return outclause;

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
		return new MaxWatchPb(voc, mpb);
	}

}
