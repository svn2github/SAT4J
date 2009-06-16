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

import java.io.Serializable;
import java.math.BigInteger;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Undoable;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public abstract class WatchPb implements PBConstr, Undoable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * constant for the initial type of inequality less than or equal
	 */
	public static final boolean ATMOST = false;

	/**
	 * constant for the initial type of inequality more than or equal
	 */
	public static final boolean ATLEAST = true;

	/**
	 * constraint activity
	 */
	protected double activity;

	/**
	 * coefficients of the literals of the constraint
	 */
	protected BigInteger[] coefs;

	/**
	 * degree of the pseudo-boolean constraint
	 */
	protected BigInteger degree;

	/**
	 * literals of the constraint
	 */
	protected int[] lits;

	/**
	 * true if the constraint is a learned constraint
	 */
	protected boolean learnt = false;

	/**
	 * sum of the coefficients of the literals satisfied or unvalued
	 */
	protected BigInteger watchCumul = BigInteger.ZERO;

	/**
	 * constraint's vocabulary
	 */
	protected ILits voc;

	/**
	 * This constructor is only available for the serialization.
	 */
	WatchPb() {
	}

	WatchPb(IDataStructurePB mpb) {
		int size = mpb.size();
		lits = new int[size];
		this.coefs = new BigInteger[size];
		mpb.buildConstraintFromMapPb(lits, coefs);

		this.degree = mpb.getDegree();

		// On peut trier suivant les coefficients
		sort();
	}

	WatchPb(int[] lits, BigInteger[] coefs, BigInteger degree) {
		this.lits = lits;
		this.coefs = coefs;
		this.degree = degree;
		// On peut trier suivant les coefficients
		sort();
	}

	/**
	 * This predicate tests wether the constraint is assertive at decision level
	 * dl
	 * 
	 * @param dl
	 * @return true iff the constraint is assertive at decision level dl.
	 */
	public boolean isAssertive(int dl) {
		BigInteger slack = BigInteger.ZERO;
		for (int i = 0; i < lits.length; i++) {
			if ((coefs[i].signum() > 0)
					&& ((!voc.isFalsified(lits[i]) || voc.getLevel(lits[i]) >= dl)))
				slack = slack.add(coefs[i]);
		}
		slack = slack.subtract(degree);
		if (slack.signum() < 0)
			return false;
		for (int i = 0; i < lits.length; i++) {
			if ((coefs[i].signum() > 0)
					&& (voc.isUnassigned(lits[i]) || voc.getLevel(lits[i]) >= dl)
					&& (slack.compareTo(coefs[i]) < 0)) {
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
		for (int q : lits) {
			if (voc.isFalsified(q)) {
				outReason.push(q ^ 1);
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
	 * to obtain the coefficient of the i-th literal of the constraint
	 * 
	 * @param i
	 *            index of the literal
	 * @return coefficient of the literal
	 */
	public BigInteger getCoef(int i) {
		return coefs[i];
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

	/**
	 * compute the slack of the current constraint slack = poss - degree of the
	 * constraint
	 * 
	 * @return la marge
	 */
	public BigInteger slackConstraint() {
		return recalcLeftSide().subtract(this.degree);
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
	public BigInteger slackConstraint(BigInteger[] theCoefs,
			BigInteger theDegree) {
		return recalcLeftSide(theCoefs).subtract(theDegree);
	}

	/**
	 * compute the sum of the coefficients of the satisfied or non-assigned
	 * literals of a described constraint (usually called poss)
	 * 
	 * @param coefs
	 *            coefficients of the constraint
	 * @return poss
	 */
	public BigInteger recalcLeftSide(BigInteger[] theCoefs) {
		BigInteger poss = BigInteger.ZERO;
		// Pour chaque litteral
		for (int i = 0; i < lits.length; i++)
			if (!voc.isFalsified(lits[i])) {
				assert theCoefs[i].signum() >= 0;
				poss = poss.add(theCoefs[i]);
			}
		return poss;
	}

	/**
	 * compute the sum of the coefficients of the satisfied or non-assigned
	 * literals of the current constraint (usually called poss)
	 * 
	 * @return poss
	 */
	public BigInteger recalcLeftSide() {
		return recalcLeftSide(this.coefs);
	}

	/**
	 * D?termine si la contrainte est toujours satisfiable
	 * 
	 * @return la contrainte est encore satisfiable
	 */
	protected boolean isSatisfiable() {
		return recalcLeftSide().compareTo(degree) >= 0;
	}

	/**
	 * is the constraint a learnt constrainte ?
	 * 
	 * @return true si la contrainte est apprise, false sinon
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
	 * Calcule le ppcm de deux nombres
	 * 
	 * @param a
	 *            premier nombre de l'op?ration
	 * @param b
	 *            second nombre de l'op?ration
	 * @return le ppcm en question
	 */
	protected static BigInteger ppcm(BigInteger a, BigInteger b) {
		return a.divide(a.gcd(b)).multiply(b);
	}

	/**
	 * Permet le r??????chantillonage de l'activit??? de la contrainte
	 * 
	 * @param d
	 *            facteur d'ajustement
	 */
	public void rescaleBy(double d) {
		activity *= d;
	}

	void selectionSort(int from, int to) {
		int i, j, best_i;
		BigInteger tmp;
		int tmp2;

		for (i = from; i < to - 1; i++) {
			best_i = i;
			for (j = i + 1; j < to; j++) {
				if ((coefs[j].compareTo(coefs[best_i]) > 0)
						|| ((coefs[j].equals(coefs[best_i])) && (lits[j] > lits[best_i])))
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
	 * La contrainte est apprise
	 */
	public void setLearnt() {
		learnt = true;
	}

	/**
	 * Simplifie la contrainte(l'all???ge)
	 * 
	 * @return true si la contrainte est satisfaite, false sinon
	 */
	public boolean simplify() {
		BigInteger cumul = BigInteger.ZERO;

		int i = 0;
		while (i < lits.length && cumul.compareTo(degree) < 0) {
			if (voc.isSatisfied(lits[i])) {
				// Mesure pessimiste
				cumul = cumul.add(coefs[i]);
			}
			i++;
		}

		return (cumul.compareTo(degree) >= 0);
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
			BigInteger buffInt = coefs[0];
			for (int i = 1; i < coefs.length; i++) {
				assert buffInt.compareTo(coefs[i]) >= 0;
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
			BigInteger pivot = coefs[indPivot];
			int litPivot = lits[indPivot];
			BigInteger tmp;
			int i = from - 1;
			int j = to;
			int tmp2;

			for (;;) {
				do
					i++;
				while ((coefs[i].compareTo(pivot) > 0)
						|| ((coefs[i].equals(pivot)) && (lits[i] > litPivot)));
				do
					j--;
				while ((pivot.compareTo(coefs[j]) > 0)
						|| ((coefs[j].equals(pivot)) && (lits[j] < litPivot)));

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
		BigInteger tmp = slackConstraint();
		for (int i = 0; i < lits.length; i++) {
			if (voc.isUnassigned(lits[i]) && tmp.compareTo(coefs[i]) < 0) {
				boolean ret = s.enqueue(lits[i], this);
				assert ret;
			}
		}
	}

	// protected abstract WatchPb watchPbNew(Lits voc, VecInt lits, VecInt
	// coefs, boolean moreThan, int degree, int[] indexer);
	/**
	 * @return Returns the degree.
	 */
	public BigInteger getDegree() {
		return degree;
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

	public BigInteger[] getCoefs() {
		BigInteger[] coefsBis = new BigInteger[coefs.length];
		System.arraycopy(coefs, 0, coefsBis, 0, coefs.length);
		return coefsBis;
	}

	public int[] getLits() {
		int[] litsBis = new int[lits.length];
		System.arraycopy(lits, 0, litsBis, 0, lits.length);
		return litsBis;
	}

	public ILits getVocabulary() {
		return voc;
	}

	/**
	 * compute an implied clause on the literals with the greater coefficients
	 */
	public IVecInt computeAnImpliedClause() {
		BigInteger cptCoefs = BigInteger.ZERO;
		int index = coefs.length;
		while ((cptCoefs.compareTo(degree) > 0) && (index > 0)) {
			cptCoefs = cptCoefs.add(coefs[--index]);
		}
		if (index > 0 && index < size() / 2) {
			// System.out.println(this);
			// System.out.println("index : "+index);
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

			WatchPb wpb = (WatchPb) pb;
			if (!degree.equals(wpb.degree) || coefs.length != wpb.coefs.length
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
						if (!wpb.coefs[ilit2].equals(coefs[ilit])) {
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

}
