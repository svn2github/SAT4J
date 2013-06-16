package org.sat4j.pb.multiobjective;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.PBSolverDecorator;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.RandomAccessModel;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolutionFoundListener;

public abstract class SortedMultiObjectiveValuesSolver extends
        PBSolverDecorator implements IMultiObjectivePBSolver,
        IOptimizationProblem, SearchListener<ISolverService> {

    private static final long serialVersionUID = 1L;

    protected IOptimizationProblem optSolver;

    private final List<ObjectiveFunction> objs = new ArrayList<ObjectiveFunction>();

    private final List<Integer> selectorVariables = new ArrayList<Integer>();

    private final List<List<Integer>> sortingValuesVariables = new ArrayList<List<Integer>>();

    private BigInteger maxObjValues = BigInteger.valueOf(Long.MIN_VALUE);

    private SolutionFoundListener sfl;

    private boolean decoratedObjFuncIsSet = false;

    public SortedMultiObjectiveValuesSolver(IPBSolver solver) {
        super(solver);
        optSolver = new PseudoOptDecorator(decorated());
    }

    public void addObjectiveFunction(ObjectiveFunction obj) {
        this.objs.add(obj);
    }

    public List<ObjectiveFunction> getObjectiveFunctions() {
        return this.objs;
    }

    private void addSortingConstraints() {
        for (ObjectiveFunction obj : this.objs) {
            BigInteger weightsSum = BigInteger.ZERO;
            for (Iterator<BigInteger> it = obj.getCoeffs().iterator(); it
                    .hasNext();) {
                weightsSum = weightsSum.add(it.next().abs());
            }
            if (this.maxObjValues.compareTo(weightsSum) < 0) {
                this.maxObjValues = weightsSum;
            }
        }
        for (int i = 0; i < this.objs.size(); ++i) {
            try {
                addSortingConstraintsForRank(i);
            } catch (ContradictionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addSortingConstraintsForRank(int rank)
            throws ContradictionException {
        List<Integer> currentRankSortingValuesVariables = new ArrayList<Integer>();
        int nbVarsNeeded = Math.max(
                1,
                (int) Math.ceil(Math.log10(this.maxObjValues.longValue() + 1)
                        / Math.log10(2)));
        for (int i = 0; i < nbVarsNeeded; ++i) {
            currentRankSortingValuesVariables.add(decorated().nextFreeVarId(
                    true));
        }
        this.sortingValuesVariables.add(currentRankSortingValuesVariables);
        BigInteger bigValue = this.maxObjValues.add(BigInteger.ONE);
        for (ObjectiveFunction obj : this.objs) {
            // obj_i(x) - y_i + M.t <= M
            int selectorVar = decorated().nextFreeVarId(true);
            this.selectorVariables.add(selectorVar);
            IVecInt newCstrVars = new VecInt();
            IVec<BigInteger> newCstrCoeffs = new Vec<BigInteger>();
            IteratorInt varsIt = obj.getVars().iterator();
            Iterator<BigInteger> coefIt = obj.getCoeffs().iterator();
            while (varsIt.hasNext()) {
                int var = varsIt.next();
                BigInteger coef = coefIt.next();
                newCstrVars.push(var);
                newCstrCoeffs.push(coef);
            }
            BigInteger factor = BigInteger.valueOf(-1l);
            for (int i = 0; i < nbVarsNeeded; ++i) {
                newCstrVars.push(currentRankSortingValuesVariables.get(i));
                newCstrCoeffs.push(factor);
                factor = factor.multiply(BigInteger.valueOf(2));
            }
            newCstrVars.push(selectorVar);
            newCstrCoeffs.push(bigValue);
            decorated().addAtMost(newCstrVars, newCstrCoeffs, bigValue);
            // obj_i(x) - y_i + M.t > 0
            newCstrVars = new VecInt();
            newCstrCoeffs = new Vec<BigInteger>();
            varsIt = obj.getVars().iterator();
            coefIt = obj.getCoeffs().iterator();
            while (varsIt.hasNext()) {
                int var = varsIt.next();
                BigInteger coef = coefIt.next();
                newCstrVars.push(var);
                newCstrCoeffs.push(coef);
            }
            factor = BigInteger.valueOf(-1l);
            for (int i = 0; i < nbVarsNeeded; ++i) {
                newCstrVars.push(currentRankSortingValuesVariables.get(i));
                newCstrCoeffs.push(factor);
                factor = factor.multiply(BigInteger.valueOf(2));
            }
            newCstrVars.push(selectorVar);
            newCstrCoeffs.push(bigValue);
            decorated().addAtLeast(newCstrVars, newCstrCoeffs, BigInteger.ONE);
        }
        IVecInt newCstrVars = new VecInt();
        IVec<BigInteger> newCstrCoeffs = new Vec<BigInteger>();
        BigInteger degree = null;
        for (int i = 0; i < this.objs.size(); ++i) {
            newCstrVars.push(this.selectorVariables.get(this.selectorVariables
                    .size() - this.objs.size() + i));
            newCstrCoeffs.push(BigInteger.ONE);
            degree = BigInteger.valueOf(this.objs.size() - rank);
        }
        decorated().addAtLeast(newCstrVars, newCstrCoeffs, degree);
    }

    public boolean admitABetterSolution() throws TimeoutException {
        return admitABetterSolution(VecInt.EMPTY);
    }

    public boolean admitABetterSolution(IVecInt assumps)
            throws TimeoutException {
        if (!this.decoratedObjFuncIsSet) {
            addSortingConstraints();
            setDecoratedObjFunction();
            this.decoratedObjFuncIsSet = true;
        }
        return this.optSolver.admitABetterSolution(assumps);
    }

    protected List<List<Integer>> getSortingValuesVariables() {
        return this.sortingValuesVariables;
    }

    protected BigInteger getMaxObjValue() {
        return this.maxObjValues;
    }

    public void setSolutionFoundListener(SolutionFoundListener listener) {
        this.sfl = listener;
    }

    public boolean hasNoObjectiveFunction() {
        return false;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return true;
    }

    protected abstract void setDecoratedObjFunction();

    @Deprecated
    public Number calculateObjective() {
        return optSolver.calculateObjective();
    }

    public Number getObjectiveValue() {
        return optSolver.getObjectiveValue();
    }

    public void forceObjectiveValueTo(Number forcedValue)
            throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public boolean isOptimal() {
        return optSolver.isOptimal();
    }

    public void setTimeoutForFindingBetterSolution(int seconds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] model() {
        return optSolver.model();
    }

    @Override
    public boolean model(int var) {
        return optSolver.model(var);
    }

    @Deprecated
    public void discard() throws ContradictionException {
        this.optSolver.discard();
    }

    public void discardCurrentSolution() throws ContradictionException {
        this.optSolver.discardCurrentSolution();
    }

    public List<Integer> getObjectiveValues() {
        List<Integer> res = new ArrayList<Integer>();
        for (ObjectiveFunction obj : getObjectiveFunctions()) {
            res.add(obj.calculateDegree(decorated()).intValue());
        }
        return res;
    }

    public void init(ISolverService solverService) {
        // nothing to do here
    }

    public void assuming(int p) {
        // nothing to do here
    }

    public void propagating(int p, IConstr reason) {
        // nothing to do here
    }

    public void backtracking(int p) {
        // nothing to do here
    }

    public void adding(int p) {
        // nothing to do here
    }

    public void learn(IConstr c) {
        // nothing to do here
    }

    public void learnUnit(int p) {
        // nothing to do here
    }

    public void delete(int[] clause) {
        // nothing to do here
    }

    public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
        // nothing to do here
    }

    public void conflictFound(int p) {
        // nothing to do here
    }

    public void solutionFound(int[] model, RandomAccessModel lazyModel) {
        this.sfl.onSolutionFound(model);
        IVecInt sol = new VecInt(model);
        this.sfl.onSolutionFound(sol);
    }

    public void beginLoop() {
        // nothing to do here
    }

    public void start() {
        // nothing to do here
    }

    public void end(Lbool result) {
        // nothing to do here
    }

    public void restarting() {
        // nothing to do here
    }

    public void backjump(int backjumpLevel) {
        // nothing to do here
    }

    public void cleaning() {
        // nothing to do here
    }

}
