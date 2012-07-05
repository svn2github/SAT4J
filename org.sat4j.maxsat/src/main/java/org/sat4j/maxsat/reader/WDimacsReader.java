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
package org.sat4j.maxsat.reader;

import java.io.IOException;
import java.math.BigInteger;

import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;

/**
 * Simple reader for the weighted maxsat problem.
 * 
 * @author daniel
 * 
 */
public class WDimacsReader extends DimacsReader {

    protected BigInteger weight;
    protected BigInteger top;

    @Override
    protected void flushConstraint() throws ContradictionException {
        try {
            this.decorator.addSoftClause(this.weight, this.literals);
        } catch (IllegalArgumentException ex) {
            if (isVerbose()) {
                System.err.println("c Skipping constraint " + this.literals);
            }
        }

    }

    @Override
    protected boolean handleLine() throws ContradictionException, IOException,
            ParseFormatException {
        this.weight = this.scanner.nextBigInteger();
        return super.handleLine();
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final WeightedMaxSatDecorator decorator;

    public WDimacsReader(WeightedMaxSatDecorator solver) {
        super(solver, "wcnf");
        this.decorator = solver;
    }

    public WDimacsReader(WeightedMaxSatDecorator solver, String format) {
        super(solver, format);
        this.decorator = solver;
    }

    @Override
    protected void readProblemLine() throws IOException, ParseFormatException {
        String line = this.scanner.nextLine().trim();

        if (line == null) {
            throw new ParseFormatException(
                    "premature end of file: <p cnf ...> expected");
        }
        String[] tokens = line.split("\\s+");
        if (tokens.length < 4 || !"p".equals(tokens[0])
                || !this.formatString.equals(tokens[1])) {
            throw new ParseFormatException("problem line expected (p cnf ...)");
        }

        int vars;

        // reads the max var id
        vars = Integer.parseInt(tokens[2]);
        assert vars > 0;
        this.solver.newVar(vars);
        // reads the number of clauses
        this.expectedNbOfConstr = Integer.parseInt(tokens[3]);
        assert this.expectedNbOfConstr > 0;
        this.solver.setExpectedNumberOfClauses(this.expectedNbOfConstr);

        if ("wcnf".equals(this.formatString)) {
            // assume we are in weighted MAXSAT mode
            if (tokens.length == 5) {
                this.top = new BigInteger(tokens[4]);
            } else {
                this.top = WeightedMaxSatDecorator.SAT4J_MAX_BIG_INTEGER;
            }
            this.decorator.setTopWeight(this.top);
        }
    }

}
