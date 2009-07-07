package org.sat4j.minisat.core;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class BugReset {

	@Test
	public void testBugKostya() throws TimeoutException, ContradictionException {
		ISolver solver = SolverFactory.newDefault();
		solver.setTimeout(3600);

		boolean res;

		// test empty
		assertTrue(solver.isSatisfiable());
		solver.reset();

		// test one statement
		solver.newVar(1);
		int[] clause = new int[] { -4 };
		// the addClause method in this case returns null! It is imposible to
		// remove this
		// fact from a knowledge base. Javadoc does not say anything about this
		// exception.
		solver.addClause(new VecInt(clause));
		res = solver.isSatisfiable();
		assertTrue(res);
		solver.reset();

		// test multiply statements
		solver.newVar(4);
		clause = new int[] { -1, -2, -3, 4 };
		solver.addClause(new VecInt(clause));
		clause = new int[] { 1 };
		solver.addClause(new VecInt(clause));
		clause = new int[] { 2 };
		solver.addClause(new VecInt(clause));
		clause = new int[] { 3 };
		solver.addClause(new VecInt(clause));
		assertTrue(solver.isSatisfiable()); // ArrayIndexOutOfBoundsException
	}
}
