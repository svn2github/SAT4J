package org.sat4j.pb.multiobjective;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class LeximinOptimizer extends SortedMultiObjectiveValuesSolver
        implements IOptimizationProblem {

    private static final long serialVersionUID = 1L;
    private boolean decoratedObjFuncIsSet = false;
    private int[] lastModel = null;
    private final boolean isOptimal = false;
    private IOptimizationProblem optSolver;

    public LeximinOptimizer(IPBSolver solver) {
        super(solver);
    }

    public List<Integer> getObjectiveValues() {
        List<Integer> res = new ArrayList<Integer>();
        for (ObjectiveFunction obj : super.getObjectiveFunctions()) {
            res.add(obj.calculateDegree(decorated()).intValue());
        }
        return res;
    }

    private void setDecoratedObjFunction() {
        super.addSortingConstraints();
        IVecInt globalObjVars = new VecInt();
        IVec<BigInteger> globalObjCoeffs = new Vec<BigInteger>();
        BigInteger factor = BigInteger.ONE;
        List<List<Integer>> sortingValuesVariables = super
                .getSortingValuesVariables();
        for (int i = sortingValuesVariables.size() - 1; i >= 0; --i) {
            for (Integer var : sortingValuesVariables.get(i)) {
                globalObjVars.push(var);
                globalObjCoeffs.push(factor);
                factor = factor.shiftLeft(1);
            }
        }
        decorated().setObjectiveFunction(
                new ObjectiveFunction(globalObjVars, globalObjCoeffs));
        optSolver = new PseudoOptDecorator(decorated());
    }

    // public boolean admitABetterSolution() throws TimeoutException {
    // return admitABetterSolution(VecInt.EMPTY);
    // }
    //
    // public boolean admitABetterSolution(IVecInt assumps)
    // throws TimeoutException {
    // if (!this.decoratedObjFuncIsSet) {
    // setDecoratedObjFunction();
    // this.decoratedObjFuncIsSet = true;
    // }
    // if (decorated().isSatisfiable()) {
    // this.lastModel = decorated().model();
    // return true;
    // } else {
    // this.isOptimal = true;
    // return false;
    // }
    // }

    public boolean admitABetterSolution() throws TimeoutException {
        return admitABetterSolution(VecInt.EMPTY);
    }

    public boolean admitABetterSolution(IVecInt assumps)
            throws TimeoutException {
        if (!this.decoratedObjFuncIsSet) {
            setDecoratedObjFunction();
            this.decoratedObjFuncIsSet = true;
        }
        return this.optSolver.admitABetterSolution(assumps);
    }

    public boolean hasNoObjectiveFunction() {
        return false;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return true;
    }

    @Deprecated
    public Number calculateObjective() {
        return getObjectiveValue();
    }

    public Number getObjectiveValue() {
        return decorated().getObjectiveFunction().calculateDegree(decorated());
    }

    public void forceObjectiveValueTo(Number forcedValue)
            throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public boolean isOptimal() {
        return this.isOptimal;
    }

    public void setTimeoutForFindingBetterSolution(int seconds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] model() {
        return this.lastModel = decorated().model();
    }

    @Deprecated
    public void discard() throws ContradictionException {
        this.optSolver.discard();
    }

    public void discardCurrentSolution() throws ContradictionException {
        this.optSolver.discardCurrentSolution();
    }

}
