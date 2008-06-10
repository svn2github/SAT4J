package org.sat4j.minisat.constraints;

import junit.framework.TestCase;

import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.ILits;

/*
 * Created on 30 oct. 2003
 *
 */

/**
 * @author leberre
 * 
 */
public class LitTest extends TestCase {

    private ILits lits;

    /**
     * Constructor for LitTest.
     * 
     * @param arg0
     */
    public LitTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        lits = new Lits();
    }

    public void testNot() {
        lits.ensurePool(5);
        int lit = lits.getFromPool(-5);
        assertEquals(lits.getFromPool(5), lit ^ 1);
    }

    public void testVar() {
        lits.ensurePool(10);
        int lit = lits.getFromPool(-5);
        assertEquals(5, lit >> 1);
        lit = lits.getFromPool(10);
        assertEquals(10, lit >> 1);

    }

    /*
     * Test pour boolean equals(Object)
     */
    public void testEqualsObject() {
        lits.ensurePool(3);
        int lit = lits.getFromPool(-3);
        assertEquals(lits.getFromPool(-3), lit);
        assertEquals(lits.getFromPool(3), lit ^ 1);
        assertFalse(lits.getFromPool(1) == lits.getFromPool(2));
    }

    /*
     * Test pour String toString()
     */
    public void testToString() {
        lits.ensurePool(3);
        int lit = lits.getFromPool(-3);
        assertEquals("-3", Lits.toString(lit));
        assertEquals("3", Lits.toString(lit ^ 1));
    }

    public void testTruthValue() {
        lits.ensurePool(3);
        int lit = lits.getFromPool(-2);
        assertTrue(lits.isUnassigned(lit));
        assertTrue(lits.isUnassigned(lit ^ 1));
        lits.satisfies(lit);
        assertTrue(lits.isSatisfied(lit));
        assertFalse(lits.isFalsified(lit));
        assertFalse(lits.isUnassigned(lit));
        assertFalse(lits.isSatisfied(lit ^ 1));
        assertTrue(lits.isFalsified(lit ^ 1));
        assertFalse(lits.isUnassigned(lit ^ 1));
        lits.unassign(lit);
        assertTrue(lits.isUnassigned(lit));
        assertTrue(lits.isUnassigned(lit ^ 1));

    }

}
