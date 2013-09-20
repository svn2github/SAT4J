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
package org.sat4j.maxsat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.OptToSatAdapter;

public class MichalBug {

    @Test
    public void testMichalReportedProblem() throws ContradictionException,
            TimeoutException {
        WeightedMaxSatDecorator maxSATSolver = new WeightedMaxSatDecorator(
                SolverFactory.newLight());

        final int OPTIMUM_FOUND = 0;
        final int UNSATISFIABLE = 1;

        maxSATSolver.newVar(2);
        maxSATSolver.setExpectedNumberOfClauses(4);

        int[] clause_1 = { 1, 2 };
        maxSATSolver.addSoftClause(1,new VecInt(clause_1));

        int[] clause_2 = { -1, -2 };
        maxSATSolver.addSoftClause(100,new VecInt(clause_2));

        int[] clause_3 = { 1, -2 };
        maxSATSolver.addSoftClause(1000,new VecInt(clause_3));

        int[] clause_4 = { -1, 2 };
        maxSATSolver.addSoftClause(100000, new VecInt(clause_4));

        IOptimizationProblem problem = new PseudoOptDecorator(maxSATSolver);

        int exitCode = UNSATISFIABLE;
        boolean isSatisfiable = false;
        try {
            while (problem.admitABetterSolution()) {
                isSatisfiable = true;
                problem.discardCurrentSolution();
            }
            if (isSatisfiable) {
                exitCode = OPTIMUM_FOUND;
            } else {
                exitCode = UNSATISFIABLE;
            }
        } catch (ContradictionException ex) {
            assert isSatisfiable;
            exitCode = OPTIMUM_FOUND;
        }

        assertEquals(OPTIMUM_FOUND, exitCode);
        int[] model = problem.model();
        assertEquals(2, model.length);
        assertEquals(-1, model[0]);
        assertEquals(-2, model[1]);
    }

    @Test
    public void testMichalWithOptAdapter() throws ContradictionException,
            TimeoutException {
        WeightedMaxSatDecorator maxSATSolver = new WeightedMaxSatDecorator(
                SolverFactory.newLight());

        maxSATSolver.newVar(2);
        maxSATSolver.setExpectedNumberOfClauses(4);

        int[] clause_1 = { 1, 2 };
        maxSATSolver.addSoftClause(1,new VecInt(clause_1));

        int[] clause_2 = { -1, -2 };
        maxSATSolver.addSoftClause(100,new VecInt(clause_2));

        int[] clause_3 = { 1, -2 };
        maxSATSolver.addSoftClause(100,new VecInt(clause_3));

        int[] clause_4 = { -1, 2 };
        maxSATSolver.addSoftClause(100000, new VecInt(clause_4));

        IProblem problem = new OptToSatAdapter(new PseudoOptDecorator(
                maxSATSolver));

        boolean isSatisfiable = problem.isSatisfiable();

        assertEquals(true, isSatisfiable);
        int[] model = problem.model();
        assertEquals(2, model.length);
        assertEquals(-1, model[0]);
        assertEquals(-2, model[1]);
    }
}
