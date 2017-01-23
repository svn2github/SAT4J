package org.sat4j.csp.constraints3;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class TestUtils {

	public static InputStream stringAsStream(String str) {
		try {
			return new ByteArrayInputStream(str.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String buildVariablesSection(String... varDeclarations) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<variables>\n");
		for(String varDecl : varDeclarations) {
			sbuf.append(varDecl);
		}
		sbuf.append("</variables>\n");
		return sbuf.toString();
	}

	public static void assertEqualsSortedModels(List<String> actual, String... expected) {
		if(expected.length != actual.size()) {
			fail(diffModels(actual, expected));
		}
		for(int i=0; i<expected.length; ++i) {
			if(!expected[i].equals(actual.get(i))) {
				fail(diffModels(actual, expected));
			}
		}
	}
	
	public static List<String> computeModels(XMLCSP3Reader reader, IPBSolver solver, String varSection, String ctrSection) {
		return TestUtils.computeModels(reader, solver, varSection, ctrSection, null);
	}

	public static List<String> computeModels(XMLCSP3Reader reader, IPBSolver solver, String varSection, String ctrSection, String objSection) {
		String strInstance = TestUtils.buildInstance(varSection, ctrSection, objSection);
		try {
			reader.parseInstance(stringAsStream(strInstance));
		} catch (ParseFormatException | IOException e) {
			fail(e.getMessage());
		} catch (ContradictionException e) {
			return new ArrayList<>();
		}
		List<String> sortedModels = TestUtils.getSortedStringModels(reader, solver, objSection != null);
		return sortedModels;
	}
	
	public static List<String> getSortedStringModels(XMLCSP3Reader reader, IPBSolver solver) {
		return getSortedStringModels(reader, solver, false);
	}

	public static List<String> getSortedStringModels(XMLCSP3Reader reader, IPBSolver solver, boolean optRequired) {
		List<int[]> models = TestUtils.getAllModels(reader, solver, optRequired);
		SortedSet<String> strModels = new TreeSet<String>();
		for(int i=0; i<models.size(); ++i) {
			strModels.add(reader.decodeModelAsValueSequence(models.get(i)));
		}
		return new ArrayList<String>(strModels);
	}
	
	public static List<int[]> getAllModels(XMLCSP3Reader reader, ISolver solver) {
		return getAllModels(reader, solver, false);
	}

	public static List<int[]> getAllModels(XMLCSP3Reader reader, ISolver solver, boolean optRequired) {
		List<int[]> models = new ArrayList<int[]>();
		try {
			while(solver.isSatisfiable()) {
				int[] model = solver.model();
				models.add(model);
				if(reader.discardModel(model)) break;
				if(models.size() == 1 && optRequired) {
					if(!fixOptValue((IPBSolver) solver, models.get(0))) break;
				}
			}
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
		return models;
	}
	
	private static boolean fixOptValue(IPBSolver solver, int[] model) {
		final ObjectiveFunction obj = solver.getObjectiveFunction();
		final BigInteger degree = obj.calculateDegree(solver);
		try {
			solver.addAtLeast(obj.getVars(), obj.getCoeffs(), degree);
			solver.addAtMost(obj.getVars(), obj.getCoeffs(), degree);
		} catch (ContradictionException e) {
			return false;
		}
		return true;
	}

	public static String buildIntegerVars(int nVars, int min, int max) {
		return buildIntegerVars(nVars, min, max, 0);
	}

	public static String buildIntegerVars(int nVars, int min, int max, int startIndex) {
		StringBuffer sbuf = new StringBuffer();
		for(int i=startIndex; i<startIndex+nVars; ++i) {
			sbuf.append("<var id=\"i");
			sbuf.append(Integer.toString(i));
			sbuf.append("\"> ");
			sbuf.append(Integer.toString(min));
			sbuf.append("..");
			sbuf.append(Integer.toString(max));
			sbuf.append(" </var>\n");
		}
		return sbuf.toString();
	}

	public static String buildBinaryVars(int n) {
		return buildBinaryVars(n, 0);
	}
	
	public static String buildBinaryVars(int n, int startIndex) {
		StringBuffer sbuf = new StringBuffer();
		for(int i=startIndex; i<startIndex+n; ++i) {
			sbuf.append("<var id=\"b");
			sbuf.append(Integer.toString(i));
			sbuf.append("\"> 0 1 </var>\n");
		}
		return sbuf.toString();
	}

	public static String buildInstance(String varSection, String ctrSection, String objSection) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<instance format=\"XCSP3\" type=\"");
		sbuf.append(objSection == null ? "CSP" : "COP");
		sbuf.append("\">\n");
		if(varSection != null) sbuf.append(varSection);
		if(ctrSection != null) sbuf.append(ctrSection);
		if(objSection != null) sbuf.append(objSection);
		sbuf.append("</instance>\n");
		return sbuf.toString();
	}

	public static String buildInstance(String varSection, String ctrSection) {
		return buildInstance(varSection, ctrSection, null);
	}

	public static String buildInstance(String varSection) {
		return buildInstance(varSection, "<constraints>\n</constraints>\n", null);
	}

	public static String buildConstraintsSection(String... ctrDeclarations) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<constraints>\n");
		for(String ctrDecl : ctrDeclarations) {
			sbuf.append(ctrDecl);
		}
		sbuf.append("</constraints>\n");
		return sbuf.toString();
	}
	
	public static String buildObjectivesSection(String... objDeclarations) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<objectives>\n");
		for(String objDecl : objDeclarations) {
			sbuf.append(objDecl);
		}
		sbuf.append("</objectives>\n");
		return sbuf.toString();
	}
	
	public static String diffModels(List<String> actual, String... expectedArray) {
		List<String> expected = Arrays.asList(expectedArray);
		return diffModels(actual, expected);
	}
	
	public static String diffModels(List<String> actual, List<String> expected) {
		List<String> onlyInExpected = new LinkedList<String>(expected);
		onlyInExpected.removeAll(actual);
		List<String> onlyInActual = new LinkedList<String>(actual);
		onlyInActual.removeAll(expected);
		StringBuffer sb = new StringBuffer();
		sb.append("<<only in expected:");
		for(String str : onlyInExpected) {
			sb.append("  ");
			sb.append(str);
		}
		sb.append('\n');
		sb.append("only in actual:");
		for(String str : onlyInActual) {
			sb.append("  ");
			sb.append(str);
		}
		sb.append(">>");
		return sb.toString();
	}

}
