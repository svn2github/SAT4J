package org.sat4j.pb.tools;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.IPBSolverService;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.constraints.pb.PBConstr;
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
import org.sat4j.tools.SearchListenerAdapter;

public class InprocCardConstrLearningSolver implements IPBSolver {

    private static final long serialVersionUID = 1L;

    private final IPBSolver solver, coSolver;
    private final CardConstrFinder cardFinder;

    public InprocCardConstrLearningSolver(IPBSolver solver) {
        this.solver = solver;
        this.coSolver = SolverFactory.newResolution();
        this.cardFinder = new CardConstrFinder(this.coSolver);
        configureSolver();
    }

    private void configureSolver() {
        this.solver
                .setSearchListener(new SearchListenerAdapter<IPBSolverService>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void conflictFound(IConstr confl, int dlevel,
                            int trailLevel) {
                        handleConflict(confl);
                    }
                });
    }

    protected void handleConflict(IConstr confl) {
        if (confl instanceof PBConstr && !confl.canBePropagatedMultipleTimes()) {
            handleCardConflict((PBConstr) confl);
        }
    }

    private void handleCardConflict(PBConstr confl) {
        // translation from Minisat literals to Dimacs literals
        IVecInt atMostLits = new VecInt(confl.getLits().length);
        for (int lit : confl.getLits()) {
            atMostLits.push((lit >> 1) * ((lit & 1) == 1 ? -1 : 1));
        }
        IVecInt discovered = this.cardFinder.searchCardFromAtMostCard(
                atMostLits, confl.getDegree().intValue());
        if (discovered == null) {
            System.out.println(this.solver.getLogPrefix() + "noCardFrom: "
                    + confl.toString());
        } else {
            System.out.println(this.solver.getLogPrefix() + "newCard: "
                    + discovered + " <= " + (atMostLits.size() - 1) + " from: "
                    + confl.toString());
        }
    }

    // solver delegation (implementation of IPBSolver methods)

    public boolean model(int var) {
        return solver.model(var);
    }

    public int[] model() {
        return solver.model();
    }

    @Deprecated
    public int newVar() {
        return solver.newVar();
    }

    public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
            boolean moreThan, BigInteger d) throws ContradictionException {
        this.coSolver.addPseudoBoolean(lits, coeffs, moreThan, d);
        return solver.addPseudoBoolean(lits, coeffs, moreThan, d);
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

    public IConstr addAtMost(IVecInt literals, IVecInt coeffs, int degree)
            throws ContradictionException {
        this.coSolver.addAtMost(literals, coeffs, degree);
        return solver.addAtMost(literals, coeffs, degree);
    }

    public boolean isSatisfiable(IVecInt assumps, boolean globalTimeout)
            throws TimeoutException {
        return solver.isSatisfiable(assumps, globalTimeout);
    }

    public void registerLiteral(int p) {
        solver.registerLiteral(p);
    }

    public boolean isSatisfiable(boolean globalTimeout) throws TimeoutException {
        return solver.isSatisfiable(globalTimeout);
    }

    public IConstr addAtMost(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger degree) throws ContradictionException {
        this.coSolver.addAtMost(literals, coeffs, degree);
        return solver.addAtMost(literals, coeffs, degree);
    }

    public void setExpectedNumberOfClauses(int nb) {
        solver.setExpectedNumberOfClauses(nb);
    }

    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        return solver.isSatisfiable(assumps);
    }

    public IConstr addClause(IVecInt literals) throws ContradictionException {
        this.coSolver.addClause(literals);
        return solver.addClause(literals);
    }

    public int[] findModel() throws TimeoutException {
        return solver.findModel();
    }

    public IConstr addAtLeast(IVecInt literals, IVecInt coeffs, int degree)
            throws ContradictionException {
        this.coSolver.addAtLeast(literals, coeffs, degree);
        return solver.addAtLeast(literals, coeffs, degree);
    }

    public IConstr addBlockingClause(IVecInt literals)
            throws ContradictionException {
        return solver.addBlockingClause(literals);
    }

    public int[] findModel(IVecInt assumps) throws TimeoutException {
        return solver.findModel(assumps);
    }

    public IConstr discardCurrentModel() throws ContradictionException {
        return solver.discardCurrentModel();
    }

    public IConstr addAtLeast(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger degree) throws ContradictionException {
        this.coSolver.addAtLeast(literals, coeffs, degree);
        return solver.addAtLeast(literals, coeffs, degree);
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

    public IConstr addExactly(IVecInt literals, IVecInt coeffs, int weight)
            throws ContradictionException {
        this.coSolver.addExactly(literals, coeffs, weight);
        return solver.addExactly(literals, coeffs, weight);
    }

    public int nVars() {
        return solver.nVars();
    }

    @Deprecated
    public void printInfos(PrintWriter out, String prefix) {
        solver.printInfos(out, prefix);
    }

    public void addAllClauses(IVec<IVecInt> clauses)
            throws ContradictionException {
        this.coSolver.addAllClauses(clauses);
        solver.addAllClauses(clauses);
    }

    public IConstr addExactly(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger weight) throws ContradictionException {
        this.coSolver.addExactly(literals, coeffs, weight);
        return solver.addExactly(literals, coeffs, weight);
    }

    public void printInfos(PrintWriter out) {
        solver.printInfos(out);
    }

    public IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        this.coSolver.addAtMost(literals, degree);
        return solver.addAtMost(literals, degree);
    }

    public void setObjectiveFunction(ObjectiveFunction obj) {
        solver.setObjectiveFunction(obj);
    }

    public ObjectiveFunction getObjectiveFunction() {
        return solver.getObjectiveFunction();
    }

    public IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        this.coSolver.addAtLeast(literals, degree);
        return solver.addAtLeast(literals, degree);
    }

    public IConstr addExactly(IVecInt literals, int n)
            throws ContradictionException {
        this.coSolver.addExactly(literals, n);
        return solver.addExactly(literals, n);
    }

    public IConstr addConstr(Constr constr) {
        this.coSolver.addConstr(constr);
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

    @Deprecated
    public void printStat(PrintStream out, String prefix) {
        solver.printStat(out, prefix);
    }

    @Deprecated
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

    public <S extends ISolverService> void setSearchListener(
            SearchListener<S> sl) {
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

}
