package org.sat4j.minisat.core;

import junit.framework.TestCase;

import org.sat4j.specs.Lbool;

/*
 * Created on 2 nov. 2003
 *
 */

/**
 * @author leberre
 * 
 */
public class LboolTest extends TestCase {

    /**
     * Constructor for LboolTest.
     * 
     * @param arg0
     */
    public LboolTest(String arg0) {
        super(arg0);
    }

    public void testNot() {
        assertEquals(Lbool.FALSE, Lbool.TRUE.not());
        assertEquals(Lbool.TRUE, Lbool.FALSE.not());
        assertEquals(Lbool.UNDEFINED, Lbool.UNDEFINED.not());
    }

    /*
     * Test pour boolean equals(Object)
     */
    public void testEqualsObject() {
        assertEquals(Lbool.FALSE, Lbool.FALSE);
        assertNotSame(Lbool.FALSE, Lbool.TRUE);
        assertNotSame(Lbool.FALSE, Lbool.UNDEFINED);
        assertNotSame(Lbool.TRUE, Lbool.UNDEFINED);
    }

    /*
     * Test pour String toString()
     */
    public void testToString() {
        assertEquals("U", Lbool.UNDEFINED.toString());
        assertEquals("T", Lbool.TRUE.toString());
        assertEquals("F", Lbool.FALSE.toString());
    }

}
