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
package org.sat4j.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;

public class SpeedTracing extends SearchListenerAdapter<ISolverService> {

	// private int maxDlevel;

	private static final long serialVersionUID = 1L;

	private final String filename;
	private PrintStream out;
	private PrintStream outClean;
	private PrintStream outRestart;

	private long begin, end;
	private int counter;
	private long index;

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
			long tmp = (end - begin);
			index += tmp;
			out.println(index / 1000.0 + "\t" + counter / tmp * 1000);
			outClean.println(index / 1000.0 + "\t" + 0);
			outRestart.println(index / 1000.0 + "\t" + 0);
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
		long indexClean = index + (end - begin);
		out.println(indexClean / 1000.0 + "\t" + counter / (end - begin) * 1000);
		outClean.println(indexClean / 1000.0 + "\t" + nVar);
		outRestart.println("#ignore");
		// out.println("# ignore");
	}

	@Override
	public void restarting() {
		end = System.currentTimeMillis();
		long indexRestart = index + (end - begin);
		out.println(indexRestart / 1000.0 + "\t" + counter / (end - begin)
				* 1000);
		outRestart.println(indexRestart / 1000.0 + "\t" + nVar);
		outClean.println("#ignore");
		// out.println("# ignore");
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
