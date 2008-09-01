package org.sat4j.maxsat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.OptToSatAdapter;

public class MichalBug {

	@Test
	public void testMichalReportedProblem() throws ContradictionException,
			TimeoutException {
		WeightedMaxSatDecorator maxSATSolver = new WeightedMaxSatDecorator(
				SolverFactory.newLight());

		final int OPTIMUM_FOUND = 0;
		final int UNSATISFIABLE = 1;

		maxSATSolver.newVar(2);
		maxSATSolver.setExpectedNumberOfClauses(4);

		int[] clause_1 = { 1, 1, 2 };
		maxSATSolver.addClause(new VecInt(clause_1));

		int[] clause_2 = { 100, -1, -2 };
		maxSATSolver.addClause(new VecInt(clause_2));

		int[] clause_3 = { 1000, 1, -2 };
		maxSATSolver.addClause(new VecInt(clause_3));

		int[] clause_4 = { 100000, -1, 2 };
		maxSATSolver.addClause(new VecInt(clause_4));

		IOptimizationProblem problem = maxSATSolver;

		int exitCode = UNSATISFIABLE;
		boolean isSatisfiable = false;
		try {
			while (problem.admitABetterSolution()) {
				isSatisfiable = true;
				problem.discardCurrentSolution();
			}
			if (isSatisfiable) {
				exitCode = OPTIMUM_FOUND;
			} else {
				exitCode = UNSATISFIABLE;
			}
		} catch (ContradictionException ex) {
			assert (isSatisfiable);
			exitCode = OPTIMUM_FOUND;
		}

		assertEquals(OPTIMUM_FOUND,exitCode);
		int[] model = problem.model();
		assertEquals(2,model.length);
		assertEquals(-1,model[0]);
		assertEquals(-2,model[1]);
	}
	
	@Test
	public void testMichalWithOptAdapter() throws ContradictionException,
			TimeoutException {
		WeightedMaxSatDecorator maxSATSolver = new WeightedMaxSatDecorator(
				SolverFactory.newLight());

		maxSATSolver.newVar(2);
		maxSATSolver.setExpectedNumberOfClauses(4);

		int[] clause_1 = { 1, 1, 2 };
		maxSATSolver.addClause(new VecInt(clause_1));

		int[] clause_2 = { 100, -1, -2 };
		maxSATSolver.addClause(new VecInt(clause_2));

		int[] clause_3 = { 1000, 1, -2 };
		maxSATSolver.addClause(new VecInt(clause_3));

		int[] clause_4 = { 100000, -1, 2 };
		maxSATSolver.addClause(new VecInt(clause_4));

		IProblem problem = new OptToSatAdapter(maxSATSolver);

		boolean isSatisfiable = problem.isSatisfiable();

		assertEquals(true, isSatisfiable);
		int[] model = problem.model();
		assertEquals(2,model.length);
		assertEquals(-1,model[0]);
		assertEquals(-2,model[1]);
	}
}
