/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.ConstrGroup;
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
		solver = new ClausalCardinalitiesDecorator<ISolver>(
				SolverFactory.newDefault());
	}

	@Test
	public void testSimpleCardCase() throws ContradictionException,
			TimeoutException {
		solver.newVar(5);

		boolean debug = false;
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5);

		IConstr constr1 = solver.addClause(clause);
		assertNotNull(constr1);

		IConstr constr2 = solver.addAtMost(clause, 1);
		assertNotNull(constr2);

		if (debug) {
			for (int i = 0; i < constr2.size(); i++) {
				System.out.println(((ConstrGroup) constr2).getConstr(i));
			}
		}

		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		int cpt = 0;

		System.out.println("testSimpleCardCase models AMO + clause");
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
		assertEquals(5, cpt);

	}

	@Test
	public void testSimpleCardCase2Power() throws ContradictionException,
			TimeoutException {
		solver.newVar(4);

		boolean debug = false;
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4);

		IConstr constr1 = solver.addClause(clause);
		assertNotNull(constr1);

		IConstr constr2 = solver.addAtMost(clause, 1);
		assertNotNull(constr2);

		if (debug) {
			for (int i = 0; i < constr2.size(); i++) {
				System.out.println(((ConstrGroup) constr2).getConstr(i));
			}
		}

		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		int cpt = 0;

		System.out.println("testSimpleCardCase2Power models AMO + clause");
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
		assertEquals(4, cpt);

	}

	@Test
	public void testSimpleCardCaseAMO() throws ContradictionException,
			TimeoutException {
		solver.newVar(5);

		boolean debug = false;
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5);

		IConstr constr2 = solver.addAtMost(clause, 1);
		assertNotNull(constr2);

		if (debug) {
			System.out.println("Constraintes AMO");
			for (int i = 0; i < constr2.size(); i++) {
				System.out.println(((ConstrGroup) constr2).getConstr(i));
			}
		}

		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		int cpt = 0;
		System.out.println("testSimpleCardCase models AMO");
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
		assertEquals(6, cpt);

	}

	@Test
	public void testSimpleCardCaseAMOWith8Variables()
			throws ContradictionException, TimeoutException {
		solver.newVar(8);

		boolean debug = false;
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5).push(6).push(7).push(8);

		IConstr constr2 = solver.addAtMost(clause, 1);
		assertNotNull(constr2);

		if (debug) {
			System.out.println("Constraintes AMO 8 variables");
			for (int i = 0; i < constr2.size(); i++) {
				System.out.println(((ConstrGroup) constr2).getConstr(i));
			}
		}

		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		System.out.println("testSimpleCardCase models AMO 8 variables");
		int cpt = 0;
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
		assertEquals(9, cpt);

	}

	@Test
	public void testSimpleCardCaseEO() throws ContradictionException,
			TimeoutException {
		solver.newVar(5);

		boolean debug = false;
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5);

		IConstr constr = solver.addExactly(clause, 1);
		assertNotNull(constr);

		assertEquals(2, constr.size());

		if (debug) {
			System.out.println("Constraintes EO");
			for (int i = 0; i < constr.size(); i++) {
				System.out.println(((ConstrGroup) constr).getConstr(i));
			}
		}

		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		int cpt = 0;
		System.out.println("testSimpleCardCase models EO");
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
		System.out
				.println("testSimpleCardCaseFor2 - AMO + clauses - 5 variables");
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
		assertEquals(15, cpt);
	}

	@Test
	public void testSimpleCardCaseFor2With7Variables()
			throws ContradictionException, TimeoutException {

		boolean debug = false;

		int nbVar = 7;
		solver.newVar(nbVar);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5).push(6).push(7);
		IConstr constr1 = solver.addClause(clause);
		assertNotNull(constr1);
		IConstr constr2 = solver.addAtMost(clause, 2);
		assertNotNull(constr2);

		if (debug) {
			System.out
					.println("Constraints Simple card case for 2 with 7 variables");
			for (int i = 0; i < constr2.size(); i++) {
				System.out.println(((ConstrGroup) constr2).getConstr(i));
			}
		}

		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		int cpt = 0;
		System.out
				.println("testSimpleCardCaseFor2With7Variables models AMO + clause - 7 variables");
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
		assertEquals(28, cpt);
	}

	@Test
	public void testSimpleCardCaseFor2With8Variables()
			throws ContradictionException, TimeoutException {

		boolean debug = false;

		int nbVar = 8;
		solver.newVar(nbVar);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5).push(6).push(7).push(8);
		IConstr constr1 = solver.addClause(clause);
		assertNotNull(constr1);
		IConstr constr2 = solver.addAtMost(clause, 2);
		assertNotNull(constr2);

		if (debug) {
			System.out
					.println("Constraints Simple card case for 2 with 8 variables");
			for (int i = 0; i < constr2.size(); i++) {
				System.out.println(((ConstrGroup) constr2).getConstr(i));
			}
		}

		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		int cpt = 0;
		System.out
				.println("testSimpleCardCaseFor2With8Variables models AMO + clause - 8 variables");
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
		assertEquals(36, cpt);
	}

	@Test
	public void testSimpleCardCaseFor4With11Variables()
			throws ContradictionException, TimeoutException {

		boolean debug = false;

		int nbVar = 11;
		solver.newVar(nbVar);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3).push(4).push(5).push(6).push(7).push(8)
				.push(9).push(10).push(11);
		IConstr constr1 = solver.addClause(clause);
		assertNotNull(constr1);
		IConstr constr2 = solver.addAtMost(clause, 4);
		assertNotNull(constr2);

		if (debug) {
			System.out
					.println("Constraints Simple card case for 4 with 11 variables");
			for (int i = 0; i < constr2.size(); i++) {
				System.out.println(((ConstrGroup) constr2).getConstr(i));
			}
		}

		ModelIterator iterator = new ModelIterator(solver);
		int[] model = null;
		System.out
				.println("testSimpleCardCaseFor4With11Variables models AMO + clause - 11 variables");
		int cpt = 0;
		while (iterator.isSatisfiable()) {
			model = iterator.model();
			assertNotNull(model);
			System.out.println(new VecInt(model));
			cpt++;
		}
		assertEquals(561, cpt);
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

	@Test
	public void testName() {
		System.out.println(solver.toString());
	}
}
