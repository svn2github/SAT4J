package org.sat4j.br4cp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
		configurator.initialize();
	}

	@Test
	public void testReadInstance() {
		assertTrue(configurator.minCost() > 0);
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
		assertEquals(12, configurator.getSizeOfCurrentDomainOf("v19"));
	}

	@Test
	public void testDomainValues() {
		Set<String> values = configurator.getCurrentDomainOf("v19");
		assertEquals(12, values.size());
		for (String value : new String[] { "0", "1", "2", "3", "4", "5", "6",
				"7", "8", "11", "12", "13" }) {
			assertTrue(values.contains(value));
		}
	}

	@Test
	public void testDomain() {
		for (String value : new String[] { "0", "1", "2", "3", "4", "5", "6",
				"7", "8", "11", "12", "13" }) {
			assertTrue(configurator.isPresentInCurrentDomain("v19", value));
		}
		for (String value : new String[] { "9", "10" }) {
			assertFalse(configurator.isPresentInCurrentDomain("v19", value));
		}
	}

	@Test
	public void testUnassign() {
		assertEquals(12, configurator.getSizeOfCurrentDomainOf("v19"));
		configurator.assignAndPropagate("v19", "11");
		assertEquals(1, configurator.getSizeOfCurrentDomainOf("v19"));
		configurator.unassignAndRestore("v19");
		assertEquals(12, configurator.getSizeOfCurrentDomainOf("v19"));
	}

	@Test
	public void testFreeVars() {
		assertTrue(configurator.getFreeVariables().contains("v19"));
		configurator.assignAndPropagate("v19", "7");
		assertFalse(configurator.getFreeVariables().contains("v19"));
		configurator.unassignAndRestore("v19");
		assertTrue(configurator.getFreeVariables().contains("v19"));
	}

	@Test
	public void testMinCosts() {
		Map<String, Integer> mincosts = configurator.minCosts("v19");
		System.out.println(mincosts);
	}

	@Test
	public void testSeries() {
		assertEquals(2, configurator.getSizeOfCurrentDomainOf("v31_0_Serie"));
		Set<String> domain = configurator.getCurrentDomainOf("v31_0_Serie");
		assertEquals(2, domain.size());
	}

	@Test
	public void testIsComplete() {
		Random rand = new Random(12345);
		assertFalse(configurator.isConfigurationComplete());
		while (!configurator.isConfigurationComplete()) {
			Set<String> free = configurator.getFreeVariables();
			System.out.println("free=>" + free);
			String var = (String) free.toArray()[rand.nextInt(free.size())];
			System.out.print("" + var + "=");
			Set<String> domain = configurator.getCurrentDomainOf(var);
			String val = (String) domain.toArray()[rand.nextInt(domain.size())];
			System.out.println(val);
			configurator.assignAndPropagate(var, val);
		}
	}

	@Test
	public void testBugYacine() {
		String[] choices = { "v13=0", "v0=12", "v23=2" };
		int[] expected = { 92, 17, 16 };
		assertFalse(configurator.isConfigurationComplete());
		List<String> list = new ArrayList<String>(
				configurator.getFreeVariables());
		Collections.sort(list);
		System.out.println(list);
		assertEquals(139, configurator.getFreeVariables().size());
		for (int i = 0; i < choices.length; i++) {
			String[] choice = choices[i].split("=");
			configurator.isPresentInCurrentDomain(choice[0], choice[1]);
			configurator.assignAndPropagate(choice[0], choice[1]);
			assertEquals(expected[i], configurator.getFreeVariables().size());
		}
	}

	@Test
	public void testBugYacine2() {
		assertEquals(2, configurator.getCurrentDomainOf("v29").size());
		configurator.assignAndPropagate("v29", "0");
		assertEquals(1, configurator.getCurrentDomainOf("v29").size());
		configurator.unassignAndRestore("v29");
		assertEquals(2, configurator.getCurrentDomainOf("v29").size());
	}
}
