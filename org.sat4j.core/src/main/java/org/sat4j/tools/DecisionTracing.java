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

	private final IVisualizationTool positiveVisu;
	private final IVisualizationTool negativeVisu;
	private final IVisualizationTool restartVisu;
	private final IVisualizationTool cleanVisu;

	// private final String filename;
	// private PrintStream outPos;
	// private PrintStream outNeg;
	// private PrintStream outRestart;
	private int nVar;

	public DecisionTracing(IVisualizationTool positiveVisu,
			IVisualizationTool negativeVisu, IVisualizationTool restartVisu,
			IVisualizationTool cleanVisu) {
		this.positiveVisu = positiveVisu;
		this.negativeVisu = negativeVisu;
		this.restartVisu = restartVisu;
		this.cleanVisu = cleanVisu;

		counter = 1;
	}

	@Override
	public void assuming(int p) {
		if (p > 0) {
			positiveVisu.addPoint(counter, p);
			negativeVisu.addInvisiblePoint(counter, 0);
		} else {
			negativeVisu.addPoint(counter, -p);
			positiveVisu.addInvisiblePoint(counter, 0);
		}
		restartVisu.addInvisiblePoint(counter, 0);
		cleanVisu.addInvisiblePoint(counter, 0);
		counter++;
	}

	@Override
	public void restarting() {
		restartVisu.addPoint(counter, nVar);
		cleanVisu.addPoint(counter, 0);
		positiveVisu.addInvisiblePoint(counter, 0);
		negativeVisu.addInvisiblePoint(counter, 0);
	}

	@Override
	public void end(Lbool result) {
		positiveVisu.end();
		negativeVisu.end();
		restartVisu.end();
		cleanVisu.end();
	}

	@Override
	public void start() {
		counter = 1;
	}

	@Override
	public void init(ISolverService solverService) {
		this.nVar = solverService.nVars();
		this.positiveVisu.init();
		this.negativeVisu.init();
		this.restartVisu.init();
		this.cleanVisu.init();
	}

	@Override
	public void cleaning() {
		restartVisu.addPoint(counter, 0);
		cleanVisu.addPoint(counter, nVar);
		positiveVisu.addInvisiblePoint(counter, 0);
		negativeVisu.addInvisiblePoint(counter, 0);
	}

}
