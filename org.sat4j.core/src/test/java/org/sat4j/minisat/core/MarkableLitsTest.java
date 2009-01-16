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
package org.sat4j.minisat.core;

import java.util.Set;

import junit.framework.TestCase;

import org.sat4j.minisat.constraints.cnf.MarkableLits;
import org.sat4j.specs.IVecInt;

public class MarkableLitsTest extends TestCase {

    private MarkableLits lits;

    public MarkableLitsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        lits = new MarkableLits();
        lits.init(10);
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.init(int)'
     */
    public void testInit() {
        assertEquals(0, lits.nVars());
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.setMark(int, int)'
     */
    public void testSetMarkIntInt() {
        assertEquals(IMarkableLits.MARKLESS, lits.getMark(2));
        lits.setMark(2, 10);
        assertEquals(10, lits.getMark(2));
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.setMark(int)'
     */
    public void testSetMarkInt() {
        assertEquals(IMarkableLits.MARKLESS, lits.getMark(2));
        lits.setMark(2);
        assertTrue(lits.isMarked(2));
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.getMark(int)'
     */
    public void testGetMark() {
        assertEquals(IMarkableLits.MARKLESS, lits.getMark(2));
        lits.setMark(2, 10);
        assertEquals(10, lits.getMark(2));
        lits.setMark(2, 5);
        assertEquals(5, lits.getMark(2));
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.isMarked(int)'
     */
    public void testIsMarked() {
        assertFalse(lits.isMarked(2));
        lits.setMark(2, 10);
        assertTrue(lits.isMarked(2));
        lits.setMark(2, IMarkableLits.MARKLESS);
        assertFalse(lits.isMarked(2));
        lits.setMark(2);
        assertTrue(lits.isMarked(2));
        lits.resetMark(2);
        assertFalse(lits.isMarked(2));
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.resetMark(int)'
     */
    public void testResetMark() {
        assertFalse(lits.isMarked(3));
        lits.setMark(3, 10);
        assertTrue(lits.isMarked(3));
        lits.resetMark(3);
        assertFalse(lits.isMarked(3));
        lits.setMark(3);
        assertTrue(lits.isMarked(3));
        lits.resetMark(3);
        assertFalse(lits.isMarked(3));
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.resetAllMarks()'
     */
    public void testResetAllMarks() {
        for (int i = 1; i <= 10; i++) {
            int p = i << 1;
            int q = p + 1;
            assertFalse(lits.isMarked(p));
            assertFalse(lits.isMarked(q));
            lits.setMark(p, i);
            lits.setMark(q, i);
            assertTrue(lits.isMarked(p));
            assertTrue(lits.isMarked(q));
        }
        lits.resetAllMarks();
        for (int p = 2; p < 22; p++) {
            assertFalse(lits.isMarked(p));
        }
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.getMarkedLiterals()'
     */
    public void testGetMarkedLiterals() {
        lits.setMark(4);
        lits.setMark(7);
        lits.setMark(11);
        IVecInt marked = lits.getMarkedLiterals();
        assertEquals(3, marked.size());
        assertTrue(marked.contains(4));
        assertTrue(marked.contains(7));
        assertTrue(marked.contains(11));
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.getMarkedLiterals(int)'
     */
    public void testGetMarkedLiteralsInt() {
        lits.setMark(4, 10);
        lits.setMark(7, 5);
        lits.setMark(11, 10);
        IVecInt marked = lits.getMarkedLiterals(10);
        assertEquals(2, marked.size());
        assertTrue(marked.contains(4));
        assertFalse(marked.contains(7));
        assertTrue(marked.contains(11));
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.getMarkedVariables()'
     */
    public void testGetMarkedVariables() {
        lits.setMark(4);
        lits.setMark(7);
        lits.setMark(11);
        IVecInt marked = lits.getMarkedVariables();
        assertEquals(3, marked.size());
        assertTrue(marked.contains(2));
        assertTrue(marked.contains(3));
        assertTrue(marked.contains(5));
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.getMarkedVariables(int)'
     */
    public void testGetMarkedVariablesInt() {
        lits.setMark(4, 10);
        lits.setMark(7, 5);
        lits.setMark(11, 10);
        IVecInt marked = lits.getMarkedVariables(10);
        assertEquals(2, marked.size());
        assertTrue(marked.contains(2));
        assertFalse(marked.contains(3));
        assertTrue(marked.contains(5));
    }

    /*
     * Test method for
     * 'org.sat4j.minisat.constraints.cnf.MarkableLits.getMarks()'
     */
    public void testGetMarks() {
        lits.setMark(4, 10);
        lits.setMark(7, 5);
        lits.setMark(11, 10);
        Set<Integer> marks = lits.getMarks();
        assertEquals(2, marks.size());
        assertTrue(marks.contains(5));
        assertFalse(marks.contains(IMarkableLits.MARKLESS));
        assertTrue(marks.contains(10));
    }

}
