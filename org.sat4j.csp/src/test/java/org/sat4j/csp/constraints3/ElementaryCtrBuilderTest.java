package org.sat4j.csp.constraints3;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class ElementaryCtrBuilderTest {
	
	private IXCSP3Solver solver;
	
	@Before
	public void setUp() {
		this.solver = TestUtils.newSolver();
	}
	
	@Test
	public void testCtrClause1() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(2));
		String ctrSection = TestUtils.buildConstraintsSection("<clause> b0 b1 </clause>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1", "1 0", "1 1");
	}
	
	@Test
	public void testCtrClause2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(2));
		String ctrSection = TestUtils.buildConstraintsSection("<clause> b0 not(b1) </clause>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0", "1 0", "1 1");
	}
	
	@Test
	public void testCtrClause3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(1));
		String ctrSection = TestUtils.buildConstraintsSection("<clause> b0 b0 </clause>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1");
	}
	
	@Test
	public void testCtrClause4() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(1));
		String ctrSection = TestUtils.buildConstraintsSection("<clause> b0 not(b0) </clause>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0", "1");
	}
	
	@Test
	public void testCtrInstantiation() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<clause> b0 b1 </clause>"
				+ "<instantiation>"
				+ "<list> b0 </list>"
				+ "<values> 1 </values>"
				+ "</instantiation>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1 0", "1 1");
	}
	
	@Test
	public void testCtrInstantiation2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<instantiation>"
				+ "<list> b0 b0 </list>"
				+ "<values> 1 1 </values>"
				+ "</instantiation>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1");
	}
	
	@Test
	public void testCtrInstantiation3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<instantiation>"
				+ "<list> b0 b0 </list>"
				+ "<values> 1 0 </values>"
				+ "</instantiation>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}

}
