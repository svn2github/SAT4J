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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.Properties;

import org.sat4j.core.ASolverFactory;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * That class is used by launchers used to solve decision problems, i.e.
 * problems with YES/NO/UNKNOWN answers.
 * 
 * @author leberre
 * 
 */
public abstract class AbstractLauncher implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public static final String SOLUTION_PREFIX = "v "; //$NON-NLS-1$

    public static final String ANSWER_PREFIX = "s "; //$NON-NLS-1$

    public static final String COMMENT_PREFIX = "c "; //$NON-NLS-1$

    protected long beginTime;

    protected ExitCode exitCode = ExitCode.UNKNOWN;

    protected Reader reader;

    protected transient PrintWriter out = new PrintWriter(System.out, true);

    protected transient Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            displayResult();
        }
    };

    protected ISolver solver;

    public boolean silent = false;

    protected boolean prime = System.getProperty("prime") != null;;

    protected AbstractLauncher() {
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    protected void displayResult() {
        if (this.solver != null) {
            System.out.flush();
            this.out.flush();
            double wallclocktime = (System.currentTimeMillis() - this.beginTime) / 1000.0;
            this.solver.printStat(this.out, COMMENT_PREFIX);
            this.solver.printInfos(this.out, COMMENT_PREFIX);
            this.out.println(ANSWER_PREFIX + this.exitCode);
            if (this.exitCode == ExitCode.SATISFIABLE) {
                int[] model = this.solver.model();
                if (this.prime) {
                    int initiallength = model.length;
                    log("returning a prime implicant ...");
                    long beginpi = System.currentTimeMillis();
                    model = this.solver.primeImplicant();
                    long endpi = System.currentTimeMillis();
                    log("removed " + (initiallength - model.length)
                            + " literals");
                    log("pi computation time: " + (endpi - beginpi) + " ms");
                }
                this.out.print(SOLUTION_PREFIX);
                this.reader.decode(model, this.out);
                this.out.println();
            }
            log("Total wall clock time (in seconds) : " + wallclocktime); //$NON-NLS-1$
        }
    }

    public abstract void usage();

    /**
     * @throws IOException
     */
    protected final void displayHeader() {
        displayLicense();
        URL url = AbstractLauncher.class.getResource("/sat4j.version"); //$NON-NLS-1$
        if (url == null) {
            log("no version file found!!!"); //$NON-NLS-1$			
        } else {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(url.openStream()));
                log("version " + in.readLine()); //$NON-NLS-1$
            } catch (IOException e) {
                log("c ERROR: " + e.getMessage());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log("c ERROR: " + e.getMessage());
                    }
                }
            }
        }
        Properties prop = System.getProperties();
        String[] infoskeys = {
                "java.runtime.name", "java.vm.name", "java.vm.version", "java.vm.vendor", "sun.arch.data.model", "java.version", "os.name", "os.version", "os.arch" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$
        for (String key : infoskeys) {
            log(key
                    + (key.length() < 14 ? "\t\t" : "\t") + prop.getProperty(key)); //$NON-NLS-1$
        }
        Runtime runtime = Runtime.getRuntime();
        log("Free memory \t\t" + runtime.freeMemory()); //$NON-NLS-1$
        log("Max memory \t\t" + runtime.maxMemory()); //$NON-NLS-1$
        log("Total memory \t\t" + runtime.totalMemory()); //$NON-NLS-1$
        log("Number of processors \t" + runtime.availableProcessors()); //$NON-NLS-1$
    }

    public void displayLicense() {
        log("SAT4J: a SATisfiability library for Java (c) 2004-2012 Artois University and CNRS"); //$NON-NLS-1$
        log("This is free software under the dual EPL/GNU LGPL licenses."); //$NON-NLS-1$
        log("See www.sat4j.org for details."); //$NON-NLS-1$
    }

    /**
     * Reads a problem file from the command line.
     * 
     * @param problemname
     *            the fully qualified name of the problem.
     * @return a reference to the problem to solve
     * @throws FileNotFoundException
     *             if the file is not found
     * @throws ParseFormatException
     *             if the problem is not expressed using the right format
     * @throws IOException
     *             for other IO problems
     * @throws ContradictionException
     *             if the problem is found trivially unsat
     */
    protected IProblem readProblem(String problemname)
            throws FileNotFoundException, ParseFormatException, IOException,
            ContradictionException {
        log("solving " + problemname); //$NON-NLS-1$
        log("reading problem ... "); //$NON-NLS-1$
        this.reader = createReader(this.solver, problemname);
        IProblem problem = this.reader.parseInstance(problemname);
        log("... done. Wall clock time " //$NON-NLS-1$
                + (System.currentTimeMillis() - this.beginTime) / 1000.0 + "s."); //$NON-NLS-1$
        log("declared #vars     " + problem.nVars()); //$NON-NLS-1$
        if (this.solver.nVars() < this.solver.realNumberOfVariables()) {
            log("internal #vars     " + this.solver.realNumberOfVariables()); //$NON-NLS-1$
        }
        log("#constraints  " + problem.nConstraints()); //$NON-NLS-1$
        problem.printInfos(this.out, COMMENT_PREFIX);
        return problem;
    }

    protected abstract Reader createReader(ISolver theSolver, String problemname);

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
            readProblem(instanceName);
            try {
                solve(this.solver);
            } catch (TimeoutException e) {
                log("timeout"); //$NON-NLS-1$
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

    protected abstract String getInstanceName(String[] args);

    protected abstract ISolver configureSolver(String[] args);

    /**
     * Display messages as comments on STDOUT
     * 
     * @param message
     */
    public void log(String message) {
        if (!this.silent) {
            this.out.println(COMMENT_PREFIX + message);
        }
    }

    protected void solve(IProblem problem) throws TimeoutException {
        this.exitCode = problem.isSatisfiable() ? ExitCode.SATISFIABLE
                : ExitCode.UNSATISFIABLE;
    }

    /**
     * Change the value of the exit code in the Launcher
     * 
     * @param exitCode
     *            the new ExitCode
     */
    public final void setExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Get the value of the ExitCode
     * 
     * @return the current value of the Exitcode
     */
    public final ExitCode getExitCode() {
        return this.exitCode;
    }

    /**
     * Obtaining the current time spent since the beginning of the solving
     * process.
     * 
     * @return the time signature at the beginning of the run() method.
     */
    public final long getBeginTime() {
        return this.beginTime;
    }

    /**
     * 
     * @return the reader used to parse the instance
     */
    public final Reader getReader() {
        return this.reader;
    }

    /**
     * To change the output stream on which statistics are displayed. By
     * default, the solver displays everything on System.out.
     * 
     * @param out
     */
    public void setLogWriter(PrintWriter out) {
        this.out = out;
    }

    public PrintWriter getLogWriter() {
        return this.out;
    }

    protected void setSilent(boolean b) {
        this.silent = b;
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        this.out = new PrintWriter(System.out, true);
        this.shutdownHook = new Thread() {
            @Override
            public void run() {
                displayResult();
            }
        };
    }

    protected <T extends ISolver> void showAvailableSolvers(
            ASolverFactory<T> afactory) {
        // if (afactory != null) {
        //			log("Available solvers: "); //$NON-NLS-1$
        // String[] names = afactory.solverNames();
        // for (int i = 0; i < names.length; i++) {
        // log(names[i]);
        // }
        // }
        showAvailableSolvers(afactory, "");
    }

    protected <T extends ISolver> void showAvailableSolvers(
            ASolverFactory<T> afactory, String framework) {
        if (afactory != null) {
            if (framework.length() > 0) {
                log("Available solvers for " + framework + ": "); //$NON-NLS-1$
            } else {
                log("Available solvers: "); //$NON-NLS-1$
            }
            String[] names = afactory.solverNames();
            for (String name : names) {
                log(name);
            }
        }
    }

}
