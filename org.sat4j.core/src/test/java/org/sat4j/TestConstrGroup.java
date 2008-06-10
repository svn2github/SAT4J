package org.sat4j;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ConstrGroup;

public class TestConstrGroup {

	@Test
	public void testDeleteGroup() throws ContradictionException {
		ISolver solver = SolverFactory.newDefault();
		ConstrGroup g1 = new ConstrGroup();
		IVecInt clause = new VecInt(new int[] { 1, 2, -3 });
		solver.addClause(clause);
		// starting group
		clause.clear();
		clause.push(2).push(-3).push(-5);
		g1.add(solver.addClause(clause));
		clause.clear();
		clause.push(-3).push(-2).push(-4);
		g1.add(solver.addClause(clause));
		assertEquals(3, solver.nConstraints());
		g1.removeFrom(solver);
		assertEquals(1, solver.nConstraints());
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void cannotPutAUnitClauseInAGroup() throws ContradictionException {
		ISolver solver = SolverFactory.newDefault();
		ConstrGroup g1 = new ConstrGroup();

		IVecInt clause = new VecInt(new int[] { 1 });
		g1.add(solver.addClause(clause));
	}

	@Test
	public void checkBugReportedByThomas() throws ContradictionException {
		ISolver solver = SolverFactory.newDefault();
		ConstrGroup g1 = new ConstrGroup();

		IVecInt clause = new VecInt(new int[] { 1 });
		solver.addClause(clause);

		// starting group
		clause.clear();
		clause.push(2).push(-3).push(-5);
		g1.add(solver.addClause(clause));

		clause.clear();
		clause.push(-3).push(-2).push(-4);
		g1.add(solver.addClause(clause));
		assertEquals(3, solver.nConstraints());

		g1.removeFrom(solver);
		assertEquals(1, solver.nConstraints());
	}
	
	@Test
	public void checkItWorksAfterRunningTheSolver() throws ContradictionException, TimeoutException {
		ISolver solver = SolverFactory.newDefault();
		ConstrGroup g1 = new ConstrGroup();

		IVecInt clause = new VecInt(new int[] { 1 });
		solver.addClause(clause);

		// starting group
		clause.clear();
		clause.push(-1).push(-2).push(-3);
		g1.add(solver.addClause(clause));

		clause.clear();
		clause.push(-1).push(2).push(-3);
		g1.add(solver.addClause(clause));
		assertEquals(3, solver.nConstraints());
		assertTrue(solver.isSatisfiable());
		assertTrue(solver.model(1));
		assertFalse(solver.model(3));
		g1.removeFrom(solver);
		assertEquals(1, solver.nConstraints());
	}
	
	@Test(expected = java.lang.IllegalArgumentException.class)
	public void checkGroupDoesNotWorkWhenClausesAreReducedByUnitPropgation() throws ContradictionException {
		ISolver solver = SolverFactory.newDefault();
		ConstrGroup g1 = new ConstrGroup();

		IVecInt clause = new VecInt(new int[] { 1 });
		solver.addClause(clause);

		// starting group
		clause.clear();
		clause.push(-1).push(-2);
		g1.add(solver.addClause(clause));
	}
	
	@Test
	public void checkTheExpectedWayToDealWithUnitClausesToRemove() throws ContradictionException, TimeoutException {
		ISolver solver = SolverFactory.newDefault();
		ConstrGroup g1 = new ConstrGroup();

		IVecInt clause = new VecInt(new int[] { 1 });
		solver.addClause(clause);

		// starting group
		clause.clear();
		clause.push(2).push(-3);		
		g1.add(solver.addClause(clause));
	
		clause.clear();
		clause.push(-2).push(4);		
		g1.add(solver.addClause(clause));
		
		IVecInt unitClauses = new VecInt(new int[] {3,-4});
		
		assertFalse(solver.isSatisfiable(unitClauses));
		
		g1.removeFrom(solver);
		assertTrue(solver.isSatisfiable(unitClauses));
	}
}
