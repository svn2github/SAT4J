/*
 * Created on 17 mars 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat;

import junit.framework.TestCase;

import org.sat4j.minisat.constraints.ClausalDataStructureWL;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.orders.VarOrder;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class VarOrderTest extends TestCase {

    /*
     * Class to test for void newVar()
     */
    public void testNewVar() {
        int p = voc.getFromPool(-1);
        order.init();
        assertEquals(p, order.select());
        voc.satisfies(2); // satisfying literal 1
        assertEquals(ILits.UNDEFINED, order.select());
    }

    /*
     * Class to test for void newVar(int)
     */
    public void testNewVarint() {
    }

    public void testSelect() {
    }

    public void testSetVarDecay() {
    }

    public void testUndo() {
    }

    public void testUpdateVar() {
    }

    public void testVarDecayActivity() {
    }

    public void testNumberOfInterestingVariables() {
    }

    public void testGetVocabulary() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        voc = new ClausalDataStructureWL().getVocabulary();
        voc.ensurePool(5);
        order = new VarOrder<ILits>();
        order.setLits(voc);
    }

    private ILits voc;

    private IOrder<ILits> order;
}
