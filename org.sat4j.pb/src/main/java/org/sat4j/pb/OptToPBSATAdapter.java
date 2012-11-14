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

import java.io.PrintWriter;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * Utility class to use optimization solvers instead of simple SAT solvers in
 * code meant for SAT solvers.
 * 
 * @author daniel
 */
public class OptToPBSATAdapter extends PBSolverDecorator {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    IOptimizationProblem problem;

    boolean modelComputed = false;

    private final IVecInt assumps = new VecInt();

    private long begin;

    public OptToPBSATAdapter(IOptimizationProblem problem) {
        super((IPBSolver) problem);
        this.problem = problem;
    }

    @Override
    public boolean isSatisfiable() throws TimeoutException {
        this.modelComputed = false;
        this.assumps.clear();
        this.begin = System.currentTimeMillis();
        if (this.problem.hasNoObjectiveFunction()) {
            return this.modelComputed = this.problem.isSatisfiable();
        }
        return this.problem.admitABetterSolution();
    }

    @Override
    public boolean isSatisfiable(boolean global) throws TimeoutException {
        return isSatisfiable();
    }

    @Override
    public boolean isSatisfiable(IVecInt myAssumps, boolean global)
            throws TimeoutException {
        return isSatisfiable(myAssumps);
    }

    @Override
    public boolean isSatisfiable(IVecInt myAssumps) throws TimeoutException {
        this.modelComputed = false;
        this.assumps.clear();
        myAssumps.copyTo(this.assumps);
        this.begin = System.currentTimeMillis();
        if (this.problem.hasNoObjectiveFunction()) {
            return this.modelComputed = this.problem.isSatisfiable(myAssumps);
        }
        return this.problem.admitABetterSolution(myAssumps);
    }

    @Override
    public int[] model() {
        return model(new PrintWriter(System.out, true));
    }

    /**
     * Compute a minimal model according to the objective function of the
     * IPBProblem decorated.
     * 
     * @param out
     *            a writer to display information in verbose mode
     * @since 2.3.2
     */
    public int[] model(PrintWriter out) {
        if (this.modelComputed) {
            return this.problem.model();
        }
        try {
            assert this.problem.admitABetterSolution(this.assumps);
            assert !this.problem.hasNoObjectiveFunction();
            do {
                this.problem.discardCurrentSolution();
                if (isVerbose()) {
                    out.println(getLogPrefix()
                            + "Current objective function value: "
                            + this.problem.getObjectiveValue() + "("
                            + (System.currentTimeMillis() - this.begin)
                            / 1000.0 + "s)");
                }
            } while (this.problem.admitABetterSolution(this.assumps));
            if (isVerbose()) {
                out.println(getLogPrefix()
                        + "Optimal objective function value: "
                        + this.problem.getObjectiveValue() + "("
                        + (System.currentTimeMillis() - this.begin) / 1000.0
                        + "s)");
            }
        } catch (TimeoutException e) {
            if (isVerbose()) {
                out.println(getLogPrefix() + "Solver timed out after "
                        + (System.currentTimeMillis() - this.begin) / 1000.0
                        + "s)");
            }
        } catch (ContradictionException e) {
            // OK, optimal model found
        }
        this.modelComputed = true;
        return this.problem.model();
    }

    @Override
    public boolean model(int var) {
        if (!this.modelComputed) {
            model();
        }
        return this.problem.model(var);
    }

    @Override
    public String toString(String prefix) {
        return prefix + "Optimization to Pseudo Boolean adapter\n"
                + super.toString(prefix);
    }

    public boolean isOptimal() {
        return this.problem.isOptimal();
    }

    /**
     * Return the value of the objective function in the last model found.
     * 
     * @return
     * @since 2.3.2
     */
    public Number getCurrentObjectiveValue() {
        return this.problem.getObjectiveValue();
    }

    /**
     * Allow to set a specific timeout when the solver is in optimization mode.
     * The solver internal timeout will be set to that value once it has found a
     * solution. That way, the original timeout of the solver may be reduced if
     * the solver finds quickly a solution, or increased if the solver finds
     * regularly new solutions (thus giving more time to the solver each time).
     * 
     * @see IOptimizationProblem#setTimeoutForFindingBetterSolution(int)
     */
    public void setTimeoutForFindingBetterSolution(int seconds) {
        this.problem.setTimeoutForFindingBetterSolution(seconds);
    }
}
