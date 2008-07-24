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
public abstract class AbstractPseudoBooleanAndPigeonHoleTest extends
        AbstractEZPseudoBooleanAndPigeonHoleTest {

    protected static final String PREFIX = System.getProperty("test.pbprefix");

    public AbstractPseudoBooleanAndPigeonHoleTest(String arg) {
        super(arg);
    }
    
    public void testaloul1011() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl10_11_pb.cnf.cr.opb"));
    }

    public void testaloul1015() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl10_15_pb.cnf.cr.opb"));
    }

    public void testaloul1020() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl10_20_pb.cnf.cr.opb"));
    }

    public void testaloul1516() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl15_16_pb.cnf.cr.opb"));
    }

    public void testaloul1520() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl15_20_pb.cnf.cr.opb"));
    }

    public void testaloul1525() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl15_25_pb.cnf.cr.opb"));
    }

    public void testaloul2021() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl20_21_pb.cnf.cr.opb"));
    }

    public void testaloul2025() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl20_25_pb.cnf.cr.opb"));
    }

    public void testaloul2030() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl20_30_pb.cnf.cr.opb"));
    }

    public void testaloul3031() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl30_31_pb.cnf.cr.opb"));
    }

    public void testaloul3035() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl30_35_pb.cnf.cr.opb"));
    }

    public void testaloul3040() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl30_40_pb.cnf.cr.opb"));
    }

    public void testaloul3536() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl35_36_pb.cnf.cr.opb"));
    }

    public void testaloul3540() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl35_40_pb.cnf.cr.opb"));
    }

    public void testaloul3545() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl35_45_pb.cnf.cr.opb"));
    }

    public void testaloul4041() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl40_41_pb.cnf.cr.opb"));
    }

    public void testaloul4045() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl40_45_pb.cnf.cr.opb"));
    }

    public void testaloul4050() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl40_50_pb.cnf.cr.opb"));
    }

    public void testaloul5051() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl50_51_pb.cnf.cr.opb"));
    }

    public void testaloul5055() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl50_55_pb.cnf.cr.opb"));
    }

    public void testaloul5060() throws FileNotFoundException, IOException,
            ParseFormatException {
        assertFalse(solveInstance(PREFIX
                + "normalized-opb/submitted/aloul/FPGA_SAT05/normalized-chnl50_60_pb.cnf.cr.opb"));
    }
}
