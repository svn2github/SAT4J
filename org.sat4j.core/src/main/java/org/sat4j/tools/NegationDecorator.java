package org.sat4j.tools;

import java.util.ArrayList;
import java.util.Collection;

import org.sat4j.core.ConstrGroup;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.TimeoutException;

/**
 * Negates the formula inside a solver.
 * 
 * @author leberre
 * 
 * @param <T>
 */
public class NegationDecorator<T extends ISolver> extends
        AbstractClauseSelectorSolver<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Collection<Integer> addedVars = new ArrayList<Integer>();

    public NegationDecorator(T decorated) {
        super(decorated);
        internalState();
    }

    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {
        int newVar = createNewVar(literals);
        IVecInt clause = new VecInt(2);
        clause.push(newVar);
        ConstrGroup group = new ConstrGroup();
        for (IteratorInt it = literals.iterator(); it.hasNext();) {
            clause.push(-it.next());
            group.add(super.addClause(clause));
            clause.pop();
        }
        addedVars.add(newVar);
        return group;
    }

    @Override
    public IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public IConstr addExactly(IVecInt literals, int n)
            throws ContradictionException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps, boolean global)
            throws TimeoutException {
        IVecInt vars = new VecInt();
        for (int var : getAddedVars()) {
            vars.push(var);
        }
        try {
            IConstr constr = super.addClause(vars);
            boolean returnValue;
            try {
                returnValue = super.isSatisfiable(assumps, global);
            } finally {
                removeConstr(constr);
            }
            return returnValue;
        } catch (ContradictionException e) {
            return false;
        }

    }

    @Override
    public Collection<Integer> getAddedVars() {
        return addedVars;
    }

}
