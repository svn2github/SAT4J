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
package org.sat4j.tools;

import java.util.HashSet;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.TimeoutException;

/**
 * The aim of this class is to compute efficiently the literals implied by the
 * set of constraints (also called backbone or unit implicates).
 * 
 * The work has been done in the context of ANR BR4CP.
 * 
 * @author leberre
 * 
 */
public class Backbone {

    private Backbone() {

    }

    /**
     * Computes the backbone of a formula following the algorithm described in
     * João Marques-Silva, Mikolás Janota, Inês Lynce: On Computing Backbones of
     * Propositional Theories. ECAI 2010: 15-20
     * 
     * @param solver
     * @return
     * @throws TimeoutException
     */
    public static IVecInt compute(ISolver solver) throws TimeoutException {
        return compute(solver, VecInt.EMPTY);
    }

    /**
     * Computes the backbone of a formula following the algorithm described in
     * João Marques-Silva, Mikolás Janota, Inês Lynce: On Computing Backbones of
     * Propositional Theories. ECAI 2010: 15-20
     * 
     * 
     * @param solver
     * @param assumptions
     * @return
     * @throws TimeoutException
     */
    public static IVecInt compute(ISolver solver, IVecInt assumptions)
            throws TimeoutException {
        boolean result = solver.isSatisfiable(assumptions);
        if (!result) {
            return VecInt.EMPTY;
        }
        return compute(solver, solver.primeImplicant(), assumptions);

    }

    public static IVecInt compute(ISolver solver, int[] implicant)
            throws TimeoutException {
        return compute(solver, implicant, VecInt.EMPTY);
    }

    public static IVecInt compute(ISolver solver, int[] implicant,
            IVecInt assumptions) throws TimeoutException {
        Set<Integer> assumptionsSet = new HashSet<Integer>();
        for (IteratorInt it = assumptions.iterator(); it.hasNext();) {
            assumptionsSet.add(it.next());
        }
        IVecInt litsToTest = new VecInt();
        for (int p : implicant) {
            if (!assumptionsSet.contains(p)) {
                litsToTest.push(-p);
            }
        }
        IVecInt candidates = new VecInt();
        assumptions.copyTo(candidates);
        int p;
        while (!litsToTest.isEmpty()) {
            p = litsToTest.last();
            candidates.push(p);
            litsToTest.pop();
            if (solver.isSatisfiable(candidates)) {
                candidates.pop();
                implicant = solver.primeImplicant();
                removeVarNotPresentAndSatisfiedLits(implicant, litsToTest,
                        solver.nVars());
            } else {
                candidates.pop().push(-p);
            }
        }
        return candidates;
    }

    private static void removeVarNotPresentAndSatisfiedLits(int[] implicant,
            IVecInt litsToTest, int n) {
        int[] marks = new int[n + 1];
        for (int p : implicant) {
            marks[p > 0 ? p : -p] = p;
        }
        int q, mark;
        for (int i = 0; i < litsToTest.size();) {
            q = litsToTest.get(i);
            mark = marks[q > 0 ? q : -q];
            if (mark == 0 || mark == q) {
                litsToTest.delete(i);
            } else {
                i++;
            }
        }
    }
}
