package org.sat4j.pb.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.sat4j.pb.constraints.pb.PBConstr;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;

public class ConflictTracing implements SearchListener {

	private static final long serialVersionUID = 1L;

	private final String filename;
	private PrintStream out;
	private long index = 1;

	public ConflictTracing(String filename) {
		this.filename = filename;
		updateWriter();
	}

	private void updateWriter() {
		try {
			out = new PrintStream(new FileOutputStream(filename + ".dat"));
		} catch (FileNotFoundException e) {
			out = System.out;
		}
	}

	public void adding(int p) {
		// TODO Auto-generated method stub

	}

	public void assuming(int p) {
		// TODO Auto-generated method stub

	}

	public void backjump(int backjumpLevel) {
		// TODO Auto-generated method stub

	}

	public void backtracking(int p) {
		// TODO Auto-generated method stub

	}

	public void beginLoop() {
		// TODO Auto-generated method stub

	}

	public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
		// TODO Auto-generated method stub

	}

	public void conflictFound(int p) {
		// TODO Auto-generated method stub

	}

	public void delete(int[] clause) {
		// TODO Auto-generated method stub

	}

	public void end(Lbool result) {
		out.close();
	}

	public void learn(IConstr c) {
		PBConstr myConstr = (PBConstr) c;
		if (myConstr.size() > 0) {
			out.printf("%d %d %d\n", index++, myConstr.getCoef(0), myConstr
					.size());
		}
	}

	public void propagating(int p, IConstr reason) {
		// TODO Auto-generated method stub

	}

	public void restarting() {
		// TODO Auto-generated method stub

	}

	public void solutionFound() {
		// TODO Auto-generated method stub

	}

	public void start() {
		// TODO Auto-generated method stub

	}

}
