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
package org.sat4j.maxsat;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.sat4j.core.ConstrGroup;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.PBSolverDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

/**
 * A decorator for solving weighted MAX SAT problems.
 * 
 * The first value of the list of literals in the addClause() method contains
 * the weight of the clause.
 * 
 * @author daniel
 * 
 */
public class WeightedMaxSatDecorator extends PBSolverDecorator {

    public static final BigInteger SAT4J_MAX_BIG_INTEGER = new BigInteger(
            "100000000000000000000000000000000000000000");

    protected int nbnewvar;

    private static final long serialVersionUID = 1L;

    private BigInteger falsifiedWeight = BigInteger.ZERO;

    private boolean maxVarIdFixed = false;
    private final boolean equivalence;

    private final IVecInt lits = new VecInt();

    private final IVec<BigInteger> coefs = new Vec<BigInteger>();

    private final ObjectiveFunction obj = new ObjectiveFunction(this.lits,
            this.coefs);

    private final Set<Integer> unitClauses = new HashSet<Integer>();

    private boolean noNewVarForUnitSoftClauses = true;

    public WeightedMaxSatDecorator(IPBSolver solver) {
        this(solver, false);
    }

    public WeightedMaxSatDecorator(IPBSolver solver, boolean equivalence) {
        super(solver);
        solver.setObjectiveFunction(this.obj);
        this.equivalence = equivalence;
    }

    @Override
    public int newVar(int howmany) {
        int res = super.newVar(howmany);
        this.maxVarIdFixed = true;
        return res;
    }

    @Override
    public void setExpectedNumberOfClauses(int nb) {
        this.lits.ensure(nb);
        this.falsifiedWeight = BigInteger.ZERO;
        super.setExpectedNumberOfClauses(nb);
    }

    protected BigInteger top = SAT4J_MAX_BIG_INTEGER;

    public void setTopWeight(BigInteger top) {
        this.top = top;
    }

    protected void checkMaxVarId() {
        if (!this.maxVarIdFixed) {
            throw new IllegalStateException(
                    "Please call newVar(int) before adding constraints!!!");
        }
    }

    /**
     * Add a soft clause to the solver.
     * 
     * That method allows to read a clause in a CNF and to consider it as soft,
     * in order to solve MAXSAT problems.
     * 
     * Note that the behavior of that method changed in release 2.3.1. Prior to
     * that, the method was expecting a weight as first element of the list of
     * literals.
     * 
     * @param literals
     *            a clause, whose weight will be one
     * @see #setTopWeight(int)
     */
    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {
        return addSoftClause(1, literals);
    }

    /**
     * Add a hard clause in the solver, i.e. a clause that must be satisfied.
     * 
     * @param literals
     *            the clause
     * @return the constraint is it is not trivially satisfied.
     * @throws ContradictionException
     */
    public IConstr addHardClause(IVecInt literals)
            throws ContradictionException {
        return super.addClause(literals);
    }

    /**
     * Add a soft clause in the solver, i.e. a clause with a weight of 1.
     * 
     * @param literals
     *            the clause.
     * @return the constraint is it is not trivially satisfied.
     * @throws ContradictionException
     */
    public IConstr addSoftClause(IVecInt literals)
            throws ContradictionException {
        return addSoftClause(1, literals);
    }

    /**
     * Add a soft clause to the solver.
     * 
     * if the weight of the clause is greater of equal to the top weight, the
     * clause will be considered as a hard clause.
     * 
     * @param weight
     *            the weight of the clause
     * @param literals
     *            the clause
     * @return the constraint is it is not trivially satisfied.
     * @throws ContradictionException
     */
    public IConstr addSoftClause(int weight, IVecInt literals)
            throws ContradictionException {
        return addSoftClause(BigInteger.valueOf(weight), literals);
    }

