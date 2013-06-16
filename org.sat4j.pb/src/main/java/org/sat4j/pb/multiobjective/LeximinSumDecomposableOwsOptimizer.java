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

    private ObjectiveFunction leximinObj;
    private int leximinObjSelector;
    private IConstr leximinObjCstr1 = null;
    private IConstr leximinObjCstr2 = null;

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
        IVec<BigInteger> leximinCoeffs = new Vec<BigInteger>();
        List<List<Integer>> sortingValuesVariables = super
                .getSortingValuesVariables();
        BigInteger factor;
        for (int i = sortingValuesVariables.size() - 1; i >= 0; --i) {
            factor = BigInteger.valueOf(this.weights[sortingValuesVariables
                    .size() - i - 1]);
            for (Integer var : sortingValuesVariables.get(i)) {
                globalObjVars.push(var);
                globalObjCoeffs.push(factor.add(BigInteger.valueOf(sumCoeff)));
                sumObjCoeffs.push(BigInteger.ONE);
                leximinCoeffs.push(factor);
                factor = factor.shiftLeft(1);
            }
        }
        decorated().setObjectiveFunction(
                new ObjectiveFunction(globalObjVars, globalObjCoeffs));
        this.sumObj = new ObjectiveFunction(globalObjVars, sumObjCoeffs);
        this.leximinObj = new ObjectiveFunction(globalObjVars, leximinCoeffs);
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
            removedAddedCstrs();
        }
        return res;
    }

    private void updateCstrs() throws ContradictionException {
        ((PseudoOptDecorator) optSolver).removeSubsumedOptConstr();
        removedAddedCstrs();
        BigInteger value = this.sumObj.calculateDegree(decorated());
        IVecInt lits = new VecInt();
        IVec<BigInteger> coeffs = new Vec<BigInteger>();
        this.sumObj.getVars().copyTo(lits);
        this.sumObj.getCoeffs().copyTo(coeffs);
        lits.push(sumObjSelector);
        coeffs.push(getMaxObjValue());
        this.sumObjCstr1 = decorated().addAtMost(lits, coeffs,
                getMaxObjValue().add(value));
        this.sumObjCstr2 = decorated().addAtLeast(lits, coeffs,
                value.add(BigInteger.ONE));
        value = this.leximinObj.calculateDegree(decorated());
        lits = new VecInt();
        coeffs = new Vec<BigInteger>();
        this.leximinObj.getVars().copyTo(lits);
        this.leximinObj.getCoeffs().copyTo(coeffs);
        lits.push(leximinObjSelector);
        coeffs.push(getMaxObjValue());
        this.leximinObjCstr1 = decorated().addAtMost(lits, coeffs,
                getMaxObjValue().add(value));
        this.leximinObjCstr2 = decorated().addAtLeast(lits, coeffs,
                value.add(BigInteger.ONE));
        optSolver.discardCurrentSolution();
    }

    private void removedAddedCstrs() {
        if (this.sumObjCstr1 != null) {
            decorated().removeSubsumedConstr(leximinObjCstr2);
            decorated().removeSubsumedConstr(leximinObjCstr1);
            decorated().removeSubsumedConstr(sumObjCstr2);
            decorated().removeSubsumedConstr(sumObjCstr1);
        }
    }
}
