package org.sat4j.pb.core;

import java.io.PrintWriter;

import org.sat4j.minisat.core.SolverStats;

public class PBSolverStats extends SolverStats {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public long numberOfReductions;

	public long numberOfLearnedConstraintsReduced;

	@Override
	public void reset() {
		super.reset();
		numberOfReductions = 0;
		numberOfLearnedConstraintsReduced = 0;
	}

	@Override
	public void printStat(PrintWriter out, String prefix) {
		super.printStat(out, prefix);
		out.println(prefix
				+ "number of reductions to clauses (during analyze)\t: "
				+ numberOfReductions);
		out.println(prefix
				+ "number of learned constraints concerned by reduction\t: "
				+ numberOfLearnedConstraintsReduced);
	}

}
