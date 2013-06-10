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
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

public abstract class SortedMultiObjectiveValuesSolver extends
        PBSolverDecorator implements IMultiObjectivePBSolver {

    private static final long serialVersionUID = 1L;

    private final List<ObjectiveFunction> objs = new ArrayList<ObjectiveFunction>();

    private final List<Integer> selectorVariables = new ArrayList<Integer>();

    private final List<List<Integer>> sortingValuesVariables = new ArrayList<List<Integer>>();

    private BigInteger maxObjValue = BigInteger.valueOf(Long.MIN_VALUE);

    private int nbRealVars = -1;

    public int getNbRealVars() {
        return this.nbRealVars;
    }

    public SortedMultiObjectiveValuesSolver(IPBSolver solver) {
        super(solver);
    }

    public void addObjectiveFunction(ObjectiveFunction obj) {
        this.objs.add(obj);
    }

    public List<ObjectiveFunction> getObjectiveFunctions() {
        return this.objs;
    }

    protected void addSortingConstraints() {
        this.nbRealVars = decorated().nextFreeVarId(false) - 1;
        for (ObjectiveFunction obj : this.objs) {
            BigInteger weightsSum = BigInteger.ZERO;
            for (Iterator<BigInteger> it = obj.getCoeffs().iterator(); it
                    .hasNext();) {
                weightsSum = weightsSum.add(it.next().abs());
            }
            if (this.maxObjValue.compareTo(weightsSum) < 0) {
                this.maxObjValue = weightsSum;
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
                (int) Math.ceil(Math.log10(this.maxObjValue.longValue() + 1)
                        / Math.log10(2)));
        for (int i = 0; i < nbVarsNeeded; ++i) {
            currentRankSortingValuesVariables.add(decorated().nextFreeVarId(
                    true));
        }
        this.sortingValuesVariables.add(currentRankSortingValuesVariables);
        BigInteger bigValue = this.maxObjValue.add(BigInteger.ONE);
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

    protected List<List<Integer>> getSortingValuesVariables() {
        return this.sortingValuesVariables;
    }

    protected BigInteger getMaxObjValue() {
        return this.maxObjValue;
    }

}
