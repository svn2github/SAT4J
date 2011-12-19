package org.sat4j.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.sat4j.minisat.core.Constr;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;

public class LBDTracing implements SearchListener {

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

	public void assuming(int p) {
		// TODO Auto-generated method stub

	}

	public void propagating(int p, IConstr reason) {
		// TODO Auto-generated method stub

	}

	public void backtracking(int p) {
		// TODO Auto-generated method stub

	}

	public void adding(int p) {
		// TODO Auto-generated method stub

	}

	public void learn(IConstr c) {
		// TODO Auto-generated method stub

	}

	public void delete(int[] clause) {
		// TODO Auto-generated method stub

	}

	public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
		out.println(((Constr) confl).getActivity());

	}

	public void conflictFound(int p) {
		// TODO Auto-generated method stub

	}

	public void solutionFound() {
		// TODO Auto-generated method stub

	}

	public void beginLoop() {
		// TODO Auto-generated method stub

	}

	public void start() {
		updateWriter();

	}

	public void end(Lbool result) {
		out.close();
	}

	public void restarting() {
		// TODO Auto-generated method stub

	}

	public void backjump(int backjumpLevel) {
		// TODO Auto-generated method stub

	}

}
