package org.sat4j.core;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.sat4j.specs.IVec;

/*
 * Created on 16 oct. 2003
 *
 */

/**
 * @author leberre
 * 
 */
public class VecTest extends TestCase {

    /**
     * Constructor for VecTest.
     * 
     * @param arg0
     */
    public VecTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myvec = new Vec<Integer>();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test pour void Vec()
     */
    public void testVec() {
        IVec<Integer> vec = new Vec<Integer>();
        assertEquals(0, vec.size());
    }

    /*
     * Test pour void Vec(int)
     */
    public void testVecint() {
        IVec<Integer> vec = new Vec<Integer>(10, Integer.valueOf(0));
        assertEquals(Integer.valueOf(0), vec.last());
        assertEquals(10, vec.size());
    }

    /*
     * Test pour void Vec(int, Object)
     */
    public void testVecintObject() {
        Integer pad = Integer.valueOf(10);
        IVec<Integer> vec = new Vec<Integer>(10, pad);
        assertEquals(pad, vec.last());
        assertEquals(10, vec.size());

    }

    public void testSize() {
        assertEquals(0, myvec.size());
        myvec.push(null);
        assertEquals(1, myvec.size());
        myvec.push(null);
        assertEquals(2, myvec.size());
        myvec.pop();
        assertEquals(1, myvec.size());
        myvec.pop();
        assertEquals(0, myvec.size());
    }

    public void testShrink() {
        for (int i = 0; i < 15; i++) {
            myvec.push(Integer.valueOf(i));
        }
        assertEquals(15, myvec.size());
        myvec.shrink(10);
        assertEquals(5, myvec.size());
        assertEquals(Integer.valueOf(4), myvec.last());
        myvec.shrink(0);
        assertEquals(5, myvec.size());
        assertEquals(Integer.valueOf(4), myvec.last());
    }

    public void testShrinkTo() {
        for (int i = 0; i < 15; i++) {
            myvec.push(Integer.valueOf(i));
        }
        assertEquals(15, myvec.size());
        myvec.shrinkTo(10);
        assertEquals(10, myvec.size());
        assertEquals(Integer.valueOf(9), myvec.last());
        myvec.shrinkTo(10);
        assertEquals(10, myvec.size());
        assertEquals(Integer.valueOf(9), myvec.last());

    }

    public void testPop() {
        for (int i = 0; i < 15; i++) {
            myvec.push(Integer.valueOf(i));
        }
        assertEquals(15, myvec.size());
        myvec.pop();
        assertEquals(14, myvec.size());
        assertEquals(Integer.valueOf(13), myvec.last());
    }

    /*
     * Test pour void growTo(int)
     */
    public void testGrowToint() {
        assertEquals(0, myvec.size());
        myvec.growTo(12, null);
        assertEquals(12, myvec.size());
        assertNull(myvec.last());
        myvec.growTo(20, null);
        assertEquals(20, myvec.size());
        assertNull(myvec.last());
    }

    /*
     * Test pour void growTo(int, Object)
     */
    public void testGrowTointObject() {
        assertEquals(0, myvec.size());
        Integer douze = Integer.valueOf(12);
        myvec.growTo(12, douze);
        assertEquals(12, myvec.size());
        assertEquals(douze, myvec.last());
        Integer treize = Integer.valueOf(13);
        myvec.growTo(20, treize);
        assertEquals(20, myvec.size());
        assertEquals(treize, myvec.last());
    }

    /*
     * Test pour void push()
     */
    public void testPush() {
        assertEquals(0, myvec.size());
        for (int i = 0; i < 10; i++) {
            myvec.push(Integer.valueOf(0));
        }
        assertEquals(10, myvec.size());
        assertEquals(Integer.valueOf(0), myvec.last());
    }

    /*
     * Test pour void push(Object)
     */
    public void testPushObject() {
        Integer deux = Integer.valueOf(2);
        assertEquals(0, myvec.size());
        for (int i = 0; i < 10; i++) {
            myvec.push(deux);
        }
        assertEquals(10, myvec.size());
        assertEquals(deux, myvec.last());
    }

    public void testClear() {
        myvec.push(null);
        myvec.push(null);
        myvec.clear();
        assertEquals(0, myvec.size());
    }

    public void testLast() {
        for (int i = 0; i < 10; i++) {
            Integer myint = Integer.valueOf(i);
            myvec.push(myint);
            assertEquals(myint, myvec.last());
        }
    }

