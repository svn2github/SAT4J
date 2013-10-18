package org.sat4j.pb.multiobjective;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IIntegerPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.core.IntegerVariable;
import org.sat4j.pb.tools.ManyCoreIntegerPB;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.RandomAccessModel;
import org.sat4j.specs.TimeoutException;

public class MulticoreOrderedObjsOWAOptimizer extends OrderedObjsOWAOptimizer {

    private static final long serialVersionUID = 1L;
    private final ManyCoreIntegerPB<IIntegerPBSolver> manyCoreSolver;
    private IIntegerPBSolver sumImpSolver;
    private IIntegerPBSolver lexImpSolver;
    private ObjectiveFunction sumObj;
    private ObjectiveFunction lexObj;
    private IntegerVariable lexBoundVar;
    private IntegerVariable sumBoundVar;
    private IVecInt sumBoundAssumps;
    private IVecInt lexBoundAssumps;
    private RandomAccessModel lastSolver;
    private int[] lastModel;
    private int[] lastModelWithInternalVariables;
    private IConstr sumCstr;
    private IConstr owaCstr;
    private ObjectiveFunction owaObj;
    private IConstr lexCstr;
    private boolean isOptimal = false;

    public MulticoreOrderedObjsOWAOptimizer(
            ManyCoreIntegerPB<IIntegerPBSolver> solver, int[] weights) {
        super(solver, weights);
        this.manyCoreSolver = solver;
        this.sumImpSolver = this.manyCoreSolver.getSolvers().get(0);
        this.sumImpSolver.setLogPrefix("# sumImp: ");
        this.lexImpSolver = this.manyCoreSolver.getSolvers().get(1);
        this.lexImpSolver.setLogPrefix("# lexImp: ");
        this.manyCoreSolver.setVerbose(true);
    }

    public MulticoreOrderedObjsOWAOptimizer(
            ManyCoreIntegerPB<IIntegerPBSolver> solver, BigInteger[] weights) {
        super(solver, weights);
        this.manyCoreSolver = solver;
    }

    @Override
    protected void setInitConstraints() {
        super.setInitConstraints();
        createSumAndLexObjs();
    }

    private void createSumAndLexObjs() {
        IVecInt auxObjsVars = new VecInt();
        IVec<BigInteger> sumObjCoeffs = new Vec<BigInteger>();
        IVec<BigInteger> lexObjCoeffs = new Vec<BigInteger>();
        BigInteger lexFactor = BigInteger.ONE;
        for (Iterator<IntegerVariable> intVarIt = super.objBoundVariables
                .iterator(); intVarIt.hasNext();) {
            BigInteger sumFactor = BigInteger.ONE;
            IntegerVariable nextBoundVar = intVarIt.next();
            for (IteratorInt nextBoundVarLitsIt = nextBoundVar.getVars()
                    .iterator(); nextBoundVarLitsIt.hasNext();) {
                auxObjsVars.push(nextBoundVarLitsIt.next());
                sumObjCoeffs.push(sumFactor);
                sumFactor = sumFactor.shiftLeft(1);
                lexObjCoeffs.push(lexFactor);
                lexFactor = lexFactor.shiftLeft(1);
            }
        }
        this.sumObj = new ObjectiveFunction(auxObjsVars, sumObjCoeffs);
        this.lexObj = new ObjectiveFunction(auxObjsVars, lexObjCoeffs);
    }

    @Override
    protected void setGlobalObj() {
        super.setGlobalObj();
        setBoundVars();
        this.sumImpSolver.setObjectiveFunction(sumObj);
        this.lexImpSolver.setObjectiveFunction(lexObj);
    }

