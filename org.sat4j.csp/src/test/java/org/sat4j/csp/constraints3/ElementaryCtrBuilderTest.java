package org.sat4j.csp.constraints3;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.reader.XMLCSP3Reader;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class ElementaryCtrBuilderTest {
	
	private IPBSolver solver;
	
	@Before
	public void setUp() {
		this.solver = SolverFactory.newDefault();
	}
	
	@Test
	public void testCtrClause1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection("<clause> b0 b1 </clause>\n");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1", "1 0", "1 1");
	}
	
	@Test
	public void testCtrClause2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection("<clause> b0 not(b1) </clause>\n");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0", "1 0", "1 1");
	}
	
	@Test
	public void testCtrClause3() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(1));
		String ctrSection = TestUtils.buildConstraintsSection("<clause> b0 b0 </clause>\n");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1");
	}
	
	@Test
	public void testCtrClause4() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(1));
		String ctrSection = TestUtils.buildConstraintsSection("<clause> b0 not(b0) </clause>\n");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0", "1");
	}
	
	@Test
	public void testCtrInstantiation() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<clause> b0 b1 </clause>"
				+ "<instantiation>"
				+ "<list> b0 </list>"
				+ "<values> 1 </values>"
				+ "</instantiation>\n");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1 0", "1 1");
	}
	
	@Test
	public void testCtrInstantiation2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<instantiation>"
				+ "<list> b0 b0 </list>"
				+ "<values> 1 1 </values>"
				+ "</instantiation>\n");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1");
	}
	
	@Test
	public void testCtrInstantiation3() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<instantiation>"
				+ "<list> b0 b0 </list>"
				+ "<values> 1 0 </values>"
				+ "</instantiation>\n");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}

}
