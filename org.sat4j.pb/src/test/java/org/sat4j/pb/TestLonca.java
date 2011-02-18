package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigInteger;

import org.junit.Test;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

public class TestLonca {

	@Test
	public void testIteratingWithNoObjectiveFunction() {
		IPBSolver solver = buildSolver1();

		IProblem problem = solver;
		int nbModel = 0;
		try {
			while (problem.isSatisfiable()) {
				int[] mod = problem.model();
				solver.addBlockingClause(new VecInt(invert(mod)));
				nbModel++;
			}
		} catch (TimeoutException e) {
			fail();
		} catch (ContradictionException e) {
			fail();
		}
		assertEquals(4, nbModel);
	}

	@Test
	public void testIteratingWithObjectiveFunction() {
		IPBSolver solver = buildSolver2();
		IProblem problem = solver;
		int nbModel = 0;
		try {
			while (problem.isSatisfiable()) {
				int[] mod = problem.model();
				solver.addBlockingClause(new VecInt(invert(mod)));
				nbModel++;
			}
		} catch (TimeoutException e) {
			fail();
		} catch (ContradictionException e) {
			fail();
		}
		assertEquals(4, nbModel);
	}

	@Test
	public void testIteratingWithObjectiveFunctionWithDecorator() {
		IPBSolver solver = buildSolver2();

		IProblem problem = new ModelIterator(solver);
		int nbModel = 0;
		try {
			while (problem.isSatisfiable()) {
				problem.model(); // needed to discard that model
				nbModel++;
			}
		} catch (TimeoutException e) {
			fail();
		}
		assertEquals(4, nbModel);
	}

	private static int[] invert(int[] mod) {
		int[] res = new int[mod.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = -mod[i];
		}
		return res;
	}

	private static IPBSolver buildSolver1() {
		IPBSolver solver = new OptToPBSATAdapter(new PseudoOptDecorator(
				SolverFactory.newResolution()));

		try {
			solver.addClause(new VecInt(new int[] { 1, 2, 3 }));
			solver.addClause(new VecInt(new int[] { -1, -2 }));
			solver.addClause(new VecInt(new int[] { -2, -3 }));
		} catch (ContradictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return solver;
	}

	private static IPBSolver buildSolver2() {
		IPBSolver solver = buildSolver1();

		// debut ajout fonction objectif
		IVecInt vars = new VecInt(new int[] { 1, 2, 3 });
		IVec<BigInteger> coeffs = new Vec<BigInteger>(new BigInteger[] {
				BigInteger.valueOf(8), BigInteger.valueOf(4),
				BigInteger.valueOf(2) });
		ObjectiveFunction func = new ObjectiveFunction(vars, coeffs);

		solver.setObjectiveFunction(func);
		// fin ajout fonction objectif

		return solver;
	}

}
