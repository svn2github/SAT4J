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
package org.sat4j.minisat;

import java.io.IOException;

import junit.framework.TestCase;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Solver;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/*
 * Created on 11 nov. 2003
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestsFonctionnels extends TestCase {

    private static final String PREFIX = System.getProperty("test.prefix");

    /**
     * Constructor for TestsFonctionnels.
     * 
     * @param arg0
     */
    public TestsFonctionnels(String arg0) {
        super(arg0);
    }

    public void testSat() {
        try {
            reader.parseInstance(PREFIX + "aim-50-yes-ok.cnf");
            assertTrue(solver.isSatisfiable());
        } catch (TimeoutException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testUnsat() throws TimeoutException {
        try {
            reader.parseInstance(PREFIX + "aim-50-no-ok.cnf");
            assertFalse(solver.isSatisfiable());
        } catch (IOException e) {
            fail();
        } catch (ParseFormatException e) {
            fail();
        } catch (ContradictionException e) {
            // OK
        }
    }

    public void testTrivialUnsat() {
        solver.newVar(1);
        IVecInt vec = new VecInt();
        vec.push(1);
        try {
            solver.addClause(vec);
        } catch (ContradictionException e) {
            fail();
        }
        vec.clear();
        vec.push(-1);
        try {
            solver.addClause(vec);
            fail();
        } catch (ContradictionException e1) {
        }
    }

    public void testTrivialSat() throws TimeoutException {
        solver.reset();
        solver.newVar(2);
        try {
            IVecInt vec = new VecInt();
            vec.push(1);
            solver.addClause(vec);
            vec.clear();
            vec.push(-2);
            solver.addClause(vec);
            assertTrue(solver.isSatisfiable());
        } catch (ContradictionException e) {
            fail();
        }
    }

    @Deprecated
    public void testTrivialSatNewVar() throws TimeoutException {
        try {
            solver.newVar(0);
            solver.newVar();
            IVecInt vec = new VecInt();
            vec.push(1);
            solver.addClause(vec);
            vec.clear();
            solver.newVar();
            vec.push(-2);
            solver.addClause(vec);
            assertTrue(solver.isSatisfiable());
        } catch (ContradictionException e) {
            fail();
        }
    }

    public void testBug001() throws TimeoutException {
        solver.reset();
        try {
            reader.parseInstance(PREFIX + "bug001.cnf");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(solver.isSatisfiable());
    }

    public void testTrivialInconsistentFormula() {
        solver.reset();
        try {
            reader.parseInstance(PREFIX + "test3.dimacs");
            assertFalse(solver.isSatisfiable());
        } catch (ContradictionException e) {
            // OK
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testCommentsInInstance() {
        solver.reset();
        try {
            reader.parseInstance("EZCNF:"+PREFIX + "testcomments.cnf");
            assertFalse(solver.isSatisfiable());
        } catch (ContradictionException e) {
            // OK
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testRemoveConstraints() throws TimeoutException {
        try {
            solver.newVar(3);
            assertEquals(0, solver.nConstraints());
            IVecInt vec = new VecInt();
            vec.push(1).push(-2);
            IConstr c = solver.addClause(vec);
            assertNotNull(c);
            assertEquals(1, solver.nConstraints());
            vec.clear();
            vec.push(-1).push(-2);
            c = solver.addClause(vec);
            assertNotNull(c);
            assertEquals(2, solver.nConstraints());
            vec.clear();
            vec.push(-1).push(2);
            solver.addClause(vec);
            // assertNotNull(c);
            assertEquals(3, solver.nConstraints());
            vec.clear();
            vec.push(1).push(2);
            solver.addClause(vec);
            assertEquals(4, solver.nConstraints());
            // assertNotNull(c);
            assertFalse(solver.isSatisfiable());
            solver.removeConstr(c);
            assertEquals(3, solver.nConstraints());
            assertTrue(solver.isSatisfiable());
            assertEquals(1, solver.model()[0]);
            assertEquals(2, solver.model()[1]);
            vec.clear();
            vec.push(-1).push(-2);
            try {
                c = solver.addClause(vec);
                assertNotNull(c);
                assertEquals(4, solver.nConstraints());
                assertFalse(solver.isSatisfiable());
            } catch (ContradictionException ce) {
                // its fine
            }
        } catch (ContradictionException e) {
            fail();
        }
    }

    public void testRemoveAtLeast() {
        solver.newVar(3);
        IVecInt c1 = new VecInt().push(1).push(2).push(3);
        try {
            solver.addClause(c1); // 4 12 14
            assertEquals(1, solver.nConstraints());
            assertEquals(3, c1.size());
            IConstr atLeast = solver.addAtLeast(c1, 2);
            assertEquals(2, solver.nConstraints());
            solver.removeConstr(atLeast);
            assertEquals(1, solver.nConstraints());
        } catch (ContradictionException e) {
            fail();
        }
    }

    public void testIsImplied() {
        solver.newVar(3);
        IVecInt c1 = new VecInt().push(1);
        try {
            solver.addClause(c1);
            assertTrue("isImplied(1) ", solver.getVocabulary().isImplied(2));
            assertFalse("isImplied(2) :", solver.getVocabulary().isImplied(4));
            solver.propagate();
            assertTrue("isImplied(1) ", solver.getVocabulary().isImplied(2));
            assertFalse("isImplied(2) :", solver.getVocabulary().isImplied(4));
        } catch (ContradictionException e) {
            fail();
        }
    }

    public void testIsImplied3() {
        solver.newVar(1);
        IVecInt c1 = new VecInt().push(-1);
        try {
            solver.addClause(c1);
            solver.propagate();
        } catch (ContradictionException e) {
            fail();
        }
        assertTrue("isImplied(1) ", solver.getVocabulary().isImplied(2));
        assertFalse("isSatisfiedl(1)", solver.getVocabulary().isSatisfied(2));
        assertTrue("isFalsified(1)", solver.getVocabulary().isFalsified(2));
    }

    public void testWhenNewVarNotCalled() {
        IVecInt c1 = new VecInt().push(-1);
        try {
            solver.addClause(c1);
            solver.propagate();
        } catch (ContradictionException e) {
            fail();
        }
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        solver = SolverFactory.newMiniSAT();
        reader = new InstanceReader(solver);
    }

    private Solver<ILits,DataStructureFactory<ILits>> solver;

    private InstanceReader reader;
}