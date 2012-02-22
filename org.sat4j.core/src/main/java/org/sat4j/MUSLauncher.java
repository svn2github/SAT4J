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

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.GroupedCNFReader;
import org.sat4j.reader.LecteurDimacs;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.xplain.Explainer;
import org.sat4j.tools.xplain.HighLevelXplain;
import org.sat4j.tools.xplain.MinimizationStrategy;
import org.sat4j.tools.xplain.Xplain;

public class MUSLauncher extends AbstractLauncher {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int[] mus;

	private Explainer xplain;

	private boolean highLevel = false;

	@Override
	public void usage() {
		log("java -jar sat4j-mus.jar [Insertion|Deletion|QuickXplain] <cnffile>|<gcnffile>");
	}

	@Override
	protected Reader createReader(ISolver theSolver, String problemname) {
		if (highLevel) {
			return new GroupedCNFReader((HighLevelXplain<ISolver>) theSolver);
		}
		return new LecteurDimacs(theSolver);
	}

	@Override
	protected String getInstanceName(String[] args) {
		if (args.length == 0) {
			return null;
		}
		return args[args.length - 1];
	}

	@Override
	protected ISolver configureSolver(String[] args) {
		String problemName = args[args.length - 1];
		if (problemName.endsWith(".gcnf")) {
			highLevel = true;
		}
		ISolver solver;
		if (highLevel) {
			HighLevelXplain<ISolver> hlxp = new HighLevelXplain<ISolver>(
					SolverFactory.newDefault());
			xplain = hlxp;
			solver = hlxp;
		} else {
			Xplain<ISolver> xp = new Xplain<ISolver>(
					SolverFactory.newDefault(), false);
			xplain = xp;
			solver = xp;
		}
		if (args.length == 2) {
			// retrieve minimization strategy
			String className = "org.sat4j.tools.xplain." + args[0] + "Strategy";
			try {
				xplain.setMinimizationStrategy((MinimizationStrategy) Class
						.forName(className).newInstance());
			} catch (Exception e) {
				log(e.getMessage());
			}
		}
		solver.setTimeout(Integer.MAX_VALUE);
		solver.setDBSimplificationAllowed(true);
		getLogWriter().println(solver.toString(COMMENT_PREFIX)); //$NON-NLS-1$
		return solver;
	}

	@Override
	protected void displayResult() {
		if (solver != null) {
			double wallclocktime = (System.currentTimeMillis() - beginTime) / 1000.0;
			solver.printStat(out, COMMENT_PREFIX);
			solver.printInfos(out, COMMENT_PREFIX);
			out.println(ANSWER_PREFIX + exitCode);
			if (exitCode == ExitCode.SATISFIABLE) {
				int[] model = solver.model();
				out.print(SOLUTION_PREFIX);
				reader.decode(model, out);
				out.println();
			} else if (exitCode == ExitCode.UNSATISFIABLE && mus != null) {
				out.print(SOLUTION_PREFIX);
				reader.decode(mus, out);
				out.println();
			}
			log("Total wall clock time (in seconds) : " + wallclocktime); //$NON-NLS-1$
		}
	}

	@Override
	public void run(String[] args) {
		mus = null;
		super.run(args);
		double wallclocktime = (System.currentTimeMillis() - beginTime) / 1000.0;
		if (exitCode == ExitCode.UNSATISFIABLE) {
			try {
				log("Unsat detection wall clock time (in seconds) : "
						+ wallclocktime);
				log("Size of initial " + (highLevel ? "high level " : "")
						+ "unsat subformula: "
						+ solver.unsatExplanation().size());
				log("Computing " + (highLevel ? "high level " : "") + "MUS ...");
				double beginmus = System.currentTimeMillis();
				mus = xplain.minimalExplanation();
				log("Size of the " + (highLevel ? "high level " : "") + "MUS: "
						+ mus.length);
				log("Unsat core  computation wall clock time (in seconds) : "
						+ (System.currentTimeMillis() - beginmus) / 1000.0);
			} catch (TimeoutException e) {
				log("Cannot compute " + (highLevel ? "high level " : "")
						+ "MUS within the timeout.");
			}
		}

	}

	public static void main(final String[] args) {
		MUSLauncher lanceur = new MUSLauncher();
		if (args.length < 1 || args.length > 2) {
			lanceur.usage();
			return;
		}
		lanceur.run(args);
		System.exit(lanceur.getExitCode().value());
	}
}
