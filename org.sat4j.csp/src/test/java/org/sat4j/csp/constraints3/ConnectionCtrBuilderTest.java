package org.sat4j.csp.constraints3;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class ConnectionCtrBuilderTest {
	
	private IXCSP3Solver solver;
	
	@Before
	public void setUp() {
		this.solver = TestUtils.newSolver();
	}

	@Test
	public void testChannel1() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(4, 0, 3));
		String ctrSection = TestUtils.buildConstraintsSection("<channel> i0 i1 i2 i3 </channel>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1 2 3", "0 1 3 2", "0 2 1 3", "0 3 2 1", "1 0 2 3", "1 0 3 2", "2 1 0 3", "2 3 0 1", "3 1 2 0", "3 2 1 0");
	}
	
	@Test
	public void testChannel2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(4, 1, 4));
		String ctrSection = TestUtils.buildConstraintsSection("<channel>"
				+ "<list startIndex=\"1\"> i0 i1 i2 i3 </list>"
				+ "</channel>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1 2 3 4", "1 2 4 3", "1 3 2 4", "1 4 3 2", "2 1 3 4", "2 1 4 3", "3 2 1 4", "3 4 1 2", "4 2 3 1", "4 3 2 1");
	}
	
	@Test
	public void testChannel3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(6, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<channel>"
				+ "<list> i0 i1 i2 </list>"
				+ "<list> i3 i4 i5 </list>"
				+ "</channel>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1 2 0 1 2", "0 2 1 0 2 1", "1 0 2 1 0 2", "1 2 0 2 0 1", "2 0 1 1 2 0", "2 1 0 2 1 0");
	}
	
	@Test
	public void testChannel4() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 1, 3)+TestUtils.buildIntegerVars(3, 0, 2, 3));
		String ctrSection = TestUtils.buildConstraintsSection("<channel>"
				+ "<list startIndex=\"0\"> i0 i1 i2 </list>"
				+ "<list startIndex=\"1\"> i3 i4 i5 </list>"
				+ "</channel>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1 2 3 0 1 2", "1 3 2 0 2 1", "2 1 3 1 0 2", "2 3 1 2 0 1", "3 1 2 1 2 0", "3 2 1 2 1 0");
	}
	
	@Test
	public void testChannel5() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 1));
		String ctrSection = TestUtils.buildConstraintsSection("<channel> i0 i0 </channel>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testChannel6() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 1));
		String ctrSection = TestUtils.buildConstraintsSection("<channel>"
				+ "<list> i0 i1 </list>"
				+ "<list> i0 i1 </list>"
				+ "</channel>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1", "1 0");
	}
	
	@Test
	public void testChannel7() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 1));
		String ctrSection = TestUtils.buildConstraintsSection("<channel>"
				+ "<list> i0 i0 </list>"
				+ "<list> i1 i1 </list>"
				+ "</channel>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testElement1() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(4));
		String ctrSection = TestUtils.buildConstraintsSection("<element>"
				+ "<list startIndex=\"1\"> b1 b2 b3 </list>"
				+ "<value> b0 </value>"
				+ "</element>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0 0", "0 0 0 1", "0 0 1 0", "0 0 1 1", "0 1 0 0", "0 1 0 1", "0 1 1 0", "1 0 0 1", "1 0 1 0", "1 0 1 1", "1 1 0 0", "1 1 0 1", "1 1 1 0", "1 1 1 1");
	}
	
	@Test
	public void testElement2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2)+TestUtils.buildBooleanVars(4));
		String ctrSection = TestUtils.buildConstraintsSection("<element>"
				+ "<list startIndex=\"1\"> b1 b2 b3 </list>"
				+ "<index rank=\"any\"> i0 </index>"
				+ "<value> b0 </value>"
				+ "</element>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		String[] models = new String[42];
		int cpt = 0;
		for(int i=0; i<3; ++i) {
			for(String mod : new String[]{"0 0 0 0", "0 0 0 1", "0 0 1 0", "0 0 1 1", "0 1 0 0", "0 1 0 1", "0 1 1 0", "1 0 0 1", "1 0 1 0", "1 0 1 1", "1 1 0 0", "1 1 0 1", "1 1 1 0", "1 1 1 1"}) {
				models[cpt] = Integer.toString(i)+" "+mod;
				++cpt;
			}
		}
		TestUtils.assertEqualsSortedModels(sortedModels, models);
	}
	
	@Test
	public void testElement3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2)+TestUtils.buildBooleanVars(4));
		String ctrSection = TestUtils.buildConstraintsSection("<element>"
				+ "<list startIndex=\"0\"> b1 b2 b3 </list>"
				+ "<index rank=\"first\"> i0 </index>"
				+ "<value> b0 </value>"
				+ "</element>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0 0 0", "0 0 0 0 1", "0 0 0 1 0", "0 0 0 1 1", "0 1 1 0 0", "0 1 1 0 1", "0 1 1 1 0", "0 1 1 1 1",
				"1 0 1 0 0", "1 0 1 0 1", "1 1 0 1 0", "1 1 0 1 1",
				"2 0 1 1 0", "2 1 0 0 1");
	}
	
	@Test
	public void testElement4() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 1, 3)+TestUtils.buildBooleanVars(4));
		String ctrSection = TestUtils.buildConstraintsSection("<element>"
				+ "<list startIndex=\"1\"> b1 b2 b3 </list>"
				+ "<index rank=\"last\"> i0 </index>"
				+ "<value> b0 </value>"
				+ "</element>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1 0 0 1 1", "1 1 1 0 0",
				"2 0 0 0 1", "2 0 1 0 1", "2 1 0 1 0", "2 1 1 1 0",
				"3 0 0 0 0", "3 0 0 1 0", "3 0 1 0 0", "3 0 1 1 0", "3 1 0 0 1", "3 1 0 1 1", "3 1 1 0 1", "3 1 1 1 1");
	}
	
	@Test
	public void testElement5() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(3));
		String ctrSection = TestUtils.buildConstraintsSection("<element>"
				+ "<list> b0 b1 b2 </list>"
				+ "<value> 0 </value>"
				+ "</element>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0", "0 0 1", "0 1 0", "0 1 1", "1 0 0", "1 0 1", "1 1 0");
	}
	
	@Test
	public void testElement6() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(1));
		String ctrSection = TestUtils.buildConstraintsSection("<element>"
				+ "<list> b0 b0 b0 </list>"
				+ "<value> 0 </value>"
				+ "</element>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0");
	}
	
	@Test
	public void testElement7() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(1));
		String ctrSection = TestUtils.buildConstraintsSection("<element>"
				+ "<list> b0 b0 b0 </list>"
				+ "<value> b0 </value>"
				+ "</element>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0", "1");
	}
	
	@Test
	public void testElement8() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(1)+TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<element>"
				+ "<list> b0 b0 b0 </list>"
				+ "<index rank=\"first\"> i0 </index>"
				+ "<value> b0 </value>"
				+ "</element>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0", "1 0");
	}
	
	@Test
	public void testElement9() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(1)+TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<element>"
				+ "<list> b0 b0 b0 </list>"
				+ "<index rank=\"last\"> i0 </index>"
				+ "<value> b0 </value>"
				+ "</element>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 2", "1 2");
	}
	
	@Test
	public void testMaximum1() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<maximum>"
				+ "<list> i0 i1 i2 </list>"
				+ "<condition> (eq,2) </condition>"
				+ "</maximum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 2", "0 1 2", "0 2 0", "0 2 1", "0 2 2",
				"1 0 2", "1 1 2", "1 2 0", "1 2 1", "1 2 2",
				"2 0 0", "2 0 1", "2 0 2", "2 1 0", "2 1 1", "2 1 2", "2 2 0", "2 2 1", "2 2 2");
	}
	
	@Test
	public void testMaximum2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<maximum>"
				+ "<list> i1 i2 </list>"
				+ "<condition> (eq,i0) </condition>"
				+ "</maximum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0", "1 0 1", "1 1 0", "1 1 1", "2 0 2", "2 1 2", "2 2 0", "2 2 1", "2 2 2");
	}
	
	@Test
	public void testMaximum3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<maximum>"
				+ "<list> i0 i0 </list>"
				+ "<condition> (eq,i0) </condition>"
				+ "</maximum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0", "1", "2");
	}
	
	@Test
	public void testMaximum4() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2) + "<var id=\"i3\"> 2 </var><var id=\"i4\"> 1 </var>");
		String ctrSection = TestUtils.buildConstraintsSection("<maximum>"
				+ "<list> i0 i1 i2 </list>"
				+ "<index rank=\"first\"> i4 </index>"
				+ "<condition> (eq,i3) </condition>"
				+ "</maximum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 2 0 2 1", "0 2 1 2 1", "0 2 2 2 1",
				"1 2 0 2 1", "1 2 1 2 1", "1 2 2 2 1");
	}
	
	@Test
	public void testMaximum5() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2) + "<var id=\"i3\"> 2 </var><var id=\"i4\"> 2 </var>");
		String ctrSection = TestUtils.buildConstraintsSection("<maximum>"
				+ "<list startIndex=\"1\"> i0 i1 i2 </list>"
				+ "<index rank=\"first\"> i4 </index>"
				+ "<condition> (eq,i3) </condition>"
				+ "</maximum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 2 0 2 2", "0 2 1 2 2", "0 2 2 2 2",
				"1 2 0 2 2", "1 2 1 2 2", "1 2 2 2 2");
	}
	
	@Test
	public void testMaximum6() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2) + "<var id=\"i3\"> 2 </var><var id=\"i4\"> 2 </var>");
		String ctrSection = TestUtils.buildConstraintsSection("<maximum>"
				+ "<list startIndex=\"1\"> i0 i1 i2 </list>"
				+ "<index rank=\"last\"> i4 </index>"
				+ "<condition> (eq,i3) </condition>"
				+ "</maximum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 2 0 2 2", "0 2 1 2 2", "1 2 0 2 2", "1 2 1 2 2", "2 2 0 2 2", "2 2 1 2 2");
	}
	
	
	@Test
	public void testMinimum1() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<minimum>"
				+ "<list> i0 i1 i2 </list>"
				+ "<condition> (eq,2) </condition>"
				+ "</minimum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "2 2 2");
	}
	
	@Test
	public void testMinimum2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<minimum>"
				+ "<list> i1 i2 </list>"
				+ "<condition> (eq,i0) </condition>"
				+ "</minimum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0", "0 0 1", "0 0 2", "0 1 0", "0 2 0", "1 1 1", "1 1 2", "1 2 1", "2 2 2");
	}
	
	@Test
	public void testMinimum3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<minimum>"
				+ "<list> i0 i0 </list>"
				+ "<condition> (eq,i0) </condition>"
				+ "</minimum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0", "1", "2");
	}
	
	@Test
	public void testMinimum4() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2) + "<var id=\"i3\"> 1 </var><var id=\"i4\"> 1 </var>");
		String ctrSection = TestUtils.buildConstraintsSection("<minimum>"
				+ "<list> i0 i1 i2 </list>"
				+ "<index rank=\"first\"> i4 </index>"
				+ "<condition> (eq,i3) </condition>"
				+ "</minimum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "2 1 1 1 1", "2 1 2 1 1");
	}
	
	@Test
	public void testMinimum5() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2) + "<var id=\"i3\"> 1 </var><var id=\"i4\"> 2 </var>");
		String ctrSection = TestUtils.buildConstraintsSection("<minimum>"
				+ "<list startIndex=\"1\"> i0 i1 i2 </list>"
				+ "<index rank=\"first\"> i4 </index>"
				+ "<condition> (eq,i3) </condition>"
				+ "</minimum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "2 1 1 1 2", "2 1 2 1 2");
	}
	
	@Test
	public void testMinimum6() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2) + "<var id=\"i3\"> 1 </var><var id=\"i4\"> 2 </var>");
		String ctrSection = TestUtils.buildConstraintsSection("<minimum>"
				+ "<list startIndex=\"1\"> i0 i1 i2 </list>"
				+ "<index rank=\"last\"> i4 </index>"
				+ "<condition> (eq,i3) </condition>"
				+ "</minimum>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1 1 2 1 2", "2 1 2 1 2");
	}

}
