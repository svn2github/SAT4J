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

import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;

public class SpeedTracing extends SearchListenerAdapter<ISolverService> {

	// private int maxDlevel;

	private static final long serialVersionUID = 1L;

	private final IVisualizationTool visuTool;
	private final IVisualizationTool cleanVisuTool;
	private final IVisualizationTool restartVisuTool;

	private long begin, end;
	private int counter;
	private long index;

	private int nVar;

	public SpeedTracing(IVisualizationTool visuTool,
			IVisualizationTool cleanVisuTool, IVisualizationTool restartVisuTool) {
		this.visuTool = visuTool;
		this.cleanVisuTool = cleanVisuTool;
		this.restartVisuTool = restartVisuTool;

		visuTool.init();
		cleanVisuTool.init();
		restartVisuTool.init();

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
			visuTool.addPoint(index / 1000.0, counter / tmp * 1000);
			cleanVisuTool.addPoint(index / 1000.0, 0);
			restartVisuTool.addPoint(index / 1000.0, 0);
			begin = System.currentTimeMillis();
			counter = 0;
		}
		counter++;
	}

	@Override
	public void end(Lbool result) {
		visuTool.end();
		cleanVisuTool.end();
		restartVisuTool.end();
	}

	@Override
	public void cleaning() {
		end = System.currentTimeMillis();
		long indexClean = index + (end - begin);
		visuTool.addPoint(indexClean / 1000.0, counter / (end - begin) * 1000);
		cleanVisuTool.addPoint(indexClean / 1000.0, nVar);
		restartVisuTool.addInvisiblePoint(indexClean, 0);
		// out.println("# ignore");
	}

	@Override
	public void restarting() {
		end = System.currentTimeMillis();
		long indexRestart = index + (end - begin);
		visuTool.addPoint(indexRestart / 1000.0, counter / (end - begin) * 1000);
		restartVisuTool.addPoint(indexRestart / 1000.0, nVar);
		cleanVisuTool.addInvisiblePoint(indexRestart, 0);
	}

	@Override
	public void start() {
		visuTool.init();
		cleanVisuTool.init();
		restartVisuTool.init();

		begin = System.currentTimeMillis();
		counter = 0;
		index = 0;
	}

	@Override
	public void init(ISolverService solverService) {
		nVar = solverService.nVars();
	}
}
