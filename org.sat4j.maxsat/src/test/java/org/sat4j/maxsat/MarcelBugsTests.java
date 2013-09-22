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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.maxsat.reader.WDimacsReader;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.tools.OptToSatAdapter;

public class MarcelBugsTests {

    private WeightedMaxSatDecorator maxsat;
    private WDimacsReader reader;

    @Before
    public void init() {
        this.maxsat = new WeightedMaxSatDecorator(SolverFactory.newLight());
        this.reader = new WDimacsReader(this.maxsat);
    }

    @Test
    public void testProblemWithDuplicatedOppositeLiterals2() {
        testProblemWithExpectedAnswer("Inconsistent2.wcnf", new int[] { -1, 2,
                3 }, 5);
    }

    @Test
    public void testProblemWithDuplicatedOppositeLiterals1() {
        testProblemWithExpectedAnswer("Inconsistent1.wcnf",
                new int[] { 1, 2, 3 }, 4);
    }

    @Test
    public void testSimpleProblemWithTwoOppositeLiterals() {
        testProblemWithExpectedAnswer("Inconsistent_Example.wcnf",
                new int[] { -1 }, 1);
    }

    @Test
    public void testProblemWithNegatedLiterals() {
        testProblemWithExpectedAnswer("Example.wcnf",
                new int[] { 1, 2, -3, 4 }, 0);
    }

    @Test
    public void testProblemWithDuplicatedLiterals() {
        testProblemWithExpectedAnswer("AnotherExample.wcnf", new int[] { 1 }, 2);
    }

    private void testProblemWithExpectedAnswer(String filename,
            int[] expectation, int expectedValue) {
        try {
            IProblem problem = this.reader.parseInstance(System.getProperty("test.prefix")+filename);
            assertNotNull(problem);
            IOptimizationProblem optproblem = new PseudoOptDecorator(
                    this.maxsat);
            IProblem satproblem = new OptToSatAdapter(optproblem);
            assertTrue(satproblem.isSatisfiable());
            int[] model = satproblem.model();
            assertNotNull(model);
            assertArrayEquals(expectation, model);
            assertEquals(expectedValue, optproblem.getObjectiveValue()
                    .intValue());

        } catch (Exception e) {
            fail(" Problem when reading instance : " + e);
        }
    }
}
