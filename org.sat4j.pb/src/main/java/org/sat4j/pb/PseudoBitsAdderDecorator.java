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
package org.sat4j.pb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;

import org.sat4j.core.VecInt;
import org.sat4j.pb.reader.OPBReader2007;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.GateTranslator;
import org.sat4j.tools.SolverDecorator;

/**
 * A decorator that computes minimal pseudo boolean models.
 * 
 * @author daniel
 * 
 */
public class PseudoBitsAdderDecorator extends SolverDecorator<IPBSolver>
        implements IPBSolver {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private ObjectiveFunction objfct;

    private final GateTranslator gator;
    private final IPBSolver solver;

    public PseudoBitsAdderDecorator(IPBSolver solver) {
        super(solver);
        this.gator = new GateTranslator(solver);
        this.solver = solver;
    }

    public void setObjectiveFunction(ObjectiveFunction objf) {
        this.objfct = objf;
    }

    @Override
    public boolean isSatisfiable() throws TimeoutException {
        return isSatisfiable(VecInt.EMPTY);
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        if (this.objfct == null) {
            return this.gator.isSatisfiable(assumps);
        }
        System.out.println("c Original number of variables and constraints");
        System.out.println("c #vars: " + this.gator.nVars() + " #constraints: "
                + this.gator.nConstraints());
        IVecInt bitsLiterals = new VecInt();
        System.out.println("c Creating optimization constraints ....");
        try {
            this.gator.optimisationFunction(this.objfct.getVars(),
                    this.objfct.getCoeffs(), bitsLiterals);
        } catch (ContradictionException e) {
            return false;
        }
        System.out.println("c ... done. " + bitsLiterals);
        System.out.println("c New number of variables and constraints");
        System.out.println("c #vars: " + this.gator.nVars() + " #constraints: "
                + this.gator.nConstraints());
        IVecInt fixedLiterals = new VecInt(bitsLiterals.size());
        IVecInt nAssumpts = new VecInt(assumps.size() + bitsLiterals.size());
        boolean result;
        for (int litIndex = bitsLiterals.size() - 1; litIndex >= 0;) {
            assumps.copyTo(nAssumpts);
            fixedLiterals.copyTo(nAssumpts);
            nAssumpts.push(-bitsLiterals.get(litIndex));
            for (int j = litIndex - 1; j >= 0; j--) {
                nAssumpts.push(bitsLiterals.get(j));
            }
            System.out.println("c assumptions " + nAssumpts);
            result = this.gator.isSatisfiable(nAssumpts, true);
            if (result) {
                // int var = bitsLiterals.get(litIndex);
                // while (!gator.model(var)) {
                // fixedLiterals.push(-var);
                // if (litIndex == 0) {
                // litIndex--;
                // break;
                // }
                // var = bitsLiterals.get(--litIndex);
                // }
                fixedLiterals.push(-bitsLiterals.get(litIndex--));
                Number value = this.objfct.calculateDegree(this.gator);
                System.out.println("o " + value);
                System.out.println("c current objective value with fixed lits "
                        + fixedLiterals);
            } else {
                fixedLiterals.push(bitsLiterals.get(litIndex--));
                System.out.println("c unsat. fixed lits " + fixedLiterals);
            }
            nAssumpts.clear();
        }
        assert fixedLiterals.size() == bitsLiterals.size();
        assumps.copyTo(nAssumpts);
        fixedLiterals.copyTo(nAssumpts);
        return this.gator.isSatisfiable(nAssumpts);
    }

    public static void main(String[] args) {
        PseudoBitsAdderDecorator decorator = new PseudoBitsAdderDecorator(
                SolverFactory.newDefault());
        decorator.setVerbose(false);
        OPBReader2007 reader = new OPBReader2007(decorator);
        long begin = System.currentTimeMillis();
        try {
            IProblem problem = reader.parseInstance(args[0]);
            if (problem.isSatisfiable()) {
                System.out.println("s OPTIMUM FOUND");
                System.out.println("v " + reader.decode(problem.model()));
                if (decorator.objfct != null) {
                    System.out
                            .println("c objective function="
                                    + decorator.objfct
                                            .calculateDegree(decorator.gator));
                }
            } else {
                System.out.println("s UNSAT");
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ContradictionException e) {
            System.out.println("s UNSAT");
            System.out.println("c trivial inconsistency");
        } catch (TimeoutException e) {
            System.out.println("s UNKNOWN");
        }
        long end = System.currentTimeMillis();
        System.out.println("c Total wall clock time: " + (end - begin) / 1000.0
                + " seconds");
    }

    public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
            boolean moreThan, BigInteger d) throws ContradictionException {
        return this.solver.addPseudoBoolean(lits, coeffs, moreThan, d);
    }

    public ObjectiveFunction getObjectiveFunction() {
        return this.objfct;
    }

    public IConstr addAtMost(IVecInt literals, IVecInt coeffs, int degree)
            throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public IConstr addAtMost(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger degree) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public IConstr addAtLeast(IVecInt literals, IVecInt coeffs, int degree)
            throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public IConstr addAtLeast(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger degree) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public IConstr addExactly(IVecInt literals, IVecInt coeffs, int weight)
            throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public IConstr addExactly(IVecInt literals, IVec<BigInteger> coeffs,
            BigInteger weight) throws ContradictionException {
        throw new UnsupportedOperationException();
    }
}
