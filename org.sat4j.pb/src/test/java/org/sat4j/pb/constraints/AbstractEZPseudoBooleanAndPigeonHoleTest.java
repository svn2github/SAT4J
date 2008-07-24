/*
 * Created on 20 mai 2004
 *
 */
package org.sat4j.pb.constraints;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;

/**
 * @author leberre
 * 
 * Those pseudo boolean problems were kindly provided by Niklas Een.
 * 
 */
public abstract class AbstractEZPseudoBooleanAndPigeonHoleTest extends
        AbstractPigeonHoleWithCardinalityTest<IPBSolver> {

    protected static final String PREFIX = System.getProperty("test.pbprefix");

    /**
     * Cr?ation d'un test
     * 
     * @param arg
     *            argument ?ventuel
     */
    public AbstractEZPseudoBooleanAndPigeonHoleTest(String arg) {
        super(arg);
    }

    @Override
    protected InstanceReader createInstanceReader(IPBSolver solver){
    	return new PBInstanceReader(solver);
    }
    
    @Override
    protected void tearDown() {
        super.tearDown();
    }


    // VASCO: traveling tournament problem
    public void testncirc43() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/ttp/normalized-circ4_3.opb"));
    }

    public void testncirc63() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/ttp/normalized-circ6_3.opb"));
    }

    public void testncirc83() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/ttp/normalized-circ8_3.opb"));
    }

    public void testncirc103() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/ttp/normalized-circ10_3.opb"));
    }

    public void testndata43() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/ttp/normalized-data4_3.opb"));
    }

    public void testndata63() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/ttp/normalized-data6_3.opb"));
    }

    public void testndata83() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/ttp/normalized-data8_3.opb"));
    }

    public void testndata103() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/ttp/normalized-data10_3.opb"));
    }

    public void testn9symml() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-9symml.opb"));
    }

    public void testnC17() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-C17.opb"));
    }

    public void testnC432() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-C432.opb"));
    }

    public void testnb1() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-b1.opb"));
    }

    public void testnc8() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-c8.opb"));
    }

    public void testncc() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-cc.opb"));
    }

    public void testncm42a() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-cm42a.opb"));
    }

    public void testncmb() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-cmb.opb"));
    }

    public void testnmux() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-mux.opb"));
    }

    public void testnmyadder() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertTrue(solveInstance(PREFIX
                + "normalized-opb/submitted/manquinho/synthesis-ptl-cmos-circuits/normalized-my_adder.opb"));
    }
}
