/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2013 Artois University and CNRS
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
package org.sat4j.pb.multiobjective;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IIntegerPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.core.IntegerVariable;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * An optimizer intended to solve an OWA based problem. The encoding sort the
 * objective values using AtLeast(x,{c1,...,cn}) constraints.
 * 
 * @author lonca
 */
public class OrderedObjsOWAOptimizer extends AbstractLinMultiObjOptimizer {

    private static final long serialVersionUID = 1L;

    private final List<IntegerVariable> objBoundVariables = new ArrayList<IntegerVariable>();

    private final List<IVecInt> atLeastFlags = new ArrayList<IVecInt>();

    private final BigInteger[] weights;

    public OrderedObjsOWAOptimizer(IIntegerPBSolver solver, int[] weights) {
        super(solver);
        this.weights = new BigInteger[weights.length];
        for (int i = 0; i < weights.length; ++i) {
            this.weights[i] = BigInteger.valueOf(weights[i]);
        }
    }

    public OrderedObjsOWAOptimizer(IIntegerPBSolver solver, BigInteger[] weights) {
        super(solver);
        this.weights = weights;
    }

    @Override
    protected void setInitConstraints() {
        BigInteger minObjValuesBound = minObjValuesBound();
        for (int i = 0; i < super.objs.size(); ++i) {
            IntegerVariable boundVar = this.integerSolver
                    .newIntegerVar(minObjValuesBound);
            this.objBoundVariables.add(boundVar);
            this.atLeastFlags.add(new VecInt());
            for (int j = 0; j < super.objs.size(); ++j) {
                addBoundConstraint(i, boundVar, j);
            }
            addFlagsCardinalityConstraint(i);
        }
    }

    private void addBoundConstraint(int boundVarIndex,
            IntegerVariable boundVar, int objIndex) {
        IVecInt literals = new VecInt();
        IVec<BigInteger> coeffs = new Vec<BigInteger>();
        super.objs.get(objIndex).getVars().copyTo(literals);
        super.objs.get(objIndex).getCoeffs().copyTo(coeffs);
        int flagLit = decorated().nextFreeVarId(true);
        this.atLeastFlags.get(boundVarIndex).push(flagLit);
        literals.push(flagLit);
        coeffs.push(minObjValuesBound().negate());
        try {
            this.integerSolver.addAtMost(literals, coeffs,
                    new Vec<IntegerVariable>().push(boundVar),
                    new Vec<BigInteger>().push(BigInteger.ONE.negate()),
                    BigInteger.ZERO);
        } catch (ContradictionException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFlagsCardinalityConstraint(int card) {
        try {
            decorated().addAtMost(this.atLeastFlags.get(card), card);
        } catch (ContradictionException e) {
            throw new RuntimeException(e);
        }
    }

    private BigInteger minObjValuesBound() {
        BigInteger maxValue = BigInteger.ZERO;
        for (Iterator<ObjectiveFunction> objsIt = this.objs.iterator(); objsIt
                .hasNext();) {
            ObjectiveFunction nextObj = objsIt.next();
            BigInteger maxObjValue = maxObjValue(nextObj);
            if (maxValue.compareTo(maxObjValue) < 0) {
                maxValue = maxObjValue;
            }
        }
        return maxValue.add(BigInteger.ONE);
    }

    private BigInteger maxObjValue(ObjectiveFunction obj) {
        IVec<BigInteger> objCoeffs = obj.getCoeffs();
        BigInteger coeffsSum = BigInteger.ZERO;
        for (Iterator<BigInteger> objCoeffsIt = objCoeffs.iterator(); objCoeffsIt
                .hasNext();) {
            coeffsSum = coeffsSum.add(objCoeffsIt.next());
        }
        return coeffsSum;
    }

    @Override
    public Number calculateObjective() {
        super.objectiveValue = BigInteger.ZERO;
        BigInteger[] objValues = getObjectiveValues();
        for (int i = 0; i < objValues.length; ++i) {
            super.objectiveValue = super.objectiveValue.add(objValues[i]
                    .multiply(this.weights[i]));
        }
        return getObjectiveValue();
    }

    @Override
    protected void setGlobalObj() {
        try {
            decorated().setObjectiveFunction(
                    new ObjectiveFunction(new VecInt(), new Vec<BigInteger>()));
            for (int i = 0; i < objBoundVariables.size(); ++i) {
                this.integerSolver.addIntegerVariableToObjectiveFunction(
                        objBoundVariables.get(i), weights[weights.length - i
                                - 1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
