package org.sat4j.csp.constraints3;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
* 
* Utility methods for XCSP3 solvers test cases.
* The newSolver() method must be implemented in order to return a new (clean) instance of IXCSP3Solver.
*/
public class TestUtils {
	
	private static Class<? extends IXCSP3Solver> solverClass;

	public static void setSolverClass(Class<? extends IXCSP3Solver> solverClass) {
		TestUtils.solverClass = solverClass;
	}
	
	public static IXCSP3Solver newSolver() {
		return IXCSP3Solver.newSolver(TestUtils.solverClass);
	}
	
	/**
	 * Returns an XCSP3 declaration line for boolean variables (integer variables whose domain is 0,1) as a string.
	 * Variables are named b0, b1, ... and may take all the integer values between their lower and higher bound.
	 * 
	 * @param n the number of boolean variables to declare
	 * @return the string declaring the variables
	 */
	public static String buildBooleanVars(int n) {
		return buildBooleanVars(n, 0);
	}
	
	/**
	 * Returns an XCSP3 declaration line for boolean variables (integer variables whose domain is 0,1) as a string.
	 * Variables are named from bk, where k is the provided start index.
	 * 
	 * @param n the number of boolean variables to declare
	 * @param startIndex the start index from which variable names are generated
	 * @return the string declaring the variables
	 */
	public static String buildBooleanVars(int n, int startIndex) {
		StringBuffer sbuf = new StringBuffer();
		for(int i=startIndex; i<startIndex+n; ++i) {
			sbuf.append("<var id=\"b");
			sbuf.append(Integer.toString(i));
			sbuf.append("\"> 0 1 </var>\n");
		}
		return sbuf.toString();
	}
	
	/**
	 * Returns an XCSP3 declaration line for integer variables as a string.
	 * Variables are named i0, i1, ... and may take all the integer values between their lower and higher bound.
	 * 
	 * @param nVars the number of integer variables to declare
	 * @param min the lower bound (included)
	 * @param max the higher bound (included)
	 * @return the string declaring the variables
	 */
	public static String buildIntegerVars(int nVars, int min, int max) {
		return buildIntegerVars(nVars, min, max, 0);
	}

	/**
	 * Returns an XCSP3 declaration line for integer variables as a string.
	 * Variables are named from ik, where k is the provided start index.
	 * They may take all the integer values between their lower and higher bound.
	 * 
	 * @param nVars the number of integer variables to declare
	 * @param min the lower bound (included)
	 * @param max the higher bound (included)
	 * @param startIndex the start index from which variable names are generated
	 * @return the string declaring the variables
	 */
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

	/**
	 * Returns an XCSP3 instance as a string provided the variable, constraint, and optional objective sections.
	 * Objective section must be set to null for decision problems.
	 * 
	 * @param varSection the variable section
	 * @param ctrSection the constraint section
	 * @param objSection the objective section
	 * @return the corresponding XCSP3 instance
	 */
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

	/**
	 * Returns a decision XCSP3 instance as a string provided the variable and constraint sections.
	 * 
	 * @param varSection the variable section
	 * @param ctrSection the constraint section
	 * @return the corresponding XCSP3 instance
	 */
	public static String buildInstance(String varSection, String ctrSection) {
		return buildInstance(varSection, ctrSection, null);
	}
	
	/**
	 * Returns the XCSP3 variable section corresponding to the provided variable declarations as a string.
	 * 
	 * @param varDeclarations the variable declarations
	 * @return the corresponding variable section.
	 */
	public static String buildVariablesSection(String... varDeclarations) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<variables>\n");
		for(String varDecl : varDeclarations) {
			sbuf.append(varDecl);
		}
		sbuf.append("</variables>\n");
		return sbuf.toString();
	}

	/**
	 * Returns the XCSP3 constraint section corresponding to the provided constraint declarations as a string.
	 * 
	 * @param ctrDeclarations the constraint declarations
	 * @return the corresponding constraint section.
	 */
	public static String buildConstraintsSection(String... ctrDeclarations) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<constraints>\n");
		for(String ctrDecl : ctrDeclarations) {
			sbuf.append(ctrDecl);
		}
		sbuf.append("</constraints>\n");
		return sbuf.toString();
	}
	
	/**
	 * Returns the XCSP3 objective section corresponding to the provided objective declarations as a string.
	 * 
	 * @param objDeclarations the objective declarations
	 * @return the corresponding objective section.
	 */
	public static String buildObjectivesSection(String... objDeclarations) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<objectives>\n");
		for(String objDecl : objDeclarations) {
			sbuf.append(objDecl);
		}
		sbuf.append("</objectives>\n");
		return sbuf.toString();
	}
	
	/**
	 * Computes the whole set of models a decision instance admits.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models. Models must be returned in the alphabetical order.
	 * 
	 * @param solver the solver that will compute the models
	 * @param instance the instance
	 * @return the sorted set of models the instance admits
	 */
	public static List<String> computeModels(IXCSP3Solver solver, String instance) {
		return solver.computeModels(instance);
	}
	
	/**
	 * Computes the whole set of models a decision instance admits.
	 * The instance is given by its variable and constraint sections.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models. Models must be returned in the alphabetical order.
	 * 
	 * @param solver the solver that will compute the models
	 * @param varSection the instance variable section
	 * @param ctrSection the instance constraint section
	 * @return the sorted set of models the instance admits
	 */
	public static List<String> computeModels(IXCSP3Solver solver, String varSection, String ctrSection) {
		return solver.computeModels(varSection, ctrSection);
	}
	
	/**
	 * Computes the whole set of models an optimization instance admits.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models. Models must be returned in the alphabetical order.
	 * 
	 * @param solver the solver that will compute the models
	 * @param instance the instance
	 * @return the sorted set of models the instance admits
	 */
	public static List<String> computeOptimalModels(IXCSP3Solver solver, String instance) {
		return solver.computeModels(instance);
	}
	
	/**
	 * Computes the whole set of models an optimization instance admits.
	 * The instance is given by its variable, constraint and objective sections.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models. Models must be returned in the alphabetical order.
	 * 
	 * @param solver the solver that will compute the models
	 * @param varSection the instance variable section
	 * @param ctrSection the instance constraint section
	 * @return the sorted set of models the instance admits
	 */
	public static List<String> computeOptimalModels(IXCSP3Solver solver, String varSection, String ctrSection, String objSection) {
		return solver.computeOptimalModels(varSection, ctrSection, objSection);
	}

	/**
	 * Check if two lists of models are equivalent ; call fail() junit method if it is not the case.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models.
	 * This method is order sensitive.
	 * 
	 * @param actual the actual models
	 * @param expected the expected models
	 */
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
	
	/**
	 * Check if two lists of models are equivalent ; call fail() junit method if it is not the case.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models.
	 * This method is order sensitive.
	 * 
	 * @param actual the actual models
	 * @param expected the expected models
	 */
	public static void assertEqualsSortedModels(List<String> actual, List<String> expected) {
		String[] expectedArray = new String[expected.size()];
		expectedArray = expected.toArray(expectedArray);
		assertEqualsSortedModels(actual, expectedArray);
	}
	
	private static String diffModels(List<String> actual, String... expectedArray) {
		List<String> expected = Arrays.asList(expectedArray);
		return diffModels(actual, expected);
	}
	
	private static String diffModels(List<String> actual, List<String> expected) {
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
