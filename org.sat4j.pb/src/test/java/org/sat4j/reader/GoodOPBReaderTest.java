/*
 * Created on 8 sept. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.reader;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.reader.GoodOPBReader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class GoodOPBReaderTest extends MockObjectTestCase {

    /**
     * @param arg0
     */
    public GoodOPBReaderTest(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public void testParseResetSolverBeforeParsing() {
        // expectations
        mockSolver.expects(once()).method("reset");

        try {
            String test = "";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test))); // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testIgnoreCommentedLines() {
        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean");
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));
        try {
            String test = "* Comment \n -V2 V3 V4 >= 12";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testSkipMinObjectiveFunction() {
        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean");
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));
        try {
            String test = "* Comment \n min : -V1 -V2 -V3 \n -V2 V3 V4 >= 12";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testSkipMaxObjectiveFunction() {

        // expectations
        IVecInt expectedLits = new VecInt();
        expectedLits.push(-1);
        expectedLits.push(2);
        expectedLits.push(3);
        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>(3, BigInteger.ONE);

        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(12)));
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));

        try {
            String test = "* Comment \n max : ~V1 ~V2 ~V3 \n ~V2 V3 V4 >= 12";
            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testSkipConstraintName() {

        // expectations
        IVecInt expectedLits = new VecInt();
        expectedLits.push(-1);
        expectedLits.push(2);
        expectedLits.push(3);
        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>(3, BigInteger.ONE);

        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(12)));
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));

        try {
            String test = "* Comment \n max : ~V1 ~V2 ~V3 \n C1 : ~V2 V3 V4 >= 12";
            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testReadCardinalityConstraints() {

        IVecInt expectedLits = new VecInt();
        expectedLits.push(-1);
        expectedLits.push(2);
        expectedLits.push(3);

        IVecInt expectedLits2 = new VecInt();
        expectedLits2.push(1);
        expectedLits2.push(3);
        expectedLits2.push(4);
        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>(3, BigInteger.ONE);

        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(12))).id("first");

        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits2), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(34))).after("first");
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));
        try {
            String test = "* Comment \n ~V2 V3 V4 >= 12 \n V2 +V4 V6 >= 34";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testReadProblematicCardinalityConstraints() {

        IVecInt expectedLits = new VecInt().push(-1).push(2).push(3);

        IVecInt expectedLits2 = new VecInt().push(1).push(-3).push(4);

        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>(3, BigInteger.ONE);

        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(12))).id("first");

        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits2), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(34))).after("first");
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));
        try {
            String test = "* Comment \n ~ V2  + V3 + V4 >= 12 \n V2 + ~V4 + V6 >= 34";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testReadPBConstraints() {

        IVecInt expectedLits = new VecInt().push(1).push(2);
        expectedLits.push(3);
        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>();
        expectedCoeffs.push(BigInteger.valueOf(32));
        expectedCoeffs.push(BigInteger.valueOf(-64));
        expectedCoeffs.push(BigInteger.valueOf(-123456));

        IVecInt expectedLits2 = new VecInt().push(1).push(3).push(4);
        IVec<BigInteger> expectedCoeffs2 = new Vec<BigInteger>(3,
                BigInteger.ONE);

        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(12))).id("first");

        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits2), eq(expectedCoeffs2), eq(true),
                eq(BigInteger.valueOf(34))).after("first");
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));
        try {
            String test = "* Comment \n 32 V2 -64 V3 -123456 V4 >= 12 \n V2 +V4 V6 >= 34";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testReadProblematicPBConstraints() {

        IVecInt expectedLits = new VecInt().push(-1).push(2);
        expectedLits.push(3);
        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>().push(
                BigInteger.valueOf(32)).push(BigInteger.valueOf(-64)).push(
                BigInteger.valueOf(-123456));

        IVecInt expectedLits2 = new VecInt().push(1).push(3).push(4);
        IVec<BigInteger> expectedCoeffs2 = new Vec<BigInteger>(3,
                BigInteger.ONE);

        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(12))).id("first");

        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits2), eq(expectedCoeffs2), eq(true),
                eq(BigInteger.valueOf(34))).after("first");
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));
        try {
            String test = "* Comment \n 32 ~ V2 -64 V3 -123456 V4 >= 12 \n  + V2 +V4 + V6 >= 34";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testDiscardTrailingComma() {

        IVecInt expectedLits = new VecInt().push(-1).push(2);
        expectedLits.push(3);
        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>().push(
                BigInteger.valueOf(32)).push(BigInteger.valueOf(-64)).push(
                BigInteger.valueOf(-123456));

        IVecInt expectedLits2 = new VecInt().push(1).push(3).push(4);
        IVec<BigInteger> expectedCoeffs2 = new Vec<BigInteger>(3,
                BigInteger.ONE);

        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(12))).id("first");

        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits2), eq(expectedCoeffs2), eq(true),
                eq(BigInteger.valueOf(34))).after("first");
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));
        try {
            String test = "* Comment \n 32 ~ V2 -64 V3 -123456 V4 >= 12; \n  + V2 +V4 + V6 >= 34 ;";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testProblemWithLeadingPlus() {

        IVecInt expectedLits = new VecInt().push(-1).push(2);
        expectedLits.push(3);
        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>().push(
                BigInteger.valueOf(32)).push(BigInteger.valueOf(-64)).push(
                BigInteger.valueOf(-123456));

        IVecInt expectedLits2 = new VecInt().push(1).push(3).push(4);
        IVec<BigInteger> expectedCoeffs2 = new Vec<BigInteger>(3,
                BigInteger.ONE);

        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(true),
                eq(BigInteger.valueOf(12))).id("first");

        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits2), eq(expectedCoeffs2), eq(true),
                eq(BigInteger.valueOf(34))).after("first");
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                onConsecutiveCalls(returnValue(1), returnValue(2),
                        returnValue(3), returnValue(4)));
        try {
            String test = "* Comment \n +32 ~ V2 -64 V3 -123456 V4 >= 12; \n  + V2 +V4 + V6 >= 34 ;";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    public void testEnigmaProblem() {

        // create literals
        IVecInt expectedLits = new VecInt();
        for (int i = 1; i <= 90; i++) {
            expectedLits.push(i);
        }
        // create coeffs
        IVec<BigInteger> expectedCoeffs = new Vec<BigInteger>();
        BigInteger sum = BigInteger.ZERO;
        BigInteger coef;
        int[] values = { 202, -79, 100023, -89810, -9980, 1000, 100, 10000,
                100, -1 };

        for (int j = 0; j < values.length; j++) {
            for (int i = 1; i <= 9; i++) {
                expectedCoeffs.push(coef = BigInteger.valueOf(values[j] * i));
                if (coef.signum() > 0) {
                    sum = sum.add(coef);
                }
            }
        }

        assert expectedLits.size() == expectedCoeffs.size();
        assert expectedCoeffs.last().equals(BigInteger.valueOf(-9));

        // expectations
        mockSolver.expects(once()).method("reset");
        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(true),
                eq(BigInteger.ZERO)).id("first");

        mockSolver.expects(once()).method("addPseudoBoolean").with(
                eq(expectedLits), eq(expectedCoeffs), eq(false),
                eq(BigInteger.ZERO)).after("first");
        mockSolver.expects(atLeastOnce()).method("newVar").will(
                new ReturnCounterStub());
        try {

            String test = "BILANCIO: +202 A1 +404 A2 +606 A3 +808 A4 +1010 A5 +1212 A6 +1414 A7 +1616 A8 +1818 A9 "
                    + "-79 B1 -158 B2 -237 B3 -316 B4 -395 B5 -474 B6 -553 B7 -632 B8 -711 B9 "
                    + " +100023 C1 +200046 C2 +300069 C3 +400092 C4 +500115 C5 +600138 C6 +700161 C7 +800184 C8 +900207 C9 "
                    + " -89810 D1 -179620 D2 -269430 D3 -359240 D4 -449050 D5 -538860 D6 -628670 D7 -718480 D8 -808290 D9 "
                    + " -9980 E1 -19960 E2 -29940 E3 -39920 E4 -49900 E5 -59880 E6 -69860 E7 -79840 E8 -89820 E9 "
                    + " +1000 F1 +2000 F2 +3000 F3 +4000 F4 +5000 F5 +6000 F6 +7000 F7 +8000 F8 +9000 F9 "
                    + " +100 G1 +200 G2 +300 G3 +400 G4 +500 G5 +600 G6 +700 G7 +800 G8 +900 G9 "
                    + " +10000 H1 +20000 H2 +30000 H3 +40000 H4 +50000 H5 +60000 H6 +70000 H7 +80000 H8 +90000 H9 "
                    + " +100 I1 +200 I2 +300 I3 +400 I4 +500 I5 +600 I6 +700 I7 +800 I8 +900 I9 "
                    + " -L1 -2 L2 -3 L3 -4 L4 -5 L5 -6 L6 -7 L7 -8 L8 -9 L9 = 0";
            // ;
            // String test = "* Comment \n +32 - V2 -64 V3 -123456 V4 >= 12; \n
            // + V2 +V4 + V6 >= 34 ;";

            // execute
            parser.parseInstance(new LineNumberReader(new StringReader(test)));
            // verify
            mockSolver.verify();

        } catch (ContradictionException e) {
            fail("Trivialy UNSAT");
        } catch (IOException e) {
            fail("I/O Error");
        } catch (ParseFormatException e) {
            fail("Parsing Error");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        mockSolver = new Mock(ISolver.class);
        parser = new GoodOPBReader((IPBSolver) mockSolver.proxy());

    }

    private Mock mockSolver;

    private GoodOPBReader parser;
}

class ReturnCounterStub implements Stub {
    private int counter = 1;

    public StringBuffer describeTo(StringBuffer buffer) {
        return buffer.append("return <" + counter + "> ");
    }

    public Object invoke(Invocation invocation) throws Throwable {
        return new Integer(counter++);
    }
}