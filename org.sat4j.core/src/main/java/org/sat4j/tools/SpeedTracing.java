package org.sat4j.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;

public class SpeedTracing extends SearchListenerAdapter {

	// private int maxDlevel;

	private static final long serialVersionUID = 1L;

	private final String filename;
	private PrintStream out;
	private PrintStream outRestart;

	private long begin, end;
	private int counter;
	private int index;

	public SpeedTracing(String filename) {
		this.filename = filename;
		updateWriter();
	}

	private void updateWriter() {
		try {
			out = new PrintStream(new FileOutputStream(filename + ".dat"));
			outRestart = new PrintStream(new FileOutputStream(filename
					+ "-restart.dat"));
		} catch (FileNotFoundException e) {
			out = System.out;
			outRestart = System.out;
		}
		begin = System.currentTimeMillis();
		counter = 0;
		index = 0;
	}

	@Override
	public void assuming(int p) {
		end = System.currentTimeMillis();
		if (end - begin >= 2000) {
			index += (end - begin) / 1000;
			out.println(index + "\t" + counter / 2);
			begin = System.currentTimeMillis();
			counter = 0;
		}
		counter++;
	}

	@Override
	public void end(Lbool result) {
		out.close();
		outRestart.close();
	}

	@Override
	public void start() {
		updateWriter();
	}

	@Override
	public void init(ISolverService solverService) {
	}
}
