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

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.AbstractLauncher;
import org.sat4j.ILauncherMode;
import org.sat4j.maxsat.reader.WDimacsReader;
import org.sat4j.opt.MinOneDecorator;
import org.sat4j.pb.ConstraintRelaxingPseudoOptDecorator;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.tools.ManyCorePB;
import org.sat4j.pb.tools.SearchOptimizerListener;
import org.sat4j.reader.LecteurDimacs;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

/**
 * Generic launcher to be used for solving optimization problems.
 * 
 * @author daniel
 * @since 2.0
 * 
 */
public class GenericOptLauncher extends AbstractLauncher {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GenericOptLauncher() {
        setLauncherMode(ILauncherMode.OPTIMIZATION);
    }

    @SuppressWarnings("nls")
    private Options createCLIOptions() {
        Options options = new Options();
        options.addOption("s", "solver", true,
                "specifies the name of a PB solver");
        options.addOption("t", "timeout", true,
                "specifies the timeout (in seconds)");
        options.addOption("p", "parallel", false,
                "uses CP and RES pseudo-boolean solvers in parallel");
        options.addOption("T", "timeoutms", true,
                "specifies the timeout (in milliseconds)");
        options.addOption("K", "kind", true,
                "kind of problem: minone, maxsat, etc.");
        options.addOption("i", "incomplete", false,
                "incomplete mode for maxsat");
        options.addOption("I", "inner mode", false, "optimize using inner mode");
        options.addOption("c", "clean databases", false,
                "clean up the database at root level");
        options.addOption("k", "keep Hot", false,
                "Keep heuristics accross calls to the SAT solver");
        options.addOption("e", "equivalence", false,
                "Use an equivalence instead of an implication for the selector variables");
        options.addOption("pi", "prime-implicant", false,
                "Use prime implicants instead of models for evaluating the objective function");
        options.addOption("n", "no solution line", false,
                "Do not display a solution line (useful if the solution is large)");
        options.addOption("l", "lower bounding", false,
                "search solution by lower bounding instead of by upper bounding");
        options.addOption("m", "mystery", false, "mystery option");
        options.addOption("B", "External&Internal", false, "External&Internal optimization");
        return options;
    }

    @Override
    public void displayLicense() {
        super.displayLicense();
        log("This software uses some libraries from the Jakarta Commons project. See jakarta.apache.org for details."); //$NON-NLS-1$
    }

    @Override
    public void usage() {
        this.out.println("java -jar sat4j-maxsat.jar instance-name"); //$NON-NLS-1$
    }

    @Override
    protected Reader createReader(ISolver aSolver, String problemname) {
        Reader reader;
        if (problemname.contains(".wcnf")) { //$NON-NLS-1$
            reader = new WDimacsReader(this.wmsd);
        } else {
            reader = new LecteurDimacs(aSolver);
        }
        reader.setVerbosity(true);
        return reader;
    }

    
    @Override
    protected String getInstanceName(String[] args) {
        return args[args.length - 1];
    }

    private WeightedMaxSatDecorator wmsd;

    @Override
    protected ISolver configureSolver(String[] args) {
        ISolver asolver = null;
        Options options = createCLIOptions();
        if (args.length == 0) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("java -jar sat4j-maxsat.jar", options, true);
            System.exit(0);
        } else {
            try {
                CommandLine cmd = new PosixParser().parse(options, args);
                int problemindex = args.length - 1;
                setDisplaySolutionLine(!cmd.hasOption("n"));
                boolean equivalence = cmd.hasOption("e");
                String kind = cmd.getOptionValue("K"); //$NON-NLS-1$
                if (kind == null) {
                    kind = "maxsat";
                }
                String aPBSolverName = cmd.getOptionValue("s");
                if (aPBSolverName==null) {
                    aPBSolverName = "Default";
                }
                if ("minone".equalsIgnoreCase(kind)) {
                    asolver = new MinOneDecorator(org.sat4j.minisat.SolverFactory.newDefault());
                } else if ("mincost".equalsIgnoreCase(kind)
                        || args[problemindex].endsWith(".p2cnf")) {
                    asolver = new MinCostDecorator(SolverFactory.newDefault());
                } else {
                    assert "maxsat".equalsIgnoreCase(kind);
                    if (cmd.hasOption("m")) {
                        this.wmsd = new WeightedMaxSatDecorator(
                                org.sat4j.pb.SolverFactory.newSATUNSAT(),
                                equivalence);
                    } else if (cmd.hasOption("p")) {
                        this.wmsd = new WeightedMaxSatDecorator(
                                org.sat4j.pb.SolverFactory.newBoth(),
                                equivalence);
                    } else {
                        this.wmsd = new WeightedMaxSatDecorator(
                                org.sat4j.pb.SolverFactory.instance().createSolverByName(aPBSolverName), equivalence);
                    }
                    if (cmd.hasOption("l")) {
                        asolver = new ConstraintRelaxingPseudoOptDecorator(
                                this.wmsd);
                    } else if (cmd.hasOption("I")){
                        this.wmsd.setSearchListener(new SearchOptimizerListener(ILauncherMode.DECISION));
                        setLauncherMode(ILauncherMode.DECISION);
                        asolver = this.wmsd;
                    }else if(cmd.hasOption("B")){
                        IPBSolver internal = org.sat4j.pb.SolverFactory.newDefault();
                        internal.setSearchListener(new SearchOptimizerListener(ILauncherMode.DECISION));
                        IPBSolver external = org.sat4j.pb.SolverFactory.newDefault();
                        external = new OptToPBSATAdapter(new PseudoOptDecorator(external),ILauncherMode.DECISION);
                        ManyCorePB mc = new ManyCorePB(external, internal);
                        this.wmsd = new WeightedMaxSatDecorator(mc, equivalence);
                        setLauncherMode(ILauncherMode.DECISION);
                        asolver = this.wmsd;
                    }else{
                        asolver = new PseudoOptDecorator(this.wmsd, false,
                                cmd.hasOption("pi"));
                    }
                }
                if (cmd.hasOption("i")) {
                    setIncomplete(true);
                }
                if (cmd.hasOption("c")) {
                    asolver.setDBSimplificationAllowed(true);
                }
                if (cmd.hasOption("k")) {
                    asolver.setKeepSolverHot(true);
                }
                String timeout = cmd.getOptionValue("t");
                if (timeout == null) {
                    timeout = cmd.getOptionValue("T");
                    if (timeout != null) {
                        asolver.setTimeoutMs(Long.parseLong(timeout));
                    }
                } else {
                    asolver.setTimeout(Integer.parseInt(timeout));
                }
                getLogWriter().println(asolver.toString(COMMENT_PREFIX));
            } catch (ParseException e1) {
                HelpFormatter helpf = new HelpFormatter();
                helpf.printHelp("java -jar sat4jopt.jar", options, true);
            }
        }
        return asolver;
    }

    public static void main(String[] args) {
        AbstractLauncher lanceur = new GenericOptLauncher();
        lanceur.run(args);
    }

    @Override
    protected IProblem readProblem(String problemname)
            throws ParseFormatException, IOException,
            ContradictionException {
        super.readProblem(problemname);
        return solver;
    }
}
