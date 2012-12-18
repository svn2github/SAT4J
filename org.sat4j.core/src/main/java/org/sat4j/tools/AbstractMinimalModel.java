package org.sat4j.tools;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public class AbstractMinimalModel extends SolverDecorator<ISolver> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected final IVecInt pLiterals;
    protected final SolutionFoundListener modelListener;

    public static IVecInt positiveLiterals(ISolver solver) {
        IVecInt literals = new VecInt(solver.nVars());
        for (int i = 1; i <= solver.nVars(); i++) {
            literals.push(i);
        }
        return literals;
    }

    public static IVecInt negativeLiterals(ISolver solver) {
        IVecInt literals = new VecInt(solver.nVars());
        for (int i = 1; i <= solver.nVars(); i++) {
            literals.push(-i);
        }
        return literals;
    }

    public AbstractMinimalModel(ISolver solver) {
        this(solver, SolutionFoundListener.VOID);
    }

    public AbstractMinimalModel(ISolver solver, IVecInt p) {
        this(solver, p, SolutionFoundListener.VOID);
    }

    public AbstractMinimalModel(ISolver solver,
            SolutionFoundListener modelListener) {
        this(solver, negativeLiterals(solver), modelListener);
    }

    public AbstractMinimalModel(ISolver solver, IVecInt p,
            SolutionFoundListener modelListener) {
        super(solver);
        this.pLiterals = new VecInt(p.size());
        p.copyTo(this.pLiterals);
        this.modelListener = modelListener;

    }

}
