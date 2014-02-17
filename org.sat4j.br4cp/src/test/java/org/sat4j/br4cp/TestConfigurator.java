package org.sat4j.br4cp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import br4cp.Configurator;

public class TestConfigurator {

	private Configurator configurator;
	
	@Before
	public void setUp() {
		configurator = new Br4cpConfigurator();
	}
	
	@Test
	public void testReadInstance() {
		configurator.readProblem("small");
		assertTrue(configurator.mincost()>0);
	}
	
	@Test
	public void testAssume() {
		configurator.readProblem("small");
		configurator.assign("v93", "7");
		configurator.propagateChoices();
		assertTrue(configurator.isPresentInCurrentDomain("v93", "7"));
		assertTrue(configurator.isPossiblyConsistent());
	}
	
	@Test
	public void testAssumeWrongValue() {
		configurator.readProblem("small");
		configurator.assign("v19", "10");
		configurator.propagateChoices();
		assertFalse(configurator.isPossiblyConsistent());
	}

}
