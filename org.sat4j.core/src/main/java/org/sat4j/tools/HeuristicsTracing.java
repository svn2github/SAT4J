package org.sat4j.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.sat4j.specs.ISolverService;

public class HeuristicsTracing extends SearchListenerAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String filename;
	private ISolverService solverService;

	public HeuristicsTracing(String filename) {
		this.filename = filename;
	}

	@Override
	public void solutionFound() {
		trace();
	}

	@Override
	public void restarting() {
		trace();
	}

	private void trace() {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(filename
					+ ".dat"));
			int n = solverService.nVars();
			double[] heuristics = solverService.getVariableHeuristics();
			for (int i = 1; i <= n; i++) {
				out.printf("%g %d\n", heuristics[i], i);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init(ISolverService solverService) {
		this.solverService = solverService;
	}
}
