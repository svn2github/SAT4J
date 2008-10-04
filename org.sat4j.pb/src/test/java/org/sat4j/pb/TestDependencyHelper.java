package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.pb.tools.DependencyHelper;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class TestDependencyHelper {

	private DependencyHelper<String> helper;
	
	@Before
	public void setUp() {
		helper = new DependencyHelper<String>(SolverFactory.newDefault(),10);
	}
	
	@Test
	public void testBasicRequirements() throws ContradictionException, TimeoutException {
		helper.implication("A").implies("B").and("C").and("D").named("I1");
		helper.implication("B").impliesNot("C").named("I2");
		helper.setTrue("A","User selection");
		assertFalse(helper.hasASolution());		
		assertEquals("C",helper.getConflictingElement());
		String [] cause = helper.why();
		assertEquals(3,cause.length);
		assertEquals("I1",cause[0]);
		assertEquals("I2",cause[1]);
		assertEquals("User selection",cause[2]);
	}
	
	@Test
	public void testDisjunctions() throws ContradictionException, TimeoutException {
		helper.implication("A").implies("B").and("C").and("D");
		helper.range("C").either("C1").or("C2").or("C3");
		helper.setTrue("A","User selection");
		assertTrue(helper.hasASolution());
		IVec<String> solution = helper.getSolution();
		assertTrue(solution.contains("A"));
		assertTrue(solution.contains("B"));
		assertTrue(solution.contains("C"));
		assertTrue(solution.contains("D"));
		if (solution.contains("C1")) {
			assertFalse(solution.contains("C2"));
			assertFalse(solution.contains("C3"));
		}
		if (solution.contains("C2")) {
			assertFalse(solution.contains("C1"));
			assertFalse(solution.contains("C3"));
		}
		if (solution.contains("C3")) {
			assertFalse(solution.contains("C1"));
			assertFalse(solution.contains("C2"));
		}
	}
	
}
