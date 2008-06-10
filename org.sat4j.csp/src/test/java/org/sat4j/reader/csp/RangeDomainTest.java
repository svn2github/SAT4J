/*
 * Created on 11 juil. 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.sat4j.reader.csp;

import junit.framework.TestCase;

import org.sat4j.csp.RangeDomain;
import org.sat4j.specs.IteratorInt;


public class RangeDomainTest extends TestCase {

    public void testSize() {
        RangeDomain domain = new RangeDomain(3, 4);
        assertEquals(2, domain.size());
    }

    public void testGet() {
        RangeDomain domain = new RangeDomain(2, 5);
        assertEquals(4, domain.size());
        assertEquals(2, domain.get(0));
        assertEquals(3, domain.get(1));
        assertEquals(4, domain.get(2));
        assertEquals(5, domain.get(3));
    }

    public void testIterator() {
        RangeDomain domain = new RangeDomain(2, 5);
        IteratorInt it = domain.iterator();
        assertTrue(it.hasNext());
        assertEquals(2, it.next());
        assertTrue(it.hasNext());
        assertEquals(3, it.next());
        assertTrue(it.hasNext());
        assertEquals(4, it.next());
        assertTrue(it.hasNext());
        assertEquals(5, it.next());
        assertFalse(it.hasNext());
    }

    public void testNegativeBounds() {
        RangeDomain domain = new RangeDomain(-2, 4);
        assertEquals(7, domain.size());
    }

    public void testPos() {
        RangeDomain domain = new RangeDomain(1, 5);
        assertEquals(1, domain.pos(2));
        assertEquals(0, domain.pos(1));
        assertEquals(2, domain.pos(3));
        assertEquals(3, domain.pos(4));
        assertEquals(4, domain.pos(5));
    }
}
