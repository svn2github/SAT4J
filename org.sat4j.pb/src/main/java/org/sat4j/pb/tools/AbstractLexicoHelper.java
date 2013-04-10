package org.sat4j.pb.tools;

import java.math.BigInteger;
import java.util.Collection;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolverService;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.RandomAccessModel;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;

public abstract class AbstractLexicoHelper<T, C> extends DependencyHelper<T, C>
        implements SearchListener<IPBSolverService> {

    private final LexicoDecoratorPB lexico;

    public AbstractLexicoHelper(LexicoDecoratorPB lexico) {
        super(new OptToPBSATAdapter(lexico));
        this.lexico = lexico;
        this.lexico.setSearchListener(this);
    }

    public AbstractLexicoHelper(LexicoDecoratorPB lexico,
            boolean explanationEnabled) {
        super(new OptToPBSATAdapter(lexico), explanationEnabled);
        this.lexico = lexico;
        this.lexico.setSearchListener(this);
    }

    public AbstractLexicoHelper(LexicoDecoratorPB lexico,
            boolean explanationEnabled, boolean canonicalOptFunctionEnabled) {
        super(new OptToPBSATAdapter(lexico), explanationEnabled,
                canonicalOptFunctionEnabled);
        this.lexico = lexico;
        this.lexico.setSearchListener(this);
    }

    private boolean hasASolution;

    public void init(IPBSolverService solverService) {
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
        this.hasASolution = true;
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

    public void addCriterion(Collection<T> things) {
        IVecInt literals = new VecInt(things.size());
        for (T thing : things) {
            literals.push(getIntValue(thing));
        }
        this.lexico.addCriterion(literals);
    }

    public void addWeightedCriterion(Collection<WeightedObject<T>> things) {
        IVecInt literals = new VecInt(things.size());
        IVec<BigInteger> coefs = new Vec<BigInteger>(things.size());
        for (WeightedObject<T> wo : things) {
            literals.push(getIntValue(wo.thing));
            coefs.push(wo.getWeight());
        }
        this.lexico.addCriterion(literals, coefs);
    }

    /**
     * 
     * @return true if the set of constraints entered inside the solver can be
     *         satisfied.
     * @throws TimeoutException
     */
    @Override
    public boolean hasASolution() throws TimeoutException {
        try {
            return super.hasASolution();
        } catch (TimeoutException e) {
            if (this.hasASolution) {
                return true;
            } else {
                throw e;
            }
        }
    }

    /**
     * 
     * @return true if the set of constraints entered inside the solver can be
     *         satisfied.
     * @throws TimeoutException
     */
    @Override
    public boolean hasASolution(IVec<T> assumps) throws TimeoutException {
        try {
            return super.hasASolution(assumps);
        } catch (TimeoutException e) {
            if (this.hasASolution) {
                return true;
            } else {
                throw e;
            }
        }
    }

    /**
     * 
     * @return true if the set of constraints entered inside the solver can be
     *         satisfied.
     * @throws TimeoutException
     */
    @Override
    public boolean hasASolution(Collection<T> assumps) throws TimeoutException {
        try {
            return super.hasASolution(assumps);
        } catch (TimeoutException e) {
            if (this.hasASolution) {
                return true;
            } else {
                throw e;
            }
        }
    }

    public boolean isOptimal() {
        return ((OptToPBSATAdapter) getSolver()).isOptimal();
    }

}
