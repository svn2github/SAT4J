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
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * Very simple Dimacs file parser. Allow solvers to read the constraints from a
 * Dimacs formatted file. It should be used that way:
 * 
 * <pre>
 * DimacsReader solver = new DimacsReader(SolverFactory.OneSolver());
 * solver.readInstance(&quot;mybench.cnf&quot;);
 * if (solver.isSatisfiable()) {
 *     // SAT case
 * } else {
 *     // UNSAT case
 * }
 * </pre>
 * 
 * That parser is not used for efficiency reasons. It will be updated with Java
 * 1.5 scanner feature.
 * 
 * @version 1.0
 * @author dlb
 * @author or
 */
public class DimacsReader extends Reader implements Serializable {

    private static final long serialVersionUID = 1L;

    protected int expectedNbOfConstr; // as announced on the p cnf line

    protected final ISolver solver;

    private boolean checkConstrNb = true;

    protected final String formatString;

    public DimacsReader(ISolver solver) {
        this(solver, "cnf");
    }

    public DimacsReader(ISolver solver, String format) {
        this.solver = solver;
        formatString = format;
    }

    public void disableNumberOfConstraintCheck() {
        checkConstrNb = false;
    }

    /**
     * Skip comments at the beginning of the input stream.
     * 
     * @param in
     *            the input stream
     * @throws IOException
     *             if an IO problem occurs.
     */
    protected void skipComments(final LineNumberReader in) throws IOException {
        int c;

        do {
            in.mark(4);
            c = in.read();
            if (c == 'c') {
                in.readLine();
            } else {
                in.reset();
            }
        } while (c == 'c');
    }

    /**
     * @param in
     *            the input stream
     * @throws IOException
     *             iff an IO occurs
     * @throws ParseFormatException
     *             if the input stream does not comply with the DIMACS format.
     */
    protected void readProblemLine(LineNumberReader in) throws IOException,
            ParseFormatException {

        String line = in.readLine();

        if (line == null) {
            throw new ParseFormatException(
                    "premature end of file: <p cnf ...> expected  on line "
                            + in.getLineNumber());
        }
        StringTokenizer stk = new StringTokenizer(line);

        if (!(stk.hasMoreTokens() && stk.nextToken().equals("p")
                && stk.hasMoreTokens() && stk.nextToken().equals(formatString))) {
            throw new ParseFormatException(
                    "problem line expected (p cnf ...) on line "
                            + in.getLineNumber());
        }

        int vars;

        // reads the max var id
        vars = Integer.parseInt(stk.nextToken());
        assert vars > 0;
        solver.newVar(vars);
        // reads the number of clauses
        expectedNbOfConstr = Integer.parseInt(stk.nextToken());
        assert expectedNbOfConstr > 0;
        solver.setExpectedNumberOfClauses(expectedNbOfConstr);
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
    protected void readConstrs(LineNumberReader in) throws IOException,
            ParseFormatException, ContradictionException {
        String line;

        int realNbOfConstr = 0;

        IVecInt literals = new VecInt();

        while (true) {
            line = in.readLine();

            if (line == null) {
                // end of file
                if (literals.size() > 0) {
                    // no 0 end the last clause
                    solver.addClause(literals);
                    realNbOfConstr++;
                }

                break;
            }

            if (line.startsWith("c ")) {
                // ignore comment line
                continue;
            }
            if (line.startsWith("%") && expectedNbOfConstr == realNbOfConstr) {
                System.out
                        .println("Ignoring the rest of the file (SATLIB format");
                break;
            }
            boolean added = handleConstr(line, literals);
            if (added) {
                realNbOfConstr++;
            }
        }
        if (checkConstrNb && expectedNbOfConstr != realNbOfConstr) {
            throw new ParseFormatException("wrong nbclauses parameter. Found "
                    + realNbOfConstr + ", " + expectedNbOfConstr + " expected");
        }
    }

    protected boolean handleConstr(String line, IVecInt literals)
            throws ContradictionException {
        int lit;
        boolean added = false;
        Scanner scan;
        scan = new Scanner(line);
        while (scan.hasNext()) {
            lit = scan.nextInt();

            if (lit == 0) {
                if (literals.size() > 0) {
                    solver.addClause(literals);
                    literals.clear();
                    added = true;
                }
            } else {
                literals.push(lit);
            }
        }
        return added;
    }

    @Override
    public final IProblem parseInstance(final java.io.Reader in)
            throws ParseFormatException, ContradictionException, IOException {
        return parseInstance(new LineNumberReader(in));

    }

    /**
     * @param in
     *            the input stream
     * @throws ParseFormatException
     *             if the input stream does not comply with the DIMACS format.
     * @throws ContradictionException
     *             si le probl?me est trivialement inconsitant
     */
    private IProblem parseInstance(LineNumberReader in)
            throws ParseFormatException, ContradictionException {
        solver.reset();
        try {
            skipComments(in);
            readProblemLine(in);
            readConstrs(in);
            return solver;
        } catch (IOException e) {
            throw new ParseFormatException(e);
        } catch (NumberFormatException e) {
            throw new ParseFormatException("integer value expected on line "
                    + in.getLineNumber(), e);
        }
    }

    @Override
    public String decode(int[] model) {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < model.length; i++) {
            stb.append(model[i]);
            stb.append(" ");
        }
        stb.append("0");
        return stb.toString();
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        for (int i = 0; i < model.length; i++) {
            out.print(model[i]);
            out.print(" ");
        }
        out.print("0");
    }

    protected ISolver getSolver() {
        return solver;
    }
}