    private void setBoundVars() {
        owaObj = this.manyCoreSolver.getObjectiveFunction();
        this.sumBoundVar = this.sumImpSolver.newIntegerVar(super
                .minObjValuesBound().multiply(
                        BigInteger.valueOf(super.objs.size())));
        this.lexBoundVar = this.lexImpSolver.newIntegerVar(super
                .minObjValuesBound().pow(super.objs.size()));
        try {
            this.sumImpSolver.addAtMost(
                    this.sumObj.getVars(),
                    this.sumObj.getCoeffs(),
                    new Vec<IntegerVariable>(
                            new IntegerVariable[] { sumBoundVar }),
                    new Vec<BigInteger>(new BigInteger[] { BigInteger.ONE
                            .negate() }), BigInteger.ONE.negate());
            this.lexImpSolver.addAtMost(
                    this.lexObj.getVars(),
                    this.lexObj.getCoeffs(),
                    new Vec<IntegerVariable>(
                            new IntegerVariable[] { lexBoundVar }),
                    new Vec<BigInteger>(new BigInteger[] { BigInteger.ONE
                            .negate() }), BigInteger.ONE.negate());
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void discardCurrentSolution() throws ContradictionException {
        this.sumBoundAssumps = new VecInt();
        setBoundAssumps(this.sumBoundAssumps, sumObjValue(), this.sumBoundVar);
        this.lexBoundAssumps = new VecInt();
        setBoundAssumps(this.lexBoundAssumps, lexObjValue(), this.lexBoundVar);
        if (this.owaCstr != null) {
            this.manyCoreSolver.removeSubsumedConstr(this.owaCstr);
            this.manyCoreSolver.removeSubsumedConstr(this.sumCstr);
            this.manyCoreSolver.removeSubsumedConstr(this.lexCstr);
        }
        this.lexCstr = this.manyCoreSolver.addAtMost(this.lexObj.getVars(),
                this.lexObj.getCoeffs(), maxLexBound());
        this.sumCstr = this.manyCoreSolver.addAtMost(this.sumObj.getVars(),
                this.sumObj.getCoeffs(), maxSumBound());
        this.owaCstr = this.manyCoreSolver.addAtMost(this.owaObj.getVars(),
                this.owaObj.getCoeffs(),
                super.objectiveValue.subtract(BigInteger.ONE));
    }

    private BigInteger maxLexBound() {
        BigInteger maxObjValue = super.objectiveValue.divide(
                BigInteger.valueOf(super.objs.size())).add(BigInteger.ONE);
        return maxObjValue.multiply(BigInteger.valueOf(
                1 << super.objBoundVariables.get(0).getVars().size()).multiply(
                BigInteger.valueOf(super.objs.size() - 1)));
    }

    private BigInteger lexObjValue() {
        BigInteger res = BigInteger.ZERO;
        BigInteger[] objValues = getObjectiveValues();
        BigInteger factor = BigInteger.ONE;
        for (BigInteger objVal : objValues) {
            res = res.add(objVal.multiply(factor));
            factor = factor.multiply(BigInteger
                    .valueOf(1 << super.objBoundVariables.get(0).getVars()
                            .size()));
        }
        return res;
    }

    private BigInteger sumObjValue() {
        BigInteger res = BigInteger.ZERO;
        for (BigInteger objVal : getObjectiveValues())
            res = res.add(objVal);
        return res;
    }

    private void setBoundAssumps(IVecInt assumps, BigInteger bound,
            IntegerVariable boundVar) {
        int i;
        for (i = 0; i < bound.bitLength(); ++i)
            assumps.push(bound.testBit(i) ? boundVar.getVars().get(i)
                    : -boundVar.getVars().get(i));
        for (; i < boundVar.getVars().size(); ++i)
            assumps.push(-boundVar.getVars().get(i));
    }

    @Override
    public void discard() throws ContradictionException {
        discardCurrentSolution();
    }

    @Override
    public boolean admitABetterSolution() throws TimeoutException {
        return admitABetterSolution(new VecInt());
    }

    @Override
    public synchronized boolean admitABetterSolution(IVecInt assumps)
            throws TimeoutException {
        if (!this.initConstraintsSet) {
            setInitConstraints();
            setGlobalObj();
            this.initConstraintsSet = true;
        }
        solverLauncherThread sumImpThread = new solverLauncherThread(
                sumImpSolver, sumBoundAssumps);
        sumImpThread.start();
        solverLauncherThread lexImpThread = new solverLauncherThread(
                lexImpSolver, lexBoundAssumps);
        lexImpThread.start();
        boolean done = false;
        synchronized (MulticoreOrderedObjsOWAOptimizer.class) {
            while (!done) {
                try {
                    sumImpThread.join(500);
                    lexImpThread.join(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (sumImpThread.computationDone && sumImpThread.isSat) {
                    lexImpThread.expireTimeout();
                }
                if (lexImpThread.computationDone && lexImpThread.isSat) {
                    sumImpThread.expireTimeout();
                }
                done = sumImpThread.isComputationDone()
                        && lexImpThread.isComputationDone();
            }
        }
        this.lastSolver = null;
        if (sumImpThread.isSat) {
            this.lastSolver = this.sumImpSolver;
        } else if (lexImpThread.isSat) {
            this.lastSolver = this.lexImpSolver;
        } else if (sumImpThread.timeoutOccured()
                && lexImpThread.timeoutOccured()) {
            throw new TimeoutException();
        }
        boolean res = this.lastSolver != null;
        if (res) {
            this.lastModel = ((ISolver) this.lastSolver).model();
            this.lastModelWithInternalVariables = ((ISolver) this.lastSolver)
                    .modelWithInternalVariables();
            calculateObjective();
            if (isVerbose()) {
                System.out.println("# global: "
                        + "Current objective functions values: "
                        + Arrays.toString(getObjectiveValues()));
                System.out.println("# global: "
                        + "Current OWA objective function value: "
                        + super.objectiveValue);
            }
        } else {
            this.isOptimal = true;
        }
        return res;
    }

    private BigInteger maxSumBound() {
        BigInteger weigthsSum = BigInteger.ZERO;
        for (BigInteger weight : super.weights)
            weigthsSum = weigthsSum.add(weight);
        BigInteger res = super.objectiveValue.divide(weigthsSum);
        res = res.add(BigInteger.ONE);
        res = res.multiply(BigInteger.valueOf(super.objs.size()));
        System.out.println("->" + res);
        return res;
    }

    @Override
    public boolean isOptimal() {
        return this.isOptimal;
    }

    @Override
    public int[] model() {
        return this.lastModel;
    }

    @Override
    public boolean model(int var) {
        return this.lastModelWithInternalVariables[var - 1] > 0;
    }

    @Override
    public int[] modelWithInternalVariables() {
        return this.lastModelWithInternalVariables;
    }

    class solverLauncherThread extends Thread {

        private final IIntegerPBSolver solver;
        private final IVecInt assumps;
        private boolean timeoutOccured;
        private boolean computationDone = false;
        private boolean isSat = false;

        public solverLauncherThread(IIntegerPBSolver solver, IVecInt... assumps) {
            this.solver = solver;
            this.assumps = new VecInt();
            for (IVecInt vector : assumps)
                if (vector != null)
                    vector.copyTo(this.assumps);
        }

        @Override
        public void run() {
            try {
                this.isSat = this.solver.isSatisfiable(assumps);
            } catch (TimeoutException e) {
                this.timeoutOccured = true;
            }
            synchronized (solverLauncherThread.class) {
                this.computationDone = true;
            }
        }

        public synchronized void expireTimeout() {
            this.solver.expireTimeout();
        }

        public boolean isComputationDone() {
            boolean res;
            synchronized (solverLauncherThread.class) {
                res = this.computationDone;
            }
            return res;
        }

        public boolean timeoutOccured() {
            return this.timeoutOccured;
        }

        public boolean isSat() {
            return this.isSat;
        }

    }

}