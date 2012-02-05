package org.sat4j.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.sat4j.minisat.core.Constr;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.Lbool;

public class LBDTracing extends SearchListenerAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String filename;
	private PrintStream out;

	public LBDTracing(String filename) {
		this.filename = filename;
	}

	private void updateWriter() {
		try {
			out = new PrintStream(new FileOutputStream(filename + ".dat"));
		} catch (FileNotFoundException e) {
			System.err.println(e);
			out = System.out;
		}
	}

	@Override
	public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
		out.println(((Constr) confl).getActivity());

	}

	@Override
	public void start() {
		updateWriter();

	}

	@Override
	public void end(Lbool result) {
		out.close();
	}
}
