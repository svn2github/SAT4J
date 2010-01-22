package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.Vec;
import org.sat4j.pb.tools.DependencyHelper;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class TestObjectiveFunction {

	private DependencyHelper<String, String> helper;

	@Before
	public void setUp() {
		helper = new DependencyHelper<String, String>(SolverFactory
				.newEclipseP2());
	}

	@Test
	public void testObjectiveFunctionWithAllWeightsToNull()
			throws ContradictionException, TimeoutException {
		helper.clause("C1", "A1", "A2");
		helper.clause("C2", "A1", "A3");
		helper.addToObjectiveFunction("A2", 0);
		helper.addToObjectiveFunction("A3", 0);
		IVec<String> assump = new Vec<String>();
		assump.push("A1");
		assertTrue(helper.hasASolution(assump));
		IVec<String> solution = helper.getSolution();
		assertEquals(1, solution.size());
		assertTrue(solution.contains("A1"));
	}
}
