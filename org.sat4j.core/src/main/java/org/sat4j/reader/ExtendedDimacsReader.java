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
import java.util.Scanner;
import java.util.StringTokenizer;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.tools.GateTranslator;

/**
 * Reader for the Extended Dimacs format proposed by Fahiem Bacchus and Toby
 * Walsh.
 * 
 * @author leberre
 * 
 */
public class ExtendedDimacsReader extends DimacsReader {

    public static final int FALSE = 1;

    public static final int TRUE = 2;

    public static final int NOT = 3;

    public static final int AND = 4;

    public static final int NAND = 5;

    public static final int OR = 6;

    public static final int NOR = 7;

    public static final int XOR = 8;

    public static final int XNOR = 9;

    public static final int IMPLIES = 10;

    public static final int IFF = 11;

    public static final int IFTHENELSE = 12;

    public static final int ATLEAST = 13;

    public static final int ATMOST = 14;

    public static final int COUNT = 15;

    /**
     * 
     * 
     */
    private static final long serialVersionUID = 1L;

    public ExtendedDimacsReader(ISolver solver) {
        super(new GateTranslator(solver));
    }

    /**
     * @param in
     *            the input stream
     * @throws IOException
     *             iff an IO occurs
     * @throws ParseFormatException
     *             if the input stream does not comply with the DIMACS format.
     */
    @Override
    protected void readProblemLine(LineNumberReader in) throws IOException,
            ParseFormatException {

        String line = in.readLine();

        if (line == null) {
            throw new ParseFormatException(
                    "premature end of file: <p noncnf ...> expected  on line "
                            + in.getLineNumber());
        }
        StringTokenizer stk = new StringTokenizer(line);

        if (!(stk.hasMoreTokens() && stk.nextToken().equals("p")
                && stk.hasMoreTokens() && stk.nextToken().equals("noncnf"))) {
            throw new ParseFormatException(
                    "problem line expected (p noncnf ...) on line "
                            + in.getLineNumber());
        }

        int vars;

        // reads the max var id
        vars = Integer.parseInt(stk.nextToken());
        assert vars > 0;
        solver.newVar(vars);
        try {
            ((GateTranslator)solver).gateTrue(vars);
        } catch (ContradictionException e) {
            assert false;
            System.err.println("Contradiction when asserting root variable?");
        }
        disableNumberOfConstraintCheck();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.DimacsReader#handleConstr(java.lang.String,
     *      org.sat4j.specs.IVecInt)
     */
    @Override
    protected boolean handleConstr(String line, IVecInt literals)
            throws ContradictionException {
        boolean added = true;
        assert literals.size() == 0;
        Scanner scan = new Scanner(line);
        GateTranslator gater = (GateTranslator)solver;
        while (scan.hasNext()) {
            int gateType = scan.nextInt();
            assert gateType > 0;
            int nbparam = scan.nextInt();
            assert nbparam != 0;
            assert nbparam == -1 || gateType >= ATLEAST;
            for (int i = 0; i < nbparam; i++) {
                scan.nextInt();
            }
            // readI/O until reaching ending 0
            int y = scan.nextInt();
            int x;
            while ((x = scan.nextInt()) != 0) {
                literals.push(x);
            }
            switch (gateType) {
            case FALSE:
                assert literals.size()==0;
                gater.gateFalse(y);
                break;
            case TRUE:
                assert literals.size()==0;
                gater.gateTrue(y);
                break;
            case OR:
                gater.or(y, literals);
                break;
            case NOT:
                assert literals.size()==1;
                gater.not(y, literals.get(0));
                break;
            case AND:
                gater.and(y, literals);
                break;
            case XOR:
                gater.xor(y, literals);
                break;
            case IFF:
                gater.iff(y, literals);
                break;
            case IFTHENELSE:
                assert literals.size()==3;
                gater.ite(y, literals.get(0),literals.get(1),literals.get(2));
                break;
            default:
                throw new UnsupportedOperationException("Gate type " + gateType
                        + " not handled yet");
            }
        }
        literals.clear();
        return added;
    }


}
