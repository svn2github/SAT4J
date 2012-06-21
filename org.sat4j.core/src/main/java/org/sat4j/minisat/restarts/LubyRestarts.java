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
package org.sat4j.minisat.restarts;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;

/**
 * Luby series
 */
public final class LubyRestarts implements RestartStrategy {

	public static final int DEFAULT_LUBY_FACTOR = 32;

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	// 21-06-2012 back from SAT 2012
	// computing luby values the way presented by Donald Knuth in his invited
	// talk at the SAT 2012 conference
	// u1
	private int un = 1;
	// v1
	private int vn = 1;

	/**
	 * returns the current value of the luby sequence.
	 * 
	 * @return the current value of the luby sequence.
	 */
	public int luby() {
		return vn;
	}

	/**
	 * Computes and return the next value of the luby sequence. That method has
	 * a side effect of the value returned by luby(). luby()!=nextLuby() but
	 * nextLuby()==luby().
	 * 
	 * @return the new current value of the luby sequence.
	 * @see #luby()
	 */
	public int nextLuby() {
		if ((un & -un) == vn) {
			un = un + 1;
			vn = 1;
		} else {
			vn = vn << 1;
		}
		return vn;
	}

	private int factor;

	private int bound;
	private int conflictcount;

	public LubyRestarts() {
		this(DEFAULT_LUBY_FACTOR); // uses TiniSAT default
	}

	/**
	 * @param factor
	 *            the factor used for the Luby series.
	 * @since 2.1
	 */
	public LubyRestarts(int factor) {
		setFactor(factor);
	}

	public final void setFactor(int factor) {
		this.factor = factor;
	}

	public int getFactor() {
		return factor;
	}

	public void init(SearchParams params) {
		un = 1;
		vn = 1;
		bound = luby() * factor;
	}

	public long nextRestartNumberOfConflict() {
		return bound;
	}

	public void onRestart() {
		bound = nextLuby() * factor;
		conflictcount = 0;
	}

	@Override
	public String toString() {
		return "luby style (SATZ_rand, TiniSAT) restarts strategy with factor "
				+ factor;
	}

	public boolean shouldRestart() {
		return conflictcount >= bound;
	}

	public void onBackjumpToRootLevel() {
		conflictcount = 0;
	}

	public void reset() {
		conflictcount = 0;
	}

	public void newConflict() {
		conflictcount++;
	}

	public SearchParams getSearchParams() {
		return new SearchParams();
	}

	public void newLearnedClause(Constr learned, int trailLevel) {
	}
}
