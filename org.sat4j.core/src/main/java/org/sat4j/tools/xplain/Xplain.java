/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.tools.xplain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolverDecorator;

/**
 * Explanation framework for SAT4J.
 * 
 * The explanation uses selector variables and assumptions.
 * 
 * It is based on a two steps method: 1) extraction of a set of assumptions
 * implying the inconsistency 2) minimization of that set.
 * 
 * @author daniel
 * 
 * @param <T>
 *            a subinterface to ISolver.
 * @since 2.1
 */
public class Xplain<T extends ISolver> extends SolverDecorator<T> implements
        Explainer {

    protected Map<Integer, IConstr> constrs = new HashMap<Integer, IConstr>();

    protected IVecInt assump;

    private int lastCreatedVar;
    private boolean pooledVarId = false;
    private final IVecInt lastClause = new VecInt();
    private IConstr lastConstr;
    private final boolean skipDuplicatedEntries;

    private MinimizationStrategy xplainStrategy = new DeletionStrategy();

    public Xplain(T solver, boolean skipDuplicatedEntries) {
        super(solver);
        this.skipDuplicatedEntries = skipDuplicatedEntries;
    }

    public Xplain(T solver) {
        this(solver, true);
    }

    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {
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

    /**
     * 
     * @param literals
     * @return
     * @since 2.1
     */
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
    public IConstr addExactly(IVecInt literals, int n)
            throws ContradictionException {
        throw new UnsupportedOperationException(
                "Explanation requires Pseudo Boolean support. See XplainPB class instead.");
    }

    @Override
    public IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        throw new UnsupportedOperationException(
                "Explanation requires Pseudo Boolean support. See XplainPB class instead.");
    }

    @Override
    public IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        throw new UnsupportedOperationException(
                "Explanation requires Pseudo Boolean support. See XplainPB class instead.");
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
     * @since 2.2.4
     * @return
     * @throws TimeoutException
     */
    private IVecInt explanationKeys() throws TimeoutException {
        assert !isSatisfiable(this.assump);
        ISolver solver = decorated();
        if (solver instanceof SolverDecorator<?>) {
            solver = ((SolverDecorator<? extends ISolver>) solver).decorated();
        }
        return this.xplainStrategy.explain(solver, this.constrs, this.assump);
    }

    public int[] minimalExplanation() throws TimeoutException {
        IVecInt keys = explanationKeys();
        keys.sort();
        List<Integer> allKeys = new ArrayList<Integer>(this.constrs.keySet());
        Collections.sort(allKeys);
        int[] model = new int[keys.size()];
        int i = 0;
        for (IteratorInt it = keys.iterator(); it.hasNext();) {
            model[i++] = allKeys.indexOf(it.next()) + 1;
        }
        return model;
    }

    /**
     * @since 2.1
     * @return
     * @throws TimeoutException
     */
    public Collection<IConstr> explain() throws TimeoutException {
        IVecInt keys = explanationKeys();
        Collection<IConstr> explanation = new ArrayList<IConstr>(keys.size());
        for (IteratorInt it = keys.iterator(); it.hasNext();) {
            explanation.add(this.constrs.get(it.next()));
        }
        return explanation;
    }

    /**
     * @since 2.1
     */
    public void cancelExplanation() {
        this.xplainStrategy.cancelExplanationComputation();
    }

    /**
     * 
     * @since 2.1
     */
    public Collection<IConstr> getConstraints() {
        return this.constrs.values();
    }

    @Override
    public int[] findModel() throws TimeoutException {
        this.assump = VecInt.EMPTY;
        IVecInt extraVariables = new VecInt();
        for (Integer p : this.constrs.keySet()) {
            extraVariables.push(-p);
        }
        return super.findModel(extraVariables);
    }

    @Override
    public int[] findModel(IVecInt assumps) throws TimeoutException {
        this.assump = assumps;
        IVecInt extraVariables = new VecInt();
        assumps.copyTo(extraVariables);
        for (Integer p : this.constrs.keySet()) {
            extraVariables.push(-p);
        }
        return super.findModel(extraVariables);
    }

    @Override
    public boolean isSatisfiable() throws TimeoutException {
        this.assump = VecInt.EMPTY;
        IVecInt extraVariables = new VecInt();
        for (Integer p : this.constrs.keySet()) {
            extraVariables.push(-p);
        }
        return super.isSatisfiable(extraVariables);
    }

    @Override
    public boolean isSatisfiable(boolean global) throws TimeoutException {
        this.assump = VecInt.EMPTY;
        IVecInt extraVariables = new VecInt();
        for (Integer p : this.constrs.keySet()) {
            extraVariables.push(-p);
        }
        return super.isSatisfiable(extraVariables, global);
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        this.assump = assumps;
        IVecInt extraVariables = new VecInt();
        assumps.copyTo(extraVariables);
        for (Integer p : this.constrs.keySet()) {
            extraVariables.push(-p);
        }
        return super.isSatisfiable(extraVariables);
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps, boolean global)
            throws TimeoutException {
        this.assump = assumps;
        IVecInt extraVariables = new VecInt();
        assumps.copyTo(extraVariables);
        for (Integer p : this.constrs.keySet()) {
            extraVariables.push(-p);
        }
        return super.isSatisfiable(extraVariables, global);
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

    @Override
    public String toString(String prefix) {
        System.out.println(prefix + "Explanation (MUS) enabled solver");
        System.out.println(prefix + this.xplainStrategy);
        return super.toString(prefix);
    }

    public void setMinimizationStrategy(MinimizationStrategy strategy) {
        this.xplainStrategy = strategy;
    }

    @Override
    public boolean removeConstr(IConstr c) {
        if (this.lastConstr == c) {
            this.lastClause.clear();
            this.lastConstr = null;
        }
        return super.removeConstr(c);
    }

    @Override
    public boolean removeSubsumedConstr(IConstr c) {
        if (this.lastConstr == c) {
            this.lastClause.clear();
            this.lastConstr = null;
        }
        return super.removeSubsumedConstr(c);
    }

}
