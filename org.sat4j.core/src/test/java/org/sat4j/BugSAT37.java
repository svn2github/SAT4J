package org.sat4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class BugSAT37 {

	@Test
	public void testSolver() {
		ISolver s = SolverFactory.newDefault();
		int resVars = s.newVar(6);
		assertEquals(6, resVars);
		try {
			s.addClause(new VecInt(new int[] { -1, -3 }));
			s.addClause(new VecInt(new int[] { -2, -4 }));
			s.addClause(new VecInt(new int[] { 1 }));
			s.addClause(new VecInt(new int[] { 3 }));
			IConstr r = s.addAtMost(new VecInt(new int[] { 5, 6 }), 1);
			int[] model = s.findModel();
			assertNull(model);
			s.removeConstr(r);
			model = s.findModel();
			assertNull(model);
		} catch (ContradictionException e) {
			return;
		} catch (TimeoutException e) {
			return;
		}
	}

}
