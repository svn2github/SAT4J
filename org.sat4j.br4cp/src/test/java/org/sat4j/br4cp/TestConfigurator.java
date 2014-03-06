package org.sat4j.br4cp;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import br4cp.Configurator;

public class TestConfigurator {

	private Configurator configurator;
	
	@Before
	public void setUp() {
		configurator = new Br4cpConfigurator();
		configurator.readProblem("small");
	}
	
	@Test
	public void testReadInstance() {
		assertTrue(configurator.minCost()>0);
	}
	
	@Test
	public void testAssume() {
		configurator.assignAndPropagate("v93", "7");
		assertTrue(configurator.isPresentInCurrentDomain("v93", "7"));
		assertTrue(configurator.isPossiblyConsistent());
	}
	
	@Test
	public void testAssumeWrongValue() {
		configurator.assignAndPropagate("v19", "10");
		assertFalse(configurator.isPossiblyConsistent());
	}
	
	@Test
	public void testDomainSize() {
		assertEquals(12,configurator.getSizeOfCurrentDomainOf("v19"));
	}
	
	@Test
	public void testDomainValues() {
		Set<String> values = configurator.getCurrentDomainOf("v19");
		System.out.println(values);
		assertEquals(12,values.size());
	}
	
	@Test
	public void testDomain() {
		for (String value : new String[]{"0","1","2","3","4","5","6","7","8","11","12","13"}) {
			assertTrue(configurator.isPresentInCurrentDomain("v19",value));
		}
		for (String value : new String[]{"9","10"}) {
			assertFalse(configurator.isPresentInCurrentDomain("v19",value));
		}
	}

	@Test
	public void testUnassign() {
		assertEquals(12,configurator.getSizeOfCurrentDomainOf("v19"));
		configurator.assignAndPropagate("v19", "11");
		assertEquals(1,configurator.getSizeOfCurrentDomainOf("v19"));
		configurator.unassignAndRestore("v19");
		assertEquals(12,configurator.getSizeOfCurrentDomainOf("v19"));
	}
	
	@Test
	public void testFreeVars() {
		System.out.println(configurator.getFreeVariables());
		assertTrue(configurator.getFreeVariables().contains("v19"));
		configurator.assignAndPropagate("v19", "7");
		assertFalse(configurator.getFreeVariables().contains("v19"));
		configurator.unassignAndRestore("v19");
		assertTrue(configurator.getFreeVariables().contains("v19"));
	}
	
	@Test
	public void testMinCosts() {
		Map<String,Integer> mincosts = configurator.minCosts("v19");
		System.out.println(mincosts);
		
	}
	
	
	@Test
	public void testSeries() {
		assertEquals(2,configurator.getSizeOfCurrentDomainOf("v31.0.Serie"));
		Set<String> domain = configurator.getCurrentDomainOf("v31.0.Serie");
		assertEquals(2,domain.size());
	}
}
