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

import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;

/**
 * @since 2.2
 */
public class DecisionTracing extends SearchListenerAdapter<ISolverService> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int counter;

	private final String filename;
	private PrintStream outPos;
	private PrintStream outNeg;
	private PrintStream outRestart;
	private int nVar;

	public DecisionTracing(String filename) {
		this.filename = filename;
	}

	private void updateWriter() {
		try {
			outPos = new PrintStream(
					new FileOutputStream(filename + "-pos.dat"));
			outNeg = new PrintStream(
					new FileOutputStream(filename + "-neg.dat"));
			outRestart = new PrintStream(new FileOutputStream(filename
					+ "-restart.dat"));
		} catch (FileNotFoundException e) {
			outPos = System.out;
			outNeg = System.out;
			outRestart = System.out;
		}
		counter = 1;
	}

	@Override
	public void assuming(int p) {
		if (p > 0) {
			outPos.println(counter + "\t" + p);
			outNeg.println("#" + counter + "\t" + "0");
		} else {
			outNeg.println(counter + "\t" + -p);
			outPos.println("#" + counter + "\t" + "0");
		}
		outRestart.println("#" + counter + "\t" + "0");
		counter++;

	}

	@Override
	public void restarting() {
		outRestart.println(counter + "\t" + nVar);
		outNeg.println("#" + counter + "\t" + "0");
		outPos.println("#" + counter + "\t" + "0");
		// counter++;
	}

	@Override
	public void end(Lbool result) {
		outPos.close();
		outNeg.close();
		outRestart.close();
	}

	@Override
	public void start() {
		updateWriter();
	}

	@Override
	public void init(ISolverService solverService) {
		this.nVar = solverService.nVars();
	}

}
