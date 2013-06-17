package org.sat4j.pb.multiobjective;

import java.math.BigInteger;
import java.util.List;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class LeximinSumDecomposableOwsOptimizer extends
        SortedMultiObjectiveValuesSolver {

    private static final long serialVersionUID = 1L;

    private final int[] weights;

    private final int sumCoeff;

    private ObjectiveFunction sumObj;
    private int sumObjSelector;
    private IConstr sumObjCstr1 = null;
    private IConstr sumObjCstr2 = null;
    private int lastSumValue = Integer.MAX_VALUE;

    private ObjectiveFunction leximinObj;
    private int leximinObjSelector;
    private IConstr leximinObjCstr1 = null;
    private IConstr leximinObjCstr2 = null;
    private int lastLeximinValue = Integer.MAX_VALUE;

    private BigInteger bigValue;

    public LeximinSumDecomposableOwsOptimizer(IPBSolver solver, int[] weights,
            int sumCoeff) {
        super(solver);
        this.weights = weights;
        this.sumCoeff = sumCoeff;
    }

    @Override
    protected void setDecoratedObjFunction() {
        IVecInt globalObjVars = new VecInt();
        IVec<BigInteger> globalObjCoeffs = new Vec<BigInteger>();
        IVec<BigInteger> sumObjCoeffs = new Vec<BigInteger>();
        IVec<BigInteger> leximinObjCoeffs = new Vec<BigInteger>();
        List<List<Integer>> sortingValuesVariables = super
                .getSortingValuesVariables();
        BigInteger leximinFactor = BigInteger.ONE;
        BigInteger factor = null;
        for (int i = sortingValuesVariables.size() - 1; i >= 0; --i) {
            factor = BigInteger.valueOf(
                    this.weights[sortingValuesVariables.size() - i - 1]).add(
                    BigInteger.valueOf(this.sumCoeff));
            BigInteger sumFactor = BigInteger.ONE;
            for (Integer var : sortingValuesVariables.get(i)) {
                globalObjVars.push(var);
                globalObjCoeffs.push(factor);
                sumObjCoeffs.push(sumFactor);
                leximinObjCoeffs.push(leximinFactor);
                factor = factor.shiftLeft(1);
                sumFactor = sumFactor.shiftLeft(1);
                leximinFactor = leximinFactor.shiftLeft(1);
            }
        }
        this.bigValue = factor;
        decorated().setObjectiveFunction(
                new ObjectiveFunction(globalObjVars, globalObjCoeffs));
        this.sumObj = new ObjectiveFunction(globalObjVars, sumObjCoeffs);
        this.leximinObj = new ObjectiveFunction(globalObjVars, leximinObjCoeffs);
        this.leximinObjSelector = decorated().nextFreeVarId(true);
        this.sumObjSelector = decorated().nextFreeVarId(true);
        try {
            decorated().addAtLeast(
                    new VecInt(new int[] { this.sumObjSelector,
                            this.leximinObjSelector }), 1);
        } catch (ContradictionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean admitABetterSolution() throws TimeoutException {
        return admitABetterSolution(VecInt.EMPTY);
    }

    @Override
    public boolean admitABetterSolution(IVecInt assumps)
            throws TimeoutException {
        boolean res = super.admitABetterSolution(assumps);
        if (res) {
            try {
                updateCstrs();
            } catch (ContradictionException e) {
                // nothing to do here
            }
        } else {
            removedSubsumedAddedCstrs();
        }
        return res;
    }

    private void updateCstrs() throws ContradictionException {
        BigInteger sumValue = this.sumObj.calculateDegree(decorated());
        IVecInt sumLits = new VecInt();
        IVec<BigInteger> sumCoeffs = new Vec<BigInteger>();
        this.sumObj.getVars().copyTo(sumLits);
        this.sumObj.getCoeffs().copyTo(sumCoeffs);
        sumLits.push(sumObjSelector);
        sumCoeffs.push(bigValue);
        BigInteger leximinValue = this.leximinObj.calculateDegree(decorated());
        IVecInt leximinLits = new VecInt();
        IVec<BigInteger> leximinCoeffs = new Vec<BigInteger>();
        this.leximinObj.getVars().copyTo(leximinLits);
        this.leximinObj.getCoeffs().copyTo(leximinCoeffs);
        leximinLits.push(leximinObjSelector);
        leximinCoeffs.push(bigValue);
        ((PseudoOptDecorator) optSolver).removeSubsumedOptConstr();
        if ((sumValue.intValue() <= this.lastSumValue)
                && (leximinValue.intValue() <= this.lastLeximinValue)) {
            removedSubsumedAddedCstrs();
        } else {
            removedAddedCstrs();
        }
        this.lastSumValue = sumValue.intValue();
        this.lastLeximinValue = leximinValue.intValue();
        this.sumObjCstr1 = decorated().addAtMost(sumLits, sumCoeffs,
                bigValue.add(sumValue));
        this.sumObjCstr2 = decorated().addAtLeast(sumLits, sumCoeffs,
                sumValue.add(BigInteger.ONE));
        this.leximinObjCstr1 = decorated().addAtMost(leximinLits,
                leximinCoeffs, bigValue.add(leximinValue));
        this.leximinObjCstr2 = decorated().addAtLeast(leximinLits,
                leximinCoeffs, leximinValue.add(BigInteger.ONE));
        optSolver.discardCurrentSolution();
    }

    private void removedSubsumedAddedCstrs() {
        if (this.sumObjCstr1 != null) {
            decorated().removeSubsumedConstr(leximinObjCstr2);
            decorated().removeSubsumedConstr(leximinObjCstr1);
            decorated().removeSubsumedConstr(sumObjCstr2);
            decorated().removeSubsumedConstr(sumObjCstr1);
        }
    }

    private void removedAddedCstrs() {
        if (this.sumObjCstr1 != null) {
            decorated().removeConstr(leximinObjCstr2);
            decorated().removeConstr(leximinObjCstr1);
            decorated().removeConstr(sumObjCstr2);
            decorated().removeConstr(sumObjCstr1);
        }
    }
}
