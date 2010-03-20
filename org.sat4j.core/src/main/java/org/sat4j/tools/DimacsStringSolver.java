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
package org.sat4j.tools;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;

/**
 * Solver used to write down a CNF into a String.
 * 
 * It is especially useful compared to the DimacsOutputSolver because the number
 * of clauses does not need to be known in advance.
 * 
 * @author leberre
 * 
 */
public class DimacsStringSolver implements ISolver {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	private StringBuffer out;

	private int nbvars;

	private int nbclauses;

	private boolean fixedNbClauses = false;

	private boolean firstConstr = true;

	private int firstCharPos;

	private final int initBuilderSize;

	private int maxvarid = 0;

	public DimacsStringSolver() {
		this(16);
	}

	public DimacsStringSolver(int initSize) {
		out = new StringBuffer(initSize);
		initBuilderSize = initSize;
	}

	public StringBuffer getOut() {
		return out;
	}

	public int newVar() {
		return 0;
	}

	public int newVar(int howmany) {
		setNbVars(howmany);
		return howmany;
	}

	protected void setNbVars(int howmany) {
		nbvars = howmany;
		maxvarid = howmany;
	}

	public void setExpectedNumberOfClauses(int nb) {
		out.append(" ");
		out.append(nb);
		nbclauses = nb;
		fixedNbClauses = true;
	}

	public IConstr addClause(IVecInt literals) throws ContradictionException {
		if (firstConstr) {
			if (!fixedNbClauses) {
				firstCharPos = 7 + Integer.toString(nbvars).length();
				out.append("                    ");
				out.append("\n");
				nbclauses = 0;
			}
			firstConstr = false;
		}
		if (!fixedNbClauses) {
			nbclauses++;
		}
		for (IteratorInt iterator = literals.iterator(); iterator.hasNext();) {
			out.append(iterator.next()).append(" ");
		}
		out.append("0\n");
		return null;
	}

	public boolean removeConstr(IConstr c) {
		throw new UnsupportedOperationException();
	}

	public void addAllClauses(IVec<IVecInt> clauses)
			throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	public IConstr addAtMost(IVecInt literals, int degree)
			throws ContradictionException {
		if (degree > 1) {
			throw new UnsupportedOperationException(
					"Not a clausal problem! degree " + degree);
		}
		assert degree == 1;
		if (firstConstr) {
			firstCharPos = 0;
			out.append("                    ");
			out.append("\n");
			nbclauses = 0;
			firstConstr = false;
		}

		for (int i = 0; i <= literals.size(); i++) {
			for (int j = i + 1; j < literals.size(); j++) {
				if (!fixedNbClauses) {
					nbclauses++;
				}
				out.append(-literals.get(i));
				out.append(" ");
				out.append(-literals.get(j));
				out.append(" 0\n");
			}
		}
		return null;
	}

	public IConstr addAtLeast(IVecInt literals, int degree)
			throws ContradictionException {
		if (degree > 1) {
			throw new UnsupportedOperationException(
					"Not a clausal problem! degree " + degree);
		}
		assert degree == 1;
		return addClause(literals);
	}

	public void setTimeout(int t) {
		// TODO Auto-generated method stub

	}

	public void setTimeoutMs(long t) {
		// TODO Auto-generated method stub
	}

	public int getTimeout() {
		return 0;
	}

	/**
	 * @since 2.1
	 */
	public long getTimeoutMs() {
		return 0L;
	}

	public void reset() {
		fixedNbClauses = false;
		firstConstr = true;
		out = new StringBuffer(initBuilderSize);
		maxvarid = 0;
	}

	public void printStat(PrintStream output, String prefix) {
		// TODO Auto-generated method stub
	}

	public void printStat(PrintWriter output, String prefix) {
		// TODO Auto-generated method stub

	}

	public Map<String, Number> getStat() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(String prefix) {
		return "Dimacs output solver";
	}

	public void clearLearntClauses() {
		// TODO Auto-generated method stub

	}

	public int[] model() {
		throw new UnsupportedOperationException();
	}

	public boolean model(int var) {
		throw new UnsupportedOperationException();
	}

	public boolean isSatisfiable() throws TimeoutException {
		throw new TimeoutException("There is no real solver behind!");
	}

	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
		throw new TimeoutException("There is no real solver behind!");
	}

	public int[] findModel() throws TimeoutException {
		throw new UnsupportedOperationException();
	}

	public int[] findModel(IVecInt assumps) throws TimeoutException {
		throw new UnsupportedOperationException();
	}

	public int nConstraints() {
		return nbclauses;
	}

	public int nVars() {
		return maxvarid;
	}

	@Override
	public String toString() {
		// String numClauses = Integer.toString(nbclauses);
		// int numClausesLength = numClauses.length();
		// for (int i = 0; i < numClausesLength; ++i) {
		// out.setCharAt(firstCharPos + i, numClauses.charAt(i));
		// }
		out.insert(firstCharPos, "p cnf " + maxvarid + " " + nbclauses);
		return out.toString();
	}

	public void expireTimeout() {
		// TODO Auto-generated method stub

	}

	public boolean isSatisfiable(IVecInt assumps, boolean global)
			throws TimeoutException {
		throw new TimeoutException("There is no real solver behind!");
	}

	public boolean isSatisfiable(boolean global) throws TimeoutException {
		throw new TimeoutException("There is no real solver behind!");
	}

	public void printInfos(PrintWriter output, String prefix) {
	}

	public void setTimeoutOnConflicts(int count) {

	}

	public boolean isDBSimplificationAllowed() {
		return false;
	}

	public void setDBSimplificationAllowed(boolean status) {

	}

	/**
	 * @since 2.1
	 */
	public void setSearchListener(SearchListener sl) {
	}

	/**
	 * @since 2.1
	 */
	public int nextFreeVarId(boolean reserve) {
		if (reserve) {
			maxvarid++;
			return maxvarid;
		}
		return maxvarid;
	}

	/**
	 * @since 2.1
	 */
	public boolean removeSubsumedConstr(IConstr c) {
		return false;
	}

	/**
	 * @since 2.1
	 */
	public IConstr addBlockingClause(IVecInt literals)
			throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @since 2.2
	 */
	public SearchListener getSearchListener() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @since 2.2
	 */
	public boolean isVerbose() {
		return true;
	}

	/**
	 * @since 2.2
	 */
	public void setVerbose(boolean value) {
		// do nothing
	}

	/**
	 * @since 2.2
	 */
	public void setLogPrefix(String prefix) {
		// do nothing

	}

	/**
	 * @since 2.2
	 */
	public String getLogPrefix() {
		return "";
	}

	/**
	 * @since 2.2
	 */
	public IVecInt unsatExplanation() {
		throw new UnsupportedOperationException();
	}
}
