package org.sat4j.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;

public class SpeedTracing extends SearchListenerAdapter {

	// private int maxDlevel;

	private static final long serialVersionUID = 1L;

	private final String filename;
	private PrintStream out;
	private PrintStream outClean;
	private PrintStream outRestart;

	private long begin, end;
	private int counter;
	private int index;

	private int nVar;

	public SpeedTracing(String filename) {
		this.filename = filename;
		updateWriter();
	}

	private void updateWriter() {
		try {
			out = new PrintStream(new FileOutputStream(filename + ".dat"));
			outClean = new PrintStream(new FileOutputStream(filename
					+ "-clean.dat"));
			outRestart = new PrintStream(new FileOutputStream(filename
					+ "-restart.dat"));
		} catch (FileNotFoundException e) {
			out = System.out;
			outClean = System.out;
			outRestart = System.out;
		}
		begin = System.currentTimeMillis();
		counter = 0;
		index = 0;
	}

	@Override
	public void assuming(int p) {

	}

	@Override
	public void propagating(int p, IConstr reason) {
		end = System.currentTimeMillis();
		if (end - begin >= 2000) {
			long tmp = (end - begin) / 1000;
			index += tmp;
			out.println(index + "\t" + counter / tmp);
			outClean.println(index + "\t" + 0);
			outRestart.println(index + "\t" + 0);
			begin = System.currentTimeMillis();
			counter = 0;
		}
		counter++;
	}

	@Override
	public void end(Lbool result) {
		out.close();
		outClean.close();
	}

	@Override
	public void cleaning() {
		end = System.currentTimeMillis();
		int indexClean = index + (int) (end - begin) / 1000;
		outClean.println(indexClean + "\t" + nVar);
		outRestart.println("#ignore");
		out.println("# ignore");
	}

	@Override
	public void restarting() {
		end = System.currentTimeMillis();
		int indexRestart = index + (int) (end - begin) / 1000;
		outRestart.println(indexRestart + "\t" + nVar);
		outClean.println("#ignore");
		out.println("# ignore");
	}

	@Override
	public void start() {
		updateWriter();
	}

	@Override
	public void init(ISolverService solverService) {
		nVar = solverService.nVars();
	}
}
