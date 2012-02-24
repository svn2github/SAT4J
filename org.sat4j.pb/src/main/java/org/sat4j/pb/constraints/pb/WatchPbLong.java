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

import java.io.Serializable;
import java.math.BigInteger;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Propagatable;
import org.sat4j.minisat.core.Undoable;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public abstract class WatchPbLong implements Propagatable, Constr, Undoable,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * constraint activity
	 */
	protected double activity;

	/**
	 * coefficients of the literals of the constraint
	 */
	protected long[] coefs;

	protected long sumcoefs;

	/**
	 * degree of the pseudo-boolean constraint
	 */
	protected long degree;

	/**
	 * literals of the constraint
	 */
	protected int[] lits;

	/**
	 * true if the constraint is a learned constraint
	 */
	protected boolean learnt = false;

	/**
	 * constraint's vocabulary
	 */
	protected ILits voc;

	/**
	 * This constructor is only available for the serialization.
	 */
	WatchPbLong() {
	}

	/** Constructor used for learnt constraints. */
	WatchPbLong(IDataStructurePB mpb) {
		int size = mpb.size();
		lits = new int[size];
		BigInteger[] bigCoefs = new BigInteger[size];
		mpb.buildConstraintFromMapPb(lits, bigCoefs);
		coefs = toLong(bigCoefs);

		this.degree = mpb.getDegree().longValue();
		sumcoefs = 0;
		for (long c : coefs) {
			sumcoefs += c;
		}
		learnt = true;
		// arrays are sorted by decreasing coefficients
		sort();
	}

	/** Constructor used for original constraints. */
	WatchPbLong(int[] lits, BigInteger[] coefs, BigInteger degree,
			BigInteger sumCoefs) { // NOPMD
		this.lits = lits;
		this.coefs = toLong(coefs);
		this.degree = degree.longValue();
		sumcoefs = sumCoefs.longValue();
		// arrays are sorted by decreasing coefficients
		sort();
	}

	public static long[] toLong(BigInteger[] bigValues) {
		long[] res = new long[bigValues.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = bigValues[i].longValue();
		}
		return res;
	}

	/**
	 * This predicate tests wether the constraint is assertive at decision level
	 * dl
	 * 
	 * @param dl
	 * @return true iff the constraint is assertive at decision level dl.
	 */
	public boolean isAssertive(int dl) {
		long slack = 0;
		for (int i = 0; i < lits.length; i++) {
			if ((coefs[i] > 0)
					&& ((!voc.isFalsified(lits[i]) || voc.getLevel(lits[i]) >= dl)))
				slack = slack + coefs[i];
		}
		slack = slack - degree;
		if (slack < 0)
			return false;
		for (int i = 0; i < lits.length; i++) {
			if ((coefs[i] > 0)
					&& (voc.isUnassigned(lits[i]) || voc.getLevel(lits[i]) >= dl)
					&& (slack < coefs[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * compute the reason for the assignment of a literal
	 * 
	 * @param p
	 *            a falsified literal (or Lit.UNDEFINED)
	 * @param outReason
	 *            list of falsified literals for which the negation is the
	 *            reason of the assignment
	 * @see org.sat4j.minisat.core.Constr#calcReason(int, IVecInt)
	 */
	public void calcReason(int p, IVecInt outReason) {
		long sumfalsified = 0;
		long tous = 0;
		final int[] mlits = lits;
		boolean ok = true;
		for (int i = 0; i < mlits.length; i++) {
			int q = mlits[i];
			if (voc.isFalsified(q)) {
				if (ok) {
					outReason.push(q ^ 1);
					sumfalsified += coefs[i];
					if (sumcoefs - sumfalsified < degree) {
						ok = false;
						tous = sumfalsified;
						// return;
					}
				} else {
					tous += coefs[i];
				}
			}
		}
	}

	abstract protected void computeWatches() throws ContradictionException;

	abstract protected void computePropagation(UnitPropagationListener s)
			throws ContradictionException;

	/**
	 * to obtain the i-th literal of the constraint
	 * 
	 * @param i
	 *            index of the literal
	 * @return the literal
	 */
	public int get(int i) {
		return lits[i];
	}

	/**
	 * to obtain the activity value of the constraint
	 * 
	 * @return activity value of the constraint
	 * @see org.sat4j.minisat.core.Constr#getActivity()
	 */
	public double getActivity() {
		return activity;
	}

	/**
	 * increase activity value of the constraint
	 * 
	 * @see org.sat4j.minisat.core.Constr#incActivity(double)
	 */
	public void incActivity(double claInc) {
		if (learnt) {
			activity += claInc;
		}
	}

	public void setActivity(double d) {
		if (learnt) {
			activity = d;
		}
	}

	/**
	 * compute the slack of the current constraint slack = poss - degree of the
	 * constraint
	 * 
	 * @return la marge
	 */
	public long slackConstraint() {
		return computeLeftSide() - this.degree;
	}

	/**
	 * compute the slack of a described constraint slack = poss - degree of the
	 * constraint
	 * 
	 * @param theCoefs
	 *            coefficients of the constraint
	 * @param theDegree
	 *            degree of the constraint
	 * @return slack of the constraint
	 */
	public long slackConstraint(long[] theCoefs, long theDegree) {
		return computeLeftSide(theCoefs) - theDegree;
	}

	/**
	 * compute the sum of the coefficients of the satisfied or non-assigned
	 * literals of a described constraint (usually called poss)
	 * 
	 * @param coefs
	 *            coefficients of the constraint
	 * @return poss
	 */
	public long computeLeftSide(long[] theCoefs) {
		long poss = 0;
		// for each literal
		for (int i = 0; i < lits.length; i++)
			if (!voc.isFalsified(lits[i])) {
				assert theCoefs[i] >= 0;
				poss = poss + theCoefs[i];
			}
		return poss;
	}

	/**
	 * compute the sum of the coefficients of the satisfied or non-assigned
	 * literals of the current constraint (usually called poss)
	 * 
	 * @return poss
	 */
	public long computeLeftSide() {
		return computeLeftSide(this.coefs);
	}

	/**
	 * tests if the constraint is still satisfiable.
	 * 
	 * this method is only called in assertions.
	 * 
	 * @return the constraint is satisfiable
	 */
	protected boolean isSatisfiable() {
		return computeLeftSide() >= degree;
	}

	/**
	 * is the constraint a learnt constraint ?
	 * 
	 * @return true if the constraint is learnt, else false
	 * @see org.sat4j.specs.IConstr#learnt()
	 */
	public boolean learnt() {
		return learnt;
	}

	/**
	 * The constraint is the reason of a unit propagation.
	 * 
	 * @return true
	 */
	public boolean locked() {
		for (int p : lits) {
			if (voc.getReason(p) == this) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ppcm : least common multiple for two integers (plus petit commun
	 * multiple)
	 * 
	 * @param a
	 *            one integer
	 * @param b
	 *            the other integer
	 * @return the least common multiple of a and b
	 */
	protected static BigInteger ppcm(BigInteger a, BigInteger b) {
		return a.divide(a.gcd(b)).multiply(b);
	}

	/**
	 * to re-scale the activity of the constraint
	 * 
	 * @param d
	 *            adjusting factor
	 */
	public void rescaleBy(double d) {
		activity *= d;
	}

	void selectionSort(int from, int to) {
		int i, j, best_i;
		long tmp;
		int tmp2;

		for (i = from; i < to - 1; i++) {
			best_i = i;
			for (j = i + 1; j < to; j++) {
				if ((coefs[j] > coefs[best_i])
						|| ((coefs[j] == coefs[best_i]) && (lits[j] > lits[best_i])))
					best_i = j;
			}
			tmp = coefs[i];
			coefs[i] = coefs[best_i];
			coefs[best_i] = tmp;
			tmp2 = lits[i];
			lits[i] = lits[best_i];
			lits[best_i] = tmp2;
		}
	}

	/**
	 * the constraint is learnt
	 */
	public void setLearnt() {
		learnt = true;
	}

	/**
	 * simplify the constraint (if it is satisfied)
	 * 
	 * @return true if the constraint is satisfied, else false
	 */
	public boolean simplify() {
		long cumul = 0;

		int i = 0;
		while (i < lits.length && cumul < degree) {
			if (voc.isSatisfied(lits[i])) {
				// strong measure
				cumul = cumul + coefs[i];
			}
			i++;
		}

		return (cumul >= degree);
	}

	public final int size() {
		return lits.length;
	}

	/**
	 * sort coefficient and literal arrays
	 */
	final protected void sort() {
		assert this.lits != null;
		if (coefs.length > 0) {
			this.sort(0, size());
			long buffInt = coefs[0];
			for (int i = 1; i < coefs.length; i++) {
				assert buffInt >= coefs[i];
				buffInt = coefs[i];
			}
		}
	}

	/**
	 * sort partially coefficient and literal arrays
	 * 
	 * @param from
	 *            index for the beginning of the sort
	 * @param to
	 *            index for the end of the sort
	 */
	final protected void sort(int from, int to) {
		int width = to - from;
		if (width <= 15)
			selectionSort(from, to);

		else {
			int indPivot = width / 2 + from;
			long pivot = coefs[indPivot];
			int litPivot = lits[indPivot];
			long tmp;
			int i = from - 1;
			int j = to;
			int tmp2;

			for (;;) {
				do
					i++;
				while ((coefs[i] > pivot)
						|| ((coefs[i] == pivot) && (lits[i] > litPivot)));
				do
					j--;
				while ((pivot > coefs[j])
						|| ((coefs[j] == pivot) && (lits[j] < litPivot)));

				if (i >= j)
					break;

				tmp = coefs[i];
				coefs[i] = coefs[j];
				coefs[j] = tmp;
				tmp2 = lits[i];
				lits[i] = lits[j];
				lits[j] = tmp2;
			}

			sort(from, i);
			sort(i, to);
		}

	}

	@Override
	public String toString() {
		StringBuffer stb = new StringBuffer();

		if (lits.length > 0) {
			for (int i = 0; i < lits.length; i++) {
				// if (voc.isUnassigned(lits[i])) {
				stb.append(" + ");
				stb.append(this.coefs[i]);
				stb.append(".");
				stb.append(Lits.toString(this.lits[i]));
				stb.append("[");
				stb.append(voc.valueToString(lits[i]));
				stb.append("@");
				stb.append(voc.getLevel(lits[i]));
				stb.append("]");
				stb.append(" ");
				// }
			}
			stb.append(">= ");
			stb.append(this.degree);
		}
		return stb.toString();
	}

	public void assertConstraint(UnitPropagationListener s) {
		long tmp = slackConstraint();
		for (int i = 0; i < lits.length; i++) {
			if (voc.isUnassigned(lits[i]) && tmp < coefs[i]) {
				boolean ret = s.enqueue(lits[i], this);
				assert ret;
			}
		}
	}

	public void register() {
		assert learnt;
		try {
			computeWatches();
		} catch (ContradictionException e) {
			System.out.println(this);
			assert false;
		}
	}

	/**
	 * to obtain the literals of the constraint.
	 * 
	 * @return a copy of the array of the literals
	 */
	public int[] getLits() {
		int[] litsBis = new int[lits.length];
		System.arraycopy(lits, 0, litsBis, 0, lits.length);
		return litsBis;
	}

	public ILits getVocabulary() {
		return voc;
	}

	/**
	 * compute an implied clause on the literals with the greater coefficients.
	 * 
	 * @return a vector containing the literals for this clause.
	 */
	public IVecInt computeAnImpliedClause() {
		long cptCoefs = 0;
		int index = coefs.length;
		while ((cptCoefs > degree) && (index > 0)) {
			cptCoefs = cptCoefs + coefs[--index];
		}
		if (index > 0 && index < size() / 2) {
			IVecInt literals = new VecInt(index);
			for (int j = 0; j <= index; j++)
				literals.push(lits[j]);
			return literals;
		}
		return null;
	}

	public boolean coefficientsEqualToOne() {
		return false;
	}

	@Override
	public boolean equals(Object pb) {
		if (pb == null) {
			return false;
		}
		// this method should be simplified since now two constraints should
		// have
		// always
		// their literals in the same order
		try {

			WatchPbLong wpb = (WatchPbLong) pb;
			if (degree != wpb.degree || coefs.length != wpb.coefs.length
					|| lits.length != wpb.lits.length) {
				return false;
			}
			int lit;
			boolean ok;
			for (int ilit = 0; ilit < coefs.length; ilit++) {
				lit = lits[ilit];
				ok = false;
				for (int ilit2 = 0; ilit2 < coefs.length; ilit2++)
					if (wpb.lits[ilit2] == lit) {
						if (wpb.coefs[ilit2] != coefs[ilit]) {
							return false;
						}

						ok = true;
						break;

					}
				if (!ok) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		long sum = 0;
		for (int p : lits) {
			sum += p;
		}
		return (int) sum / lits.length;
	}

	public void forwardActivity(double claInc) {
		if (!learnt) {
			activity += claInc;
		}
	}

	public void remove(UnitPropagationListener upl) {
	}

	public boolean propagate(UnitPropagationListener s, int p) {
		return false;
	}

	public void undo(int p) {
	}

	public boolean canBePropagatedMultipleTimes() {
		return true;
	}

	public Constr toConstraint() {
		return this;
	}
}
