package org.sat4j;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.LecteurDimacs;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.xplain.Xplain;

public class MUSLauncher extends AbstractLauncher {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int[] mus;

	@Override
	public void usage() {
		log("java -jar sat4j-mus.jar <cnffile>");
	}

	@Override
	protected Reader createReader(ISolver theSolver, String problemname) {
		return new LecteurDimacs(theSolver);
	}

	@Override
	protected String getInstanceName(String[] args) {
		if (args.length == 0) {
			return null;
		}
		return args[0];
	}

	@Override
	protected ISolver configureSolver(String[] args) {
		ISolver asolver = new Xplain<ISolver>(SolverFactory.newDefault());
		asolver.setTimeout(Integer.MAX_VALUE);
		asolver.setDBSimplificationAllowed(true);
		getLogWriter().println(asolver.toString(COMMENT_PREFIX)); //$NON-NLS-1$
		return asolver;
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
				log("Computing MUS ...");
				mus = ((Xplain<ISolver>) solver).explainInTermsOfClauseIndex();
				log("Unsat core  computation wall clock time (in seconds) : "
						+ (System.currentTimeMillis() - wallclocktime) / 1000.0);
			} catch (TimeoutException e) {
				log("Cannot compute MUS within the timeout.");
			}
		}

	}

	public static void main(final String[] args) {
		MUSLauncher lanceur = new MUSLauncher();
		if (args.length != 1) {
			lanceur.usage();
			return;
		}
		lanceur.run(args);
		System.exit(lanceur.getExitCode().value());
	}
}
