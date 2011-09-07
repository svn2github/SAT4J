package org.sat4j.pb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

@RunWith(Parameterized.class)
public class BugSAT34 {

	ISolver system;
	String solvername;

	public BugSAT34(ISolver system, String solvername) {
		this.system = system;
		this.solvername = solvername;
	}

	@Parameters
	public static Collection<Object[]> generateSolvers() {
		Collection<Object[]> solvers = new ArrayList<Object[]>();
		for (String name : SolverFactory.instance().solverNames()) {
			if (!"DimacsOutput".equals(name) && !"OPBStringSolver".equals(name))
				solvers.add(new Object[] {
						SolverFactory.instance().createSolverByName(name), name });
		}
		return solvers;
	}

	@Test
	public void testUnitClause() {
		system.newVar(3);
		try {
			// x_1 v x_2 <=> x_3
			system.addClause(new VecInt(new int[] { 1, 2, -3 }));
			system.addClause(new VecInt(new int[] { -1, 3 }));
			system.addClause(new VecInt(new int[] { -2, 3 }));
			// not both true
			system.addClause(new VecInt(new int[] { -1, -2 }));
			// x2 is true
			IConstr unit = system.addClause(new VecInt(new int[] { 2 }));
			assertNotNull(solvername + " has unit clause problem", unit);
			// x3 is true
			IConstr cl = system.addAtLeast(new VecInt(new int[] { 3 }), 1);
			assertNotNull(solvername + " has unit clause problem", cl);
			int[] model = system.findModel();
			assertNotNull(solvername + " has a model problem", model);
			system.removeConstr(cl);
			// x3 is true
			cl = system.addAtLeast(new VecInt(new int[] { 3 }), 1);
			assertNotNull(solvername + " has unit clause problem", cl);
			// x1 is true
			// so here x1, x2 and x3 is true
			IConstr r = system.addClause(new VecInt(new int[] { 1 }));
			assertNotNull(r);
			assertNotNull(solvername + " has unit clause problem", r);
			model = system.findModel();
			assertNull(solvername + " has a model problem", model);
		} catch (ContradictionException e) {
		} catch (TimeoutException e) {
		}
		;
	}

}
