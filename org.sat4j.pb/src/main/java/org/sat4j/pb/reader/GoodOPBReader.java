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
* Based on the pseudo boolean algorithms described in:
* A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
* Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
* Volume 24, Issue 3, March 2005 Page(s): 305 - 317
* 
* and 
* Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
* Framework. Ph.D. Dissertation, University of Oregon.
*******************************************************************************/
package org.sat4j.pb.reader;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * This class is a quick hack to read opb formatted files. The reader skip
 * commented lines (beginning with COMMENT_SYMBOL) and expect constraints of the
 * form: [name :] [[+|-]COEF] [*] [+|-]LIT >=|<=|= DEGREE where COEF and DEGREE
 * are plain integer and LIT is an identifier.
 * 
 * @author leberre
 */
public class GoodOPBReader extends Reader implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private static final String COMMENT_SYMBOL = "*";

    private final IPBSolver solver;

    private final Map<String, Integer> map = new HashMap<String, Integer>();

    private final IVec<String> decode = new Vec<String>();

    /**
     * 
     */
    public GoodOPBReader(IPBSolver solver) {
        this.solver = solver;
    }

    @Override
    public final IProblem parseInstance(final java.io.Reader in)
            throws ParseFormatException, ContradictionException, IOException {
        return parseInstance(new LineNumberReader(in));
    }

    private IProblem parseInstance(LineNumberReader in)
            throws ContradictionException, IOException {
        solver.reset();
        String line;
        while ((line = in.readLine()) != null) {
            // cannot trim is line is null
            line = line.trim();
            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1);
            }
            parseLine(line);
        }
        return solver;
    }

    void parseLine(String line) throws ContradictionException {
        // Skip commented line
        if (line.startsWith(COMMENT_SYMBOL))
            return;
        if (line.startsWith("p")) // ignore p cnf with pbchaff format
            return;
        if (line.startsWith("min:") || line.startsWith("min :"))
            return; // we will use that case later
        if (line.startsWith("max:") || line.startsWith("max :"))
            return; // we will use that case later

        // skip name of constraints:
        int index = line.indexOf(":");
        if (index != -1) {
            line = line.substring(index + 1);
        }

        IVecInt lits = new VecInt();
        IVec<BigInteger> coeffs = new Vec<BigInteger>();
        Scanner stk = new Scanner(line)
                .useDelimiter("\\s*\\*\\s*|\\s*\\+\\s*|\\s+");
        while (stk.hasNext()) {
            String token = stk.next();
            if (">=".equals(token) || "<=".equals(token) || "=".equals(token)) {
                assert stk.hasNext();
                String tok = stk.next();
                // we need to remove + from the integer
                if (tok.startsWith("+")) {
                    tok = tok.substring(1);
                }
                BigInteger d = new BigInteger(tok);

                try {
                    if (">=".equals(token) || "=".equals(token)) {
                        solver.addPseudoBoolean(lits, coeffs, true, d);
                    }
                    if ("<=".equals(token) || "=".equals(token)) {
                        solver.addPseudoBoolean(lits, coeffs, false, d);
                    }
                } catch (ContradictionException ce) {
                    throw ce;
                }
            } else {
                // on est toujours en train de lire la partie gauche de la
                // contrainte
                if ("+".equals(token)) {
                    assert stk.hasNext();
                    token = stk.next();
                } else if ("-".equals(token)) {
                    assert stk.hasNext();
                    token = token + stk.next();
                }
                BigInteger coef;
                // should contain a coef and a literal
                try {
                    // we need to remove + from the integer
                    if (token.startsWith("+")) {
                        token = token.substring(1);
                    }
                    coef = new BigInteger(token);
                    assert stk.hasNext();
                    token = stk.next();
                } catch (NumberFormatException nfe) {
                    // its only an identifier
                    coef = BigInteger.ONE;
                }
                if ("-".equals(token) || "~".equals(token)) {
                    assert stk.hasNext();
                    token = token + stk.next();
                }
                boolean negative = false;
                if (token.startsWith("+")) {
                    token = token.substring(1);
                } else if (token.startsWith("-")) {
                    token = token.substring(1);
                    assert coef.equals(BigInteger.ONE);
                    coef = BigInteger.ONE.negate();
                } else if (token.startsWith("~")) {
                    token = token.substring(1);
                    negative = true;
                }
                Integer id = map.get(token);
                if (id == null) {
                	id = decode.size()+1;
                    map.put(token, id);
                    decode.push(token);
                }
                coeffs.push(coef);
                int lid = (negative ? -1 : 1) * id.intValue();
                lits.push(lid);
                assert coeffs.size() == lits.size();
            }
        }
    }

    @Override
    public String decode(int[] model) {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < model.length; i++) {
            if (model[i] < 0) {
                stb.append("-");
                stb.append(decode.get(-model[i] - 1));
            } else {
                stb.append(decode.get(model[i] - 1));
            }
            stb.append(" ");
        }
        return stb.toString();
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        for (int i = 0; i < model.length; i++) {
            if (model[i] < 0) {
                out.print("-");
                out.print(decode.get(-model[i] - 1));
            } else {
                out.print(decode.get(model[i] - 1));
            }
            out.print(" ");
        }
    }
}
