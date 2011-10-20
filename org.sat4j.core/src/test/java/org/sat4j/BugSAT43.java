package org.sat4j;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class BugSAT43 {

	@Test
	public void testNoDeclaredVariables() throws ContradictionException,
			TimeoutException {
		ISolver solver = SolverFactory.newDefault();
		assertEquals(0, solver.nVars());
		assertEquals(0, solver.realNumberOfVariables());
		for (int i = 0; i < 10; i++) {
			solver.nextFreeVarId(true);
		}
		assertEquals(10, solver.nVars());
		assertEquals(10, solver.realNumberOfVariables());
		solver.addClause(new VecInt(new int[] { 1, 2, 3 }));
		int[] model1 = solver.findModel();
		assertEquals(3, model1.length);
		int[] model2 = solver.modelWithInternalVariables();
		assertEquals(3, model2.length);
	}

	@Test
	public void testDeclaredVariables() throws ContradictionException,
			TimeoutException {
		ISolver solver = SolverFactory.newDefault();
		solver.newVar(10);
		assertEquals(10, solver.nVars());
		assertEquals(10, solver.realNumberOfVariables());
		solver.addClause(new VecInt(new int[] { 1, 2, 3 }));
		int[] model1 = solver.findModel();
		assertEquals(3, model1.length);
		int[] model2 = solver.modelWithInternalVariables();
		assertEquals(3, model2.length);
		for (int i = 0; i < 10; i++) {
			solver.nextFreeVarId(true);
		}
		assertEquals(10, solver.nVars());
		assertEquals(20, solver.realNumberOfVariables());
		model1 = solver.findModel();
		assertEquals(3, model1.length);
		System.out.println(new VecInt(model1));
		model2 = solver.modelWithInternalVariables();
		assertEquals(3, model2.length);
		int[] clause = { 14, 16, 19 };
		solver.addClause(new VecInt(clause));
		model1 = solver.findModel();
		assertEquals(3, model1.length);
		System.out.println(new VecInt(model1));
		model2 = solver.modelWithInternalVariables();
		assertEquals(6, model2.length);
	}

	@Test
	public void implicitDeclarationOfVariables() throws ContradictionException,
			TimeoutException {
		ISolver solver = SolverFactory.newDefault();
		assertEquals(0, solver.nVars());
		assertEquals(0, solver.realNumberOfVariables());
		solver.addClause(new VecInt(new int[] { 1, 2, 3 }));
		assertEquals(3, solver.nVars());
		assertEquals(3, solver.realNumberOfVariables());
		for (int i = 0; i < 10; i++) {
			solver.nextFreeVarId(true);
		}
		assertEquals(13, solver.nVars());
		assertEquals(13, solver.realNumberOfVariables());
		int[] model1 = solver.findModel();
		assertEquals(3, model1.length);
		int[] model2 = solver.modelWithInternalVariables();
		assertEquals(3, model2.length);
	}
}
