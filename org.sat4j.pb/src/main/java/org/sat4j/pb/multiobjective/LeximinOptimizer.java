package org.sat4j.pb.multiobjective;

import java.math.BigInteger;
import java.util.List;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class LeximinOptimizer extends SortedMultiObjectiveValuesSolver {

    private static final long serialVersionUID = 1L;

    public LeximinOptimizer(IPBSolver solver) {
        super(solver);
    }

    @Override
    protected void setDecoratedObjFunction() {
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
    }

}
