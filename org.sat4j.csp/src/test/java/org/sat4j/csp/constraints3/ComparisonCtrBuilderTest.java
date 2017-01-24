package org.sat4j.csp.constraints3;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class ComparisonCtrBuilderTest {
	
	private IXCSP3Solver solver;
	
	@Before
	public void setUp() {
		this.solver = TestUtils.newSolver();
	}

	@Test
	public void testCtrAllDiff() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<allDifferent> i0 i1 i2 </allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1 2", "0 2 1", "1 0 2", "1 2 0", "2 0 1", "2 1 0");
	}
	
	@Test
	public void testCtrAllDiff2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<allDifferent> i0 i0 </allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testCtrAllDiffExcept() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<allDifferent>"
				+ "<list> i0 i1 i2 </list>"
				+ "<except> 0 </except>"
				+ "</allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		List<String> expectedModels = new ArrayList<String>();
		for(int i=0; i<=2; ++i) {
			for(int j=0; j<=2; ++j) {
				if(j>0 && i==j) continue;
				for(int k=0; k<=2; ++k) {
					if(k>0 && (i==k || j==k)) continue;
					expectedModels.add(i+" "+j+" "+k);
				}
			}
		}
		String[] arrExpectedModels = new String[expectedModels.size()];
		arrExpectedModels = expectedModels.toArray(arrExpectedModels);
		TestUtils.assertEqualsSortedModels(sortedModels, arrExpectedModels);
	}
	
	@Test
	public void testCtrAllDiffExcept2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<allDifferent>"
				+ "<list> i0 i0 </list>"
				+ "<except> 0 </except>"
				+ "</allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0", "1", "2");
	}
	
	@Test
	public void testCtrAllDiffList() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(6));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<allDifferent>"
				+ "<list> b0 b1 </list>"
				+ "<list> b2 b3 </list>"
				+ "<list> b4 b5 </list>"
				+ "</allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		List<String> expectedModels = new ArrayList<String>();
		for(int i=0; i<=3; ++i) {
			for(int j=0; j<=3; ++j) {
				if(i == j) continue;
				for(int k=0; k<=3; ++k) {
					if(i == k || j == k) continue;
					expectedModels.add(((i&2)>>1)+" "+(i&1)+" "+((j&2)>>1)+" "+(j&1)+" "+((k&2)>>1)+" "+(k&1));
				}
			}
		}
		String[] arrExpectedModels = new String[expectedModels.size()];
		arrExpectedModels = expectedModels.toArray(arrExpectedModels);
		TestUtils.assertEqualsSortedModels(sortedModels, arrExpectedModels);
	}
	
	@Test
	public void testCtrAllDiffList2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<allDifferent>"
				+ "<list> b0 b0 </list>"
				+ "<list> b1 b1 </list>"
				+ "</allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1", "1 0");
	}
	
	@Test
	public void testCtrAllDiffList3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildBooleanVars(2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<allDifferent>"
				+ "<list> b0 b1 </list>"
				+ "<list> b0 b1 </list>"
				+ "</allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testCtrAllDiffMatrix() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(6, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<allDifferent>"
				+ "<matrix>"
				+ "(i0,i1)"
				+ "(i2,i3)"
				+ "(i4,i5)"
				+ "</matrix>"
				+ "</allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		List<String> expectedModels = new ArrayList<String>();
		int[] values = new int[6];
		int curIndex = 0;
		values[0] = -1;
		boolean shouldContinue = false;
		for(;;) {
			shouldContinue = false;
			++values[curIndex];
			if(values[curIndex] == 3) {
				if(curIndex == 0) break;
				values[curIndex] = 0;
				--curIndex;
				continue;
			}
			int line = curIndex/2;
			for(int i=2*line; i<curIndex; ++i) {
				if(values[i] == values[curIndex]) {
					shouldContinue = true;
					break;
				}
			}
			if(shouldContinue) continue;
			int col = curIndex%2;
			for(int i=col; i<curIndex; i+=2) {
				if(values[i] == values[curIndex]) {
					shouldContinue = true;
					break;
				}
			}
			if(shouldContinue) continue;
			if(curIndex == 5) {
				String mod = Integer.toString(values[0]);
				for(int i=1; i<6; ++i) mod += " "+values[i];
				expectedModels.add(mod);
				continue;
			}
			values[curIndex+1] = -1;
			++curIndex;
		}
		String[] arrExpectedModels = new String[expectedModels.size()];
		arrExpectedModels = expectedModels.toArray(arrExpectedModels);
		TestUtils.assertEqualsSortedModels(sortedModels, arrExpectedModels);
	}
	
	@Test
	public void testCtrAllDiffMatrix2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<allDifferent>"
				+ "<matrix>"
				+ "(i0,i1)"
				+ "(i1,i0)"
				+ "</matrix>"
				+ "</allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1", "0 2", "1 0", "1 2", "2 0", "2 1");
	}
	
	@Test
	public void testCtrAllDiffMatrix3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<allDifferent>"
				+ "<matrix>"
				+ "(i0,i0)"
				+ "(i1,i1)"
				+ "</matrix>"
				+ "</allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testCtrAllDiffMatrix4() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<allDifferent>"
				+ "<matrix>"
				+ "(i0,i1)"
				+ "(i0,i1)"
				+ "</matrix>"
				+ "</allDifferent>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testCtrAllEqual() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<allEqual> i0 i1 i2 </allEqual>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0", "1 1 1", "2 2 2");
	}
	
	@Test
	public void testCtrAllEqual2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 2));
		String ctrSection = TestUtils.buildConstraintsSection("<allEqual> i0 i0 i0 </allEqual>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0", "1", "2");
	}
	
	@Test
	public void testCtrOrdered1() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 3));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<ordered>"
				+ "<list>i0 i1 i2</list>"
				+ "<operator> gt </operator>"
				+ "</ordered>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "2 1 0", "3 1 0", "3 2 0", "3 2 1");
	}
	
	@Test
	public void testCtrOrdered2() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<ordered>"
				+ "<list>i0 i1 i2</list>"
				+ "<operator> le </operator>"
				+ "</ordered>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 0 0", "0 0 1", "0 1 1", "1 1 1");
	}
	
	@Test
	public void testCtrOrdered3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<ordered>"
				+ "<list>i0 i0 i0</list>"
				+ "<operator> le </operator>"
				+ "</ordered>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0", "1");
	}
	
	@Test
	public void testCtrOrdered4() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(1, 0, 1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<ordered>"
				+ "<list>i0 i0 i0</list>"
				+ "<operator> lt </operator>"
				+ "</ordered>\n");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testLex1() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(6, 0, 1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<lex>"
				+ "<list> i0 i1 i2 </list>"
				+ "<list> i3 i4 i5 </list>"
				+ "<operator> lt </operator>"
				+ "</lex>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, 
				"0 0 0 0 0 1", "0 0 0 0 1 0", "0 0 0 0 1 1", "0 0 0 1 0 0", "0 0 0 1 0 1", "0 0 0 1 1 0", "0 0 0 1 1 1",
				"0 0 1 0 1 0", "0 0 1 0 1 1", "0 0 1 1 0 0", "0 0 1 1 0 1", "0 0 1 1 1 0", "0 0 1 1 1 1",
				"0 1 0 0 1 1", "0 1 0 1 0 0", "0 1 0 1 0 1", "0 1 0 1 1 0", "0 1 0 1 1 1",
				"0 1 1 1 0 0", "0 1 1 1 0 1", "0 1 1 1 1 0", "0 1 1 1 1 1",
				"1 0 0 1 0 1", "1 0 0 1 1 0", "1 0 0 1 1 1",
				"1 0 1 1 1 0", "1 0 1 1 1 1",
				"1 1 0 1 1 1");
	}
	
	public void testLex2() {
		System.out.println("ClassCastException in testLex2 ; check origin (seems to be the parser)");
	}
	// TODO: ClassCastException occurs for buildCtrLexMatrix ; bug in parser ?
	//	@Test
	//	public void testLex2() {
	//		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
	//		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(6, 0, 1));
	//		String ctrSection = "<lex>"
	//				+ "<matrix>"
	//				+ "(i0 i1 i2)"
	//				+ "(i3 i4 i5)"
	//				+ "</matrix>"
	//				+ "<operator> le </operator>"
	//				+ "</lex>";
	//		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
	//		TestUtils.assertEqualsSortedModels(sortedModels, 
	//				"0 0 0 0 0 0", "0 0 0 0 0 1", "0 0 0 0 1 1", "0 0 0 1 1 1",
	//				"0 0 1 0 0 1", "0 0 1 0 1 1", "0 0 1 1 1 1",
	//				"0 1 1 0 1 1", "0 1 1 1 1 1",
	//				"1 1 1 1 1 1");
	//	}
	
	@Test
	public void testLex3() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<lex>"
				+ "<list> i0 i1 i2 </list>"
				+ "<list> i0 i1 i2 </list>"
				+ "<operator> lt </operator>"
				+ "</lex>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}
	
	@Test
	public void testLex4() {
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 1));
		String ctrSection = TestUtils.buildConstraintsSection(""
				+ "<lex>"
				+ "<list> i0 i0 i0 </list>"
				+ "<list> i1 i1 i1 </list>"
				+ "<operator> lt </operator>"
				+ "</lex>");
		List<String> sortedModels = TestUtils.computeModels(solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 1");
	}

}
