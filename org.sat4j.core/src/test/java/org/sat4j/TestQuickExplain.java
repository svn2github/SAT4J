/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *******************************************************************************/
package org.sat4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.xplain.Xplain;

public class TestQuickExplain {

	@Test
	public void testGlobalInconsistency() throws ContradictionException, TimeoutException {
		Xplain<ISolver> solver = new Xplain<ISolver>(SolverFactory.newDefault());
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
		Xplain<ISolver> solver = new Xplain<ISolver>(SolverFactory.newDefault());
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
		Xplain<ISolver> solver = new Xplain<ISolver>(SolverFactory.newDefault());
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
	
	@Test
	public void testTheCaseOfTwoMUSes() throws ContradictionException, TimeoutException {
		Xplain<ISolver> solver = new Xplain<ISolver>(SolverFactory.newDefault());
		solver.newVar(4);
		IVecInt clause = new VecInt();
		clause.push(1).push(2);
		solver.addClause(clause);
		clause.clear();
		clause.push(1).push(-2);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(3);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(-3);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(4);
		solver.addClause(clause);
		clause.clear();
		clause.push(-1).push(-4);
		solver.addClause(clause);
		clause.clear();
		assertFalse(solver.isSatisfiable());
		IVecInt explanation = solver.explain();
		assertEquals(4,explanation.size());
		assertTrue(explanation.contains(1));
		assertTrue(explanation.contains(2));
	}
	
	@Test
	public void testEclipseTestCase() throws ContradictionException, TimeoutException {
		Xplain<ISolver> solver = new Xplain<ISolver>(SolverFactory.newDefault());
		solver.newVar(100000);
		IVecInt clause = new VecInt();
		clause.push(-1);
		solver.addClause(clause);
		clause.clear();
		clause.push(-2).push(3);
		solver.addClause(clause);
		clause.clear();
		clause.push(-2).push(1);
		solver.addClause(clause);
		clause.clear();
		clause.push(2);
		solver.addClause(clause);
		clause.clear();
		assertFalse(solver.isSatisfiable());
		IVecInt explanation = solver.explain();
		assertEquals(3,explanation.size());
	}
}
