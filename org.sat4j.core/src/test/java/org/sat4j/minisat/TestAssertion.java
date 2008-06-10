/*
 * Created on 20 avr. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.minisat;

import junit.framework.TestCase;

/**
 * @author roussel
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestAssertion extends TestCase {

    public void testAssertion() {
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        if (!assertionsEnabled)
            fail("assertions should be enabled !");
    }

}
