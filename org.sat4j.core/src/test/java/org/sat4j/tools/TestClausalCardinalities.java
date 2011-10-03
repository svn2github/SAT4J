package org.sat4j.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class TestClausalCardinalities {

	private ISolver solver;

	@Before
	public void setUp() {
		solver = new ClausalCardinalitiesDecorator<ISolver>(SolverFactory
				.newDefault());
	}

	@Test
	public void testSimpleCardCase() throws ContradictionException,
			TimeoutException {
		solver.newVar(5);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5);
		IConstr constr1 = solver.addClause(clause);
		assertNotNull(constr1);
		IConstr constr2 = solver.addAtMost(clause, 1);
		assertNotNull(constr2);
		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		int cpt = 0;
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
		assertEquals(5, cpt);
	}

	@Test
	public void testSimpleCardCaseFor2() throws ContradictionException,
			TimeoutException {
		solver.newVar(5);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5);
		IConstr constr1 = solver.addClause(clause);
		assertNotNull(constr1);
		IConstr constr2 = solver.addAtMost(clause, 2);
		assertNotNull(constr2);
		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		int cpt = 0;
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
	}

	@Test
	public void testSimpleCardCaseForUnsat() throws ContradictionException,
			TimeoutException {
		solver.newVar(5);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5);
		IConstr constr1 = solver.addClause(clause);
		assertNotNull(constr1);
		IConstr constr2 = solver.addAtMost(clause, 0);
		assertNotNull(constr2);
		assertFalse(solver.isSatisfiable());
	}
}
