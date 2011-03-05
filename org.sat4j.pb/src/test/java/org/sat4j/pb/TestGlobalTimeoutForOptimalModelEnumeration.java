package org.sat4j.pb;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

public class TestGlobalTimeoutForOptimalModelEnumeration {

	private ISolver solver;

	@Before
	public void setUp() throws ContradictionException {
		IPBSolver pbsolver = new OptToPBSATAdapter(new PseudoOptDecorator(
				SolverFactory.newDefault()));
		IVecInt clause = new VecInt();
		for (int i = 1; i <= 1000; i++)
			clause.push(-i);
		pbsolver.addClause(clause);
		Vec<BigInteger> weights = new Vec<BigInteger>();
		for (int i = 1; i <= 1000; i += 2) {
			weights.push(BigInteger.valueOf(5));
			weights.push(BigInteger.valueOf(10));
		}
		pbsolver.setObjectiveFunction(new ObjectiveFunction(clause, weights));
		solver = new ModelIterator(pbsolver);
	}

	@Test(expected = TimeoutException.class, timeout = 2500)
	public void testTimeoutOnSeconds() throws TimeoutException {
		solver.setTimeout(2);
		while (solver.isSatisfiable()) {
			solver.model(); // needed to discard that solution
		}
	}

	@Test(expected = TimeoutException.class, timeout = 2500)
	public void testTimeoutOnConflicts() throws TimeoutException {
		solver.setTimeoutOnConflicts(1000);
		while (solver.isSatisfiable()) {
			solver.model(); // needed to discard that solution
		}
	}
}
