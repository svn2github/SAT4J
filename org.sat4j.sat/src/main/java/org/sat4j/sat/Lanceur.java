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
package org.sat4j.sat;

/**
 * This class is used to launch the SAT solvers from the command line. It is
 * compliant with the SAT competition (www.satcompetition.org) I/O format. The
 * launcher is to be used as follows:
 * 
 * <pre>
 *                [solvername] filename [key=value]*
 * </pre>
 * 
 * If no solver name is given, then the default solver of the solver factory is
 * used (@see org.sat4j.core.ASolverFactory#defaultSolver()).
 * 
 * This class is no longer used since 2.3.3 because it cannot launch maxsat problems.
 * 
 * @see Launcher
 * 
 * @author sroussel
 * 
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.AbstractLauncher;
import org.sat4j.ExitCode;
import org.sat4j.ILauncherMode;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.core.IPBCDCLSolver;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ILogAble;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ConflictDepthTracing;
import org.sat4j.tools.ConflictLevelTracing;
import org.sat4j.tools.DecisionTracing;
import org.sat4j.tools.DotSearchTracing;
import org.sat4j.tools.FileBasedVisualizationTool;
import org.sat4j.tools.LearnedClausesSizeTracing;
import org.sat4j.tools.MultiTracing;

@Deprecated
public class Lanceur extends AbstractLauncher implements ILogAble {

    private static final String NUMBER = "number";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final String CURRENT_OPTIMUM_VALUE_PREFIX = "o "; //$NON-NLS-1$

    private boolean incomplete = false;

    private boolean isModeOptimization = false;

    private IProblem problem;

    private boolean modeTracing = false;

    private boolean launchRemoteControl;

    private static AbstractLauncher lanceur;

    public static void main(final String[] args) {
        lanceur = new Lanceur();
        lanceur.run(args);

    }

    private String filename;

    private int k = -1;

    /**
     * Configure the solver according to the command line parameters.
     * 
     * @param args
     *            the command line
     * @return a solver properly configured.
     */
    @SuppressWarnings({ "nls", "unchecked" })
    @Override
    protected ICDCL configureSolver(String[] args) {
        Options options = createCLIOptions();

        try {
            CommandLine cmd = new PosixParser().parse(options, args);

            if (cmd.hasOption("opt")) {
                this.isModeOptimization = true;
            }

            String framework = cmd.getOptionValue("l"); //$NON-NLS-1$
            if (this.isModeOptimization) {
                framework = "pb";
            } else if (framework == null) { //$NON-NLS-1$
                framework = "minisat";
            }

            try {
                Class<?> clazz = Class
                        .forName("org.sat4j." + framework + ".SolverFactory"); //$NON-NLS-1$ //$NON-NLS-2$
                Class<?>[] params = {};
                Method m = clazz.getMethod("instance", params); //$NON-NLS-1$
            } catch (Exception e) { // DLB Findbugs warning ok
                log("Wrong framework: " + framework
                        + ". Using minisat instead.");
            }

            ICDCL asolver = Solvers.configureSolver(args, this);

            this.launchRemoteControl = cmd.hasOption("remote");

            this.filename = cmd.getOptionValue("f");

            if (cmd.hasOption("d")) {
                String dotfilename = null;
                if (this.filename != null) {
                    dotfilename = cmd.getOptionValue("d");
                }
                if (dotfilename == null) {
                    dotfilename = "sat4j.dot";
                }
                asolver.setSearchListener(new DotSearchTracing(dotfilename,
                        null));
            }

            if (cmd.hasOption("m")) {
                setSilent(true);
            }

            if (cmd.hasOption("k")) {
                Integer myk = Integer.valueOf(cmd.getOptionValue("k"));
                if (myk != null) {
                    this.k = myk.intValue();
                }
            }

            if (this.isModeOptimization) {
                assert asolver instanceof IPBSolver;
                this.problem = new PseudoOptDecorator((IPBCDCLSolver) asolver);
            }

            int others = 0;
            String[] rargs = cmd.getArgs();
            if (this.filename == null && rargs.length > 0) {
                this.filename = rargs[others++];
            }

            if (cmd.hasOption("r")) {
                this.modeTracing = true;
                if (!cmd.hasOption("remote")) {
                    asolver.setSearchListener(new MultiTracing(
                            new ConflictLevelTracing(
                                    new FileBasedVisualizationTool(
                                            this.filename + "-conflict-level"),
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-conflict-level-restart"),
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-conflict-level-clean")),
                            new DecisionTracing(
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-decision-indexes-pos"),
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-decision-indexes-neg"),
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-decision-indexes-restart"),
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-decision-indexes-clean")),
                            new LearnedClausesSizeTracing(
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-learned-clauses-size"),
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-learned-clauses-size-restart"),
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-learned-clauses-size-clean")),
                            new ConflictDepthTracing(
                                    new FileBasedVisualizationTool(
                                            this.filename + "-conflict-depth"),
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-conflict-depth-restart"),
                                    new FileBasedVisualizationTool(
                                            this.filename
                                                    + "-conflict-depth-clean"))));
                }
            }

            // use remaining data to configure the solver
            while (others < rargs.length) {
                String[] param = rargs[others].split("="); //$NON-NLS-1$
                assert param.length == 2;
                log("setting " + param[0] + " to " + param[1]); //$NON-NLS-1$ //$NON-NLS-2$
                try {
                    BeanUtils.setProperty(asolver, param[0], param[1]);
                } catch (Exception e) {
                    log("Cannot set parameter : " //$NON-NLS-1$
                            + args[others]);
                }
                others++;
            }

            getLogWriter().println(asolver.toString(COMMENT_PREFIX)); //$NON-NLS-1$
            return asolver;
        } catch (ParseException e1) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("java -jar sat4j.jar", options, true);
            usage();
        }
        return null;
    }

    @Override
    protected Reader createReader(ISolver theSolver, String problemname) {
        if (theSolver instanceof IPBSolver) {
            return new PBInstanceReader((IPBSolver) theSolver);
        }
        return new InstanceReader(theSolver);
    }

    @Override
    public void displayLicense() {
        super.displayLicense();
        log("This software uses some libraries from the Jakarta Commons project. See jakarta.apache.org for details."); //$NON-NLS-1$
    }

    @Override
    protected String getInstanceName(String[] args) {
        return this.filename;
    }

    @Override
    protected IProblem readProblem(String problemname)
            throws ParseFormatException, IOException, ContradictionException {
        ISolver theSolver = (ISolver) super.readProblem(problemname);
        if (this.k > 0) {
            IVecInt literals = new VecInt();
            for (int i = 1; i <= theSolver.nVars(); i++) {
                literals.push(-i);
            }
            theSolver.addAtLeast(literals, this.k);
            log("Limiting solutions to those having at least " + this.k
                    + " variables assigned to false");
        }
        return theSolver;
    }

    @Override
    protected void solve(IProblem problem) throws TimeoutException {
        if (this.isModeOptimization) {
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
                        } else if (this.incomplete) {
                            setExitCode(ExitCode.UPPER_BOUND);
                        }
                        isSatisfiable = true;
                        log("OPTIMIZING..."); //$NON-NLS-1$
                    }
                    log("Got one! Elapsed wall clock time (in seconds):" //$NON-NLS-1$
                            + (System.currentTimeMillis() - getBeginTime())
                            / 1000.0);
                    getLogWriter().println(
                            CURRENT_OPTIMUM_VALUE_PREFIX
                                    + optproblem.getObjectiveValue());
                    optproblem.discardCurrentSolution();
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
        } else {
            this.exitCode = problem.isSatisfiable() ? ExitCode.SATISFIABLE
                    : ExitCode.UNSATISFIABLE;
        }
    }

    @Override
    protected void displayResult() {
        if (this.isModeOptimization) {
            displayAnswer();

            log("Total wall clock time (in seconds): " //$NON-NLS-1$
                    + (System.currentTimeMillis() - getBeginTime()) / 1000.0);
        } else {
            super.displayResult();
        }
    }

    protected void displayAnswer() {
        if (this.solver == null) {
            return;
        }
        System.out.flush();
        PrintWriter out = getLogWriter();
        out.flush();
        this.solver.printStat(out, COMMENT_PREFIX);
        this.solver.printInfos(out, COMMENT_PREFIX);
        ExitCode exitCode = getExitCode();
        out.println(ILauncherMode.ANSWER_PREFIX + exitCode);
        if (exitCode == ExitCode.SATISFIABLE
                || exitCode == ExitCode.OPTIMUM_FOUND || this.incomplete
                && exitCode == ExitCode.UPPER_BOUND) {
            out.print(ILauncherMode.SOLUTION_PREFIX);
            getReader().decode(this.problem.model(), out);
            out.println();
            if (this.isModeOptimization) {
                IOptimizationProblem optproblem = (IOptimizationProblem) this.problem;
                if (!optproblem.hasNoObjectiveFunction()) {
                    log("objective function=" + optproblem.getObjectiveValue()); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
    public void run(String[] args) {
        try {
            displayHeader();
            this.solver = configureSolver(args);
            if (this.solver == null) {
                usage();
                return;
            }
            if (!this.silent) {
                this.solver.setVerbose(true);
            }
            String instanceName = getInstanceName(args);
            if (instanceName == null) {
                usage();
                return;
            }
            this.beginTime = System.currentTimeMillis();
            if (!this.launchRemoteControl) {
                readProblem(instanceName);
                try {
                    if (this.problem != null) {
                        solve(this.problem);
                    } else {
                        solve(this.solver);
                    }
                } catch (TimeoutException e) {
                    log("timeout"); //$NON-NLS-1$
                }
                System.exit(lanceur.getExitCode().value());
            } else {
                RemoteControlFrame frame = new RemoteControlFrame(
                        this.filename, "", args);
                frame.activateTracing(this.modeTracing);
                frame.setOptimisationMode(this.isModeOptimization);
            }
        } catch (FileNotFoundException e) {
            System.err.println("FATAL " + e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println("FATAL " + e.getLocalizedMessage());
        } catch (ContradictionException e) {
            this.exitCode = ExitCode.UNSATISFIABLE;
            log("(trivial inconsistency)"); //$NON-NLS-1$
        } catch (ParseFormatException e) {
            System.err.println("FATAL " + e.getLocalizedMessage());
        }
    }

    @Override
    public void usage() {
        Solvers.usage(this);
    }

    public static Options createCLIOptions() {
        Options options = new Options();
        options.addOption("l", "library", true,
                "specifies the name of the library used (minisat by default)");
        options.addOption("s", "solver", true,
                "specifies the name of a prebuilt solver from the library");
        options.addOption("S", "Solver", true,
                "setup a solver using a solver config string");
        options.addOption("t", "timeout", true,
                "specifies the timeout (in seconds)");
        options.addOption("T", "timeoutms", true,
                "specifies the timeout (in milliseconds)");
        options.addOption("C", "conflictbased", false,
                "conflict based timeout (for deterministic behavior)");
        options.addOption("d", "dot", true,
                "creates a sat4j.dot file in current directory representing the search");
        options.addOption("f", "filename", true,
                "specifies the file to use (in conjunction with -d for instance)");
        options.addOption("m", "mute", false, "Set launcher in silent mode");
        options.addOption("k", "kleast", true,
                "limit the search to models having at least k variables set to false");
        options.addOption("r", "trace", false,
                "traces the behavior of the solver");
        options.addOption("opt", "optimize", false,
                "uses solver in optimize mode instead of sat mode (default)");
        options.addOption("rw", "randomWalk", true,
                "specifies the random walk probability ");
        options.addOption("remote", "remoteControl", false,
                "launches remote control");
        options.addOption("H", "hot", false,
                "keep the solver hot (do not reset heuristics) when a model is found");
        options.addOption("y", "simplify", false,
                "simplify the set of clauses is possible");
        Option op = options.getOption("l");
        op.setArgName("libname");
        op = options.getOption("s");
        op.setArgName("solvername");
        op = options.getOption("S");
        op.setArgName("solverStringDefinition");
        op = options.getOption("t");
        op.setArgName(NUMBER);
        op = options.getOption("T");
        op.setArgName(NUMBER);
        op = options.getOption("C");
        op.setArgName(NUMBER);
        op = options.getOption("k");
        op.setArgName(NUMBER);
        op = options.getOption("d");
        op.setArgName("filename");
        op = options.getOption("f");
        op.setArgName("filename");
        op = options.getOption("r");
        op.setArgName("searchlistener");
        op = options.getOption("rw");
        op.setArgName(NUMBER);
        return options;
    }

}
