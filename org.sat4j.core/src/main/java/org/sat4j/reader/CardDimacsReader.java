/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
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
*******************************************************************************/
package org.sat4j.reader;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * A reader for cardinality contraints.
 * 
 * @author leberre
 */
@Deprecated
public class CardDimacsReader extends DimacsReader {

    /**
     * 
     */
    private static final long serialVersionUID = 3258130241376368435L;

    public CardDimacsReader(ISolver solver) {
        super(solver);
    }

    /**
     * @param in
     *            the input stream
     * @throws IOException
     *             iff an IO problems occurs
     * @throws ParseFormatException
     *             if the input stream does not comply with the DIMACS format.
     * @throws ContradictionException
     *             si le probl?me est trivialement inconsistant.
     */
    @Override
    protected void readConstrs(LineNumberReader in) throws IOException,
            ParseFormatException, ContradictionException {
        int lit;
        String line;
        StringTokenizer stk;

        int realNbOfClauses = 0;

        IVecInt literals = new VecInt();

        while (true) {
            line = in.readLine();

            if (line == null) {
                // end of file
                if (literals.size() > 0) {
                    // no 0 end the last clause
                    solver.addClause(literals);
                    realNbOfClauses++;
                }

                break;
            }

            if (line.startsWith("c ")) {
                // skip commented line
                continue;
            }
            if (line.startsWith("%") && expectedNbOfConstr == realNbOfClauses) {
                System.out
                        .println("Ignoring the rest of the file (SATLIB format");
                break;
            }
            stk = new StringTokenizer(line);
            String token;

            while (stk.hasMoreTokens()) {
                // on lit le prochain token
                token = stk.nextToken();

                if ("<=".equals(token) || ">=".equals(token)) {
                    // on est sur une contrainte de cardinalit?
                    readCardinalityConstr(token, stk, literals);
                    literals.clear();
                    realNbOfClauses++;
                } else {
                    lit = Integer.parseInt(token);
                    if (lit == 0) {
                        if (literals.size() > 0) {
                            solver.addClause(literals);
                            literals.clear();
                            realNbOfClauses++;
                        }
                    } else {
                        literals.push(lit);
                    }
                }
            }
        }
        if (expectedNbOfConstr != realNbOfClauses) {
            throw new ParseFormatException("wrong nbclauses parameter. Found "
                    + realNbOfClauses + ", " + expectedNbOfConstr + " expected");
        }
    }

    private void readCardinalityConstr(String token, StringTokenizer stk,
            IVecInt literals) throws ContradictionException,
            ParseFormatException {
        int card = Integer.parseInt(stk.nextToken());
        int lit = Integer.parseInt(stk.nextToken());
        if (lit == 0) {
            if ("<=".equals(token)) {
                solver.addAtMost(literals, card);
            } else if (">=".equals(token)) {
                solver.addAtLeast(literals, card);
            }
        } else
            throw new ParseFormatException();
    }

}
