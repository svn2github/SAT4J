package org.sat4j.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IGroupSolver;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public class GroupClauseSelectorSolver<T extends ISolver> extends
        AbstractClauseSelectorSolver<T> implements IGroupSolver {

    private static final long serialVersionUID = 1L;

    protected Map<Integer, Integer> varToHighLevel = new HashMap<Integer, Integer>();
    private int lastCreatedVar;
    private boolean pooledVarId = false;
    private final Map<Integer, Integer> highLevelToVar = new HashMap<Integer, Integer>();

    public GroupClauseSelectorSolver(T solver) {
        super(solver);
    }

    public IConstr addControlableClause(IVecInt literals, int desc)
            throws ContradictionException {
        if (desc == 0) {
            return super.addClause(literals);
        }
        Integer hlvar = this.highLevelToVar.get(desc);
        if (hlvar == null) {
            hlvar = createNewVar(literals);
            this.highLevelToVar.put(desc, hlvar);
            this.varToHighLevel.put(hlvar, desc);
        }
        literals.push(hlvar);
        IConstr constr = super.addClause(literals);
        return constr;
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

    public IConstr addClause(IVecInt literals, int desc)
            throws ContradictionException {
        return addControlableClause(literals, desc);
    }

    @Override
    public Collection<Integer> getAddedVars() {
        return varToHighLevel.keySet();
    }

    @Override
    public int[] model() {
        int[] fullmodel = super.modelWithInternalVariables();
        if (fullmodel == null) {
            return null;
        }
        int[] model = new int[fullmodel.length - this.varToHighLevel.size()];
        int j = 0;
        for (int element : fullmodel) {
            if (this.varToHighLevel.get(Math.abs(element)) == null) {
                model[j++] = element;
            }
        }
        return model;
    }

}
