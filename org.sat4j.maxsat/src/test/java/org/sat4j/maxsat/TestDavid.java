package org.sat4j.maxsat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.opt.MaxSatDecorator;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.OptToSatAdapter;

public class TestDavid {

	@Test
	public void testMaxsat() throws ContradictionException, TimeoutException {
		MaxSatDecorator maxsat = new MaxSatDecorator(SolverFactory.newLight());
		maxsat.newVar(3);
		IVecInt literals = new VecInt();
		literals.push(1).push(-2).push(3);
		maxsat.addClause(literals);
		literals.clear();
		literals.push(-1).push(-2);
		maxsat.addClause(literals);
		literals.clear();
		literals.push(2);
		maxsat.addClause(literals);
		literals.clear();
		literals.push(-3);
		maxsat.addClause(literals);
		OptToSatAdapter opt = new OptToSatAdapter(maxsat);
		assertTrue(opt.isSatisfiable());
		assertTrue(maxsat.calculateObjective().equals(1));
	}
	
	@Test
	public void testMaxsatBis() throws ContradictionException, TimeoutException {
		MaxSatDecorator maxsat = new MaxSatDecorator(SolverFactory.newLight());
		maxsat.newVar(3);
		IVecInt literals = new VecInt();
		literals.push(1).push(-2);
		maxsat.addClause(literals);
		literals.clear();
		literals.push(-1).push(-2);
		maxsat.addClause(literals);
		literals.clear();
		literals.push(2);
		maxsat.addClause(literals);
		literals.clear();
		literals.push(-3);
		maxsat.addClause(literals);
		OptToSatAdapter opt = new OptToSatAdapter(maxsat);
		assertTrue(opt.isSatisfiable());
		assertTrue(maxsat.calculateObjective().equals(1));
		assertFalse(opt.model(3));
	}
	
	@Test
	public void testPartialWeightedMaxsat() throws ContradictionException, TimeoutException {
		WeightedMaxSatDecorator maxsat = new WeightedMaxSatDecorator(SolverFactory.newLight());
		maxsat.newVar(3);
		IVecInt literals = new VecInt();
		literals.push(1).push(-2).push(3);
		maxsat.addHardClause(literals);
		literals.clear();
		literals.push(-1).push(-2);
		maxsat.addHardClause(literals);
		literals.clear();
		literals.push(2);
		maxsat.addSoftClause(10, literals);
		literals.clear();
		literals.push(-3);
		maxsat.addSoftClause(5, literals);
		OptToPBSATAdapter opt = new OptToPBSATAdapter(maxsat);
		assertTrue(opt.isSatisfiable());
		assertTrue(opt.model(2));
		assertTrue(opt.model(3));
	}
	
	@Test
	public void testWeightedMinimization() throws ContradictionException, TimeoutException {
		WeightedMaxSatDecorator maxsat = new WeightedMaxSatDecorator(SolverFactory.newLight());
		maxsat.newVar(3);
		IVecInt literals = new VecInt();
		literals.push(1).push(-2).push(3);
		maxsat.addHardClause(literals);
		literals.clear();
		literals.push(-1).push(-2);
		maxsat.addHardClause(literals);
		literals.clear();
		literals.push(-2).push(3);
		IVecInt coefs = new VecInt().push(10).push(5);
		maxsat.addWeightedLiteralsToMinimize(literals, coefs);
		OptToPBSATAdapter opt = new OptToPBSATAdapter(maxsat);
		assertTrue(opt.isSatisfiable());
		assertTrue(opt.model(2));
		assertTrue(opt.model(3));
	}
	
	@Test
	public void testExampleDavid() throws ContradictionException, TimeoutException {
		WeightedMaxSatDecorator maxsat = new WeightedMaxSatDecorator(SolverFactory.newLight());
		maxsat.newVar(3);
		IVecInt literals = new VecInt();
		literals.push(1).push(-2).push(3);
		maxsat.addHardClause(literals);
		literals.clear();
		literals.push(-1).push(-2);
		maxsat.addHardClause(literals);
		literals.clear();
		literals.push(1).push(2).push(3);
		maxsat.addLiteralsToMinimize(literals);
		OptToPBSATAdapter opt = new OptToPBSATAdapter(maxsat);
		assertTrue(opt.isSatisfiable());
		assertFalse(opt.model(1));
		assertFalse(opt.model(2));
		assertFalse(opt.model(3));
	}
}
