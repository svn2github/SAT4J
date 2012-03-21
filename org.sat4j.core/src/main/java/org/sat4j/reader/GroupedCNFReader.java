/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.reader;

import java.io.IOException;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.xplain.HighLevelXplain;

public class GroupedCNFReader extends DimacsReader {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int numberOfComponents;

	private final HighLevelXplain<ISolver> hlxplain;

	private int currentComponentIndex;

	public GroupedCNFReader(HighLevelXplain<ISolver> solver) {
		super(solver, "gcnf");
		hlxplain = solver;
	}

	/**
	 * @param in
	 *            the input stream
	 * @throws IOException
	 *             iff an IO occurs
	 * @throws ParseFormatException
	 *             if the input stream does not comply with the DIMACS format.
	 * @since 2.1
	 */
	@Override
	protected void readProblemLine() throws IOException, ParseFormatException {

		String line = scanner.nextLine();

		if (line == null) {
			throw new ParseFormatException("premature end of file: <p "
					+ formatString + " ...> expected");
		}
		line = line.trim();
		String[] tokens = line.split("\\s+");
		if (tokens.length < 5 || !"p".equals(tokens[0])
				|| !formatString.equals(tokens[1])) {
			throw new ParseFormatException("problem line expected (p "
					+ formatString + " ...)");
		}

		int vars;

		// reads the max var id
		vars = Integer.parseInt(tokens[2]);
		assert vars > 0;
		solver.newVar(vars);
		// reads the number of clauses
		expectedNbOfConstr = Integer.parseInt(tokens[3]);
		assert expectedNbOfConstr > 0;
		numberOfComponents = Integer.parseInt(tokens[4]);
		solver.setExpectedNumberOfClauses(expectedNbOfConstr);
	}

	/**
	 * @since 2.1
	 */
	@Override
	protected boolean handleLine() throws ContradictionException, IOException,
			ParseFormatException {
		int lit;
		boolean added = false;
		String component = scanner.next();
		if (!component.startsWith("{") || !component.endsWith("}")) {
			throw new ParseFormatException(
					"Component index required at the beginning of the clause");
		}
		currentComponentIndex = Integer.valueOf(component.substring(1,
				component.length() - 1));
		if (currentComponentIndex < 0
				|| currentComponentIndex > numberOfComponents) {
			throw new ParseFormatException("wrong component index: "
					+ currentComponentIndex);
		}
		while (!scanner.eof()) {
			lit = scanner.nextInt();
			if (lit == 0) {
				if (literals.size() > 0) {
					flushConstraint();
					literals.clear();
					added = true;
				}
				break;
			}
			literals.push(lit);
		}
		return added;
	}

	/**
	 * 
	 * @throws ContradictionException
	 * @since 2.1
	 */
	@Override
	protected void flushConstraint() throws ContradictionException {
		try {
			if (currentComponentIndex == 0) {
				hlxplain.addClause(literals);
			} else {
				hlxplain.addClause(literals, currentComponentIndex);
			}
		} catch (IllegalArgumentException ex) {
			if (isVerbose()) {
				System.err.println("c Skipping constraint " + literals);
			}
		}
	}
}
