package org.sat4j.pb.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.tools.XplainPB;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class TestQuickExplainPB {

	@Test
	public void testGlobalInconsistency() throws ContradictionException, TimeoutException {
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
		IVecInt explanation = solver.explain();
		assertEquals(4,explanation.size());
	}
	
	@Test
	public void testGlobalInconsistencyPB() throws ContradictionException, TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(4);
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.push(BigInteger.valueOf(3)).push(BigInteger.valueOf(2)).push(BigInteger.ONE);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.valueOf(4));
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(3).push(4);
		coeffs.push(BigInteger.valueOf(3)).push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.valueOf(4));
		clause.clear();
		coeffs.clear();
		assertFalse(solver.isSatisfiable());
		IVecInt explanation = solver.explain();
		assertEquals(2,explanation.size());
		assertTrue(explanation.contains(1));
		assertTrue(explanation.contains(2));
	}
	
	@Test
	public void testAlmostGlobalInconsistency() throws ContradictionException, TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(3);
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
		clause.push(1).push(3);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.ONE);
		clause.clear();
		coeffs.clear();
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
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(3);
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
		clause.push(1).push(3);
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
		IVecInt explanation = solver.explain();
		assertEquals(4,explanation.size());
		assertTrue(explanation.contains(1));
		assertTrue(explanation.contains(2));
		assertTrue(explanation.contains(4));
		assertTrue(explanation.contains(5));
	}
	
	@Test
	public void testAlmostGlobalInconsistencyPB() throws ContradictionException, TimeoutException {
		XplainPB solver = new XplainPB(SolverFactory.newDefault());
		solver.newVar(4);
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.push(BigInteger.valueOf(3)).push(BigInteger.valueOf(2)).push(BigInteger.ONE);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.valueOf(4));
		clause.clear();
		coeffs.clear();
		clause.push(2).push(-3).push(4);
		coeffs.push(BigInteger.ONE).push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.valueOf(2));
		clause.clear();
		coeffs.clear();
		clause.push(-1).push(3).push(4);
		coeffs.push(BigInteger.valueOf(3)).push(BigInteger.ONE).push(BigInteger.ONE);
		solver.addPseudoBoolean(clause, coeffs, true, BigInteger.valueOf(4));
		clause.clear();
		coeffs.clear();
		assertFalse(solver.isSatisfiable());
		IVecInt explanation = solver.explain();
		assertEquals(2,explanation.size());
		assertTrue(explanation.contains(1));
		assertTrue(explanation.contains(3));
	}
}
