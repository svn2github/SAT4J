/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
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
* 
* Based on the original MiniSat specification from:
* 
* An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
* Sixth International Conference on Theory and Applications of Satisfiability
* Testing, LNCS 2919, pp 502-518, 2003.
*
* See www.minisat.se for the original solver in C++.
* 
*******************************************************************************/
package org.sat4j.specs;


/**
 * Represents an optimization problem. The SAT solver will find suboptimal solutions
 * of the problem until no more solutions are available. The latest solution found 
 * will be the optimal one.
 * 
 * Such kind of problem is supposed to be handled:
 * <pre> 
        boolean isSatisfiable = false;

        IOptimizationProblem optproblem = (IOptimizationProblem) problem;

        try {
            while (optproblem.admitABetterSolution()) {
                if (!isSatisfiable) {
                    if (optproblem.nonOptimalMeansSatisfiable()) {
                        setExitCode(ExitCode.SATISFIABLE);
                        if (optproblem.hasNoObjectiveFunction()) {
                            return;
                        }
                        log("SATISFIABLE"); //$NON-NLS-1$
                    }
                    isSatisfiable = true;
                    log("OPTIMIZING..."); //$NON-NLS-1$
                }
                log("Got one! Elapsed wall clock time (in seconds):" //$NON-NLS-1$
                        + (System.currentTimeMillis() - getBeginTime())
                        / 1000.0);
                getLogWriter().println(
                        CURRENT_OPTIMUM_VALUE_PREFIX
                                + optproblem.calculateObjective());
                optproblem.discard();
            }
            if (isSatisfiable) {
                setExitCode(ExitCode.OPTIMUM_FOUND);
            } else {
                setExitCode(ExitCode.UNSATISFIABLE);
            }
        } catch (ContradictionException ex) {
            assert isSatisfiable;
            setExitCode(ExitCode.OPTIMUM_FOUND);
        }
  </pre>
 * 
 * @author leberre
 *
 */
public interface IOptimizationProblem extends IProblem {

    /**
     * Look for a solution of the optimization problem.
     * 
     * @return true if a better solution than current one can be found. 
     * @throws TimeoutException if the solver cannot answer in reasonable time.
     * @see ISolver#setTimeout(int)
     */
    boolean admitABetterSolution() throws TimeoutException;

    /**
     * If the optimization problem has no objective function, then it is a simple
     * decision problem.
     * 
     * @return true if the problem is a decision problem, false if the problem is 
     * an optimization problem.
     */
    boolean hasNoObjectiveFunction();

    /**
     * A suboptimal solution has different meaning depending of the optimization problem
     * considered.
     * 
     * For instance, in the case of MAXSAT, a suboptimal solution does not mean that the problem is 
     * satisfiable, while in pseudo boolean optimization, it is true.
     * 
     * @return true if founding a suboptimal solution means that the problem is satisfiable. 
     */
    boolean nonOptimalMeansSatisfiable();

    /**
     * Compute the value of the objective function for the current solution.
     * A call to that method only makes sense if hasNoObjectiveFunction()==false.
     * 
     * @return the value of the objective function.
     */
    Number calculateObjective();

    /**
     * Discard the current solution in the optimization problem.
     * 
     * @throws ContradictionException if a trivial inconsistency is detected.
     */
    void discard() throws ContradictionException;

}
