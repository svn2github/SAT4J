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

import org.sat4j.core.Vec;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ILogAble;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.Backbone;
import org.sat4j.tools.LexicoDecorator;
import org.sat4j.tools.SolutionFoundListener;

/**
 * Allow to change the behavior of the launcher (either decision or optimization
 * mode)
 * 
 * @since 2.3.3
 * @author sroussel
 * 
 */
public interface ILauncherMode extends SolutionFoundListener {

    String SOLUTION_PREFIX = "v "; //$NON-NLS-1$

    String ANSWER_PREFIX = "s "; //$NON-NLS-1$

    String CURRENT_OPTIMUM_VALUE_PREFIX = "o ";

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
     * @param reader
     *            the problem reader
     * @param beginTime
     *            the time at which the solver was launched
     * @param displaySolutionLine
     *            indicates whether the solution line shound be displayed or not
     *            (not recommended for large solutions)
     */
    void displayResult(ISolver solver, IProblem problem, ILogAble logger,
            PrintWriter out, Reader reader, long beginTime,
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
    void solve(IProblem problem, Reader reader, ILogAble logger,
            PrintWriter out, long beginTime);

    /**
     * Allows the launcher to specifically return an upper bound of the optimal
     * solution in case of a time out (for maxsat competitions for instance).
     * 
     * @param isIncomplete
     */
    void setIncomplete(boolean isIncomplete);

    /**
     * Allow the launcher to get the current status of the problem: SAT, UNSAT,
     * UPPER_BOUND, etc.
     * 
     * @return
     */
    ExitCode getCurrentExitCode();

    /**
     * Allow to set a specific exit code to the launcher (in case of trivial
     * unsatisfiability for instance).
     */
    void setExitCode(ExitCode exitCode);

    /**
     * The launcher is in decision mode: the answer is either SAT, UNSAT or
     * UNKNOWN
     */
    ILauncherMode DECISION = new ILauncherMode() {

        private ExitCode exitCode = ExitCode.UNKNOWN;

        public void displayResult(ISolver solver, IProblem problem,
                ILogAble logger, PrintWriter out, Reader reader,
                long beginTime, boolean displaySolutionLine) {
            if (solver != null) {
                out.flush();
                double wallclocktime = (System.currentTimeMillis() - beginTime) / 1000.0;
                solver.printStat(out);
                out.println(ANSWER_PREFIX + exitCode);
                if (exitCode != ExitCode.UNKNOWN
                        && exitCode != ExitCode.UNSATISFIABLE) {
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
                    if (System.getProperty("backbone") != null) {
                        logger.log("computing the backbone of the formula ...");
                        long beginpi = System.currentTimeMillis();
                        model = solver.primeImplicant();
                        try {
                            IVecInt backbone = Backbone.compute(solver, model);
                            long endpi = System.currentTimeMillis();
                            out.print(solver.getLogPrefix());
                            reader.decode(backbone.toArray(), out);
                            out.println();
                            logger.log("backbone computation time: "
                                    + (endpi - beginpi) + " ms");
                        } catch (TimeoutException e) {
                            logger.log("timeout, cannot compute the backbone.");
                        }

                    }
                    if (nbSolutionFound >= 1) {
                        logger.log("Found " + nbSolutionFound + " solution(s)");
                    }
                    out.print(SOLUTION_PREFIX);
                    reader.decode(model, out);
                    out.println();
                }
                logger.log("Total wall clock time (in seconds) : " + wallclocktime); //$NON-NLS-1$
            }
        }

        private int nbSolutionFound;

        private PrintWriter out;
        private long beginTime;

        public void solve(IProblem problem, Reader reader, ILogAble logger,
                PrintWriter out, long beginTime) {
            this.exitCode = ExitCode.UNKNOWN;
            this.out = out;
            this.nbSolutionFound = 0;
            this.beginTime = beginTime;
            try {
                if (problem.isSatisfiable()) {
                    if (this.exitCode == ExitCode.UNKNOWN) {
                        this.exitCode = ExitCode.SATISFIABLE;
                    }
                } else {
                    if (this.exitCode == ExitCode.UNKNOWN) {
                        this.exitCode = ExitCode.UNSATISFIABLE;
                    }
                }
            } catch (TimeoutException e) {
                logger.log("timeout");
            }

        }

        public void setIncomplete(boolean isIncomplete) {
        }

        public ExitCode getCurrentExitCode() {
            return this.exitCode;
        };

        public void onSolutionFound(int[] solution) {
            this.nbSolutionFound++;
            this.exitCode = ExitCode.SATISFIABLE;
            this.out.printf("c Found solution #%d  (%.2f)s%n", nbSolutionFound,
                    (System.currentTimeMillis() - beginTime) / 1000.0);
        }

        public void onSolutionFound(IVecInt solution) {
            throw new UnsupportedOperationException("Not implemented yet!");
        }

        public void onUnsatTermination() {
            if (this.exitCode == ExitCode.SATISFIABLE) {
                this.exitCode = ExitCode.OPTIMUM_FOUND;
            }
        }

        public void setExitCode(ExitCode exitCode) {
            this.exitCode = exitCode;
        }
    };

    /**
     * The launcher is in optimization mode: the answer is either SAT,
     * UPPER_BOUND, OPTIMUM_FOUND, UNSAT or UNKNOWN. Using the incomplete
     * property, the solver returns an upper bound of the optimal solution when
     * a time out occurs.
     */
    ILauncherMode OPTIMIZATION = new ILauncherMode() {

        private int nbSolutions;

        private ExitCode exitCode = ExitCode.UNKNOWN;

        private boolean isIncomplete = false;

        public void setIncomplete(boolean isIncomplete) {
            this.isIncomplete = isIncomplete;
        }

        public void displayResult(ISolver solver, IProblem problem,
                ILogAble logger, PrintWriter out, Reader reader,
                long beginTime, boolean displaySolutionLine) {
            if (solver == null) {
                return;
            }
            System.out.flush();
            out.flush();
            solver.printStat(out);
            out.println(ANSWER_PREFIX + exitCode);
            if (exitCode == ExitCode.SATISFIABLE
                    || exitCode == ExitCode.OPTIMUM_FOUND || isIncomplete
                    && exitCode == ExitCode.UPPER_BOUND) {
                assert this.nbSolutions > 0;
                logger.log("Found " + this.nbSolutions + " solution(s)");

                if (displaySolutionLine) {
                    out.print(SOLUTION_PREFIX);
                    reader.decode(problem.model(), out);
                    out.println();
                }
                IOptimizationProblem optproblem = (IOptimizationProblem) problem;
                if (!optproblem.hasNoObjectiveFunction()) {
                    String objvalue;
                    if (optproblem instanceof LexicoDecorator<?>) {
                        IVec<Number> values = new Vec<Number>();
                        LexicoDecorator<?> lexico = (LexicoDecorator<?>) optproblem;
                        for (int i = 0; i < lexico.numberOfCriteria(); i++) {
                            values.push(lexico.getObjectiveValue(i));
                        }
                        objvalue = values.toString();

                    } else {
                        objvalue = optproblem.getObjectiveValue().toString();
                    }
                    logger.log("objective function=" + objvalue); //$NON-NLS-1$
                }
            }

            logger.log("Total wall clock time (in seconds): " //$NON-NLS-1$
                    + (System.currentTimeMillis() - beginTime) / 1000.0);
        }

        public void solve(IProblem problem, Reader reader, ILogAble logger,
                PrintWriter out, long beginTime) {
            boolean isSatisfiable = false;
            this.nbSolutions = 0;
            IOptimizationProblem optproblem = (IOptimizationProblem) problem;
            exitCode = ExitCode.UNKNOWN;

            try {
                while (optproblem.admitABetterSolution()) {
                    ++this.nbSolutions;
                    if (!isSatisfiable) {
                        if (optproblem.nonOptimalMeansSatisfiable()) {
                            logger.log("SATISFIABLE");
                            exitCode = ExitCode.SATISFIABLE;
                            if (optproblem.hasNoObjectiveFunction()) {
                                return;
                            }
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
                    exitCode = ExitCode.OPTIMUM_FOUND;
                } else {
                    exitCode = ExitCode.UNSATISFIABLE;
                }
            } catch (ContradictionException ex) {
                assert isSatisfiable;
                exitCode = ExitCode.OPTIMUM_FOUND;
            } catch (TimeoutException e) {
                logger.log("timeout");
            }

        }

        public ExitCode getCurrentExitCode() {
            return exitCode;
        }

        public void onSolutionFound(int[] solution) {
            throw new UnsupportedOperationException("Not implemented yet!");
        }

        public void onSolutionFound(IVecInt solution) {
            throw new UnsupportedOperationException("Not implemented yet!");
        }

        public void onUnsatTermination() {
            // do nothing
        }

        public void setExitCode(ExitCode exitCode) {
            this.exitCode = exitCode;
        }
    };

}
