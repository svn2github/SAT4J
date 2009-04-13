package org.sat4j.pb;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public class PseudoAllSolutionDecorator extends PseudoOptDecorator {

	public PseudoAllSolutionDecorator(IPBSolver solver) {
		super(solver);
	}

	@Override
	public void discardCurrentSolution() throws ContradictionException {
		int[] last = super.model();
		IVecInt clause = new VecInt(last.length);
		for (int q : last) {
			clause.push(-q);
		}
		addClause(clause);
	}

}
