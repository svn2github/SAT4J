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
 * Based on the original MiniSat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 * 
 *******************************************************************************/
package org.sat4j.minisat.constraints.cnf;

import java.io.Serializable;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

/**
 * Lazy data structure for clause using Watched Literals.
 * 
 * @author leberre
 */
public abstract class WLClause implements Constr, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * @since 2.1
	 */
	protected double activity;

	protected final int[] lits;

	protected final ILits voc;

	protected int literal0;
	protected int literal1;

	/**
	 * Creates a new basic clause
	 * 
	 * @param voc
	 *            the vocabulary of the formula
	 * @param ps
	 *            A VecInt that WILL BE EMPTY after calling that method.
	 */
	public WLClause(IVecInt ps, ILits voc) {
		lits = new int[ps.size() - 2];
		literal0 = ps.get(0);
		literal1 = ps.get(1);
		ps.moveTo(2, lits);
		assert ps.size() == 0;
		this.voc = voc;
		activity = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Constr#calcReason(Solver, Lit, Vec)
	 */
	public void calcReason(int p, IVecInt outReason) {
		// assert outReason.size() == 0
		// && ((p == ILits.UNDEFINED) || (p == lits[0]));
		if (p == ILits.UNDEFINED) {
			outReason.push(literal0 ^ 1);
		}
		outReason.push(literal1 ^ 1);
		final int[] mylits = lits;
		for (int i = 0; i < mylits.length; i++) {
			assert voc.isFalsified(mylits[i]);
			outReason.push(mylits[i] ^ 1);
		}
	}

	/**
	 * @since 2.1
	 */
	public void remove(UnitPropagationListener upl) {
		voc.removeWatch(literal0 ^ 1, this);
		voc.removeWatch(literal1 ^ 1, this);
		// la clause peut etre effacee
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Constr#simplify(Solver)
	 */
	public boolean simplify() {
		if (voc.isSatisfied(literal0) || voc.isSatisfied(literal1)) {
			return true;
		}

		for (int i = 0; i < lits.length; i++) {
			if (voc.isSatisfied(lits[i])) {
				return true;
			}
		}
		return false;
	}

	public boolean propagate(UnitPropagationListener s, int p) {
		// literal1 must contain a falsified literal
		if (literal0 == (p ^ 1)) {
			literal0 = literal1;
			literal1 = p ^ 1;
		}
		if (voc.isSatisfied(literal0)) {
			voc.watch(p, this);
			return true;
		}
		final int[] mylits = lits;
		int previous = p ^ 1, tmp;
		// look for new literal to watch: applying move to front strategy
		for (int i = 0; i < mylits.length; i++) {
			if (!voc.isFalsified(mylits[i])) {
				literal1 = mylits[i];
				mylits[i] = previous;
				voc.watch(literal1 ^ 1, this);
				return true;
			} else {
				tmp = previous;
				previous = mylits[i];
				mylits[i] = tmp;
			}
		}
		// assert voc.isFalsified(mylits[1]);
		// the clause is now either unit or null
		// move back the literals to their initial position
		literal1 = mylits[0];
		for (int i = 1; i < mylits.length; i++) {
			mylits[i - 1] = mylits[i];
		}
		mylits[mylits.length - 1] = previous;
		voc.watch(p, this);
		// propagates first watched literal
		return s.enqueue(literal0, this);
	}

	/*
	 * For learnt clauses only @author leberre
	 */
	public boolean locked() {
		return voc.getReason(literal0) == this;
	}

	/**
	 * @return the activity of the clause
	 */
	public double getActivity() {
		return activity;
	}

	@Override
	public String toString() {
		StringBuffer stb = new StringBuffer();
		stb.append(literal0);
		stb.append(" ");
		stb.append(literal1);
		stb.append(" ");
		for (int i = 0; i < lits.length; i++) {
			stb.append(Lits.toString(lits[i]));
			stb.append("["); //$NON-NLS-1$
			stb.append(voc.valueToString(lits[i]));
			stb.append("]"); //$NON-NLS-1$
			stb.append(" "); //$NON-NLS-1$
		}
		return stb.toString();
	}

	/**
	 * Retourne le ieme literal de la clause. Attention, cet ordre change durant
	 * la recherche.
	 * 
	 * @param i
	 *            the index of the literal
	 * @return the literal
	 */
	public int get(int i) {
		if (i == 0) {
			return literal0;
		}
		if (i == 1) {
			return literal1;
		}
		return lits[i - 2];
	}

	/**
	 * @param d
	 */
	public void rescaleBy(double d) {
		activity *= d;
	}

	public int size() {
		return lits.length + 2;
	}

	public void assertConstraint(UnitPropagationListener s) {
		boolean ret = s.enqueue(literal0, this);
		assert ret;
	}

	public ILits getVocabulary() {
		return voc;
	}

	public int[] getLits() {
		int[] tmp = new int[size()];
		tmp[0] = literal0;
		tmp[1] = literal1;
		System.arraycopy(lits, 0, tmp, 2, size());
		return tmp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		try {
			WLClause wcl = (WLClause) obj;
			if (lits.length != wcl.lits.length)
				return false;
			boolean ok;
			for (int lit : lits) {
				ok = false;
				for (int lit2 : wcl.lits)
					if (lit == lit2) {
						ok = true;
						break;
					}
				if (!ok)
					return false;
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

	/**
	 * @since 2.2.1
	 */
	public int getShortCircuitLiteral() {
		return lits[lits.length / 2];
	}
}
