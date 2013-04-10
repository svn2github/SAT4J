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
