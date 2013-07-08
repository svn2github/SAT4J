package org.sat4j.pb.multiobjective;

import java.math.BigInteger;
import java.util.List;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class OwsOptimizer extends SortedMultiObjectiveValuesSolver {

    private static final long serialVersionUID = 1L;
    private final int[] weights;

    public OwsOptimizer(IPBSolver solver, int[] weights) {
        super(solver);
        this.weights = weights;
    }

    public OwsOptimizer(IPBSolver solver, String weights) {
        super(solver);
        String[] strWeights = weights.split(",");
        int nbWeights = strWeights.length;
        this.weights = new int[nbWeights];
        for (int i = 0; i < nbWeights; ++i) {
            this.weights[i] = Integer.valueOf(strWeights[i]);
        }
    }

    @Override
    protected void setDecoratedObjFunction() {
        IVecInt globalObjVars = new VecInt();
        IVec<BigInteger> globalObjCoeffs = new Vec<BigInteger>();
        List<List<Integer>> sortingValuesVariables = super
                .getSortingValuesVariables();
        BigInteger factor;
        for (int i = sortingValuesVariables.size() - 1; i >= 0; --i) {
            factor = BigInteger.valueOf(this.weights[sortingValuesVariables
                    .size() - i - 1]);
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