    public void testGet() {
        for (int i = 0; i < 10; i++) {
            Integer myint = Integer.valueOf(i);
            myvec.push(myint);
            assertEquals(myint, myvec.get(i));
        }
    }

    public void testCopyTo() {
        Vec<Integer> nvec = new Vec<Integer>();
        myvec.growTo(15, Integer.valueOf(15));
        myvec.copyTo(nvec);
        assertEquals(15, nvec.size());
        assertEquals(15, myvec.size());
        assertEquals(myvec.last(), nvec.last());

    }

    public void testMoveTo() {
        Vec<Integer> nvec = new Vec<Integer>();
        myvec.growTo(15, Integer.valueOf(15));
        myvec.moveTo(nvec);
        assertEquals(15, nvec.size());
        assertEquals(0, myvec.size());
        assertEquals(Integer.valueOf(15), nvec.last());
    }

    public void testSelectionSort() {
        Vec<Integer> nvec = new Vec<Integer>();
        for (int i = 30; i > 0; i--) {
            nvec.push(Integer.valueOf(i));
        }
        Comparator<Integer> comp = new DefaultComparator<Integer>();
        nvec.selectionSort(0, 30, comp);
        for (int i = 1; i <= 30; i++) {
            assertEquals(i, nvec.get(i - 1).intValue());
        }
    }

    public void testSort() {
        IVec<Integer> nvec = new Vec<Integer>();
        for (int i = 101; i > 0; i--) {
            nvec.push(Integer.valueOf(i));
        }
        nvec.push(Integer.valueOf(30));
        nvec.push(Integer.valueOf(40));
        Comparator<Integer> comp = new DefaultComparator<Integer>();
        nvec.sort(comp);
        for (int i = 1; i <= 30; i++) {
            assertEquals(i, nvec.get(i - 1).intValue());
        }
    }

    public void testSortEmpty() {
        IVec<Integer> nvec = new Vec<Integer>();
        Comparator<Integer> comp = new DefaultComparator<Integer>();
        nvec.sort(comp);
    }

    public void testSortUnique() {
        IVec<Integer> nvec = new Vec<Integer>();
        for (int i = 101; i > 0; i--) {
            nvec.push(Integer.valueOf(i));
        }
        nvec.push(Integer.valueOf(30));
        nvec.push(Integer.valueOf(40));
        nvec.push(Integer.valueOf(50));
        nvec.push(Integer.valueOf(55));
        nvec.push(Integer.valueOf(60));
        Comparator<Integer> comp = new DefaultComparator<Integer>();
        nvec.sortUnique(comp);
        for (int i = 1; i <= 101; i++) {
            assertEquals(i, nvec.get(i - 1).intValue());
        }
    }

    public void testDelete() {
        IVec<Integer> nvec = new Vec<Integer>();
        for (int i = 0; i < 100; i++) {
            nvec.push(Integer.valueOf(i));
        }
        assertEquals(Integer.valueOf(10), nvec.delete(10));
        assertEquals(Integer.valueOf(99), nvec.get(10));
        nvec.clear();
        nvec.push(Integer.valueOf(1));
        assertEquals(Integer.valueOf(1), nvec.delete(0));
    }

    public void testEquals() {
        IVec<Integer> nvec = new Vec<Integer>(3, Integer.valueOf(2));
        IVec<Integer> vect = new Vec<Integer>(3, Integer.valueOf(2));
        IVec<Integer> vecf = new Vec<Integer>(4, Integer.valueOf(2));
        IVec<Integer> vecf2 = new Vec<Integer>(2, Integer.valueOf(2));
        IVec<Integer> vecf3 = new Vec<Integer>(3, Integer.valueOf(3));
        assertEquals(nvec, vect);
        assertFalse(nvec.equals(vecf));
        assertFalse(nvec.equals(vecf2));
        assertFalse(nvec.equals(vecf3));

    }

    public void testIterator() {
        Vec<String> str = new Vec<String>();
        str.push("titi");
        str.push("toto");
        str.push("tata");
        Iterator<String> it = str.iterator();
        assertTrue(it.hasNext());
        assertEquals("titi", it.next());
        assertTrue(it.hasNext());
        assertEquals("toto", it.next());
        assertTrue(it.hasNext());
        assertEquals("tata", it.next());
        assertFalse(it.hasNext());
    }

    public void testNoSuchElementException() {
        Vec<String> str = new Vec<String>();
        Iterator<String> it = str.iterator();
        assertFalse(it.hasNext());
        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
    }

    private IVec<Integer> myvec;

}
