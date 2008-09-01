/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *******************************************************************************/
package org.sat4j.maxsat;

import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.Solver;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.PBSolverDecorator;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * A decorator for solving weighted MAX SAT problems.
 * 
 * The first value of the list of literals in the addClause() method contains
 * the weight of the clause.
 * 
 * @author daniel
 * 
 */
public class WeightedMaxSatDecorator extends PBSolverDecorator implements
        IOptimizationProblem {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected int nborigvars;

    private int nbexpectedclauses;

    private long falsifiedWeight;

    protected int nbnewvar;

    protected int[] prevfullmodel;

    public WeightedMaxSatDecorator(IPBSolver solver) {
        super(solver);
        IOrder<?> order = ((Solver<?, ? extends DataStructureFactory<?>>) solver)
                .getOrder();
        if (order instanceof VarOrderHeapObjective) {
            ((VarOrderHeapObjective) order).setObjectiveFunction(obj);
        }
    }

    @Override
    public int newVar(int howmany) {
        nborigvars = super.newVar(howmany);
        return nborigvars;
    }

    @Override
    public void setExpectedNumberOfClauses(int nb) {
        nbexpectedclauses = nb;
        lits.ensure(nb);
        falsifiedWeight = 0;
        super.setExpectedNumberOfClauses(nb);
        super.newVar(nborigvars + nbexpectedclauses);
    }

    @Override
    public int[] model() {
        int[] shortmodel = new int[nborigvars];
        for (int i = 0; i < nborigvars; i++) {
            shortmodel[i] = prevfullmodel[i];
        }
        return shortmodel;
    }

    protected int top = Integer.MAX_VALUE;

    public void setTopWeight(int top) {
        this.top = top;
    }

    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {
        int weight = literals.get(0);
        if (weight < top) {

            BigInteger bigweight = BigInteger.valueOf(weight);
            if (literals.size() == 2) {
                // if there is only a coefficient and a literal, no need to
                // create
                // a new variable
                // check first if the literal is already in the list:
                int lit = -literals.get(1);
                int index = lits.containsAt(lit);
                if (index != -1) {
                    coefs.set(index, coefs.get(index).add(bigweight));
                } else {
                    // check if the opposite literal is already there
                    index = lits.containsAt(-lit);
                    if (index != -1) {
                    	falsifiedWeight += weight;
                        BigInteger oldw = coefs.get(index);
                        BigInteger diff = oldw.subtract(bigweight);
                        if (diff.signum() > 0) {
                            coefs.set(index, diff);
                        } else if (diff.signum() < 0) {
                            lits.set(index, lit);
                            coefs.set(index, diff.abs());
                            // remove from falsifiedWeight the
                            // part of the weight that will remain 
                            // in the objective function
                            falsifiedWeight += diff.intValue();
                        } else {
                            assert diff.signum() == 0;                            
                            lits.delete(index);
                            coefs.delete(index);
                        }
                    } else {
                        lits.push(lit);
                        coefs.push(bigweight);
                    }
                }
                return null;
            }
            coefs.push(bigweight);
            int newvar = nborigvars + ++nbnewvar;
            literals.set(0, newvar);
            lits.push(newvar);
        } else {
            literals.delete(0);
        }
        return super.addClause(literals);
    }

    public boolean admitABetterSolution() throws TimeoutException {
        boolean result = super.isSatisfiable(true);
        if (result) {
            int nbtotalvars = nborigvars + nbnewvar;
            if (prevfullmodel == null)
                prevfullmodel = new int[nbtotalvars];
            for (int i = 1; i <= nbtotalvars; i++) {
                prevfullmodel[i - 1] = super.model(i) ? i : -i;
            }
        }
        return result;
    }

    @Override
    public void reset() {
        coefs.clear();
        lits.clear();
        nbnewvar = 0;
        super.reset();
    }

    public boolean hasNoObjectiveFunction() {
        return false;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return false;
    }

    private int counter;

    public Number calculateObjective() {
        counter = 0;
        for (int q : prevfullmodel) {
            int index = lits.containsAt(q);
            if (index != -1) {
                counter += coefs.get(index).intValue();
            }
        }
        return falsifiedWeight + counter;
    }

    private final IVecInt lits = new VecInt();

    private final IVec<BigInteger> coefs = new Vec<BigInteger>();

    private final ObjectiveFunction obj = new ObjectiveFunction(lits, coefs);

    public void discard() throws ContradictionException {
        assert lits.size() == coefs.size();
        super.addPseudoBoolean(lits, coefs, false, BigInteger
                .valueOf(counter - 1));
    }

}
