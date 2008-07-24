/*
 * Created on 20 mai 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.pb.constraints;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.sat4j.minisat.AbstractAcceptanceTestCase;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ISolver;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractPigeonHoleWithCardinalityTest<T extends ISolver> extends
        AbstractAcceptanceTestCase<T> {

    protected static final String PREFIX = System.getProperty("test.pbprefix");

    /**
     * Cr?ation d'un test
     * 
     * @param arg
     *            argument ?ventuel
     */
    public AbstractPigeonHoleWithCardinalityTest(String arg) {
        super(arg);
    }

    public void testPN34() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX + "pigeons/PN-3-4.opb"));
    }

    public void testPN4() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "pigeons/PN-4-3.opb"));
    }

    public void testPN45() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX + "pigeons/PN-4-5.opb"));
    }

    public void testPN5() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "pigeons/PN-5-4.opb"));
    }

    public void testPN56() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX + "pigeons/PN-5-6.opb"));
    }

    public void testPN6() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "pigeons/PN-6-5.opb"));
    }

    public void testPN67() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX + "pigeons/PN-6-7.opb"));
    }

    public void testPN7() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "pigeons/PN-7-6.opb"));
    }

    public void testPN78() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX + "pigeons/PN-7-8.opb"));
    }

    public void testPN8() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "pigeons/PN-8-7.opb"));
    }

    public void testPN89() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX + "pigeons/PN-8-9.opb"));
    }

    public void testPN9() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "pigeons/PN-9-8.opb"));
    }

    public void testPN910() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX + "pigeons/PN-9-10.opb"));
    }

    public void testPN10() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "pigeons/PN-10-9.opb"));
    }
}
