package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * @author sroussel
 * 
 */
public class BugSAT21 {

	@Test
	public void testAtLeastWithNegativeLiteralsAsText()
			throws ContradictionException {

		IPBSolver pbSolver = new OPBStringSolver();
		pbSolver.newVar(2);
		pbSolver.setExpectedNumberOfClauses(1);

		int[] constr = { -1, 2 };

		pbSolver.addAtLeast(new VecInt(constr), 1);
		String expected = "* #variable= 2 #constraint= 1 \n\n-1 x1 +1 x2 >= 0 ;\n";
		assertEquals(expected, pbSolver.toString());

	}

	@Test
	public void testAtLeastWithNegativeLiterals()
			throws ContradictionException, TimeoutException {

		IPBSolver pbSolver = SolverFactory.newDefault();
		pbSolver.newVar(2);
		pbSolver.setExpectedNumberOfClauses(1);

		int[] constr = { -1, 2 };

		pbSolver.addAtLeast(new VecInt(constr), 1);
		IVecInt assumps = new VecInt();
		assumps.push(1).push(-2);
		assertFalse(pbSolver.isSatisfiable(assumps));
		assumps.clear();
		assumps.push(-1).push(-2);
		assertTrue(pbSolver.isSatisfiable(assumps));
		assumps.clear();
		assumps.push(1).push(2);
		assertTrue(pbSolver.isSatisfiable(assumps));
		assumps.clear();
		assumps.push(-1).push(2);
		assertTrue(pbSolver.isSatisfiable(assumps));

	}

	@Test
	public void testAlMostWithNegativeLiteralsAsText()
			throws ContradictionException {

		IPBSolver pbSolver = new OPBStringSolver();
		pbSolver.newVar(2);
		pbSolver.setExpectedNumberOfClauses(1);

		int[] constr = { -1, 2 };

		pbSolver.addAtMost(new VecInt(constr), 1);
		String expected = "* #variable= 2 #constraint= 1 \n\n+1 x1 -1 x2 >= 0 ;\n";
		assertEquals(expected, pbSolver.toString());

	}
	
	@Test
	public void testAtMostWithNegativeLiterals()
			throws ContradictionException, TimeoutException {

		IPBSolver pbSolver = SolverFactory.newDefault();
		pbSolver.newVar(2);
		pbSolver.setExpectedNumberOfClauses(1);

		int[] constr = { -1, 2 };

		pbSolver.addAtMost(new VecInt(constr), 1);
		IVecInt assumps = new VecInt();
		assumps.push(1).push(-2);
		assertTrue(pbSolver.isSatisfiable(assumps));
		assumps.clear();
		assumps.push(-1).push(-2);
		assertTrue(pbSolver.isSatisfiable(assumps));
		assumps.clear();
		assumps.push(1).push(2);
		assertTrue(pbSolver.isSatisfiable(assumps));
		assumps.clear();
		assumps.push(-1).push(2);
		assertFalse(pbSolver.isSatisfiable(assumps));

	}

}
