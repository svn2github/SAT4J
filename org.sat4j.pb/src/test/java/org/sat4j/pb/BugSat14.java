package org.sat4j.pb;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class BugSat14 {

	@Test
	public void testSatCallAfterExpireTimeout() throws ContradictionException {
		ISolver solver = SolverFactory.newDefault();
		IVecInt clause = new VecInt();
		for (int i = 4; i < 1000; i++) {
			clause.push(1).push(-2).push(3).push(i);
			solver.addClause(clause);
			clause.clear();
		}
		for (int i = 4; i < 10000; i++) {
			clause.push(-i);
		}
		solver.addClause(clause);
		clause.clear();
		clause.push(10).push(-20);
		solver.addClause(clause);
		clause.clear();
		clause.push(-10).push(20);
		solver.addClause(clause);
		clause.clear();
		clause.push(10).push(20);
		solver.addClause(clause);
		clause.clear();
		clause.push(-10).push(-20);
		solver.addClause(clause);
		clause.clear();
		solver.setTimeoutMs(10);
		try {
			boolean result = solver.isSatisfiable(true);
		} catch (TimeoutException e) {
			System.out.println("Exception launched");
		}
		solver.expireTimeout();
		solver.setTimeout(300);
		try {
			boolean result = solver.isSatisfiable(true);
		} catch (TimeoutException e) {
			fail();
		}
	}
}
