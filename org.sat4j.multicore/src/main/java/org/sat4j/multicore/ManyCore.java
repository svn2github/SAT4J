package org.sat4j.multicore;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

import org.sat4j.core.ASolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class ManyCore implements ISolver, OutcomeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String[] availableSolvers; // = { };

	private ISolver[] solvers;
	private int numberOfSolvers;
	private int winnerId;
	private boolean needToWait;
	private boolean resultFound;
	private int remainingSolvers;

	public ManyCore(ASolverFactory<? extends ISolver> factory, String ... solverNames) {
		availableSolvers = solverNames;
		Runtime runtime = Runtime.getRuntime();
		long memory = runtime.maxMemory();
		int numberOfCores = runtime.availableProcessors();
		if (memory > 1000000000L) {
			numberOfSolvers = numberOfCores;
		} else {
			numberOfSolvers = 1;
		}
		if (solverNames.length<numberOfSolvers) {
			throw new IllegalArgumentException("I need more solver names to run ManyCore on such computer!");
		}
		solvers = new ISolver[numberOfSolvers];
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i] = factory.createSolverByName(
					availableSolvers[i]);
		}
	}

	public void addAllClauses(IVec<IVecInt> clauses)
			throws ContradictionException {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].addAllClauses(clauses);
		}
	}

	public IConstr addAtLeast(IVecInt literals, int degree)
			throws ContradictionException {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].addAtLeast(literals, degree);
		}
		return null;
	}

	public IConstr addAtMost(IVecInt literals, int degree)
			throws ContradictionException {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].addAtMost(literals, degree);
		}
		return null;
	}

	public IConstr addClause(IVecInt literals) throws ContradictionException {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].addClause(literals);
		}
		return null;
	}

	public void clearLearntClauses() {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].clearLearntClauses();
		}
	}

	public void expireTimeout() {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].expireTimeout();
		}
	}

	public Map<String, Number> getStat() {
		return solvers[winnerId].getStat();
	}

	public int getTimeout() {
		return solvers[0].getTimeout();
	}

	public int newVar() {
		throw new UnsupportedOperationException();
	}

	public int newVar(int howmany) {
		int result = 0;
		for (int i = 0; i < numberOfSolvers; i++) {
			result = solvers[i].newVar(howmany);
		}
		return result;
	}

	@Deprecated
	public void printStat(PrintStream out, String prefix) {
		solvers[winnerId].printStat(out, prefix);
	}

	public void printStat(PrintWriter out, String prefix) {
		solvers[winnerId].printStat(out, prefix);
	}

	public boolean removeConstr(IConstr c) {
		throw new UnsupportedOperationException();
	}

	public void reset() {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].reset();
		}
	}

	public void setExpectedNumberOfClauses(int nb) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].setExpectedNumberOfClauses(nb);
		}
	}

	public void setTimeout(int t) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].setTimeout(t);
		}
	}

	public void setTimeoutMs(long t) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].setTimeoutMs(t);
		}
	}

	public void setTimeoutOnConflicts(int count) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].setTimeoutOnConflicts(count);
		}
	}

	public String toString(String prefix) {
		StringBuffer res = new StringBuffer();
		res.append("ManyCore solver with ");
		res.append(numberOfSolvers);
        res.append(" solvers running in parallel");
        res.append("\n");
        for (int i=0;i<numberOfSolvers;i++) {
        	res.append(solvers[i].toString(prefix));
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
		remainingSolvers = numberOfSolvers;
		needToWait = true;
		for (int i = 0; i < numberOfSolvers; i++) {
			new Thread(new RunnableSolver(i, solvers[i], this)).start();
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

	public boolean isSatisfiable(IVecInt assumps, boolean globalTimeout)
			throws TimeoutException {
		throw new UnsupportedOperationException();
	}

	public boolean isSatisfiable(boolean globalTimeout) throws TimeoutException {
		throw new UnsupportedOperationException();
	}

	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
		throw new UnsupportedOperationException();
	}

	public int[] model() {
		return solvers[winnerId].model();
	}

	public boolean model(int var) {
		return solvers[winnerId].model(var);
	}

	public int nConstraints() {
		return solvers[0].nConstraints();
	}

	public int nVars() {
		return solvers[0].nVars();
	}

	public void printInfos(PrintWriter out, String prefix) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].printInfos(out, prefix);
		}
	}

	public synchronized void onFinishWithAnswer(boolean finished,
			boolean result, int index) {
		if (finished) {
			winnerId = index;
			resultFound = result;
			for (int i = 0; i < numberOfSolvers; i++) {
				if (i != winnerId)
					solvers[i].expireTimeout();
			}
			System.out.println("c And the winner is "+availableSolvers[winnerId]);
			needToWait = false;
		} else {
			remainingSolvers--;
		}
	}

	public boolean isDBSimplificationAllowed() {
		return solvers[0].isDBSimplificationAllowed();
	}

	public void setDBSimplificationAllowed(boolean status) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers[i].setDBSimplificationAllowed(status);
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

	public RunnableSolver(int i, ISolver solver, OutcomeListener ol) {
		index = i;
		this.solver = solver;
		this.ol = ol;
	}

	public void run() {
		try {
			boolean result = solver.isSatisfiable();
			ol.onFinishWithAnswer(true, result, index);
		} catch (TimeoutException e) {
			ol.onFinishWithAnswer(false, false, index);
		}
	}

}
