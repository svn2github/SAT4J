package org.sat4j.multicore;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sat4j.core.ASolverFactory;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ConstrGroup;

public class ManyCore<S extends ISolver> implements ISolver, OutcomeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String[] availableSolvers; // = { };

	protected final List<S> solvers;
	protected final int numberOfSolvers;
	private int winnerId;
	private boolean needToWait;
	private boolean resultFound;
	private int remainingSolvers;

	public ManyCore(ASolverFactory<S> factory, String... solverNames) {
		availableSolvers = solverNames;
		Runtime runtime = Runtime.getRuntime();
		long memory = runtime.maxMemory();
		int numberOfCores = runtime.availableProcessors();
		if (memory > 500000000L) {
			numberOfSolvers = solverNames.length < numberOfCores ? solverNames.length
					: numberOfCores;
		} else {
			numberOfSolvers = 1;
		}
		solvers = new ArrayList<S>(numberOfSolvers);
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.add(factory.createSolverByName(availableSolvers[i]));
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
		ConstrGroup group = new ConstrGroup();
		for (int i = 0; i < numberOfSolvers; i++) {
			group.add(solvers.get(i).addAtLeast(literals, degree));
		}
		return group;
	}

	public IConstr addAtMost(IVecInt literals, int degree)
			throws ContradictionException {
		ConstrGroup group = new ConstrGroup();
		for (int i = 0; i < numberOfSolvers; i++) {
			group.add(solvers.get(i).addAtMost(literals, degree));
		}
		return group;
	}

	public IConstr addClause(IVecInt literals) throws ContradictionException {
		ConstrGroup group = new ConstrGroup();
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
		ConstrGroup group = (ConstrGroup) c;
		boolean removed = true;
		for (int i = 0; i < numberOfSolvers; i++) {
			removed = removed & solvers.get(i).removeConstr(group.getConstr(i));
		}
		return removed;
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
			res.append("\n");
		}
		return res.toString();
	}

	public int[] findModel() throws TimeoutException {
		throw new UnsupportedOperationException();
	}

	public int[] findModel(IVecInt assumps) throws TimeoutException {
		throw new UnsupportedOperationException();
	}

	public boolean isSatisfiable() throws TimeoutException {
		return isSatisfiable(VecInt.EMPTY, false);
	}

	public boolean isSatisfiable(IVecInt assumps, boolean globalTimeout)
			throws TimeoutException {
		remainingSolvers = numberOfSolvers;
		needToWait = true;
		for (int i = 0; i < numberOfSolvers; i++) {
			new Thread(new RunnableSolver(i, solvers.get(i), assumps,
					globalTimeout, this)).start();
		}
		try {
			do {
				Thread.sleep(1000);
			} while (needToWait && remainingSolvers > 0);
		} catch (InterruptedException e) {
			// TODO: handle exception
		}
		if (remainingSolvers == 0) {
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
		if (finished) {
			winnerId = index;
			resultFound = result;
			for (int i = 0; i < numberOfSolvers; i++) {
				if (i != winnerId)
					solvers.get(i).expireTimeout();
			}
			System.out.println("c And the winner is "
					+ availableSolvers[winnerId]);
			needToWait = false;
		} else {
			remainingSolvers--;
		}
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
		ConstrGroup group = new ConstrGroup();
		for (int i = 0; i < numberOfSolvers; i++) {
			group.add(solvers.get(i).addBlockingClause(literals));
		}
		return group;
	}

	public boolean removeSubsumedConstr(IConstr c) {
		ConstrGroup group = (ConstrGroup) c;
		boolean removed = true;
		for (int i = 0; i < numberOfSolvers; i++) {
			removed = removed
					& solvers.get(i).removeSubsumedConstr(group.getConstr(i));
		}
		return removed;
	}

	public boolean isVerbose() {
		return false;
	}

	public void setVerbose(boolean value) {
		// do nothing
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
		return solvers.get(0).unsatExplanation();
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
		} catch (TimeoutException e) {
			ol.onFinishWithAnswer(false, false, index);
		}
	}

}