    public IConstr addSoftClause(BigInteger weight, IVecInt literals)
            throws ContradictionException {
        checkMaxVarId();
        if (weight.compareTo(this.top) < 0) {

            if (literals.size() == 1 && noNewVarForUnitSoftClauses) {
                // if there is only a coefficient and a literal, no need to
                // create
                // a new variable
                // check first if the literal is already in the list:
                int lit = -literals.get(0);
                int index = this.lits.containsAt(lit);

                this.unitClauses.add(-lit);

                if (index == -1) {
                    // check if the opposite literal is already there
                    index = this.lits.containsAt(-lit);
                    if (index != -1) {
                        this.falsifiedWeight = this.falsifiedWeight.add(weight);
                        BigInteger oldw = this.coefs.get(index);
                        BigInteger diff = oldw.subtract(weight);
                        if (diff.signum() > 0) {
                            this.coefs.set(index, diff);
                        } else if (diff.signum() < 0) {
                            this.lits.set(index, lit);
                            this.coefs.set(index, diff.abs());
                            // remove from falsifiedWeight the
                            // part of the weight that will remain
                            // in the objective function
                            this.falsifiedWeight = this.falsifiedWeight
                                    .add(diff);
                        } else {
                            assert diff.signum() == 0;
                            this.lits.delete(index);
                            this.coefs.delete(index);
                        }
                        this.obj.setCorrection(this.falsifiedWeight);

                    } else {
                        registerLiteral(lit);
                        this.lits.push(lit);
                        this.coefs.push(weight);
                    }
                } else {
                    this.coefs.set(index, this.coefs.get(index).add(weight));
                }
                return UnitWeightedClause.instance();
            }
            this.coefs.push(weight);
            int newvar = nextFreeVarId(true);
            literals.push(newvar);
            this.lits.push(newvar);
            if (this.equivalence) {
                ConstrGroup constrs = new ConstrGroup();
                IConstr constr = super.addClause(literals);
                if (constr==null&&isVerbose()) {
                    System.out.println(getLogPrefix()+" solft constraint "+literals+"("+weight+") is ignored");
                }
                if (constr!=null) {
                    constrs.add(constr);
                    IVecInt clause = new VecInt(2);
                    clause.push(-newvar);
                    for (int i = 0; i < literals.size() - 1; i++) {
                        clause.push(-literals.get(i));
                        constrs.add(super.addClause(clause));
                        clause.pop();
                    }
                } 
                return constrs;
            }
        }
        IConstr constr = super.addClause(literals);
        if (constr==null&&isVerbose()) {
            System.out.println(getLogPrefix()+" hard constraint "+literals+"("+weight+") is ignored");
        }
        return constr;
    }

