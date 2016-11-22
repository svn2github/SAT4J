package org.sat4j.csp;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Map;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.Constr;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;
import org.sat4j.specs.UnitClauseProvider;

public class CspSatSolverDecorator implements ICspPBSatSolver {

	private static final long serialVersionUID = 1L;
	
	private IPBSolver solver;
	
	private boolean shouldDisplayEncoding = false;

	public CspSatSolverDecorator(IPBSolver solver) {
		this.solver = solver;
	}

	public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs, boolean moreThan, BigInteger d)
			throws ContradictionException {
		return solver.addPseudoBoolean(lits, coeffs, moreThan, d);
	}

	public IConstr addAtMost(IVecInt literals, IVecInt coeffs, int degree) throws ContradictionException {
		return solver.addAtMost(literals, coeffs, degree);
	}

	public IConstr addAtMost(IVecInt literals, IVec<BigInteger> coeffs, BigInteger degree)
			throws ContradictionException {
		return solver.addAtMost(literals, coeffs, degree);
	}

	public IConstr addAtLeast(IVecInt literals, IVecInt coeffs, int degree) throws ContradictionException {
		return solver.addAtLeast(literals, coeffs, degree);
	}

	public IConstr addAtLeast(IVecInt literals, IVec<BigInteger> coeffs, BigInteger degree)
			throws ContradictionException {
		return solver.addAtLeast(literals, coeffs, degree);
	}

	public IConstr addExactly(IVecInt literals, IVecInt coeffs, int weight) throws ContradictionException {
		return solver.addExactly(literals, coeffs, weight);
	}

	public IConstr addExactly(IVecInt literals, IVec<BigInteger> coeffs, BigInteger weight)
			throws ContradictionException {
		return solver.addExactly(literals, coeffs, weight);
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		solver.setObjectiveFunction(obj);
	}

	public ObjectiveFunction getObjectiveFunction() {
		return solver.getObjectiveFunction();
	}

	public boolean model(int var) {
		return solver.model(var);
	}

	public int[] model() {
		return solver.model();
	}

	@SuppressWarnings("deprecation")
	public int newVar() {
		return solver.newVar();
	}

	public int[] primeImplicant() {
		return solver.primeImplicant();
	}

	public int nextFreeVarId(boolean reserve) {
		return solver.nextFreeVarId(reserve);
	}

	public boolean primeImplicant(int p) {
		return solver.primeImplicant(p);
	}

	public boolean isSatisfiable() throws TimeoutException {
		return solver.isSatisfiable();
	}

	public boolean isSatisfiable(IVecInt assumps, boolean globalTimeout) throws TimeoutException {
		return solver.isSatisfiable(assumps, globalTimeout);
	}

	public void registerLiteral(int p) {
		solver.registerLiteral(p);
	}

	public boolean isSatisfiable(boolean globalTimeout) throws TimeoutException {
		return solver.isSatisfiable(globalTimeout);
	}

	public void setExpectedNumberOfClauses(int nb) {
		solver.setExpectedNumberOfClauses(nb);
	}

	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
		return solver.isSatisfiable(assumps);
	}

	public IConstr addClause(IVecInt literals) throws ContradictionException {
		return solver.addClause(literals);
	}

	public int[] findModel() throws TimeoutException {
		return solver.findModel();
	}

	public IConstr addBlockingClause(IVecInt literals) throws ContradictionException {
		return solver.addBlockingClause(literals);
	}

	public int[] findModel(IVecInt assumps) throws TimeoutException {
		return solver.findModel(assumps);
	}

	public IConstr discardCurrentModel() throws ContradictionException {
		return solver.discardCurrentModel();
	}

	public IVecInt createBlockingClauseForCurrentModel() {
		return solver.createBlockingClauseForCurrentModel();
	}

	public boolean removeConstr(IConstr c) {
		return solver.removeConstr(c);
	}

	public int nConstraints() {
		return solver.nConstraints();
	}

	public int newVar(int howmany) {
		return solver.newVar(howmany);
	}

	public boolean removeSubsumedConstr(IConstr c) {
		return solver.removeSubsumedConstr(c);
	}

	public int nVars() {
		return solver.nVars();
	}

	@SuppressWarnings("deprecation")
	public void printInfos(PrintWriter out, String prefix) {
		solver.printInfos(out, prefix);
	}

	public void addAllClauses(IVec<IVecInt> clauses) throws ContradictionException {
		solver.addAllClauses(clauses);
	}

	public void printInfos(PrintWriter out) {
		solver.printInfos(out);
	}

	public IConstr addAtMost(IVecInt literals, int degree) throws ContradictionException {
		return solver.addAtMost(literals, degree);
	}

	public IConstr addAtLeast(IVecInt literals, int degree) throws ContradictionException {
		return solver.addAtLeast(literals, degree);
	}

	public IConstr addExactly(IVecInt literals, int n) throws ContradictionException {
		return solver.addExactly(literals, n);
	}

	public IConstr addParity(IVecInt literals, boolean even) {
		return solver.addParity(literals, even);
	}

	public IConstr addConstr(Constr constr) {
		return solver.addConstr(constr);
	}

	public void setTimeout(int t) {
		solver.setTimeout(t);
	}

	public void setTimeoutOnConflicts(int count) {
		solver.setTimeoutOnConflicts(count);
	}

	public void setTimeoutMs(long t) {
		solver.setTimeoutMs(t);
	}

	public int getTimeout() {
		return solver.getTimeout();
	}

	public long getTimeoutMs() {
		return solver.getTimeoutMs();
	}

	public void expireTimeout() {
		solver.expireTimeout();
	}

	public void reset() {
		solver.reset();
	}

	@SuppressWarnings("deprecation")
	public void printStat(PrintStream out, String prefix) {
		solver.printStat(out, prefix);
	}

	@SuppressWarnings("deprecation")
	public void printStat(PrintWriter out, String prefix) {
		solver.printStat(out, prefix);
	}

	public void printStat(PrintWriter out) {
		solver.printStat(out);
	}

	public Map<String, Number> getStat() {
		return solver.getStat();
	}

	public String toString(String prefix) {
		return solver.toString(prefix);
	}

	public void clearLearntClauses() {
		solver.clearLearntClauses();
	}

	public void setDBSimplificationAllowed(boolean status) {
		solver.setDBSimplificationAllowed(status);
	}

	public boolean isDBSimplificationAllowed() {
		return solver.isDBSimplificationAllowed();
	}

	public <S extends ISolverService> void setSearchListener(SearchListener<S> sl) {
		solver.setSearchListener(sl);
	}

	public void setUnitClauseProvider(UnitClauseProvider ucp) {
		solver.setUnitClauseProvider(ucp);
	}

	public <S extends ISolverService> SearchListener<S> getSearchListener() {
		return solver.getSearchListener();
	}

	public boolean isVerbose() {
		return solver.isVerbose();
	}

	public void setVerbose(boolean value) {
		solver.setVerbose(value);
	}

	public void setLogPrefix(String prefix) {
		solver.setLogPrefix(prefix);
	}

	public String getLogPrefix() {
		return solver.getLogPrefix();
	}

	public IVecInt unsatExplanation() {
		return solver.unsatExplanation();
	}

	public int[] modelWithInternalVariables() {
		return solver.modelWithInternalVariables();
	}

	public int realNumberOfVariables() {
		return solver.realNumberOfVariables();
	}

	public boolean isSolverKeptHot() {
		return solver.isSolverKeptHot();
	}

	public void setKeepSolverHot(boolean keepHot) {
		solver.setKeepSolverHot(keepHot);
	}

	public ISolver getSolvingEngine() {
		return solver.getSolvingEngine();
	}

	@Override
	public boolean shouldOnlyDisplayEncoding() {
		return this.shouldDisplayEncoding;
	}

	@Override
	public void setShouldOnlyDisplayEncoding(boolean b) {
		this.shouldDisplayEncoding = b;
	}

}
