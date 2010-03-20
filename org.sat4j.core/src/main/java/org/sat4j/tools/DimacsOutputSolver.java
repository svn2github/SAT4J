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

import java.io.ObjectInputStream;
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
 * Solver used to display in a writer the CNF instance in Dimacs format.
 * 
 * That solver is useful to produce CNF files to be used by third party solvers.
 * 
 * @author leberre
 * 
 */
public class DimacsOutputSolver implements ISolver {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	private transient PrintWriter out;

	private int nbvars;

	private int nbclauses;

	private boolean fixedNbClauses = false;

	private boolean firstConstr = true;

	public DimacsOutputSolver() {
		this(new PrintWriter(System.out, true));
	}

	public DimacsOutputSolver(PrintWriter pw) {
		out = pw;
	}

	private void readObject(ObjectInputStream stream) {
		out = new PrintWriter(System.out, true);
	}

	public int newVar() {
		return 0;
	}

	public int newVar(int howmany) {
		out.print("p cnf " + howmany);
		nbvars = howmany;
		return 0;
	}

	public void setExpectedNumberOfClauses(int nb) {
		out.println(" " + nb);
		nbclauses = nb;
		fixedNbClauses = true;
	}

	public IConstr addClause(IVecInt literals) throws ContradictionException {
		if (firstConstr) {
			if (!fixedNbClauses) {
				out.println(" XXXXXX");
			}
			firstConstr = false;
		}
		for (IteratorInt iterator = literals.iterator(); iterator.hasNext();)
			out.print(iterator.next() + " ");
		out.println("0");
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
			if (!fixedNbClauses) {
				out.println("XXXXXX");
			}
			firstConstr = false;
		}
		for (int i = 0; i <= literals.size(); i++) {
			for (int j = i + 1; j < literals.size(); j++) {
				out.println("" + (-literals.get(i)) + " " + (-literals.get(j))
						+ " 0");
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
		return nbvars;
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
		throw new UnsupportedOperationException();
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