    /**
     * Allow adding a new soft cardinality constraint in the solver.
     * 
     * @param literals
     *            the literals of the cardinality constraint.
     * @param degree
     *            the degree of the cardinality constraint.
     * @return a pseudo boolean constraint encoding that soft constraint.
     * @throws ContradictionException
     *             if a trivial contradiction is found.
     * @since 2.3
     */
    public IConstr addSoftAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        return addSoftAtLeast(BigInteger.ONE, literals, degree);
    }

    /**
     * Allow adding a new soft cardinality constraint in the solver.
     * 
     * @param weight
     *            the weight of the constraint.
     * @param literals
     *            the literals of the cardinality constraint.
     * @param degree
     *            the degree of the cardinality constraint.
     * @return a pseudo boolean constraint encoding that soft constraint.
     * @throws ContradictionException
     *             if a trivial contradiction is found.
     * @since 2.3
     */
    public IConstr addSoftAtLeast(int weight, IVecInt literals, int degree)
            throws ContradictionException {
        return addSoftAtLeast(BigInteger.valueOf(weight), literals, degree);
    }

    /**
     * Allow adding a new soft cardinality constraint in the solver.
     * 
     * @param weight
     *            the weight of the constraint.
     * @param literals
     *            the literals of the cardinality constraint.
     * @param degree
     *            the degree of the cardinality constraint.
     * @return a pseudo boolean constraint encoding that soft constraint.
     * @throws ContradictionException
     *             if a trivial contradiction is found.
     * @since 2.3
     */
    public IConstr addSoftAtLeast(BigInteger weight, IVecInt literals,
            int degree) throws ContradictionException {
        checkMaxVarId();
        if (weight.compareTo(this.top) < 0) {
            this.coefs.push(weight);
            int newvar = nextFreeVarId(true);
            this.lits.push(newvar);
            IVec<BigInteger> cardcoeffs = new Vec<BigInteger>(
                    literals.size() + 1);
            cardcoeffs.growTo(literals.size(), BigInteger.ONE);
            literals.push(newvar);
            BigInteger bigDegree = BigInteger.valueOf(degree);
            cardcoeffs.push(bigDegree);
            return addPseudoBoolean(literals, cardcoeffs, true, bigDegree);
        } else {
            return addAtLeast(literals, degree);
        }
    }

    /**
     * Allow adding a new soft cardinality constraint in the solver.
     * 
     * @param literals
     *            the literals of the cardinality constraint.
     * @param degree
     *            the degree of the cardinality constraint.
     * @return a pseudo boolean constraint encoding that soft constraint.
     * @throws ContradictionException
     *             if a trivial contradiction is found.
     * @since 2.3
     */
    public IConstr addSoftAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        return addSoftAtMost(BigInteger.ONE, literals, degree);
    }

    /**
     * Allow adding a new soft cardinality constraint in the solver.
     * 
     * @param weight
     *            the weight of the constraint.
     * @param literals
     *            the literals of the cardinality constraint.
     * @param degree
     *            the degree of the cardinality constraint.
     * @return a pseudo boolean constraint encoding that soft constraint.
     * @throws ContradictionException
     *             if a trivial contradiction is found.
     * @since 2.3
     */
    public IConstr addSoftAtMost(int weight, IVecInt literals, int degree)
            throws ContradictionException {
        return addSoftAtMost(BigInteger.valueOf(weight), literals, degree);
    }

    /**
     * Allow adding a new soft cardinality constraint in the solver.
     * 
     * @param weight
     *            the weight of the constraint.
     * @param literals
     *            the literals of the cardinality constraint.
     * @param degree
     *            the degree of the cardinality constraint.
     * @return a pseudo boolean constraint encoding that soft constraint.
     * @throws ContradictionException
     *             if a trivial contradiction is found.
     * @since 2.3
     */
    public IConstr addSoftAtMost(BigInteger weight, IVecInt literals, int degree)
            throws ContradictionException {
        checkMaxVarId();
        if (weight.compareTo(this.top) < 0) {
            this.coefs.push(weight);
            int newvar = nextFreeVarId(true);
            this.lits.push(newvar);
            IVec<BigInteger> cardcoeffs = new Vec<BigInteger>(
                    literals.size() + 1);
            cardcoeffs.growTo(literals.size(), BigInteger.ONE);
            literals.push(newvar);
            BigInteger bigDegree = BigInteger.valueOf(degree);
            cardcoeffs.push(bigDegree.negate());
            return addPseudoBoolean(literals, cardcoeffs, true, bigDegree);
        } else {
            return addAtMost(literals, degree);
        }
    }

    /**
     * Set some literals whose sum must be minimized.
     * 
     * @param literals
     *            the sum of those literals must be minimized.
     */
    public void addLiteralsToMinimize(IVecInt literals) {
        for (IteratorInt it = literals.iterator(); it.hasNext();) {
            this.lits.push(it.next());
            this.coefs.push(BigInteger.ONE);
        }
    }

    /**
     * Set some literals whose sum must be minimized.
     * 
     * @param literals
     *            the sum of those literals must be minimized.
     * @param coefficients
     *            the weight of the literals.
     */
    public void addWeightedLiteralsToMinimize(IVecInt literals,
            IVec<BigInteger> coefficients) {
        if (literals.size() != this.coefs.size()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < literals.size(); i++) {
            this.lits.push(literals.get(i));
            this.coefs.push(coefficients.get(i));
        }
    }

    /**
     * Set some literals whose sum must be minimized.
     * 
     * @param literals
     *            the sum of those literals must be minimized.
     * @param coefficients
     *            the weight of the literals.
     */
    public void addWeightedLiteralsToMinimize(IVecInt literals,
            IVecInt coefficients) {
        if (literals.size() != coefficients.size()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < literals.size(); i++) {
            this.lits.push(literals.get(i));
            this.coefs.push(BigInteger.valueOf(coefficients.get(i)));
        }
    }

    @Override
    public void reset() {
        this.coefs.clear();
        this.lits.clear();
        this.nbnewvar = 0;
        super.reset();
    }

    /**
     * Force the solver to find a solution with a specific value (nice to find
     * all optimal solutions for instance).
     * 
     * @param forcedValue
     *            the expected value of the solution
     * @throws ContradictionException
     *             if that value is trivially not possible.
     */
    public void forceObjectiveValueTo(Number forcedValue)
            throws ContradictionException {
        if (this.lits.size() > 0) {
            // there is at least one soft clause
            super.addPseudoBoolean(this.lits, this.coefs, false,
                    (BigInteger) forcedValue);
        }
    }

    /**
     * Returns the set of unit clauses added to the solver.
     */
    public Set<Integer> getUnitClauses() {
        return this.unitClauses;
    }

    /**
     * Returns true if no new variable should be created when adding a soft unit
     * clause, false otherwise. By default, this is set to true.
     * 
     * @return
     */
    public boolean isNoNewVarForUnitSoftClauses() {
        return noNewVarForUnitSoftClauses;
    }

    /**
     * Sets whether new variables should be created when adding new clauses. By
     * default, this is set to true. This can be useful when computing all
     * muses.
     * 
     * @param noNewVarForUnitSoftClauses
     */
    public void setNoNewVarForUnitSoftClauses(boolean noNewVarForUnitSoftClauses) {
        this.noNewVarForUnitSoftClauses = noNewVarForUnitSoftClauses;
    }

}
