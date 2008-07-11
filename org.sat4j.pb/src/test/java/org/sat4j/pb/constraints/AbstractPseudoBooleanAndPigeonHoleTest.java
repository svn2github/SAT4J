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
        AbstractPigeonHoleWithCardinalityTest<IPBSolver> {

    protected static final String PREFIX = System.getProperty("test.pbprefix");

    /**
     * Cr?ation d'un test
     * 
     * @param arg
     *            argument ?ventuel
     */
    public AbstractPseudoBooleanAndPigeonHoleTest(String arg) {
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

    // // Tests normalises

    // public void testnfrb30151() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb30-15-1.opb"));
    // }
    //	
    // public void testnfrb30152() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb30-15-2.opb"));
    // }
    //	
    // public void testnfrb30153() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb30-15-3.opb"));
    // }
    //	
    // public void testnfrb30154() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb30-15-4.opb"));
    // }
    //	
    // public void testnfrb30155() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb30-15-5.opb"));
    // }
    //	
    // public void testnfrb35171() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb35-17-1.opb"));
    // }
    //	
    // public void testnfrb35172() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb35-17-2.opb"));
    // }
    //	
    // public void testnfrb35173() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb35-17-3.opb"));
    // }
    //	
    // public void testnfrb35174() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb35-17-4.opb"));
    // }
    //	
    // public void testnfrb35175() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb35-17-5.opb"));
    // }
    //	
    // public void testnfrb40191() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb40-19-1.opb"));
    // }
    //	
    // public void testnfrb40192() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb40-19-2.opb"));
    // }
    //	
    // public void testnfrb40193() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb40-19-3.opb"));
    // }
    //	
    // public void testnfrb40194() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb40-19-4.opb"));
    // }
    //	
    // public void testnfrb40195() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb40-19-5.opb"));
    // }
    //	
    // public void testnfrb45211() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb45-21-1.opb"));
    // }
    //	
    // public void testnfrb45212() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb45-21-2.opb"));
    // }
    //	
    // public void testnfrb45213() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb45-21-3.opb"));
    // }
    //	
    // public void testnfrb45214() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb45-21-4.opb"));
    // }
    //	
    // public void testnfrb45215() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb45-21-5.opb"));
    // }
    //	
    // public void testnfrb50231() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb50-23-1.opb"));
    // }
    //	
    //	
    // public void testnfrb50232() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb50-23-2.opb"));
    // }
    //	
    //	
    // public void testnfrb50233() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb50-23-3.opb"));
    // }
    //	
    // public void testnfrb50234() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb50-23-4.opb"));
    // }
    //	
    // public void testnfrb50235() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb50-23-5.opb"));
    // }
    //	
    // public void testnfrb53241() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb53-24-1.opb"));
    // }
    //	
    // public void testnfrb53242() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb53-24-2.opb"));
    // }
    //	
    // public void testnfrb53243() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb53-24-3.opb"));
    // }
    //	
    // public void testnfrb53244() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb53-24-4.opb"));
    // }
    //	
    // public void testnfrb53245() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb53-24-5.opb"));
    // }
    //	
    // public void testnfrb56251() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb56-25-1.opb"));
    // }
    //	
    // public void testnfrb56252() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb56-25-2.opb"));
    // }
    //	
    // public void testnfrb56253() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb56-25-3.opb"));
    // }
    //	
    // public void testnfrb56254() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb56-25-4.opb"));
    // }
    //	
    // public void testnfrb56255() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb56-25-5.opb"));
    // }
    //	
    // public void testnfrb59261() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb59-26-1.opb"));
    // }
    //	
    // public void testnfrb59262() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb59-26-2.opb"));
    // }
    //	
    // public void testnfrb59263() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb59-26-3.opb"));
    // }
    //	
    // public void testnfrb59264() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb59-26-4.opb"));
    // }
    //	
    // public void testnfrb59265() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/kexu/normalized-frb59-26-5.opb"));
    // }

    // // VASCO: Logic synthesis
    // public void testn5xp1b() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-5xp1.b.opb"));
    // }
    //    	
    // public void testn9symb() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-9sym.b.opb"));
    // }
    //    	
    // public void testnalu4b() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-alu4.b.opb"));
    // }
    //    	
    // public void testnapex4a() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-apex4.a.opb"));
    // }
    //    	
    // public void testnbench1pi() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-bench1.pi.opb"));
    // }
    //    	
    // public void testnclipb() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-clip.b.opb"));
    // }
    //    	
    // public void testncountb() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-count.b.opb"));
    // }
    //    	
    // public void testne64() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-e64.b.opb"));
    // }
    //    	
    // public void testnf51mb() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-f51m.b.opb"));
    // }
    //    	
    // public void testnjac3() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-jac3.opb"));
    // }
    //    	
    // public void testnrotb() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-rot.b.opb"));
    // }
    //    	
    // public void testnsao2b() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/logic-synthesis/normalized-sao2.b.opb"));
    // }
    //    	
    // // VASCO: Routing problems
    // public void testns3331() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s3-3-3-1pb.opb"));
    // }
    //    	
    // public void testns3332() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s3-3-3-2pb.opb"));
    // }
    //    	
    // public void testns3333() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s3-3-3-3pb.opb"));
    // }
    //    	
    // public void testns3334() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s3-3-3-4pb.opb"));
    // }
    //    	
    // public void testns3335() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s3-3-3-5pb.opb"));
    // }
    //    	
    // public void testns4431() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-1pb.opb"));
    // }
    //    	
    // public void testns4432() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-2pb.opb"));
    // }
    //    	
    // public void testns4433() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-3pb.opb"));
    // }
    //    	
    // public void testns4434() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-4pb.opb"));
    // }
    //    	
    // public void testns4435() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-5pb.opb"));
    // }
    //    	
    // public void testns4436() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-6pb.opb"));
    // }
    //    	
    // public void testns4437() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-7pb.opb"));
    // }
    //    	
    // public void testns4438() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-8pb.opb"));
    // }
    //    	
    // public void testns4439() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-9pb.opb"));
    // }
    //    	
    // public void testns44310() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/submitted/manquinho/routing/normalized-s4-4-3-10pb.opb"));
    // }

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

    // public void test22ssmv() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-22s.smv.opb"));
    // }
    // public void test25ssmv() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-25s.smv.opb"));
    // }
    // public void test37ssmv() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-37s.smv.opb"));
    // }
    // public void test43ssmv() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-43s.smv.opb"));
    // }
    // public void test44ssmv() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-44s.smv.opb"));
    // }
    // public void test46ssmv() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-46s.smv.opb"));
    // }
    //	
    // public void testblastfloppy12() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-blast-floppy1-2.ucl.opb"));
    // }
    //	
    // public void testblastfloppy13() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-blast-floppy1-3.ucl.opb"));
    // }
    //	
    // public void testblastfloppy14() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-blast-floppy1-4.ucl.opb"));
    // }
    //	
    // public void testblastfloppy16() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-blast-floppy1-6.ucl.opb"));
    // }
    //	
    // public void testblastfloppy17() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-blast-floppy1-7.ucl.opb"));
    // }
    //	
    // public void testblastfloppy18() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-blast-floppy1-8.ucl.opb"));
    // }
    //	
    // public void testmpsrd13rplusc21() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/mps-v2-13-7/MIPLIB/miplib2003/normalized-mps-v2-13-7-rd-rplusc-21.opb"));
    // }
    //	
    // public void testmps13core2536() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/mps-v2-13-7/plato.asu.edu/pub/unibo/normalized-mps-v2-13-7-core2536-691.opb"));
    // }
    //	
    // public void testmps13core4284() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/mps-v2-13-7/plato.asu.edu/pub/unibo/normalized-mps-v2-13-7-core4284-1064.opb"));
    // }
    //	
    // public void testmps13core4872() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/mps-v2-13-7/plato.asu.edu/pub/unibo/normalized-mps-v2-13-7-core4872-1529.opb"));
    // }
    //	
    // public void testmps13core2586() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/mps-v2-13-7/plato.asu.edu/pub/unibo/normalized-mps-v2-13-7-core2586-950.opb"));
    // }
    //	
    // public void testtlan2() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-blast-tlan2.ucl.opb"));
    // }
    //	
    // public void testtlan3() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertFalse(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-blast-tlan3.ucl.opb"));
    // }
    //	
    // public void testibmqfull() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-cache-ibm-q-full.all.ucl.opb"));
    // }
    //	
    // public void testibmqunboundedic22() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-cache-ibm-q-unbounded.Ic22arity.ucl.opb"));
    // }
    //	
    // public void testibmqunboundedicl2() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-cache-ibm-q-unbounded.Icl2arity.ucl.opb"));
    // }
    //	
    // public void testunboundedIh1() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-cache-ibm-q-unbounded.Ih1arity.ucl.opb"));
    // }
    //	
    // public void testunboundedIh2() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-cache-ibm-q-unbounded.Ih2arity.ucl.opb"));
    // }
    //	
    // public void testcacheinv10() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-cache.inv10.ucl.opb"));
    // }
    //	
    // public void testcacheinv12() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-cache.inv12.ucl.opb"));
    // }
    //	
    // public void testcacheinv14() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-cache.inv14.ucl.opb"));
    // }
    //	
    // public void testcacheinv8() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-cache.inv8.ucl.opb"));
    // }
    //	
    // public void testdlx1crwmrm() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-dlx1c.rwmem.ucl.opb"));
    // }
    //	
    // public void testdlx1crwmem1() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-dlx1c.rwmem1.ucl.opb"));
    // }
    //	
    // public void testdlx1c() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-dlx1c.ucl.opb"));
    // }
    //	
    // public void testelfrf10() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-elf.rf10.ucl.opb"));
    // }
    //	
    // public void testelfrf6() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-elf.rf6.ucl.opb"));
    // }
    //	
    // public void testelfrf7() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-elf.rf7.ucl.opb"));
    // }
    //	
    // public void testelfrf8() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-elf.rf8.ucl.opb"));
    // }
    //	
    // public void testelfrf9() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-elf.rf9.ucl.opb"));
    // }
    //	
    // public void testburchdill2() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.burch_dill.2.accl.ucl.opb"));
    // }
    //	
    // public void testburchdill3() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.burch_dill.3.accl.ucl.opb"));
    // }
    //	
    // public void testburchdill4() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.burch_dill.4.accl.ucl.opb"));
    // }
    //	
    // public void testburchdill6() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.burch_dill.6.accl.ucl.opb"));
    // }
    //	
    //	
    // public void testburchdill8() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.burch_dill.8.accl.ucl.opb"));
    // }
    //	
    //	
    // public void testrobregvalid() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.ex.br.mem.RobRegValid_bar.ucl.opb"));
    // }
    //	
    //	
    // public void testsrc1validBar() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.ex.br.mem.Src1Valid_Src1ValidBar.ucl.opb"));
    // }
    //	
    //	
    // public void testLdValue() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.ex.mem.LdValue.ucl.opb"));
    // }
    //	
    //	
    // public void testLsqHdStrong() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.ex.mem.LsqHdStrong.ucl.opb"));
    // }
    //	
    //	
    // public void testrf10() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.rf10.ucl.opb"));
    // }
    //	
    //	
    // public void testrf6() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.rf6.ucl.opb"));
    // }
    //	
    //	
    // public void testrf7() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.rf7.ucl.opb"));
    // }
    //	
    //	
    // public void testrf8() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.rf8.ucl.opb"));
    // }
    //	
    //	
    // public void testrf9() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.rf9.ucl.opb"));
    // }
    //	
    //	
    // public void testtag10() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.tag10.ucl.opb"));
    // }
    //	
    //	
    // public void testtag12() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.tag12.ucl.opb"));
    // }
    //	
    //	
    // public void testtag14() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.tag14.ucl.opb"));
    // }
    //	
    //	
    // public void testtag8() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.tag8.ucl.opb"));
    // }
    //	
    //	
    // public void testunboundedallucl() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/uclid_pb_benchmarks/normalized-ooo.unbounded.all.ucl.opb"));
    // }
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
    // public void testwalser972() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/course-ass/normalized-ss97-2.opb"));
    // }
    //	
    // public void testwalser973() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/course-ass/normalized-ss97-3.opb"));
    // }
    //	
    // public void testwalser974() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/course-ass/normalized-ss97-4.opb"));
    // }
    //	
    // public void testwalser975() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/course-ass/normalized-ss97-5.opb"));
    // }
    //	
    // public void testwalser976() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/course-ass/normalized-ss97-6.opb"));
    // }
    //
    // public void testradar1010_095100() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-10:10:4.5:0.95:100.opb"));
    // }
    //	
    // public void testradar1010_05100() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-10:10:4.5:0.5:100.opb"));
    // }
    //	
    // public void testradar1010_09598() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-10:10:4.5:0.95:98.opb"));
    // }
    //	
    // public void testradar1020_05100() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-10:20:4.5:0.5:100.opb"));
    // }
    //	
    // public void testradar1020_095100() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-10:20:4.5:0.95:100.opb"));
    // }
    //	
    // public void testradar1020_09598() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-10:20:4.5:0.95:98.opb"));
    // }
    //	
    // public void testradar3030_05100() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-30:30:4.5:0.5:100.opb"));
    // }
    //	
    // public void testradar3030_095100() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-30:30:4.5:0.95:100.opb"));
    // }
    //	
    // public void testradar3030_09598() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-30:30:4.5:0.95:98.opb"));
    // }
    //	
    // public void testradar3070_05100() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-30:70:4.5:0.5:100.opb"));
    // }
    //	
    // public void testradar3070_095100() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-30:70:4.5:0.95:100.opb"));
    // }
    //	
    // public void testradar3070_09598() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/radar/normalized-30:70:4.5:0.95:98.opb"));
    // }
    //
    // public void testppp13_13_19() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/ppp-problems/normalized-ppp:1,3-13,19.opb"));
    // }
    // public void testppp1_11_19_21() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/ppp-problems/normalized-ppp:1-11,19,21.opb"));
    // }
    // public void testppp1_12_16() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/ppp-problems/normalized-ppp:1-12,16.opb"));
    // }
    // public void testppp1_13() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/ppp-problems/normalized-ppp:1-13.opb"));
    // }
    // public void testppp1_9_16_19() throws FileNotFoundException, IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/ppp-problems/normalized-ppp:1-9,16-19.opb"));
    // }
    // public void testppp3_13_25_26() throws FileNotFoundException,
    // IOException,
    // ParseFormatException {
    // assertTrue(solveInstance(PREFIX +
    // "normalized-opb/web/www.ps.uni-sb.de/~walser/benchmarks/ppp-problems/normalized-ppp:3-13,25,26.opb"));
    // }
    //
    //	

}
