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
package org.sat4j;

import java.io.PrintWriter;

import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * Allow to change the behavior of the launcher (either decision or optimization
 * mode)
 * 
 * @since 2.3.3
 * @author sroussel
 * 
 */
public interface ILauncherMode {

    public static final String SOLUTION_PREFIX = "v "; //$NON-NLS-1$

    public static final String ANSWER_PREFIX = "s "; //$NON-NLS-1$

    public static final String CURRENT_OPTIMUM_VALUE_PREFIX = "o ";

    /**
     * Output of the launcher when the solver stops
     * 
     * @param solver
     *            the solver that is launched by the launcher
     * @param problem
     *            the problem that is solved
     * @param logger
     *            the element that is able to log the result
     * @param out
     *            the printwriter to associate to the solver
     * @param exitCode
     *            the status of the solver's result
     * @param reader
     *            the problem reader
     * @param beginTime
     *            the time at which the solver was launched
     * @param displaySolutionLine
     *            indicates whether the solution line shound be displayed or not
     *            (not recommended for large solutions)
     */
    void displayResult(ISolver solver, IProblem problem, ILogAble logger,
            PrintWriter out, ExitCode exitCode, Reader reader, long beginTime,
            boolean displaySolutionLine);

    /**
     * Main solver call: one call for a decision problem, a loop for an
     * optimization problem.
     * 
     * @param problem
     *            the problem to solve
     * @param logger
     *            the element that is able to log the result
     * @param out
     *            the printwriter to associate to the solver
     * @param beginTime
     *            the time at which the solver starts
     * @return
     */
    ExitCode solve(IProblem problem, ILogAble logger, PrintWriter out,
            long beginTime);

    /**
     * Allows the launcher to specifically return an upper bound of the optimal
     * solution in case of a time out (for maxsat competitions for instance).
     * 
     * @param isIncomplete
     */
    void setIncomplete(boolean isIncomplete);

    /**
     * The launcher is in decision mode: the answer is either SAT, UNSAT or
     * UNKNOWN
     */
    ILauncherMode DECISION = new ILauncherMode() {
        public void displayResult(ISolver solver, IProblem problem,
                ILogAble logger, PrintWriter out, ExitCode exitCode,
                Reader reader, long beginTime, boolean displaySolutionLine) {
            if (solver != null) {
                out.flush();
                double wallclocktime = (System.currentTimeMillis() - beginTime) / 1000.0;
                solver.printStat(out, solver.getLogPrefix());
                solver.printInfos(out, solver.getLogPrefix());
                out.println(ANSWER_PREFIX + exitCode);
                if (exitCode == ExitCode.SATISFIABLE) {
                    int[] model = solver.model();
                    if (System.getProperty("prime") != null) {
                        int initiallength = model.length;
                        logger.log("returning a prime implicant ...");
                        long beginpi = System.currentTimeMillis();
                        model = solver.primeImplicant();
                        long endpi = System.currentTimeMillis();
                        logger.log("removed " + (initiallength - model.length)
                                + " literals");
                        logger.log("pi computation time: " + (endpi - beginpi)
                                + " ms");
                    }
                    out.print(SOLUTION_PREFIX);
                    reader.decode(model, out);
                    out.println();
                }
                logger.log("Total wall clock time (in seconds) : " + wallclocktime); //$NON-NLS-1$
            }
        }

        public ExitCode solve(IProblem problem, ILogAble logger,
                PrintWriter out, long beginTime) {
            try {
                if (problem.isSatisfiable()) {
                    return ExitCode.SATISFIABLE;
                }
                return ExitCode.UNSATISFIABLE;
            } catch (TimeoutException e) {
                logger.log("timeout");
                return ExitCode.UNKNOWN;
            }

        }

        public void setIncomplete(boolean isIncomplete) {
        };
    };

    /**
     * The launcher is in optimization mode: the answer is either SAT,
     * UPPER_BOUND, OPTIMUM_FOUND, UNSAT or UNKNOWN. Using the incomplete
     * property, the solver returns an upper bound of the optimal solution when
     * a time out occurs.
     */
    ILauncherMode OPTIMIZATION = new ILauncherMode() {

        private boolean isIncomplete = false;

        public void setIncomplete(boolean isIncomplete) {
            this.isIncomplete = isIncomplete;
        }

        public void displayResult(ISolver solver, IProblem problem,
                ILogAble logger, PrintWriter out, ExitCode exitCode,
                Reader reader, long beginTime, boolean displaySolutionLine) {
            if (solver == null) {
                return;
            }
            System.out.flush();
            out.flush();
            solver.printStat(out, solver.getLogPrefix());
            solver.printInfos(out, solver.getLogPrefix());
            out.println(ANSWER_PREFIX + exitCode);
            if (exitCode == ExitCode.SATISFIABLE
                    || exitCode == ExitCode.OPTIMUM_FOUND || isIncomplete
                    && exitCode == ExitCode.UPPER_BOUND) {
                if (displaySolutionLine) {
                    out.print(SOLUTION_PREFIX);
                    reader.decode(solver.model(), out);
                    out.println();
                }
                IOptimizationProblem optproblem = (IOptimizationProblem) problem;
                if (!optproblem.hasNoObjectiveFunction()) {
                    logger.log("objective function=" + optproblem.getObjectiveValue()); //$NON-NLS-1$
                }
            }

            logger.log("Total wall clock time (in seconds): " //$NON-NLS-1$
                    + (System.currentTimeMillis() - beginTime) / 1000.0);
        }

        public ExitCode solve(IProblem problem, ILogAble logger,
                PrintWriter out, long beginTime) {
            boolean isSatisfiable = false;
            IOptimizationProblem optproblem = (IOptimizationProblem) problem;
            ExitCode exitCode = ExitCode.UNKNOWN;

            try {
                while (optproblem.admitABetterSolution()) {
                    if (!isSatisfiable) {
                        if (optproblem.nonOptimalMeansSatisfiable()) {
                            logger.log("SATISFIABLE");
                            if (optproblem.hasNoObjectiveFunction()) {

                                return ExitCode.SATISFIABLE;
                            }
                            exitCode = ExitCode.SATISFIABLE;
                        } else if (isIncomplete) {
                            exitCode = ExitCode.UPPER_BOUND;
                        }
                        isSatisfiable = true;
                        logger.log("OPTIMIZING..."); //$NON-NLS-1$
                    }
                    logger.log("Got one! Elapsed wall clock time (in seconds):" //$NON-NLS-1$
                            + (System.currentTimeMillis() - beginTime) / 1000.0);
                    out.println(CURRENT_OPTIMUM_VALUE_PREFIX
                            + optproblem.getObjectiveValue());
                    optproblem.discardCurrentSolution();
                }
                if (isSatisfiable) {
                    return ExitCode.OPTIMUM_FOUND;
                } else {
                    return ExitCode.UNSATISFIABLE;
                }
            } catch (ContradictionException ex) {
                assert isSatisfiable;
                return ExitCode.OPTIMUM_FOUND;
            } catch (TimeoutException e) {
                logger.log("timeout");
                return exitCode;
            }

        }
    };

}