package org.sat4j.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.IVec;

public class LearnedTracing extends SearchListenerAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String filename;
	private ISolverService solverService;

	public LearnedTracing(String filename) {
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
			IVec<? extends IConstr> constrs = solverService
					.getLearnedConstraints();
			int n = constrs.size();
			for (int i = 0; i < n; i++) {
				out.printf("%d %g\n", i, constrs.get(i).getActivity());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init(ISolverService solverService) {
		this.solverService = solverService;
	}

	@Override
	public void cleaning() {
		trace();
	}
}
