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
public class LanguageCtrBuilderTest {
	
	private IPBSolver solver;
	
	@Before
	public void setUp() {
		this.solver = SolverFactory.newDefault();
	}
	
	@Test
	public void testRegular1() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(7, 0, 1));
		String ctrSection = "<regular>"
				+ "<list> i0 i1 i2 i3 i4 i5 i6 </list>"
				+ "<transitions>"
				+ "(a,0,a) (a,1,b) (b,1,c) (c,0,d) (d,0,d) (d,1,e) (e,0,e)"
				+ "</transitions>"
				+ "<start> a </start>"
				+ "<final> e </final>"
				+ "</regular>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels,
				"0 1 1 0 0 1 0",
				"0 1 1 0 0 1 1",
				"0 1 1 0 1 1 0",
				"0 1 1 0 1 1 1",
				"1 1 1 0 0 1 0",
				"1 1 1 0 0 1 1",
				"1 1 1 0 1 1 0",
				"1 1 1 0 1 1 1");
	}

	@Test
	public void testMDD() {
		XMLCSP3Reader reader = new XMLCSP3Reader(solver);
		String varSection = TestUtils.buildVariablesSection(TestUtils.buildIntegerVars(3, 0, 2));
		String ctrSection = "<mdd>"
				+ "<list> i0 i1 i2 </list>"
				+"<transitions>"
				+"(r,0,n1)(r,1,n2)(r,2,n3)"
				+"(n1,2,n4)(n2,2,n4)(n3,0,n5)"
				+"(n4,0,t)(n5,0,t)"
				+"</transitions>"
				+ "</mdd>";
		List<String> sortedModels = TestUtils.computeModels(reader, solver, varSection, ctrSection);
		TestUtils.assertEqualsSortedModels(sortedModels, "0 2 0", "1 2 0", "2 0 0");
	}
	
}
