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
package org.sat4j.tools;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sat4j.core.ASolverFactory;
import org.sat4j.core.ConstrGroup;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;

public class ManyCore<S extends ISolver> implements ISolver, OutcomeListener {

	private static final long MINIMAL_MEMORY_REQUIREMENT = 500000000L;

	private static final int NORMAL_SLEEP = 500;

	private static final int FAST_SLEEP = 50;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String[] availableSolvers; // = { };

	protected final List<S> solvers;
	protected final int numberOfSolvers;
	private int winnerId;
	private boolean resultFound;
	private volatile int remainingSolvers;
	private volatile int sleepTime;
	private volatile boolean solved;

	public ManyCore(ASolverFactory<S> factory, String... solverNames) {
		availableSolvers = solverNames;
		numberOfSolvers = computeNumberOfSolversInParallel(solverNames.length);
		solvers = new ArrayList<S>(numberOfSolvers);
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.add(factory.createSolverByName(availableSolvers[i]));
		}
	}

	private int computeNumberOfSolversInParallel(int upperBound) {
		Runtime runtime = Runtime.getRuntime();
		long memory = runtime.maxMemory();
		int numberOfCores = runtime.availableProcessors();
		if (memory > MINIMAL_MEMORY_REQUIREMENT) {
			return upperBound < numberOfCores ? upperBound : numberOfCores;
		} else {
			return 1;
		}
	}

	/**
	 * Create a parallel solver from a list of solvers and a list of names.
	 * 
	 * @param names
	 *            a String to describe each solver in the messages.
	 * @param solverObjects
	 *            the solvers
	 */
	public ManyCore(String[] names, S... solverObjects) {
		this(solverObjects);
		for (int i = 0; i < names.length; i++) {
			availableSolvers[i] = names[i];
		}
	}

	public ManyCore(S... solverObjects) {
		availableSolvers = new String[solverObjects.length];
		for (int i = 0; i < solverObjects.length; i++) {
			availableSolvers[i] = "solver" + i;
		}
		numberOfSolvers = computeNumberOfSolversInParallel(solverObjects.length);
		solvers = new ArrayList<S>(numberOfSolvers);
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.add(solverObjects[i]);
		}
	}

	public void addAllClauses(IVec<IVecInt> clauses)
			throws ContradictionException {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).addAllClauses(clauses);
		}
	}

	public IConstr addAtLeast(IVecInt literals, int degree)
			throws ContradictionException {
		ConstrGroup group = new ConstrGroup(false);
		for (int i = 0; i < numberOfSolvers; i++) {
			group.add(solvers.get(i).addAtLeast(literals, degree));
		}
		return group;
	}

	public IConstr addAtMost(IVecInt literals, int degree)
			throws ContradictionException {
		ConstrGroup group = new ConstrGroup(false);
		for (int i = 0; i < numberOfSolvers; i++) {
			group.add(solvers.get(i).addAtMost(literals, degree));
		}
		return group;
	}

	public IConstr addExactly(IVecInt literals, int n)
			throws ContradictionException {
		ConstrGroup group = new ConstrGroup(false);
		for (int i = 0; i < numberOfSolvers; i++) {
			group.add(solvers.get(i).addExactly(literals, n));
		}
		return group;
	}

	public IConstr addClause(IVecInt literals) throws ContradictionException {
		ConstrGroup group = new ConstrGroup(false);
		for (int i = 0; i < numberOfSolvers; i++) {
			group.add(solvers.get(i).addClause(literals));
		}
		return group;
	}

	public void clearLearntClauses() {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).clearLearntClauses();
		}
	}

	public void expireTimeout() {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).expireTimeout();
		}
		sleepTime = FAST_SLEEP;
	}

	public Map<String, Number> getStat() {
		return solvers.get(winnerId).getStat();
	}

	public int getTimeout() {
		return solvers.get(0).getTimeout();
	}

	public long getTimeoutMs() {
		return solvers.get(0).getTimeoutMs();
	}

	public int newVar() {
		throw new UnsupportedOperationException();
	}

	public int newVar(int howmany) {
		int result = 0;
		for (int i = 0; i < numberOfSolvers; i++) {
			result = solvers.get(i).newVar(howmany);
		}
		return result;
	}

	@Deprecated
	public void printStat(PrintStream out, String prefix) {
		solvers.get(winnerId).printStat(out, prefix);
	}

	public void printStat(PrintWriter out, String prefix) {
		solvers.get(winnerId).printStat(out, prefix);
	}

	public boolean removeConstr(IConstr c) {
		if (c instanceof ConstrGroup) {
			ConstrGroup group = (ConstrGroup) c;
			boolean removed = true;
			IConstr toRemove;
			for (int i = 0; i < numberOfSolvers; i++) {
				toRemove = group.getConstr(i);
				if (toRemove != null) {
					removed = removed & solvers.get(i).removeConstr(toRemove);
				}
			}
			return removed;
		}
		throw new IllegalArgumentException(
				"Can only remove a group of constraints!");
	}

	public void reset() {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).reset();
		}
	}

	public void setExpectedNumberOfClauses(int nb) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).setExpectedNumberOfClauses(nb);
		}
	}

	public void setTimeout(int t) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).setTimeout(t);
		}
	}

	public void setTimeoutMs(long t) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).setTimeoutMs(t);
		}
	}

	public void setTimeoutOnConflicts(int count) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).setTimeoutOnConflicts(count);
		}
	}

	public String toString(String prefix) {
		StringBuffer res = new StringBuffer();
		res.append(prefix);
		res.append("ManyCore solver with ");
		res.append(numberOfSolvers);
		res.append(" solvers running in parallel");
		res.append("\n");
		for (int i = 0; i < numberOfSolvers; i++) {
			res.append(solvers.get(i).toString(prefix));
			if (i < numberOfSolvers - 1) {
				res.append("\n");
			}
		}
		return res.toString();
	}

	public int[] findModel() throws TimeoutException {
		if (isSatisfiable()) {
			return model();
		}
		// A zero length array would mean that the formula is a tautology.
		return null;
	}

	public int[] findModel(IVecInt assumps) throws TimeoutException {
		if (isSatisfiable(assumps)) {
			return model();
		}
		// A zero length array would mean that the formula is a tautology.
		return null;
	}

	public boolean isSatisfiable() throws TimeoutException {
		return isSatisfiable(VecInt.EMPTY, false);
	}

	public boolean isSatisfiable(IVecInt assumps, boolean globalTimeout)
			throws TimeoutException {
		remainingSolvers = numberOfSolvers;
		solved = false;
		for (int i = 0; i < numberOfSolvers; i++) {
			new Thread(new RunnableSolver(i, solvers.get(i), assumps,
					globalTimeout, this)).start();
		}
		try {
			sleepTime = NORMAL_SLEEP;
			do {
				Thread.sleep(sleepTime);
			} while (remainingSolvers > 0);
		} catch (InterruptedException e) {
			// TODO: handle exception
		}
		if (!solved) {
			assert remainingSolvers == 0;
			throw new TimeoutException();
		}
		return resultFound;
	}

	public boolean isSatisfiable(boolean globalTimeout) throws TimeoutException {
		throw new UnsupportedOperationException();
	}

	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
		throw new UnsupportedOperationException();
	}

	public int[] model() {
		return solvers.get(winnerId).model();
	}

	public boolean model(int var) {
		return solvers.get(winnerId).model(var);
	}

	public int nConstraints() {
		return solvers.get(0).nConstraints();
	}

	public int nVars() {
		return solvers.get(0).nVars();
	}

	public void printInfos(PrintWriter out, String prefix) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).printInfos(out, prefix);
		}
	}

	public synchronized void onFinishWithAnswer(boolean finished,
			boolean result, int index) {
		if (finished && !solved) {
			winnerId = index;
			solved = true;
			resultFound = result;
			for (int i = 0; i < numberOfSolvers; i++) {
				if (i != winnerId)
					solvers.get(i).expireTimeout();
			}
			sleepTime = FAST_SLEEP;
			System.out.println(getLogPrefix() + " And the winner is "
					+ availableSolvers[winnerId]);
		}
		remainingSolvers--;
	}

	public boolean isDBSimplificationAllowed() {
		return solvers.get(0).isDBSimplificationAllowed();
	}

	public void setDBSimplificationAllowed(boolean status) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(0).setDBSimplificationAllowed(status);
		}
	}

	public void setSearchListener(SearchListener sl) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).setSearchListener(sl);
		}
	}

	/**
	 * @since 2.2
	 */
	public SearchListener getSearchListener() {
		return solvers.get(0).getSearchListener();
	}

	public int nextFreeVarId(boolean reserve) {
		return solvers.get(0).nextFreeVarId(reserve);
	}

	public IConstr addBlockingClause(IVecInt literals)
			throws ContradictionException {
		ConstrGroup group = new ConstrGroup(false);
		for (int i = 0; i < numberOfSolvers; i++) {
			group.add(solvers.get(i).addBlockingClause(literals));
		}
		return group;
	}

	public boolean removeSubsumedConstr(IConstr c) {
		if (c instanceof ConstrGroup) {
			ConstrGroup group = (ConstrGroup) c;
			boolean removed = true;
			IConstr toRemove;
			for (int i = 0; i < numberOfSolvers; i++) {
				toRemove = group.getConstr(i);
				if (toRemove != null) {
					removed = removed
							& solvers.get(i).removeSubsumedConstr(toRemove);
				}
			}
			return removed;
		}
		throw new IllegalArgumentException(
				"Can only remove a group of constraints!");
	}

	public boolean isVerbose() {
		return solvers.get(0).isVerbose();
	}

	public void setVerbose(boolean value) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).setVerbose(value);
		}
	}

	public void setLogPrefix(String prefix) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).setLogPrefix(prefix);
		}

	}

	public String getLogPrefix() {
		return solvers.get(0).getLogPrefix();
	}

	public IVecInt unsatExplanation() {
		return solvers.get(winnerId).unsatExplanation();
	}

	public int[] primeImplicant() {
		return solvers.get(winnerId).primeImplicant();
	}

	public List<S> getSolvers() {
		return new ArrayList<S>(this.solvers);
	}

	public int[] modelWithInternalVariables() {
		return solvers.get(winnerId).modelWithInternalVariables();
	}

	public int realNumberOfVariables() {
		return solvers.get(0).realNumberOfVariables();
	}

	public void registerLiteral(int p) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).registerLiteral(p);
		}

	}
}

interface OutcomeListener {
	void onFinishWithAnswer(boolean finished, boolean result, int index);
}

class RunnableSolver implements Runnable {

	private final int index;
	private final ISolver solver;
	private final OutcomeListener ol;
	private final IVecInt assumps;
	private final boolean globalTimeout;

	public RunnableSolver(int i, ISolver solver, IVecInt assumps,
			boolean globalTimeout, OutcomeListener ol) {
		index = i;
		this.solver = solver;
		this.ol = ol;
		this.assumps = assumps;
		this.globalTimeout = globalTimeout;
	}

	public void run() {
		try {
			boolean result = solver.isSatisfiable(assumps, globalTimeout);
			ol.onFinishWithAnswer(true, result, index);
		} catch (Exception e) {
			ol.onFinishWithAnswer(false, false, index);
		}
	}

}
