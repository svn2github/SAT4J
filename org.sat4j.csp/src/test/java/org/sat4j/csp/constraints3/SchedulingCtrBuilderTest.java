package org.sat4j.csp.constraints3;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.reader.XMLCSP3Reader;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class SchedulingCtrBuilderTest {
	
	private IPBSolver solver;
	
	@Before
	public void setUp() {
		this.solver = SolverFactory.newDefault();
	}
	
	private String[] stretches(int nVars, int nValues, int widthMin[], int widthMax[], int patterns[][], int last) {
		List<String> stretches = new ArrayList<>();
		for(int i=0; i<nValues; ++i) {
			if(i == last) continue;
			if(last >= 0 && patterns != null) {
					boolean patternFound = false;
					for(int j=0; j<patterns.length; ++j) {
						if(patterns[j][0] == last && patterns[j][1] == i) {
							patternFound = true;
							break;
						}
					}
					if(!patternFound) continue;
			}
			for(int j=widthMin[i]; j<= Math.min(widthMax[i],nVars); ++j) {
				StringBuffer sbuf = new StringBuffer();
				sbuf.append(i);
				for(int k=1; k<j; ++k) {
					sbuf.append(' ');
					sbuf.append(i);
				}
				String curStretch = sbuf.toString();
				if(j == nVars) {
					stretches.add(curStretch);
					continue;
				}
				String subStretches[] = stretches(nVars-j, nValues, widthMin, widthMax, patterns, i);
				for(String subStretch : subStretches) {
					stretches.add(curStretch+" "+subStretch);
					continue;
				}
			}
		}
		String[] stretchesArray = new String[stretches.size()];
		stretchesArray = stretches.toArray(stretchesArray);
		return stretchesArray;
	}
	
	private String[] stretches(int nVars, int nValues, int widthMin[], int widthMax[]) {
		return stretches(nVars, nValues, widthMin, widthMax, null, -1);
	}
	
	private String[] stretches(int nVars, int nValues, int widthMin[], int widthMax[], int patterns[][]) {
		return stretches(nVars, nValues, widthMin, widthMax, patterns, -1);
	}

	@Test
	public void testStretch1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(7, 0, 3));
		String ctrSection = "<stretch>"
				+ "<list> i0 i1 i2 i3 i4 i5 i6 </list>"
				+ "<values> 0 1 2 3 </values>"
				+ "<widths> 1..3 1..3 2..3 2..4 </widths>"
				+ "</stretch>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		String[] stretchesArrays = stretches(7, 4, new int[]{1,1,2,2}, new int[]{3,3,3,4});
		Arrays.sort(stretchesArrays);
		TestUtils.assertEqualsSortedModels(sortedModels, 
				stretchesArrays);
	}
	
	@Test
	public void testStretch2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(7, 0, 3));
		String ctrSection = "<stretch>"
				+ "<list> i0 i1 i2 i3 i4 i5 i6 </list>"
				+ "<values> 0 1 2 3 </values>"
				+ "<widths> 1..3 1..3 2..3 2..4 </widths>"
				+ "<patterns> (0,1) (1,2) (2,3) </patterns>"
				+ "</stretch>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		String[] stretchesArrays = stretches(7, 4, new int[]{1,1,2,2}, new int[]{3,3,3,4}, new int[][]{new int[]{0,1}, new int[]{1,2}, new int[]{2,3}});
		Arrays.sort(stretchesArrays);
		TestUtils.assertEqualsSortedModels(sortedModels, 
				stretchesArrays);
	}
	
	@Test
	public void testStretch3() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 2));
		String ctrSection = "<stretch>"
				+ "<list> i0 i0 i0 </list>"
				+ "<values> 0 1 2 3 </values>"
				+ "<widths> 0..0 1..1 2..2 3..3 </widths>"
				+ "</stretch>";
		List<String> sortedModels = null;
		try {
			sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		} catch(AssertionError e) {
			assertTrue(e.getMessage().startsWith("java.lang.IllegalArgumentException:"));
			return;
		}
		TestUtils.assertEqualsSortedModels(sortedModels, "3");
	}
	
	String[] noOverlapArrays(int originMax, int nOrigins, int lengthMin, int lengthMax, boolean zeroIgnored, boolean justOriginsVars) {
		List<String> result = new ArrayList<>();
		int values[] = new int[nOrigins<<1];
		for(int i=0; i<nOrigins; ++i) values[i] = 0;
		for(int i=nOrigins; i<(nOrigins<<1); ++i) values[i] = lengthMin;
		for(;;) {
			if(checkNoOverlap(values, zeroIgnored)) {
				StringBuffer sb = new StringBuffer();
				sb.append(values[0]);
				int maxIndex = justOriginsVars ? nOrigins : nOrigins<<1;
				for(int i=1; i<maxIndex; ++i) {
					sb.append(' ');
					sb.append(values[i]);
				}
				result.add(sb.toString());
			}
			boolean done = false;
			for(int i=values.length-1; i>=0; --i) {
				++values[i];
				if(i >= nOrigins && values[i] > lengthMax) {
					values[i] = lengthMin;
					continue;
				} else if(i < nOrigins && values[i] > originMax) {
					if(i == 0) {
						done = true;
						break;
					}
					values[i] = 0;
					continue;
				} else {
					break;
				}
			}
			if(done) break;
		}
		String arrayResult[] = new String[result.size()];
		arrayResult = result.toArray(arrayResult);
		return arrayResult;
	}
	
	private boolean checkNoOverlap(int[] values, boolean zeroIgnored) {
		int nOrigins = values.length >> 1;
		for(int i=0; i<nOrigins-1; ++i) {
			if(zeroIgnored && values[i+nOrigins] == 0) continue;
			for(int j=i+1; j<nOrigins; ++j) {
				if(zeroIgnored && values[j+nOrigins] == 0) continue;
				if(values[i] + values[i+nOrigins] > values[j] && values[j] + values[j+nOrigins] > values[i]) return false;
			}
		}
		return true;
	}
	
	@Test
	public void testNoOverlap1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 4));
		String ctrSection = "<noOverlap>"
				+ "<origins> i0 i1 i2 </origins>"
				+ "<lengths> 2 2 2 </lengths>"
				+ "</noOverlap>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		String[] noOverlapArrays = noOverlapArrays(4, 3, 2, 2, true, true);
		Arrays.sort(noOverlapArrays);
		TestUtils.assertEqualsSortedModels(sortedModels, noOverlapArrays);
	}
	
	@Test
	public void testNoOverlap2() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 4));
		String ctrSection = "<noOverlap zeroIgnored=\"false\">"
				+ "<origins> i0 i1 i2 </origins>"
				+ "<lengths> 0 0 0 </lengths>"
				+ "</noOverlap>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		String[] noOverlapArrays = noOverlapArrays(4, 3, 0, 0, false, true);
		Arrays.sort(noOverlapArrays);
		TestUtils.assertEqualsSortedModels(sortedModels, noOverlapArrays);
	}

	@Test
	public void testNoOverlap3() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(6, 0, 2));
		String ctrSection = "<noOverlap zeroIgnored=\"true\">"
				+ "<origins> i0 i1 i2 </origins>"
				+ "<lengths> i3 i4 i5 </lengths>"
				+ "</noOverlap>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		String[] noOverlapArrays = noOverlapArrays(2, 3, 0, 2, true, false);
		Arrays.sort(noOverlapArrays);
		TestUtils.assertEqualsSortedModels(sortedModels, noOverlapArrays);
	}
	
	@Test
	public void testNoOverlap4() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(6, 0, 2));
		String ctrSection = "<noOverlap zeroIgnored=\"false\">"
				+ "<origins> i0 i1 i2 </origins>"
				+ "<lengths> i3 i4 i5 </lengths>"
				+ "</noOverlap>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		String[] noOverlapArrays = noOverlapArrays(2, 3, 0, 2, false, false);
		Arrays.sort(noOverlapArrays);
		TestUtils.assertEqualsSortedModels(sortedModels, noOverlapArrays);
	}
	
	@Test
	public void testNoOverlap5() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(6, 0, 1));
		String ctrSection = "<noOverlap zeroIgnored=\"true\">"
				+ "<origins> (i0,i1) (i2,i3) (i4,i5) </origins>"
				+ "<lengths> (1,1) (1,1) (1,1) </lengths>"
				+ "</noOverlap>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels,
				"0 0 0 1 1 0", "0 0 0 1 1 1", "0 0 1 0 0 1", "0 0 1 0 1 1", "0 0 1 1 0 1", "0 0 1 1 1 0",
				"0 1 0 0 1 0", "0 1 0 0 1 1", "0 1 1 0 0 0", "0 1 1 0 1 1", "0 1 1 1 0 0", "0 1 1 1 1 0",
				"1 0 0 0 0 1", "1 0 0 0 1 1", "1 0 0 1 0 0", "1 0 0 1 1 1", "1 0 1 1 0 0", "1 0 1 1 0 1",
				"1 1 0 0 0 1", "1 1 0 0 1 0", "1 1 0 1 0 0", "1 1 0 1 1 0", "1 1 1 0 0 0", "1 1 1 0 0 1");
	}
	
	@Test
	public void testNoOverlap6() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(2, 0, 4));
		String ctrSection = "<noOverlap zeroIgnored=\"false\">"
				+ "<origins> i0 i1 i0 </origins>"
				+ "<lengths> 1 1 1 </lengths>"
				+ "</noOverlap>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, new String[]{});
	}

}
