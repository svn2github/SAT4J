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
package org.sat4j.tools;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * Utility class to easily feed a SAT solver using logical gates.
 * 
 * @author leberre
 *
 */
public class GateTranslator extends SolverDecorator<ISolver> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GateTranslator(ISolver solver) {
        super(solver);
    }

    /**
     * translate y <=> FALSE into a clause.
     * @param y a variable to falsify
     * @throws ContradictionException iff a trivial inconsistency is found.
     */
    public void gateFalse(int y)
            throws ContradictionException {
        IVecInt clause = new VecInt(2);
        clause.push(-y);
        processClause(clause);
    }

    /**
     * translate y <=> TRUE into a clause.
     * @param y a variable to verify
     * @throws ContradictionException
     */
    public void gateTrue(int y)
            throws ContradictionException {
        IVecInt clause = new VecInt(2);
        clause.push(y);
        processClause(clause);
    }

    /**
     * translate y <=> if x1 then x2 else x3 into clauses.  
     * @param y 
     * @param x1 the selector variable
     * @param x2 
     * @param x3
     * @throws ContradictionException
     */
    public void ite(int y, int x1, int x2, int x3) throws ContradictionException {
        IVecInt clause = new VecInt(5);
        // y <=> (x1 -> x2) and (not x1 -> x3)
        // y -> (x1 -> x2) and (not x1 -> x3)
        clause.push(-y).push(-x1).push(x2);
        processClause(clause);
        clause.clear();
        clause.push(-y).push(x1).push(x3);
        processClause(clause);
        // y <- (x1 -> x2) and (not x1 -> x3)
        // not(x1 -> x2) or not(not x1 -> x3) or y
        // x1 and not x2 or not x1 and not x3 or y
        // (x1 and not x2) or ((not x1 or y) and (not x3 or y))
        // (x1 or not x1 or y) and (not x2 or not x1 or y) and (x1 or not x3 or
        // y) and (not x2 or not x3 or y)
        // not x1 or not x2 or y and x1 or not x3 or y and not x2 or not x3 or y
        clause.clear();
        clause.push(-x1).push(-x2).push(y);
        processClause(clause);
        clause.clear();
        clause.push(x1).push(-x3).push(y);
        processClause(clause);
        clause.clear();
        clause.push(-x2).push(-x3).push(y);
        processClause(clause);
        // taken from Niklas Een et al SAT 2007 paper
        // Adding the following redundant clause will improve unit propagation
        // y -> x2 or x3
        clause.clear();
        clause.push(-y).push(x2).push(x3);
        processClause(clause);
    }

    /**
     * Translate y <=> x1 /\ x2 /\ ... /\ xn into clauses.
     * 
     * @param y
     * @param literals the x1 ... xn literals.
     * @throws ContradictionException
     */
    public void and(int y, IVecInt literals) throws ContradictionException {
        // y <=> AND x1 ... xn

        // y <= x1 .. xn
        IVecInt clause = new VecInt(literals.size() + 2);
        clause.push(y);
        for (int i = 0; i < literals.size(); i++) {
            clause.push(-literals.get(i));
        }
        processClause(clause);
        clause.clear();
        for (int i = 0; i < literals.size(); i++) {
            // y => xi
            clause.clear();
            clause.push(-y);
            clause.push(literals.get(i));
            processClause(clause);
        }
    }

    /**
     * Translate y <=> x1 /\ x2
     * @param y
     * @param x1
     * @param x2
     * @throws ContradictionException 
     */
    public void and(int y, int x1, int x2) throws ContradictionException {
        IVecInt clause = new VecInt(4);
        clause.push(-y);
        clause.push(x1);
        addClause(clause);
        clause.clear();
        clause.push(-y);
        clause.push(x2);
        addClause(clause);
        clause.clear();
        clause.push(y);
        clause.push(-x1);
        clause.push(-x2);
        addClause(clause);
    }
    
    /**
     * translate y <=> x1 \/ x2 \/ ... \/ xn into clauses.
     * 
     * @param y
     * @param literals
     * @throws ContradictionException
     */
    public void or(int y, IVecInt literals) throws ContradictionException {
        // y <=> OR x1 x2 ...xn
        // y => x1 x2 ... xn
        IVecInt clause = new VecInt(literals.size() + 2);
        literals.copyTo(clause);
        clause.push(-y);
        processClause(clause);
        clause.clear();
        for (int i = 0; i < literals.size(); i++) {
            // xi => y
            clause.clear();
            clause.push(y);
            clause.push(-literals.get(i));
            processClause(clause);
        }
    }

    private void processClause(IVecInt clause) throws ContradictionException {
        addClause(clause);
    }

    /**
     * Translate y <=> not x into clauses.
     * 
     * @param y
     * @param x
     * @throws ContradictionException
     */
    public void not(int y, int x) throws ContradictionException {
        IVecInt clause = new VecInt(3);
        // y <=> not x
        // y => not x = not y or not x
        clause.push(-y).push(-x);
        processClause(clause);
        // y <= not x = y or x
        clause.clear();
        clause.push(y).push(x);
        processClause(clause);
    }

    /**
     * translate y <=> x1 xor x2 xor ... xor xn into clauses.
     * @param y
     * @param literals
     * @throws ContradictionException
     */
    public void xor(int y, IVecInt literals) throws ContradictionException {
        literals.push(-y);
        int[] f = new int[literals.size()];
        literals.copyTo(f);
        xor2Clause(f, 0, false);
    }

    /**
     * translate y <=> (x1 <=> x2 <=> ... <=> xn) into clauses.
     * @param y
     * @param literals
     * @throws ContradictionException
     */
    public void iff(int y, IVecInt literals) throws ContradictionException {
        literals.push(y);
        int[] f = new int[literals.size()];
        literals.copyTo(f);
        iff2Clause(f, 0, false);
    }

    private void xor2Clause(int[] f, int prefix, boolean negation)
            throws ContradictionException {
        if (prefix == f.length - 1) {
            IVecInt clause = new VecInt(f.length + 1);
            for (int i = 0; i < f.length - 1; ++i) {
                clause.push(f[i]);
            }
            clause.push(f[f.length - 1] * (negation ? -1 : 1));
            processClause(clause);
            return;
        }

        if (negation) {
            f[prefix] = -f[prefix];
            xor2Clause(f, prefix + 1, false);
            f[prefix] = -f[prefix];

            xor2Clause(f, prefix + 1, true);
        } else {
            xor2Clause(f, prefix + 1, false);

            f[prefix] = -f[prefix];
            xor2Clause(f, prefix + 1, true);
            f[prefix] = -f[prefix];
        }
    }

    private void iff2Clause(int[] f, int prefix, boolean negation)
            throws ContradictionException {
        if (prefix == f.length - 1) {
            IVecInt clause = new VecInt(f.length + 1);
            for (int i = 0; i < f.length - 1; ++i) {
                clause.push(f[i]);
            }
            clause.push(f[f.length - 1] * (negation ? -1 : 1));
            processClause(clause);
            return;
        }

        if (negation) {
            iff2Clause(f, prefix + 1, false);
            f[prefix] = -f[prefix];
            iff2Clause(f, prefix + 1, true);
            f[prefix] = -f[prefix];
        } else {
            f[prefix] = -f[prefix];
            iff2Clause(f, prefix + 1, false);
            f[prefix] = -f[prefix];
            iff2Clause(f, prefix + 1, true);
        }
    }

}
