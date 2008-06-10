package org.sat4j.minisat.datatype;

import junit.framework.TestCase;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.InstanceReader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class TestAtMost extends TestCase {

    private static final String PREFIX = System.getProperty("test.prefix");

    private ISolver solver;

    private InstanceReader reader;

    public TestAtMost(String s) {
        super(s);
    }

    @Override
    protected void setUp() {
        solver = SolverFactory.newMiniSAT();
        reader = new InstanceReader(solver);
    }

    public void testUnEssaiSat() throws TimeoutException {
        try {
            solver.reset();
            // 3 variables
            solver.newVar(3);
            // premi?re contrainte de cardinalit?
            // a V b V c >= 2
            IVecInt vec = new VecInt();
            vec.push(1);
            vec.push(2);
            vec.push(3);
            solver.addAtLeast(vec, 2);

            // Deuxi?me contrainte de cardinalit?
            // a V non b >= 2
            vec = new VecInt();
            vec.push(1);
            vec.push(-2);
            solver.addAtLeast(vec, 2);

            boolean isSat = solver.isSatisfiable();

            assertTrue(isSat);
        } catch (ContradictionException e) {
            assertTrue(false);
        }
    }

    public void testUnEssaiUnsat() throws TimeoutException {
        try {
            solver.reset();
            // 2 variables
            solver.newVar(2);
            // premi?re contrainte de cardinalit?
            // a + not b >= 1
            IVecInt vecLit = new VecInt();
            vecLit.push(1);
            vecLit.push(-2);
            solver.addAtLeast(vecLit, 1);

            // Deuxi?me contrainte de cardinalit?
            // not a >= 1
            vecLit = new VecInt();
            vecLit.push(-1);
            solver.addAtLeast(vecLit, 1);

            // Troisi?me contrainte de cardinalit?
            // b >= 1
            vecLit = new VecInt();
            vecLit.push(2);
            solver.addAtLeast(vecLit, 1);

            boolean isSat = solver.isSatisfiable();

            assertFalse(isSat);
        } catch (ContradictionException e) {
            assertTrue(false);
        }

    }

    public void test2Sat() throws TimeoutException {
        try {
            solver.reset();
            // 2 variables
            solver.newVar(2);
            // premi?re contrainte de cardinalit?
            // a + not b <=3
            IVecInt vecLit = new VecInt();
            vecLit.push(1);
            vecLit.push(-2);
            solver.addAtMost(vecLit, 3);

            boolean isSat = solver.isSatisfiable();

            assertTrue(isSat);
        } catch (ContradictionException e) {
            assertTrue(false);
        }
    }

    public void test4Unsat() throws TimeoutException {
        try {
            solver.reset();
            // 2 variables
            solver.newVar(2);
            // premi?re contrainte de cardinalit?
            // a + not b >=3
            IVecInt vecLit = new VecInt();
            vecLit.push(1);
            vecLit.push(-2);

            solver.addAtLeast(vecLit, 3);

            solver.isSatisfiable();

            fail();
        } catch (ContradictionException e) {
        }
    }

    public void test3Unsat() throws TimeoutException {
        try {
            solver.reset();
            // 2 variables
            solver.newVar(2);
            // contrainte de cardinalit?
            // a >= 1
            IVecInt vecLit = new VecInt();
            vecLit.push(1);
            solver.addAtLeast(vecLit, 1);
            // contrainte de cardinalit?
            // b >= 1
            vecLit = new VecInt();
            vecLit.push(2);
            solver.addAtLeast(vecLit, 1);
            // contrainte de cardinalit?
            // a + b <=1
            vecLit = new VecInt();
            vecLit.push(1);
            vecLit.push(2);
            solver.addAtMost(vecLit, 1);

            solver.isSatisfiable();

            fail();
        } catch (ContradictionException e) {
        }

    }

    public void test5Sat() throws TimeoutException {
        try {
            solver.reset();
            // 2 variables
            solver.newVar(2);
            // premi\x{FFFD}re contrainte de cardinalit\x{FFFD}
            // a + not b <=0
            IVecInt vecLit = new VecInt();
            vecLit.push(1);
            vecLit.push(-2);

            solver.addAtMost(vecLit, 0);

            boolean isSat = solver.isSatisfiable();

            assertTrue(isSat);
        } catch (ContradictionException e) {
            assertTrue(false);
        }
    }

    // public void testRadar1() throws TimeoutException {
    // solver.reset();
    // try {
    // URL url = DimacsReader.class
    // .getResource("benchs/10_10_4.5_0.95_100.dimacs");
    // System.out.println(url);
    //
    // solver.parseInstance(url.getFile());
    // } catch (Exception e) {
    // System.out.println(e.getMessage());
    // e.printStackTrace();
    // fail();
    // }
    // System.out.println("fin Radar 1");
    // assertTrue(solver.isSatisfiable());
    // }

    public void testDimacs1() throws TimeoutException {
        solver.reset();
        try {
            reader.parseInstance(PREFIX + "test1bis.dimacs");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail();
        }
        System.out.println("fin Dimacs 1");
        assertTrue(solver.isSatisfiable());
    }

} // TestAtMost
