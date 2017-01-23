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
public class GenericCtrBuilderTest {
	
	private IPBSolver solver;
	
	@Before
	public void setUp() {
		this.solver = SolverFactory.newDefault();
	}
	
	@Test
	public void testCtrFalse() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<extension>"
				+ "<list> b0 b1 </list>"
				+ "<supports> (0,2) </supports>"
				+ "</extension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testExtension1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(3));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<extension>"
				+ "<list> b0 b1 b2 </list>"
				+ "<supports> (0,1,0) (1,0,1) </supports>"
				+ "</extension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1 0", "1 0 1");
	}
	
	@Test
	public void testExtension2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(3));
		String ctrSection = TestUtils.buildConstraintsSection("<extension>"
				+ "<list> b0 b1 b2 </list>"
				+ "<conflicts> (0,1,0) (1,0,1) </conflicts>"
				+ "</extension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0", "0 0 1", "0 1 1", "1 0 0", "1 1 0", "1 1 1");
	}
	
	@Test
	public void testExtension3() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<extension>"
				+ "<list> b0 b1 b0 </list>"
				+ "<supports> (0,1,0) (1,0,1) </supports>"
				+ "</extension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1", "1 0");
	}
	
	@Test
	public void testExtension4() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection("<extension>"
				+ "<list> b0 b1 b0 </list>"
				+ "<conflicts> (0,1,0) (1,0,1) </conflicts>"
				+ "</extension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0", "1 1");
	}
	
	@Test
	public void testExtension5() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection("<extension>"
				+ "<list> b0 b1 b0 </list>"
				+ "<supports> (0,1,0) (1,0,0) </supports>"
				+ "</extension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1");
	}
	
	@Test
	public void testExtensionStarred1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection("<extension>"
				+ "<list> b0 b1 </list>"
				+ "<supports> (0,*) (1,0) </supports>"
				+ "</extension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0", "0 1", "1 0");
	}
	
	@Test
	public void testExtensionStarred2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection("<extension>"
				+ "<list> b0 b1 </list>"
				+ "<supports> (*,*) </supports>"
				+ "</extension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0", "0 1", "1 0", "1 1");
	}
	
	@Test
	public void testCtrUnclean() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(2));
		String ctrSection = TestUtils.buildConstraintsSection("<extension>"
				+ "<list> b0 b1 </list>"
				+ "<supports> (0,2) (0,1) </supports>"
				+ "</extension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1");
	}
	
	@Test
	public void testIntension1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<intension>eq(add(i0,i1),i2)</intension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0", "0 1 1", "0 2 2", "1 0 1", "1 1 2", "2 0 2");
	}
	
	@Test
	public void testIntension2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<intension>eq(add(i0,i0),i0)</intension>");
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0");
	}

}
