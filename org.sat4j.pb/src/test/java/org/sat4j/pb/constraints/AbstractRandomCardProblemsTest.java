/*
 * Created on 29 aout 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.pb.constraints;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ISolver;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractRandomCardProblemsTest<T extends ISolver> extends
        AbstractPigeonHoleWithCardinalityTest<T> {

    /**
     * 
     */
    public AbstractRandomCardProblemsTest(String name) {
        super(name);
    }

    public void testRndDeg1() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg1.opb"));
    }

    public void testRndDeg2() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg2.opb"));
    }

    public void testRndDeg3() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg3.opb"));
    }

    public void testRndDeg4() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg4.opb"));
    }

    public void testRndDeg5() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg5.opb"));
    }

    public void testRndDeg6() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg6.opb"));
    }

    public void testRndDeg7() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg7.opb"));
    }

    public void testRndDeg8() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg8.opb"));
    }

    public void testRndDeg9() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg9.opb"));
    }

    public void testRndDeg10() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg10.opb"));
    }

    public void testRndDeg11() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg11.opb"));
    }

    public void testRndDeg12() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg12.opb"));
    }

    public void testRndDeg13() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg13.opb"));
    }

    public void testRndDeg14() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg14.opb"));
    }

    public void testRndDeg15() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg15.opb"));
    }

    public void testRndDeg16() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg16.opb"));
    }

    public void testRndDeg17() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg17.opb"));
    }

    public void testRndDeg18() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX + "random-opb/rnddeg18.opb"));
    }

}
