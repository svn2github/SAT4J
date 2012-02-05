package org.sat4j.tools;

import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;

abstract class SearchListenerAdapter implements SearchListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init(ISolverService solverService) {
	}

	public void assuming(int p) {
	}

	public void propagating(int p, IConstr reason) {
	}

	public void backtracking(int p) {
	}

	public void adding(int p) {
	}

	public void learn(IConstr c) {
	}

	public void delete(int[] clause) {
	}

	public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
	}

	public void conflictFound(int p) {
	}

	public void solutionFound() {
	}

	public void beginLoop() {
	}

	public void start() {
	}

	public void end(Lbool result) {
	}

	public void restarting() {
	}

	public void backjump(int backjumpLevel) {
	}

}
