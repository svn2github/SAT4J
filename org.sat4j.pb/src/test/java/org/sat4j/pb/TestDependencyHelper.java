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
*******************************************************************************/

package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sat4j.pb.tools.WeightedObject.newWO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.pb.tools.DependencyHelper;
import org.sat4j.pb.tools.WeightedObject;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class TestDependencyHelper {
	private static final String profile = "profile";
	private static final String junit3 = "junit_3";
	private static final String junit4 = "junit_4";
	
	private DependencyHelper<String,String> helper;
	
	@Before
	public void setUp() {
		helper = new DependencyHelper<String,String>(SolverFactory.newEclipseP2(),10);
	}
	
	@Test
	public void testBasicRequirements() throws ContradictionException, TimeoutException {
		helper.implication("A").implies("B").and("C").and("D").named("I1");
		helper.implication("B").impliesNot("C").named("I2");
		helper.setTrue("A","User selection");
		assertFalse(helper.hasASolution());		
		// assertEquals("C",helper.getConflictingElement());
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
		// assertEquals("C",helper.getConflictingElement());
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
		// assertTrue(helper.getConflictingElement().startsWith("C"));
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
	
	@Test
	public void testObjectiveFunction() throws ContradictionException, TimeoutException {
		helper.implication("A").implies("B").and("C").and("D").named("I1");
		helper.implication("C").implies("C1","C2","C3").named("C versions");
		helper.atMost(1,"C1","C2","C3").named("Singleton on C");
		helper.setTrue("A","User selection");
		helper.setObectiveFunction(newWO("C1",4),newWO("C2",2),newWO("C3",1));
		assertTrue(helper.hasASolution());
		IVec<String> solution = helper.getSolution();
		assertTrue(solution.contains("A"));
		assertTrue(solution.contains("B"));
		assertTrue(solution.contains("C"));
		assertTrue(solution.contains("C3"));
		assertFalse(solution.contains("C2"));
		assertFalse(solution.contains("C1"));
		assertTrue(solution.contains("D"));
	}
	
	@Test
	public void testJunitExample() throws ContradictionException, TimeoutException {
		helper.implication(profile).implies(junit3).named("profile->junit_3");
		helper.implication(profile).implies(junit4).named("profile->junit_4");
		helper.setObectiveFunction(WeightedObject.newWO(junit4, 1),WeightedObject.newWO(junit3, 2));
		helper.setTrue(profile, "profile must exist");
		assertTrue(helper.hasASolution());
		List<String> expected = new ArrayList<String>(Arrays.asList(profile, junit3, junit4));
		IVec<String> solution = helper.getSolution();
		for (Iterator<String> i = solution.iterator(); i.hasNext();) {
			String variable = i.next();
			assertTrue(variable + " was not part of the solution", expected.remove(variable));
		}
		assertTrue("solution contained too many variables: " + expected, expected.isEmpty());
	}
	
	@Test
	public void testJunitSingletonObjectiveExample() throws ContradictionException, TimeoutException {
		helper.implication(profile).implies(junit3, junit4).named("profile->junit");
		helper.atMost(1, junit4, junit3);
		helper.setObectiveFunction(WeightedObject.newWO(junit4, 1),WeightedObject.newWO(junit3, 2));
		helper.setTrue(profile, "profile must exist");
		assertTrue(helper.hasASolution());
		List<String> expected = new ArrayList<String>(Arrays.asList(profile, junit4));
		IVec<String> solution = helper.getSolution();
		for (Iterator<String> i = solution.iterator(); i.hasNext();) {
			String variable = i.next();
			assertTrue(variable + " was not part of the solution", expected.remove(variable));
		}
		assertTrue("solution contained too many variables: " + expected, expected.isEmpty());
	}
}
