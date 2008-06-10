/*
 * Created on 18 sept. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.pb.constraints.pb;

import junit.framework.TestCase;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WatchPbTest extends TestCase {

    /**
     * Constructor for WatchPbTest.
     * 
     * @param arg0
     */
    public WatchPbTest(String arg0) {
        super(arg0);
    }

//    public void testNormalize() {
//        // String test = "BILANCIO: +202 A1 +404 A2 +606 A3 +808 A4 +1010 A5
//        // +1212 A6 +1414 A7 +1616 A8 +1818 A9 "
//        // +"-79 B1 -158 B2 -237 B3 -316 B4 -395 B5 -474 B6 -553 B7 -632 B8 -711
//        // B9 "
//        // +" +100023 C1 +200046 C2 +300069 C3 +400092 C4 +500115 C5 +600138 C6
//        // +700161 C7 +800184 C8 +900207 C9 "
//        // +" -89810 D1 -179620 D2 -269430 D3 -359240 D4 -449050 D5 -538860 D6
//        // -628670 D7 -718480 D8 -808290 D9 "
//        // +" -9980 E1 -19960 E2 -29940 E3 -39920 E4 -49900 E5 -59880 E6 -69860
//        // E7 -79840 E8 -89820 E9 "
//        // +" +1000 F1 +2000 F2 +3000 F3 +4000 F4 +5000 F5 +6000 F6 +7000 F7
//        // +8000 F8 +9000 F9 "
//        // +" +100 G1 +200 G2 +300 G3 +400 G4 +500 G5 +600 G6 +700 G7 +800 G8
//        // +900 G9 "
//        // +" +10000 H1 +20000 H2 +30000 H3 +40000 H4 +50000 H5 +60000 H6 +70000
//        // H7 +80000 H8 +90000 H9 "
//        // +" +100 I1 +200 I2 +300 I3 +400 I4 +500 I5 +600 I6 +700 I7 +800 I8
//        // +900 I9 "
//        // +" -L1 -2 L2 -3 L3 -4 L4 -5 L5 -6 L6 -7 L7 -8 L8 -9 L9 = 0";
//
//        // create literals
//        IVecInt expectedLits = new VecInt();
//        for (int i = 1; i <= 90; i++) {
//            expectedLits.push(i);
//        }
//        // create coeffs
//        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>();
//        BigInteger sum = BigInteger.ZERO;
//        BigInteger coef;
//        int[] values = { 202, -79, 100023, -89810, -9980, 1000, 100, 10000,
//                100, -1 };
//
//        for (int j = 0; j < values.length; j++) {
//            for (int i = 1; i <= 9; i++) {
//                expectedCoeffs.push(coef = BigInteger.valueOf(values[j] * i));
//                if (coef.signum() > 0) {
//                    sum = sum.add(coef);
//                }
//            }
//        }
//        DataStructureFactory<ILits> daf = new PBMaxDataStructure();
//        Solver<ILits> solver = org.sat4j.minisat.SolverFactory.newMiniLearning(daf);
//        daf.getVocabulary().ensurePool(100);
//        try {
//            solver.addPseudoBoolean(expectedLits, expectedCoeffs, false,
//                    BigInteger.ZERO);
//            assertEquals(sum, ((WatchPb) solver.getIthConstr(0)).getDegree());
//        } catch (ContradictionException e) {
//            fail("The constraint is not a contradiction!!!");
//        }
//    }

}
