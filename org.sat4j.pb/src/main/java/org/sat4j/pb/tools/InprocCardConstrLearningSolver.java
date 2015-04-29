package org.sat4j.pb.tools;

import java.math.BigInteger;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.Pair;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.IPBSolverService;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.constraints.pb.PBConstr;
import org.sat4j.pb.core.PBDataStructureFactory;
import org.sat4j.pb.core.PBSolverCP;
import org.sat4j.specs.Constr;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SearchListenerAdapter;

public class InprocCardConstrLearningSolver extends PBSolverCP {

    private static final long serialVersionUID = 1L;

    private final IPBSolver coSolver;
    private final CardConstrFinder cardFinder;

    private Constr extendedConstr;

    public InprocCardConstrLearningSolver(
            LearningStrategy<PBDataStructureFactory> learner,
            PBDataStructureFactory dsf, IOrder order, boolean noRemove) {
        super(learner, dsf, order, noRemove);
        this.coSolver = SolverFactory.newResolution();
        this.cardFinder = new CardConstrFinder(this.coSolver);
        configureSolver();
    }

    public InprocCardConstrLearningSolver(
            LearningStrategy<PBDataStructureFactory> learner,
            PBDataStructureFactory dsf, IOrder order) {
        super(learner, dsf, order);
        this.coSolver = SolverFactory.newResolution();
        this.cardFinder = new CardConstrFinder(this.coSolver);
        configureSolver();
    }

    public InprocCardConstrLearningSolver(
            LearningStrategy<PBDataStructureFactory> learner,
            PBDataStructureFactory dsf, SearchParams params, IOrder order,
            boolean noRemove) {
        super(learner, dsf, params, order, noRemove);
        this.coSolver = SolverFactory.newResolution();
        this.cardFinder = new CardConstrFinder(this.coSolver);
        configureSolver();
    }

    public InprocCardConstrLearningSolver(
            LearningStrategy<PBDataStructureFactory> learner,
            PBDataStructureFactory dsf, SearchParams params, IOrder order,
            RestartStrategy restarter, boolean noRemove) {
        super(learner, dsf, params, order, restarter, noRemove);
        this.coSolver = SolverFactory.newResolution();
        this.cardFinder = new CardConstrFinder(this.coSolver);
        configureSolver();
    }

    public InprocCardConstrLearningSolver(
            LearningStrategy<PBDataStructureFactory> learner,
            PBDataStructureFactory dsf, SearchParams params, IOrder order,
            RestartStrategy restarter) {
        super(learner, dsf, params, order, restarter);
        this.coSolver = SolverFactory.newResolution();
        this.cardFinder = new CardConstrFinder(this.coSolver);
        configureSolver();
    }

    public InprocCardConstrLearningSolver(
            LearningStrategy<PBDataStructureFactory> learner,
            PBDataStructureFactory dsf, SearchParams params, IOrder order) {
        super(learner, dsf, params, order);
        this.coSolver = SolverFactory.newResolution();
        this.cardFinder = new CardConstrFinder(this.coSolver);
        configureSolver();
    }

    private void configureSolver() {
        this.setSearchListener(new SearchListenerAdapter<IPBSolverService>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
                handleConflict(confl);
            }
        });
    }

    protected void handleConflict(IConstr confl) {
        this.extendedConstr = null;
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
            System.out.println(getLogPrefix() + "noCardFrom: "
                    + confl.toString());
        } else {
            System.out.println(getLogPrefix() + "newCard: " + discovered
                    + " <= " + (atMostLits.size() - 1) + " from: "
                    + confl.toString());
            // IConstr constr = this.addAtMost(discovered,
            // atMostLits.size() - 1);
            IConstr constr = this.addAtMostOnTheFly(discovered, new VecInt(
                    discovered.size(), 1), atMostLits.size() - 1);
            this.extendedConstr = (Constr) constr;
        }
    }

    @Override
    public void analyzeCP(Constr myconfl, Pair results) throws TimeoutException {
        // TODO Auto-generated method stub
        super.analyzeCP(myconfl, results);
    }

    // Overriding constaint adding methods to store constraints in both solver
    // and coSolver

    @Override
    public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
            boolean moreThan, BigInteger d) throws ContradictionException {
        this.coSolver.addPseudoBoolean(lits, coeffs, moreThan, d);
        return super.addPseudoBoolean(lits, coeffs, moreThan, d);
    }

    @Override
    public IConstr addAtMost(IVecInt literals, IVecInt coeffs, int degree)
            throws ContradictionException {
        this.coSolver.addAtMost(literals, coeffs, degree);
        return super.addAtMost(literals, coeffs, degree);
    }

    @Override
    public IConstr addAtMost(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger degree) throws ContradictionException {
        this.coSolver.addAtMost(literals, coeffs, degree);
        return super.addAtMost(literals, coeffs, degree);
    }

    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {
        this.coSolver.addClause(literals);
        return super.addClause(literals);
    }

    @Override
    public IConstr addAtLeast(IVecInt literals, IVecInt coeffs, int degree)
            throws ContradictionException {
        this.coSolver.addAtLeast(literals, coeffs, degree);
        return super.addAtLeast(literals, coeffs, degree);
    }

    @Override
    public IConstr addAtLeast(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger degree) throws ContradictionException {
        this.coSolver.addAtLeast(literals, coeffs, degree);
        return super.addAtLeast(literals, coeffs, degree);
    }

    @Override
    public IConstr addExactly(IVecInt literals, IVecInt coeffs, int weight)
            throws ContradictionException {
        this.coSolver.addExactly(literals, coeffs, weight);
        return super.addExactly(literals, coeffs, weight);
    }

    @Override
    public void addAllClauses(IVec<IVecInt> clauses)
            throws ContradictionException {
        this.coSolver.addAllClauses(clauses);
        super.addAllClauses(clauses);
    }

    @Override
    public IConstr addExactly(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger weight) throws ContradictionException {
        this.coSolver.addExactly(literals, coeffs, weight);
        return super.addExactly(literals, coeffs, weight);
    }

    @Override
    public IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        this.coSolver.addAtMost(literals, degree);
        return super.addAtMost(literals, degree);
    }

    @Override
    public IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        this.coSolver.addAtLeast(literals, degree);
        return super.addAtLeast(literals, degree);
    }

    @Override
    public IConstr addExactly(IVecInt literals, int n)
            throws ContradictionException {
        this.coSolver.addExactly(literals, n);
        return super.addExactly(literals, n);
    }

    @Override
    public IConstr addConstr(Constr constr) {
        this.coSolver.addConstr(constr);
        return super.addConstr(constr);
    }

}
