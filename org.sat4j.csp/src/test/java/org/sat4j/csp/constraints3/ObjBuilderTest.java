package org.sat4j.csp.constraints3;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ObjBuilderTest {

	private IXCSP3Solver solver;
	
	@Before
	public void setUp() {
		this.solver = TestUtils.newSolver();
	}

	@Test
	public void testMinimizeFunctionalForm() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(5));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<count>"
					+ "<list> b0 b1 b2 b3 b4 </list>"
					+ "<values> 1 </values>"
					+ "<condition> (ge,1) </condition>"
				+ "</count>", ""
				+ "<count>"
					+ "<list> b0 b1 b2 b3 b4 </list>"
					+ "<values> 0 </values>"
					+ "<condition> (ge,1) </condition>"
				+ "</count>");
		String optSection = TestUtils.buildObjectivesSection(""
				+ "<minimize type=\"sum\">"
				+ "<list> b0 b1 b2 b3 b4 </list>"
				+ "</minimize>");
		List<String> sortedModels = TestUtils.computeOptimalModels(solver, varSection, ctrSection, optSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0 0 1", "0 0 0 1 0", "0 0 1 0 0", "0 1 0 0 0", "1 0 0 0 0");
	}
	
	@Test
	public void testMaximizeFunctionalForm() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(5));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<count>"
					+ "<list> b0 b1 b2 b3 b4 </list>"
					+ "<values> 1 </values>"
					+ "<condition> (ge,1) </condition>"
				+ "</count>", ""
				+ "<count>"
					+ "<list> b0 b1 b2 b3 b4 </list>"
					+ "<values> 0 </values>"
					+ "<condition> (ge,1) </condition>"
				+ "</count>");
		String optSection = TestUtils.buildObjectivesSection(""
				+ "<maximize type=\"sum\">"
				+ "<list> b0 b1 b2 b3 b4 </list>"
				+ "</maximize>");
		List<String> sortedModels = TestUtils.computeOptimalModels(solver, varSection, ctrSection, optSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1 1 1 1", "1 0 1 1 1", "1 1 0 1 1", "1 1 1 0 1", "1 1 1 1 0");
	}

}
