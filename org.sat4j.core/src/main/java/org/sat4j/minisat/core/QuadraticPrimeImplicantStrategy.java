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
package org.sat4j.minisat.core;

import static org.sat4j.core.LiteralsUtils.toInternal;

import org.sat4j.specs.IConstr;
import org.sat4j.specs.IteratorInt;

/**
 * Quadratic implementation of the model minimization strategy to compute a
 * prime implicant. The main interest of that approach is to work for any kind
 * of constraints (clauses, cardinality constraints, pseudo boolean constraints,
 * any custom constraint).
 * 
 * @author leberre
 * 
 */
public class QuadraticPrimeImplicantStrategy implements PrimeImplicantStrategy {

    private int[] prime;

    public int[] compute(Solver<? extends DataStructureFactory> solver) {
        assert solver.qhead == solver.trail.size()
                + solver.learnedLiterals.size();
        long begin = System.currentTimeMillis();
        if (solver.learnedLiterals.size() > 0) {
            solver.qhead = solver.trail.size();
        }
        if (solver.isVerbose()) {
            System.out.printf("%s implied: %d, decision: %d %n",
                    solver.getLogPrefix(), solver.implied.size(),
                    solver.decisions.size());
        }
        prime = new int[solver.realNumberOfVariables() + 1];
        int p, d;
        for (int i = 0; i < prime.length; i++) {
            prime[i] = 0;
        }
        boolean noproblem;
        for (IteratorInt it = solver.implied.iterator(); it.hasNext();) {
            d = it.next();
            p = toInternal(d);
            prime[Math.abs(d)] = d;
            noproblem = solver.setAndPropagate(p);
            assert noproblem;
        }
        boolean canBeRemoved;
        int rightlevel;
        int removed = 0;
        int propagated = 0;
        int tested = 0;
        int l2propagation = 0;

        for (int i = 0; i < solver.decisions.size(); i++) {
            d = solver.decisions.get(i);
            assert !solver.voc.isFalsified(toInternal(d));
            if (solver.voc.isSatisfied(toInternal(d))) {
                // d has been propagated
                prime[Math.abs(d)] = d;
                propagated++;
            } else if (solver.setAndPropagate(toInternal(-d))) {
                canBeRemoved = true;
                tested++;
                rightlevel = solver.currentDecisionLevel();
                for (int j = i + 1; j < solver.decisions.size(); j++) {
                    l2propagation++;
                    if (!solver.setAndPropagate(toInternal(solver.decisions
                            .get(j)))) {
                        canBeRemoved = false;
                        break;
                    }
                }
                solver.cancelUntil(rightlevel);
                if (canBeRemoved) {
                    // it is not a necessary literal
                    solver.forget(Math.abs(d));
                    IConstr confl = solver.propagate();
                    assert confl == null;
                    removed++;
                } else {
                    prime[Math.abs(d)] = d;
                    solver.cancel();
                    assert solver.voc.isUnassigned(toInternal(d));
                    noproblem = solver.setAndPropagate(toInternal(d));
                    assert noproblem;
                }
            } else {
                // conflict, literal is necessary
                prime[Math.abs(d)] = d;
                solver.cancel();
                noproblem = solver.setAndPropagate(toInternal(d));
                assert noproblem;
            }
        }
        solver.cancelUntil(0);
        int[] implicant = new int[prime.length - removed - 1];
        int index = 0;
        for (int i : prime) {
            if (i != 0) {
                implicant[index++] = i;
            }
        }
        long end = System.currentTimeMillis();
        if (solver.isVerbose()) {
            System.out.printf(
                    "%s prime implicant computation statistics ORIG%n",
                    solver.getLogPrefix());
            System.out
                    .printf("%s implied: %d, decision: %d (removed %d, tested %d, propagated %d), l2 propagation:%d, time(ms):%d %n",
                            solver.getLogPrefix(), solver.implied.size(),
                            solver.decisions.size(), removed, tested,
                            propagated, l2propagation, end - begin);
        }
        return implicant;

    }

    public int[] getPrimeImplicantAsArrayWithHoles() {
        if (prime == null) {
            throw new UnsupportedOperationException(
                    "Call the compute method first!");
        }
        return prime;
    }

}
