package org.sat4j.pb.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Collection;

import org.junit.Test;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.tools.XplainPB;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class TestQuickExplainPB {

	@Test
	public void testGlobalInconsistency() throws ContradictionException,
			TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(2);
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		IVecInt clause = new VecInt();
		clause.push(1).push(2);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(1).push(-2);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(2);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(-2);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		assertFalse(solver.isSatisfiable());
		Collection<IConstr> explanation = solver.explain();
		assertEquals(4, explanation.size());
	}

	@Test
	public void testGlobalInconsistencyPB() throws ContradictionException,
			TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(4);
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.push(BigInteger.valueOf(3)).push(BigInteger.valueOf(2)).push(
				BigInteger.ONE);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3);
		IConstr c1 = solver.addPseudoBoolean(clause, coeffs, true, BigInteger
				.valueOf(4));
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(3).push(4);
		coeffs.push(BigInteger.valueOf(3)).push(BigInteger.ONE).push(
				BigInteger.ONE);
		IConstr c2 = solver.addPseudoBoolean(clause, coeffs, true, BigInteger
				.valueOf(4));
		clause.clear();
		coeffs.clear();
		assertFalse(solver.isSatisfiable());
		Collection<IConstr> explanation = solver.explain();
		assertEquals(2, explanation.size());
		assertTrue(explanation.contains(c1));
		assertTrue(explanation.contains(c2));
	}

	@Test
	public void testAlmostGlobalInconsistency() throws ContradictionException,
			TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(3);
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		IVecInt clause = new VecInt();
		clause.push(1).push(2);
		IConstr c1 = solver.addPseudoBoolean(clause, coeffs, true,
				BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(1).push(-2);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		IConstr c2 = solver.addPseudoBoolean(clause, coeffs, true,
				BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(2);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		IConstr c3 = solver.addPseudoBoolean(clause, coeffs, true,
				BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(-2);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		IConstr c4 = solver.addPseudoBoolean(clause, coeffs, true,
				BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(1).push(3);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		assertFalse(solver.isSatisfiable());
		Collection<IConstr> explanation = solver.explain();
		assertEquals(4, explanation.size());
		assertTrue(explanation.contains(c1));
		assertTrue(explanation.contains(c2));
		assertTrue(explanation.contains(c3));
		assertTrue(explanation.contains(c4));
	}

	@Test
	public void testAlmostGlobalInconsistencyII()
			throws ContradictionException, TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(3);
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		IVecInt clause = new VecInt();
		clause.push(1).push(2);
		IConstr c1 = solver.addPseudoBoolean(clause, coeffs, true,
				BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(1).push(-2);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		IConstr c2 = solver.addPseudoBoolean(clause, coeffs, true,
				BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(1).push(3);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(2);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		IConstr c4 = solver.addPseudoBoolean(clause, coeffs, true,
				BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(-2);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		IConstr c5 = solver.addPseudoBoolean(clause, coeffs, true,
				BigInteger.ONE);
		clause.clear();
		coeffs.clear();
		assertFalse(solver.isSatisfiable());
		Collection<IConstr> explanation = solver.explain();
		assertEquals(4, explanation.size());
		assertTrue(explanation.contains(c1));
		assertTrue(explanation.contains(c2));
		assertTrue(explanation.contains(c4));
		assertTrue(explanation.contains(c5));
	}

	@Test
	public void testAlmostGlobalInconsistencyPB()
			throws ContradictionException, TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(4);
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.push(BigInteger.valueOf(3)).push(BigInteger.valueOf(2)).push(
				BigInteger.ONE);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3);
		IConstr c1 = solver.addPseudoBoolean(clause, coeffs, true, BigInteger
				.valueOf(4));
		clause.clear();
		coeffs.clear();
		clause.push(2).push(-3).push(4);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.valueOf(2));
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(3).push(4);
		coeffs.push(BigInteger.valueOf(3)).push(BigInteger.ONE).push(
				BigInteger.ONE);
		IConstr c3 = solver.addPseudoBoolean(clause, coeffs, true, BigInteger
				.valueOf(4));
		clause.clear();
		coeffs.clear();
		assertFalse(solver.isSatisfiable());
		Collection<IConstr> explanation = solver.explain();
		assertEquals(2, explanation.size());
		assertTrue(explanation.contains(c1));
		assertTrue(explanation.contains(c3));
	}

	@Test
	public void testEclipsePatchEncoding() throws ContradictionException,
			TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(12);
		IVecInt clause = new VecInt();
		clause.push(-1).push(-2).push(3);
		solver.addClause(clause);
		clause.clear();
		clause.push(-2).push(1).push(5);
		solver.addClause(clause);
		clause.clear();
		clause.push(-7).push(-2).push(8);
		solver.addClause(clause);
		clause.clear();
		clause.push(-2).push(7).push(5);
		solver.addClause(clause);
		clause.clear();
		clause.push(3).push(5).push(8);
		solver.addAtMost(clause, 1);
		clause.clear();
		clause.push(-12).push(1);
		solver.addClause(clause);
		clause.clear();
		clause.push(-12).push(2);
		solver.addClause(clause);
		clause.clear();
		clause.push(-12).push(7);
		IConstr patch = solver.addClause(clause);
		IVecInt assump = new VecInt();
		assump.push(12);
		assertFalse(solver.isSatisfiable(assump));
		Collection<IConstr> explanation = solver.explain();
		assertEquals(6, explanation.size());
		assertTrue(explanation.contains(patch));
	}

	@Test
	public void testUpdatedEclipsePatchEncoding()
			throws ContradictionException, TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(12);
		IVecInt clause = new VecInt();
		clause.push(-1).push(-2).push(3);
		solver.addClause(clause);
		clause.clear();
		clause.push(-7).push(-2).push(8);
		solver.addClause(clause);
		clause.clear();
		clause.push(-2).push(7).push(5).push(1);
		solver.addClause(clause);
		clause.clear();
		clause.push(3).push(5).push(8);
		solver.addAtMost(clause, 1);
		clause.clear();
		clause.push(-12).push(1);
		solver.addClause(clause);
		clause.clear();
		clause.push(-12).push(2);
		solver.addClause(clause);
		clause.clear();
		clause.push(-12).push(7);
		IConstr patch = solver.addClause(clause);
		IVecInt assump = new VecInt();
		assump.push(12);
		assertFalse(solver.isSatisfiable(assump));
		Collection<IConstr> explanation = solver.explain();
		assertEquals(6, explanation.size());
		assertTrue(explanation.contains(patch));
	}
}
