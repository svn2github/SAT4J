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
*******************************************************************************/
package org.sat4j.maxsat;

import static java.lang.System.out;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.AbstractLauncher;
import org.sat4j.AbstractOptimizationLauncher;
import org.sat4j.maxsat.reader.WDimacsReader;
import org.sat4j.opt.MaxSatDecorator;
import org.sat4j.opt.MinOneDecorator;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ISolver;

/**
 * Generic launcher to be used for solving optimization problems.
 * 
 * @author daniel
 * @since 2.0
 * 
 */
public class GenericOptLauncher extends AbstractOptimizationLauncher {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("nls")
    private Options createCLIOptions() {
        Options options = new Options();
        options.addOption("t", "timeout", true,
                "specifies the timeout (in seconds)");
        options.addOption("T", "timeoutms", true,
                "specifies the timeout (in milliseconds)");
        options.addOption("k", "kind", true,
                "kind of problem: minone, maxsat, etc.");
        return options;
    }

    @Override
    public void displayLicense() {
        super.displayLicense();
        log("This software uses some libraries from the Jakarta Commons project. See jakarta.apache.org for details."); //$NON-NLS-1$
    }
    
    @Override
    public void usage() {
        out.println("java -jar sat4j-maxsat.jar instance-name"); //$NON-NLS-1$
    }

    @Override
    protected Reader createReader(ISolver aSolver, String problemname) {
        if (problemname.endsWith(".wcnf")) { //$NON-NLS-1$
            return new WDimacsReader(( WeightedMaxSatDecorator)aSolver); //$NON-NLS-1$
        } 
        return new DimacsReader(aSolver);
    }

    @Override
    protected String getInstanceName(String[] args) {
        return args[args.length - 1];
    }

    @Override
    protected ISolver configureSolver(String[] args) {
        ISolver asolver = null;
        Options options = createCLIOptions();
        if (args.length == 0) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("java -jar sat4j-maxsat.jar", options, true);
        } else {
            try {
                CommandLine cmd = new PosixParser().parse(options, args);
                int problemindex = args.length - 1;
                String kind = cmd.getOptionValue("k"); //$NON-NLS-1$
                if (kind == null) { //$NON-NLS-1$
                    kind = "maxsat";
                }
                if ("minone".equalsIgnoreCase(kind)) {
                    asolver = new MinOneDecorator(SolverFactory.newDefault());
                } else if ("mincost".equalsIgnoreCase(kind)||args[problemindex].endsWith(".p2cnf")) {
                    asolver = new MinCostDecorator(SolverFactory.newDefault());
                } else {
                    assert "maxsat".equalsIgnoreCase(kind);

                    if (args[problemindex].endsWith(".wcnf")) { //$NON-NLS-1$
                        asolver = new WeightedMaxSatDecorator(SolverFactory
                                .newDefault());
                    } else {
                        asolver = new MaxSatDecorator(SolverFactory
                                .newMiniMaxSAT());
                    }
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
}
