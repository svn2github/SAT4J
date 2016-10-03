package org.sat4j.csp.constraints3;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.reader.XMLCSP3Reader;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class CountingCtrBuilderTest {
	
	private IPBSolver solver;
	
	@Before
	public void setUp() {
		this.solver = SolverFactory.newDefault();
	}

	@Test
	public void testSum1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 10)+TestUtils.buildBinaryVars(3));
		String ctrSection = "<sum>"
				+ "<list> b0 b1 b2 </list>"
				+ "<condition> (eq,i0) </condition>"
				+ "</sum>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0 0", "1 0 0 1", "1 0 1 0", "1 1 0 0", "2 0 1 1", "2 1 0 1", "2 1 1 0", "3 1 1 1");
	}
	
	
	@Test
	public void testSum2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 10)+TestUtils.buildBinaryVars(3));
		String ctrSection = "<sum>"
				+ "<list> b0 b1 b2 </list>"
				+ "<coeffs> 2 1 1 </coeffs>"
				+ "<condition> (eq,i0) </condition>"
				+ "</sum>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0 0", "1 0 0 1", "1 0 1 0", "2 0 1 1", "2 1 0 0", "3 1 0 1", "3 1 1 0", "4 1 1 1");
	}
	
	@Test
	public void testSum3() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(3));
		String ctrSection = "<sum>"
				+ "<list> b0 b1 b2 </list>"
				+ "<condition> (eq,2) </condition>"
				+ "</sum>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1 1", "1 0 1", "1 1 0");
	}
	
	@Test
	public void testSum4() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBinaryVars(1));
		String ctrSection = "<sum>"
				+ "<list> b0 b0 b0 </list>"
				+ "<condition> (eq,3) </condition>"
				+ "</sum>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1");
	}
	
	@Test
	public void testSum5() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 10)+TestUtils.buildBinaryVars(1));
		String ctrSection = "<sum>"
				+ "<list> b0 b0 b0 </list>"
				+ "<coeffs> 2 1 1 </coeffs>"
				+ "<condition> (eq,i0) </condition>"
				+ "</sum>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0", "4 1");
	}
	
	@Test
	public void testCount1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(4, 0, 2));
		String ctrSection = "<count>"
				+ "<list> i1 i2 i3 </list>"
				+ "<values> 0 1 </values>"
				+ "<condition> (ge,i0) </condition>"
				+ "</count>";
		List<String> expected = new ArrayList<String>();
		int count;
		for(int i=0; i<=2; ++i) {
			for(int j=0; j<=2; ++j) {
				for(int k=0; k<=2; ++k) {
					for(int l=0; l<=2; ++l) {
						count = 0;
						if(j < 2) ++count;
						if(k < 2) ++count;
						if(l < 2) ++count;
						if(count >= i) {
							expected.add(""+i+" "+j+" "+k+" "+l);
						}
					}
				}
			}
		}
		String[] strExpected = new String[expected.size()];
		strExpected = expected.toArray(strExpected);
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, strExpected);
	}
	
	@Test
	public void testCount2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 3)+TestUtils.buildBinaryVars(5));
		String ctrSection = "<count>"
				+ "<list> b2 b3 b4 </list>"
				+ "<values> b0 b1 </values>"
				+ "<condition> (eq,i0) </condition>"
				+ "</count>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0 1 1 1", "0 1 1 0 0 0",
				"1 0 0 0 1 1", "1 0 0 1 0 1", "1 0 0 1 1 0", "1 1 1 0 0 1", "1 1 1 0 1 0", "1 1 1 1 0 0",
				"2 0 0 0 0 1", "2 0 0 0 1 0", "2 0 0 1 0 0", "2 1 1 0 1 1", "2 1 1 1 0 1", "2 1 1 1 1 0",
				"3 0 0 0 0 0",
				"3 0 1 0 0 0", "3 0 1 0 0 1", "3 0 1 0 1 0", "3 0 1 0 1 1", "3 0 1 1 0 0", "3 0 1 1 0 1", "3 0 1 1 1 0", "3 0 1 1 1 1",
				"3 1 0 0 0 0", "3 1 0 0 0 1", "3 1 0 0 1 0", "3 1 0 0 1 1", "3 1 0 1 0 0", "3 1 0 1 0 1", "3 1 0 1 1 0", "3 1 0 1 1 1",
				"3 1 1 1 1 1");
	}
	
	@Test
	public void testCount3() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 2));
		String ctrSection = "<count>"
				+ "<list> i1 i1 i1 </list>"
				+ "<values> 0 1 </values>"
				+ "<condition> (ge,i0) </condition>"
				+ "</count>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0", "0 1", "0 2", "1 0", "1 1", "2 0", "2 1");
	}
	
	@Test
	public void testCount4() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 10));
		String ctrSection = "<count>"
				+ "<list> i0 i0 i0 </list>"
				+ "<values> i0 </values>"
				+ "<condition> (eq,i0) </condition>"
				+ "</count>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "3");
	}
	
	@Test
	public void testNValues() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2)+TestUtils.buildBinaryVars(3));
		String ctrSection = "<nValues>"
				+ "<list> b0 b1 b2 </list>"
				+ "<condition> (eq,i0) </condition>"
				+ "</nValues>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1 0 0 0", "1 1 1 1", "2 0 0 1", "2 0 1 0", "2 0 1 1", "2 1 0 0", "2 1 0 1", "2 1 1 0");
	}
	
	@Test
	public void testNValues2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = "<nValues>"
				+ "<list> i0 i0 i0 </list>"
				+ "<condition> (eq,i0) </condition>"
				+ "</nValues>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "1");
	}
	
	@Test
	public void testNValuesExcept() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2)+TestUtils.buildBinaryVars(3));
		String ctrSection = "<nValues>"
				+ "<list> b0 b1 b2 </list>"
				+ "<except> 0 </except>"
				+ "<condition> (eq,i0) </condition>"
				+ "</nValues>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0 0", "1 0 0 1", "1 0 1 0", "1 0 1 1", "1 1 0 0", "1 1 0 1", "1 1 1 0", "1 1 1 1");
	}
	
	@Test
	public void testNValuesExcept2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = "<nValues>"
				+ "<list> i0 i0 i0 </list>"
				+ "<except> 0 </except>"
				+ "<condition> (eq,i0) </condition>"
				+ "</nValues>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0", "1");
	}
	
	@Test
	public void testNValuesExcept3() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = "<nValues>"
				+ "<list> i0 i0 i0 </list>"
				+ "<except> 1 </except>"
				+ "<condition> (eq,i0) </condition>"
				+ "</nValues>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testCardinality1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(4, 0, 3));
		String ctrSection = "<cardinality>"
				+ "<list> i0 i1 i2 i3 </list>"
				+ "<values> 0 1 2 3 </values>"
				+ "<occurs> 0..1 0..1 1..2 0..1 </occurs>"
				+ "</cardinality>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		List<String> expectedList = new ArrayList<String>();
		for(int i=0; i<=3; ++i) {
			for(int j=0; j<=3; ++j) {
				if(j != 2 && j == i) continue;
				for(int k=0; k<=3; ++k) {
					if(k != 2 && k == i) continue;
					if(k != 2 && k == j) continue;
					if(k == 2 && i == 2 && j == 2) continue;
					for(int l=0; l<=3; ++l) {
						if(l != 2 && l == i) continue;
						if(l != 2 && l == j) continue;
						if(l != 2 && l == k) continue;
						if(l == 2) {
							if(i == 2 && j == 2) continue;
							if(i == 2 && k == 2) continue;
							if(j == 2 && k == 2) continue;
						}
						expectedList.add(""+i+" "+j+" "+k+" "+l);
					}
				}
			}
		}
		String[] expected = new String[expectedList.size()];
		expected = expectedList.toArray(expected);
		TestUtils.assertEqualsSortedModels(sortedModels, expected);
	}
	
	@Test
	public void testCardinality2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(4, 0, 4));
		String ctrSection = "<cardinality>"
				+ "<list> i0 i1 i2 i3 </list>"
				+ "<values closed=\"true\"> 0 1 2 3 </values>"
				+ "<occurs> 0..1 0..1 1..2 0..1 </occurs>"
				+ "</cardinality>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		List<String> expectedList = new ArrayList<String>();
		for(int i=0; i<=3; ++i) {
			for(int j=0; j<=3; ++j) {
				if(j != 2 && j == i) continue;
				for(int k=0; k<=3; ++k) {
					if(k != 2 && k == i) continue;
					if(k != 2 && k == j) continue;
					if(k == 2 && i == 2 && j == 2) continue;
					for(int l=0; l<=3; ++l) {
						if(l != 2 && l == i) continue;
						if(l != 2 && l == j) continue;
						if(l != 2 && l == k) continue;
						if(l == 2) {
							if(i == 2 && j == 2) continue;
							if(i == 2 && k == 2) continue;
							if(j == 2 && k == 2) continue;
						}
						expectedList.add(""+i+" "+j+" "+k+" "+l);
					}
				}
			}
		}
		String[] expected = new String[expectedList.size()];
		expected = expectedList.toArray(expected);
		TestUtils.assertEqualsSortedModels(sortedModels, expected);
	}
	
	@Test
	public void testCardinality3() {
		System.out.println("TODO: enable the test \"testCardinality3\" as soon as XCSP3 parsers implements integer variables for values parameter");
	}
	
	// TODO: enable the test as soon as XCSP3 parsers implements integer variables for values parameter
	//	@Test
	//	public void testCardinality3() {
	//		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
	//		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(4, 0, 2));
	//		String ctrSection = "<cardinality>"
	//				+ "<list> i0 i1 </list>"
	//				+ "<values closed=\"true\"> i2 i3 </values>"
	//				+ "<occurs> 0..1 1..2 </occurs>"
	//				+ "</cardinality>";
	//		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
	//		TestUtils.assertEqualsSortedModels(sortedModels,
	//				"0 0 1 0", "0 0 2 0",
	//				"0 1 0 1", "0 1 1 0",
	//				"0 2 0 2", "0 2 2 0",
	//				"1 0 0 1", "1 0 1 0",
	//				"1 1 0 1", "1 1 2 1",
	//				"1 2 1 2", "1 2 2 1",
	//				"2 0 0 2", "2 0 2 0",
	//				"2 1 1 2", "2 1 2 1",
	//				"2 2 0 2", "2 2 1 2");
	//	}
	
	@Test
	public void testCardinality4() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(4, 0, 2));
		String ctrSection = "<cardinality>"
				+ "<list> i0 i1 </list>"
				+ "<values> 0 1 </values>"
				+ "<occurs> i2 i3 </occurs>"
				+ "</cardinality>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 2 0", "0 1 1 1", "1 0 1 1", "1 1 0 2");
	}
	
	@Test
	public void testCardinality5() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 3));
		String ctrSection = "<cardinality>"
				+ "<list> i0 i0 i0 </list>"
				+ "<values> 0 1 2 3 </values>"
				+ "<occurs> 0..1 0..1 1..3 0..1 </occurs>"
				+ "</cardinality>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "2");
	}
	
	@Test
	public void testCardinality6() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = "<cardinality>"
				+ "<list> i0 i0 </list>"
				+ "<values> 0 1 </values>"
				+ "<occurs> i0 i0 </occurs>"
				+ "</cardinality>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testCardinality7() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = "<cardinality>"
				+ "<list> i0 i0 </list>"
				+ "<values> 1 2 </values>"
				+ "<occurs> i0 i0 </occurs>"
				+ "</cardinality>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}

}
