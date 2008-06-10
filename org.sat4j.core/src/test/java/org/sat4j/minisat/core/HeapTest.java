package org.sat4j.minisat.core;

import junit.framework.TestCase;

public class HeapTest extends TestCase {

    /*
     * Test method for 'org.sat4j.minisat.core.Heap.setBounds(int)'
     */
    public void testSetBounds() {

    }

    /*
     * Test method for 'org.sat4j.minisat.core.Heap.inHeap(int)'
     */
    public void testInHeap() {
        Heap heap = new Heap(new double[] { 0.0, 3.0, 9.0, 2.0 });
        heap.setBounds(5);
        assertFalse(heap.inHeap(1));
        assertFalse(heap.inHeap(2));
        assertFalse(heap.inHeap(3));
        heap.insert(1);
        assertTrue(heap.inHeap(1));
        assertFalse(heap.inHeap(2));
        assertFalse(heap.inHeap(3));
        heap.insert(2);
        assertTrue(heap.inHeap(1));
        assertTrue(heap.inHeap(2));
        assertFalse(heap.inHeap(3));
        heap.insert(3);
        assertTrue(heap.inHeap(1));
        assertTrue(heap.inHeap(2));
        assertTrue(heap.inHeap(3));
        assertEquals(2, heap.getmin());
        assertTrue(heap.inHeap(1));
        assertFalse(heap.inHeap(2));
        assertTrue(heap.inHeap(3));
        assertEquals(1, heap.getmin());
        assertFalse(heap.inHeap(1));
        assertFalse(heap.inHeap(2));
        assertTrue(heap.inHeap(3));
        assertEquals(3, heap.getmin());
        assertFalse(heap.inHeap(1));
        assertFalse(heap.inHeap(2));
        assertFalse(heap.inHeap(3));

    }

    /*
     * Test method for 'org.sat4j.minisat.core.Heap.increase(int)'
     */
    public void testIncrease() {

    }

    /*
     * Test method for 'org.sat4j.minisat.core.Heap.empty()'
     */
    public void testEmpty() {
        Heap heap = new Heap(new double[] {});
        assertTrue(heap.empty());
    }

    /*
     * Test method for 'org.sat4j.minisat.core.Heap.insert(int)'
     */
    public void testInsert() {
        Heap heap = new Heap(new double[] { 0.0, 1.0, 1.0, 2.0 });
        heap.setBounds(5);
        heap.insert(1);
        heap.insert(2);
        heap.insert(3);
        assertEquals(3, heap.getmin());
        assertEquals(1, heap.getmin());
        assertEquals(2, heap.getmin());
    }

    /*
     * Test method for 'org.sat4j.minisat.core.Heap.getmin()'
     */
    public void testGetmin() {
        Heap heap = new Heap(new double[] { 0.0, 3.0, 9.0, 2.0 });
        heap.setBounds(5);
        heap.insert(1);
        heap.insert(2);
        heap.insert(3);
        assertEquals(2, heap.getmin());
        assertEquals(1, heap.getmin());
        assertEquals(3, heap.getmin());
    }

    /*
     * Test method for 'org.sat4j.minisat.core.Heap.heapProperty()'
     */
    public void testHeapProperty() {

    }

    /*
     * Test method for 'org.sat4j.minisat.core.Heap.heapProperty(int)'
     */
    public void testHeapPropertyInt() {

    }

}
