package org.sat4j.csp.constraints3;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.reader.XMLCSP3Reader;

public class ObjBuilderTest {

	private IPBSolver solver;

	@Before
	public void setUp() {
		this.solver = SolverFactory.newDefault();
	}

	@Test
	public void testMinimizeFunctionalForm() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(5));
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
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection, optSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0 0 1", "0 0 0 1 0", "0 0 1 0 0", "0 1 0 0 0", "1 0 0 0 0");
	}
	
	@Test
	public void testMaximizeFunctionalForm() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(5));
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
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection, optSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1 1 1 1", "1 0 1 1 1", "1 1 0 1 1", "1 1 1 0 1", "1 1 1 1 0");
	}

}
