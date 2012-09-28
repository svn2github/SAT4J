package org.sat4j.tools;

import java.util.Collection;

import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public abstract class AbstractClauseSelectorSolver<T extends ISolver> extends
        SolverDecorator<T> {

    private static final long serialVersionUID = 1L;

    public AbstractClauseSelectorSolver(T solver) {
        super(solver);
    }

    protected abstract int createNewVar(IVecInt literals);

    public abstract Collection<Integer> getAddedVars();

}
