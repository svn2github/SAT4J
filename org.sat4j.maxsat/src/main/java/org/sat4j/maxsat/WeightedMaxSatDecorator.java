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
import org.sat4j.specs.IteratorInt;
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

    public static final BigInteger SAT4J_MAX_BIG_INTEGER = new BigInteger("100000000000000000000000000000000000000000");

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected int nborigvars;

    private int nbexpectedclauses;

    private BigInteger falsifiedWeight = BigInteger.ZERO;

    protected int nbnewvar;

	protected int[] prevmodel;
	protected boolean[] prevboolmodel;

    protected int[] prevfullmodel;
    
    private IConstr previousPBConstr;

    public WeightedMaxSatDecorator(IPBSolver solver) {
        super(solver);
        IOrder order = ((Solver<?>) solver).getOrder();
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
        falsifiedWeight = BigInteger.ZERO;
        super.setExpectedNumberOfClauses(nb);
        super.newVar(nborigvars + nbexpectedclauses);
    }

    @Override
    public int[] model() {
		return prevmodel;
    }

	@Override
	public boolean model(int var) {
		return prevboolmodel[var - 1];
    }

    protected BigInteger top = SAT4J_MAX_BIG_INTEGER;

    public void setTopWeight(BigInteger top) {
        this.top = top;
    }

	/**
	 * Add a set of literals to the solver.
	 * 
	 * Here the assumption is that the first literal (literals[0]) is the weight
	 * of the constraint as found in the MAXSAT evaluation. if the weight is
	 * greater or equal to the top weight, then the clause is hard, else it is
	 * soft.
	 * 
	 * @param literals
	 *            a weighted clause, the weight being the first element of the
	 *            vector.
	 * @see #setTopWeight(int)
	 */
    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {
        int weight = literals.get(0);
		literals.delete(0);
		return addSoftClause(weight, literals);
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
		return addSoftClause(BigInteger.valueOf(weight),literals);
	}

	public IConstr addSoftClause(BigInteger weight, IVecInt literals)
		throws ContradictionException {
        if (weight.compareTo(top)<0) {

            if (literals.size() == 1) {
                // if there is only a coefficient and a literal, no need to
                // create
                // a new variable
                // check first if the literal is already in the list:
                int lit = -literals.get(0);
                int index = lits.containsAt(lit);
                if (index != -1) {
                    coefs.set(index, coefs.get(index).add(weight));
                } else {
                    // check if the opposite literal is already there
                    index = lits.containsAt(-lit);
                    if (index != -1) {
                    	falsifiedWeight = falsifiedWeight.add(weight);
                        BigInteger oldw = coefs.get(index);
                        BigInteger diff = oldw.subtract(weight);
                        if (diff.signum() > 0) {
                            coefs.set(index, diff);
                        } else if (diff.signum() < 0) {
                            lits.set(index, lit);
                            coefs.set(index, diff.abs());
                            // remove from falsifiedWeight the
                            // part of the weight that will remain 
                            // in the objective function
                            falsifiedWeight = falsifiedWeight.add(diff);
                        } else {
                            assert diff.signum() == 0;                            
                            lits.delete(index);
                            coefs.delete(index);
                        }
                    } else {
                        lits.push(lit);
                        coefs.push(weight);
                    }
                }
                return null;
            }
            coefs.push(weight);
            int newvar = nborigvars + ++nbnewvar;
			literals.push(newvar);
            lits.push(newvar);
        }
        return super.addClause(literals);
    }

	/**
	 * Set some literals whose sum must be minimized.
	 * 
	 * @param literals
	 *            the sum of those literals must be minimized.
	 */
	public void addLiteralsToMinimize(IVecInt literals) {
		for (IteratorInt it = literals.iterator(); it.hasNext();) {
			lits.push(it.next());
			coefs.push(BigInteger.ONE);
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
		if (literals.size() != coefs.size())
			throw new IllegalArgumentException();
		for (int i = 0; i < literals.size(); i++) {
			lits.push(literals.get(i));
			coefs.push(coefficients.get(i));
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
		if (literals.size() != coefficients.size())
			throw new IllegalArgumentException();
		for (int i = 0; i < literals.size(); i++) {
			lits.push(literals.get(i));
			coefs.push(BigInteger.valueOf(coefficients.get(i)));
		}
    }
    
    public boolean admitABetterSolution() throws TimeoutException {
    	return admitABetterSolution(VecInt.EMPTY);
    }
    
    public boolean admitABetterSolution(IVecInt assumps)
		throws TimeoutException {
        boolean result = super.isSatisfiable(assumps,true);
        if (result) {
			prevboolmodel = new boolean[nVars()];
			for (int i = 0; i < nVars(); i++) {
				prevboolmodel[i] = decorated().model(i + 1);
			}
            int nbtotalvars = nborigvars + nbnewvar;
            if (prevfullmodel == null)
                prevfullmodel = new int[nbtotalvars];
            for (int i = 1; i <= nbtotalvars; i++) {
                prevfullmodel[i - 1] = super.model(i) ? i : -i;
            }
            prevmodel = new int[nborigvars];
			for (int i = 0; i < nborigvars; i++) {
				prevmodel[i] = prevfullmodel[i];
			}
			calculateObjective();
        } else {
			if (previousPBConstr != null) {
				decorated().removeConstr(previousPBConstr);
				previousPBConstr = null;
			}
		}
        return result;
    }

    @Override
    public void reset() {
        coefs.clear();
        lits.clear();
        nbnewvar = 0;
        previousPBConstr = null;
        super.reset();
    }

    public boolean hasNoObjectiveFunction() {
        return false;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return false;
    }

    private BigInteger counter;

    public Number calculateObjective() {
        counter = BigInteger.ZERO;
        for (int q : prevfullmodel) {
            int index = lits.containsAt(q);
            if (index != -1) {
                counter = counter.add(coefs.get(index));
            }
        }
        return falsifiedWeight.add(counter);
    }

    private final IVecInt lits = new VecInt();

    private final IVec<BigInteger> coefs = new Vec<BigInteger>();

    private final ObjectiveFunction obj = new ObjectiveFunction(lits, coefs);

    public void discardCurrentSolution() throws ContradictionException {
        assert lits.size() == coefs.size();
        if (previousPBConstr!=null) {
        	removeSubsumedConstr(previousPBConstr);
        }
        previousPBConstr = super.addPseudoBoolean(lits, coefs, false, counter.add(BigInteger.ONE.negate()));
    }

	public Number getObjectiveValue() {
		return falsifiedWeight.add(counter);
	}

	public void discard() throws ContradictionException {
		discardCurrentSolution();
	}

	public void forceObjectiveValueTo(Number forcedValue)
			throws ContradictionException {
		super.addPseudoBoolean(lits, coefs, false, (BigInteger)forcedValue);
	}
}
