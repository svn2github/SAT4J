package org.sat4j.minisat.core;

import junit.framework.TestCase;

/*
 * Created on 23 oct. 2003
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
public class QueueTest extends TestCase {

    /**
     * Constructor for QueueTest.
     * 
     * @param arg0
     */
    public QueueTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        qu = new IntQueue();
    }

    public void testInsert() {
        qu.ensure(15);
        for (int i = 0; i < 15; i++) {
            qu.insert(i);
        }
        for (int i = 0; i < 15; i++) {
            assertEquals(i, qu.dequeue());
        }
    }

    public void testDequeue() {
        qu.insert(1);
        qu.insert(2);
        assertEquals(2, qu.size());
        int i = qu.dequeue();
        assertEquals(1, i);
        qu.insert(3);
        assertEquals(2, qu.size());
        i = qu.dequeue();
        assertEquals(2, i);
        i = qu.dequeue();
        assertEquals(3, i);
    }

    public void testClear() {
        assertEquals(0, qu.size());
        qu.insert(1);
        qu.insert(2);
        assertEquals(2, qu.size());
        qu.clear();
        assertEquals(0, qu.size());
        qu.insert(1);
        qu.insert(2);
        assertEquals(2, qu.size());
    }

    public void testSize() {
        assertEquals(0, qu.size());
        qu.insert(1);
        assertEquals(1, qu.size());
        qu.insert(2);
        assertEquals(2, qu.size());
        qu.dequeue();
        assertEquals(1, qu.size());
        qu.dequeue();
        assertEquals(0, qu.size());
    }

    private IntQueue qu;

}
