package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * A class that translates an Aralia formatted problem into a solver.
 * 
 * @author lonca
 */
public class Br4cpAraliaReader {

	private static final String COMMENT_BEGINNING_SYM = "/*";
	private static final String DECLARATION_SYM = "#";
	private final ISolver solver;
	private BufferedReader reader = null;
	private FormulaToSolver treeToSolver;

	private ConfigVarMap varMap;

	public Br4cpAraliaReader(ISolver solver, ConfigVarMap varMap) {
		this.solver = solver;
		this.varMap = varMap;
		this.treeToSolver = new FormulaToSolver(solver, varMap);
	}

	/**
	 * Parses an Aralia instance and translate it into the solver.
	 * 
	 * @param file
	 *            the file location
	 * @throws IOException
	 *             if a problem occur while reading the file
	 */
	public void parseInstance(String file) throws IOException {
		if (this.reader == null) {
			this.reader = new BufferedReader(new FileReader(file));
			parseInstance();
			this.reader.close();
		}
	}

	private void parseInstance() throws IOException {
		String line;
		while ((line = this.reader.readLine()) != null) {
			parseLine(line);
		}
	}

	private void parseLine(String line) {
		line = normalizeLine(line);
		if ("".equals(line)) {
			return;
		}
		try {
			if (line.startsWith(DECLARATION_SYM)) {
				newDeclarationLine(line.substring(DECLARATION_SYM.length()));
			} else {
				newClausalConstraint(line);
			}
		} catch (Exception e) {
			System.err.println("unable to parse line (" + e.getMessage()
					+ ") : \"" + line + "\"");
			e.printStackTrace();
		}
	}

	private void newDeclarationLine(String line) {
		int indexOfComma = line.indexOf(',');
		int min = Integer.valueOf(line.substring(1, indexOfComma));
		int indexOfOpeningBracket = line.indexOf('[');
		int max = Integer.valueOf(line.substring(indexOfComma + 1,
				line.indexOf(',', indexOfComma + 1)));
		int indexOfClosingBracket = line.indexOf(']');
		String[] objects = line.substring(indexOfOpeningBracket + 1,
				indexOfClosingBracket).split(",");
		try {
			newCardinalityConstraint(min, max, objects);
		} catch (ContradictionException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void newCardinalityConstraint(int min, int max, String[] objects)
			throws ContradictionException {
		IVecInt lits = new VecInt(objects.length);
		for (String obj : objects) {
			Integer var = this.varMap.getSolverVar(obj);
			lits.push(var);
		}
		if (min == max) {
			this.solver.addExactly(lits, min);
		} else {
			this.solver.addAtMost(lits, max);
			if (min > 0) {
				this.solver.addAtLeast(lits, min);
			} else {
				this.varMap.setAsOptionalConfigVar(objects[0]);
			}
		}
	}

	private void newClausalConstraint(String line) {
		AraliaParser parser = new AraliaParser();
		org.sat4j.br4cp.AraliaParser.LogicFormulaNode formula = parser
				.getFormula(line);
		this.treeToSolver.encode(formula);
	}

	private String normalizeLine(String line) {
		int lastIndex = line.length() - 1;
		if (line.length() > 0 && line.charAt(lastIndex) == ';')
			line = line.substring(0, lastIndex);
		line = removeEnclosingParanthesis(line);
		line = removeComments(line);
		line = removeSpaces(line);
		return line;
	}

	private String removeEnclosingParanthesis(String line) {
		boolean found = true;
		while (found) {
			found = false;
			if (line.length() == 0)
				return line;
			int lastIndex = line.length() - 1;
			if ((line.charAt(0) != '(') || (line.charAt(lastIndex) != ')'))
				return line;
			int cptPar = 0;
			int index = 0;
			for (char c : line.toCharArray()) {
				if (c == '(')
					++cptPar;
				else if (c == ')')
					--cptPar;
				if (cptPar == 0) {
					if (index == lastIndex) {
						line = line.substring(1, lastIndex);
						found = true;
					} else {
						return line;
					}
				}
				++index;
			}
		}
		return line;
	}

	private String removeSpaces(String line) {
		char[] chars = line.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; ++i) {
			if (chars[i] != ' ') {
				sb.append(chars[i]);
			}
		}
		return sb.toString();
	}

	private String removeComments(String line) {
		int commentBeginning;
		while ((commentBeginning = line.indexOf(COMMENT_BEGINNING_SYM)) != -1) {
			int commentEnd = line.indexOf("*/", commentBeginning);
			if (commentEnd == -1) {
				throw new IllegalArgumentException("no comment ending symbol");
			}
			line = line.substring(0, commentBeginning)
					+ line.substring(commentEnd + "*/".length());
		}
		return line;
	}
}
