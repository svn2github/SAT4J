package org.sat4j.maxsat;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class TestSoftCard {

	private WeightedMaxSatDecorator wms;
	
	@Before
	public void setUp() {
		wms = new WeightedMaxSatDecorator(SolverFactory.newDefault());
	}
	@Test
	public void test() throws ContradictionException, TimeoutException {
		wms.newVar(5);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(1).push(-2);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(1).push(2);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(-1).push(3);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(1).push(2).push(3);
		wms.addAtMost(clause,1);
		clause.clear();
		assertFalse(wms.isSatisfiable());
	}
	
	@Test
	public void test2() throws ContradictionException, TimeoutException {
		wms.newVar(5);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(1).push(-2);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(1).push(2);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(-1).push(3);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(1).push(2).push(3);
		wms.addSoftAtMost(clause,1);
		clause.clear();
		assertTrue(wms.isSatisfiable());
	}

	@Test
	public void test3() throws ContradictionException, TimeoutException {
		wms.newVar(6);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(4).push(5).push(6);
		wms.addHardClause(clause);
		clause.clear();
		clause.push(1).push(2).push(3).push(4).push(5).push(6);
		wms.addSoftAtMost(clause,1);
		clause.clear();
		assertTrue(wms.isSatisfiable());
	}

}
