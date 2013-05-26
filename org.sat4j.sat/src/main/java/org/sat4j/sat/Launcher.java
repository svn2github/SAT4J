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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.AbstractLauncher;
import org.sat4j.ExitCode;
import org.sat4j.ILauncherMode;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.maxsat.reader.MSInstanceReader;
import org.sat4j.pb.ConstraintRelaxingPseudoOptDecorator;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.core.IPBCDCLSolver;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ILogAble;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ConflictDepthTracing;
import org.sat4j.tools.ConflictLevelTracing;
import org.sat4j.tools.DecisionTracing;
import org.sat4j.tools.FileBasedVisualizationTool;
import org.sat4j.tools.LearnedClausesSizeTracing;
import org.sat4j.tools.MultiTracing;

/**
 * 
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
 * This class allows to solve sat, pb and maxsat problems.
 * 
 * @author sroussel
 * @since 2.3.3
 */
public class Launcher extends AbstractLauncher implements ILogAble {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private boolean isModeOptimization = false;

    private boolean modeTracing = false;

    private boolean launchRemoteControl;

    private static AbstractLauncher launcher;

    public static void main(final String[] args) {
        launcher = new Launcher();
        launcher.run(args);

    }

    private String filename;

    private ProblemType typeProbleme = ProblemType.CNF_SAT;

    @Override
    public void usage() {
        Solvers.usage(this);
    }

    @Override
    protected Reader createReader(ISolver theSolver, String problemname) {
        InstanceReader instance = new InstanceReader(theSolver);
        switch (typeProbleme) {
        case CNF_MAXSAT:
        case WCNF_MAXSAT:
            instance = new MSInstanceReader((WeightedMaxSatDecorator) theSolver);
            break;
        case PB_OPT:
        case PB_SAT:
            instance = new PBInstanceReader((IPBSolver) theSolver);
            break;
        case CNF_SAT:
            instance = new InstanceReader(theSolver);
            break;
        }

        return instance;
    }

    @Override
    protected String getInstanceName(String[] args) {
        return this.filename;
    }

    /**
     * Configure the solver according to the command line parameters.
     * 
     * @param args
     *            the command line
     * @return a solver properly configured.
     */
    @SuppressWarnings({ "nls", "unchecked" })
    @Override
    protected ISolver configureSolver(String[] args) {
        Options options = Solvers.createCLIOptions();

        try {
            CommandLine cmd = new PosixParser().parse(options, args);

            this.isModeOptimization = cmd.hasOption("opt");

            this.filename = cmd.getOptionValue("f");

            boolean equivalence = cmd.hasOption("e");

            int others = 0;
            String[] rargs = cmd.getArgs();
            if (this.filename == null && rargs.length > 0) {
                this.filename = rargs[others++];
            }

            if (filename != null) {
                String unzipped = Solvers.uncompressed(filename);

                if (unzipped.endsWith(".cnf") && isModeOptimization) {
                    typeProbleme = ProblemType.CNF_MAXSAT;
                } else if (unzipped.endsWith(".wcnf")) {
                    typeProbleme = ProblemType.WCNF_MAXSAT;
                    isModeOptimization = true;
                } else if (unzipped.endsWith(".opb")) {
                    if (isModeOptimization) {
                        typeProbleme = ProblemType.PB_OPT;
                    } else {
                        typeProbleme = ProblemType.PB_SAT;
                    }
                } else {
                    typeProbleme = ProblemType.CNF_SAT;
                }
            } else {
                typeProbleme = ProblemType.CNF_SAT;
            }

            ISolver asolver = Solvers.configureSolver(args, this);

            this.launchRemoteControl = cmd.hasOption("remote");

            if (cmd.hasOption("m")) {
                setSilent(true);
            }

            if (cmd.hasOption("k")) {
                Integer myk = Integer.valueOf(cmd.getOptionValue("k"));
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

            switch (typeProbleme) {
            case PB_OPT:
                setLauncherMode(ILauncherMode.OPTIMIZATION);
                if (cmd.hasOption("lo")) {
                    this.problem = new ConstraintRelaxingPseudoOptDecorator(
                            (IPBSolver) asolver);
                } else {
                    this.problem = new PseudoOptDecorator((IPBSolver) asolver);
                }
                break;
            case CNF_MAXSAT:
            case WCNF_MAXSAT:
                setLauncherMode(ILauncherMode.OPTIMIZATION);
                asolver = new WeightedMaxSatDecorator((IPBCDCLSolver) asolver,
                        equivalence);
                if (cmd.hasOption("lo")) {
                    this.problem = new ConstraintRelaxingPseudoOptDecorator(
                            (WeightedMaxSatDecorator) asolver);
                } else {
                    this.problem = new PseudoOptDecorator(
                            (WeightedMaxSatDecorator) asolver, false,
                            false);
                }
                break;
            default:
                setLauncherMode(ILauncherMode.DECISION);
                break;
            }

            setIncomplete(cmd.hasOption("i"));

            setDisplaySolutionLine(!cmd.hasOption("n"));

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

            if (asolver != null) {
                getLogWriter().println(asolver.toString(COMMENT_PREFIX)); //$NON-NLS-1$
            }
            return asolver;
        } catch (ParseException e1) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("java -jar sat4j.jar", options, true);
            usage();
            System.exit(0);
        }
        return null;
    }

    @Override
    public void run(String[] args) {
        try {
            displayHeader();
            this.solver = configureSolver(args);
            if (this.solver == null) {
                usage();
                System.exit(0);
            }
            if (!this.silent) {
                this.solver.setVerbose(true);
            }
            String instanceName = getInstanceName(args);
            if (instanceName == null) {
                usage();
                System.exit(0);
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
                System.exit(launcher.getExitCode().value());
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
            this.launcherMode.setExitCode(ExitCode.UNSATISFIABLE);
            log("(trivial inconsistency)"); //$NON-NLS-1$
        } catch (ParseFormatException e) {
            System.err.println("FATAL " + e.getLocalizedMessage());
        }
    }

}
