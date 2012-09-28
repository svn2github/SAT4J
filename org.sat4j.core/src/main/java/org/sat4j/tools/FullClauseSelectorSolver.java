package org.sat4j.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

public class FullClauseSelectorSolver<T extends ISolver> extends
        AbstractClauseSelectorSolver<T> {

    protected Map<Integer, IConstr> constrs = new HashMap<Integer, IConstr>();
    private int lastCreatedVar;
    private boolean pooledVarId = false;
    protected final IVecInt lastClause = new VecInt();
    protected IConstr lastConstr;
    protected final boolean skipDuplicatedEntries;

    public FullClauseSelectorSolver(T solver, boolean skipDuplicatedEntries) {
        super(solver);
        this.skipDuplicatedEntries = skipDuplicatedEntries;
    }

    public IConstr addControlableClause(IVecInt literals)
            throws ContradictionException {
        if (this.skipDuplicatedEntries) {
            if (literals.equals(this.lastClause)) {
                // System.err.println("c Duplicated entry: " + literals);
                return null;
            }
            this.lastClause.clear();
            literals.copyTo(this.lastClause);
        }
        int newvar = createNewVar(literals);
        literals.push(newvar);
        this.lastConstr = super.addClause(literals);
        if (this.lastConstr == null) {
            discardLastestVar();
        } else {
            this.constrs.put(newvar, this.lastConstr);
        }
        return this.lastConstr;
    }

    public IConstr addNonControlableClause(IVecInt literals)
            throws ContradictionException {
        return super.addClause(literals);
    }

    /**
     * 
     * @param literals
     * @return
     * @since 2.1
     */
    @Override
    protected int createNewVar(IVecInt literals) {
        for (IteratorInt it = literals.iterator(); it.hasNext();) {
            if (Math.abs(it.next()) > nextFreeVarId(false)) {
                throw new IllegalStateException(
                        "Please call newVar(int) before adding constraints!!!");
            }
        }
        if (this.pooledVarId) {
            this.pooledVarId = false;
            return this.lastCreatedVar;
        }
        this.lastCreatedVar = nextFreeVarId(true);
        return this.lastCreatedVar;
    }

    protected void discardLastestVar() {
        this.pooledVarId = true;
    }

    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {
        return addControlableClause(literals);
    }

    @Override
    public int[] model() {
        int[] fullmodel = super.modelWithInternalVariables();
        if (fullmodel == null) {
            return null;
        }
        int[] model = new int[fullmodel.length - this.constrs.size()];
        int j = 0;
        for (int element : fullmodel) {
            if (this.constrs.get(Math.abs(element)) == null) {
                model[j++] = element;
            }
        }
        return model;
    }

    /**
     * 
     * @since 2.1
     */
    public Collection<IConstr> getConstraints() {
        return this.constrs.values();
    }

    @Override
    public Collection<Integer> getAddedVars() {
        return this.constrs.keySet();
    }

}
