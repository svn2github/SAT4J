package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.pb.tools.DependencyHelper;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class TestDependencyHelper {

	private DependencyHelper<String,String> helper;
	
	@Before
	public void setUp() {
		helper = new DependencyHelper<String,String>(SolverFactory.newDefault(),10);
	}
	
	@Test
	public void testBasicRequirements() throws ContradictionException, TimeoutException {
		helper.implication("A").implies("B").and("C").and("D").named("I1");
		helper.implication("B").impliesNot("C").named("I2");
		helper.setTrue("A","User selection");
		assertFalse(helper.hasASolution());		
		assertEquals("C",helper.getConflictingElement());
		Set<String> cause = helper.why();
		assertEquals(3,cause.size());
		Iterator<String> it = cause.iterator(); 
		assertEquals("I1",it.next());
		assertTrue(it.hasNext());
		assertEquals("I2",it.next());
		assertTrue(it.hasNext());
		assertEquals("User selection",it.next());
	}
	
	@Test
	public void testBasicRequirementsDetailedExplanation() throws ContradictionException, TimeoutException {
		helper.implication("A").implies("B").named("I1b");
		helper.implication("A").implies("C").named("I1c");
		helper.implication("A").implies("D").named("I1d");
		helper.implication("B").impliesNot("C").named("I2");
		helper.setTrue("A","User selection");
		assertFalse(helper.hasASolution());		
		assertEquals("C",helper.getConflictingElement());
		Set<String> cause = helper.why();
		assertEquals(4,cause.size());
		Iterator<String> it = cause.iterator(); 
		assertEquals("I1b",it.next());
		assertEquals("I1c",it.next());
		assertEquals("I2",it.next());
		assertEquals("User selection",it.next());
	}
	
	@Test
	public void testDisjunctions() throws ContradictionException, TimeoutException {
		helper.implication("A").implies("B").and("C").and("D").named("I1");
		helper.implication("C").implies("C1","C2","C3").named("C versions");
		helper.atMost(1,"C1","C2","C3").named("Singleton on C");
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
	
	@Test
	public void testDisjunctionExplanation() throws ContradictionException, TimeoutException {
		helper.implication("A").implies("B").and("C").and("D").named("I1");
		helper.implication("B").impliesNot("C1").named("I2");
		helper.implication("D").impliesNot("C2").named("I3");
		helper.implication("C").implies("C1","C2").named("C versions");
		helper.atMost(1,"C1","C2","C3").named("Singleton on C");
		helper.setTrue("A","User selection");
		assertFalse(helper.hasASolution());
		assertTrue(helper.getConflictingElement().startsWith("C"));
		Set<String> cause = helper.why();
		assertEquals(5,cause.size());
		Iterator<String> it = cause.iterator(); 
		assertEquals("C versions",it.next());
		assertEquals("I1",it.next());
		assertEquals("I2",it.next());
		assertEquals("I3",it.next());
		assertEquals("User selection",it.next());
	}
	
	@Test
	public void testExplanationForASolution() throws ContradictionException, TimeoutException {
		helper.implication("A").implies("B").and("C").and("D").named("I1");
		helper.implication("C").implies("C1","C2","C3").named("C versions");
		helper.atMost(1,"C1","C2","C3").named("Singleton on C");
		helper.setTrue("A","User selection");
		assertTrue(helper.hasASolution());
		IVec<String> solution = helper.getSolution();
		assertTrue(solution.contains("A"));
		assertTrue(solution.contains("B"));
		assertTrue(solution.contains("C"));
		assertTrue(solution.contains("D"));
		Set<String> cause = helper.why("D");
		assertEquals(2,cause.size());
		Iterator<String> it = cause.iterator(); 
		assertEquals("I1",it.next());
		assertEquals("User selection",it.next());
	}
	
}
