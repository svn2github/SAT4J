package org.sat4j.maxsat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.maxsat.reader.WDimacsReader;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.tools.OptToSatAdapter;

public class MarcelBugsTests {

	private WeightedMaxSatDecorator maxsat;
	private WDimacsReader reader;
	
	@Before
	public void init() {
		maxsat = new WeightedMaxSatDecorator(SolverFactory.newLight());
		reader = new WDimacsReader(maxsat);
	}
	
	@Test
	public void testProblemWithDuplicatedOppositeLiterals2() {
		testProblemWithExpectedAnswer("Inconsistent2.wcnf", new int[] {-1,2,3},5);
	}
	
	@Test
	public void testProblemWithDuplicatedOppositeLiterals1() {
		testProblemWithExpectedAnswer("Inconsistent1.wcnf", new int[] {1,2,3},4);
	}
	
	@Test
	public void testSimpleProblemWithTwoOppositeLiterals() {
		testProblemWithExpectedAnswer("Inconsistent_Example.wcnf", new int[] {-1},1);
	}

	@Test
	public void testProblemWithNegatedLiterals() {
		testProblemWithExpectedAnswer("Example.wcnf", new int[] {1,2,-3,4},0);
	}
	
	@Test
	public void testProblemWithDuplicatedLiterals() {
		testProblemWithExpectedAnswer("AnotherExample.wcnf", new int[] {1},2);
	}
	
	private void testProblemWithExpectedAnswer(String filename, int [] expectation, int expectedValue) {
		URL url = MarcelBugsTests.class.getResource(filename);
		try {
			IOptimizationProblem problem = (IOptimizationProblem)reader.parseInstance(url.getFile());
			assertNotNull(problem);
			IProblem satproblem = new OptToSatAdapter(problem);
			assertTrue(satproblem.isSatisfiable());
			int [] model = satproblem.model();
			assertNotNull(model);
			assertArrayEquals(expectation, model);
			assertEquals(expectedValue,problem.getObjectiveValue());
			
		} catch (Exception e) {
			fail(" Problem when reading instance : "+e);
		}		
	}
}
