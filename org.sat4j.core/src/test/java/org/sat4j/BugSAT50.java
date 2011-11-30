package org.sat4j;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.SubsetVarOrder;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class BugSAT50 {

	@Test
	public void test() throws ContradictionException, TimeoutException {
		Solver<DataStructureFactory> solver = SolverFactory.newGlucose();
		int[] backdoor = { 1, 2, 3 };
		IOrder order = new SubsetVarOrder(backdoor);
		solver.setOrder(order);
		IVecInt clause = new VecInt();
		clause.push(1).push(4);
		solver.addClause(clause);
		clause = new VecInt();
		clause.push(2).push(5);
		solver.addClause(clause);
		clause = new VecInt();
		clause.push(3).push(6);
		solver.addClause(clause);
		assertTrue(solver.isSatisfiable());
	}

	@Test
	public void test2() throws ContradictionException, TimeoutException {
		Solver<DataStructureFactory> solver = SolverFactory.newGlucose();
		int[] backdoor = { 1, 2, 3 };
		IOrder order = new SubsetVarOrder(backdoor);
		solver.setOrder(order);
		IVecInt clause = new VecInt();
		clause.push(-1).push(4);
		solver.addClause(clause);
		clause = new VecInt();
		clause.push(-2).push(5);
		solver.addClause(clause);
		clause = new VecInt();
		clause.push(-3).push(6);
		solver.addClause(clause);
		assertTrue(solver.isSatisfiable());
	}
}
