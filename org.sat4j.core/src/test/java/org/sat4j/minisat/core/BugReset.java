package org.sat4j.minisat.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

	@Test
	public void testWithReset() throws TimeoutException, ContradictionException {
		ISolver solver = SolverFactory.newDefault();
		int[] clause;
		boolean res;

		// prepare the solver to accept MAXVAR variables. MANDATORY
		solver.newVar(4);
		// not mandatory for SAT solving. MANDATORY for MAXSAT solving
		solver.setExpectedNumberOfClauses(6);
		// Feed the solver using Dimacs format, using arrays of int
		// (best option to avoid dependencies on SAT4J IVecInt)

		// test empty
		res = solver.isSatisfiable();
		assertTrue(res);
		solver.reset();

		// test one clause
		solver.newVar(1);
		clause = new int[] { -4 };
		solver.addClause(new VecInt(clause));
		res = solver.isSatisfiable();
		assertTrue(res);
		solver.reset();

		// test multiply statements
		solver.newVar(4);
		addData(solver);
		assertTrue(solver.isSatisfiable());

		clause = new int[] { -4 };
		solver.addClause(new VecInt(clause));
		try {
			addData(solver);
			res = solver.isSatisfiable();
		} catch (ContradictionException e) {
			res = false;
		}
		assertFalse(res);
		solver.reset();
	}

	private void addData(ISolver solver) throws ContradictionException {
		int[] clause = new int[] { -1, -2, -3, 4 };
		solver.addClause(new VecInt(clause));

		clause = new int[] { 1 };
		solver.addClause(new VecInt(clause));

		clause = new int[] { 2 };
		solver.addClause(new VecInt(clause));

		clause = new int[] { 3 };
		solver.addClause(new VecInt(clause));

	}

	@Test
	public void problemTest() throws TimeoutException, ContradictionException {
		ISolver solver = SolverFactory.newDefault();
		solver.setTimeout(3600);
		solver.newVar(4);
		solver.setExpectedNumberOfClauses(5);

		for (int i = 0; i < 10; i++) {
			solve(solver, new int[] {}, true);
			solve(solver, new int[] { -4 }, false);
		}
	}

	private void solve(ISolver solver, int[] clause, boolean value)
			throws ContradictionException, TimeoutException {
		boolean res = true;
		try {
			if (clause.length > 0)
				solver.addClause(new VecInt(clause));
			clause = new int[] { -1, 2, 4 };
			solver.addClause(new VecInt(clause));
			clause = new int[] { 1, -2 };
			solver.addClause(new VecInt(clause));
			clause = new int[] { 1, 2 };
			solver.addClause(new VecInt(clause));
			clause = new int[] { -1, -2 };
			solver.addClause(new VecInt(clause));

		} catch (ContradictionException e) {
			res = false;
		}
		if (res) {
			res = solver.isSatisfiable();
		}
		solver.reset();
		solver.clearLearntClauses();
		assertEquals(res, value);
	}

}
