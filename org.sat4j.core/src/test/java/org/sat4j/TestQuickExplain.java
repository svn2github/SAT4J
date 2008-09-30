package org.sat4j;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.QuickXplain;

public class TestQuickExplain {

	@Test
	public void testGlobalInconsistency() throws ContradictionException, TimeoutException {
		QuickXplain<ISolver> solver = new QuickXplain<ISolver>(SolverFactory.newDefault());
		solver.newVar(2);
		IVecInt clause = new VecInt();
		clause.push(1).push(2);
		solver.addClause(clause);
		clause.clear();
		clause.push(1).push(-2);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(2);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(-2);
		solver.addClause(clause);
		clause.clear();
		assertFalse(solver.isSatisfiable());
		IVecInt explanation = solver.explain();
		assertEquals(4,explanation.size());
	}
	
	@Test
	public void testAlmostGlobalInconsistency() throws ContradictionException, TimeoutException {
		QuickXplain<ISolver> solver = new QuickXplain<ISolver>(SolverFactory.newDefault());
		solver.newVar(3);
		IVecInt clause = new VecInt();
		clause.push(1).push(2);
		solver.addClause(clause);
		clause.clear();
		clause.push(1).push(-2);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(2);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(-2);
		solver.addClause(clause);
		clause.clear();
		clause.push(1).push(3);
		solver.addClause(clause);
		clause.clear();
		assertFalse(solver.isSatisfiable());
		IVecInt explanation = solver.explain();
		assertEquals(4,explanation.size());
		assertTrue(explanation.contains(1));
		assertTrue(explanation.contains(2));
		assertTrue(explanation.contains(3));
		assertTrue(explanation.contains(4));
	}
	
	@Test
	public void testAlmostGlobalInconsistencyII() throws ContradictionException, TimeoutException {
		QuickXplain<ISolver> solver = new QuickXplain<ISolver>(SolverFactory.newDefault());
		solver.newVar(3);
		IVecInt clause = new VecInt();
		clause.push(1).push(2);
		solver.addClause(clause);
		clause.clear();
		clause.push(1).push(-2);
		solver.addClause(clause);
		clause.clear();
		clause.push(1).push(3);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(2);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(-2);
		solver.addClause(clause);
		clause.clear();
		assertFalse(solver.isSatisfiable());
		IVecInt explanation = solver.explain();
		assertEquals(4,explanation.size());
		assertTrue(explanation.contains(1));
		assertTrue(explanation.contains(2));
		assertTrue(explanation.contains(4));
		assertTrue(explanation.contains(5));
	}
}
