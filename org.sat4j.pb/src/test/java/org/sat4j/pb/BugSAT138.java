package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.pb.reader.OPBReader2012;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class BugSAT138 {

    private OptToPBSATAdapter solver;
    private Reader reader;

    @Before
    public void init() {
        PBSolverHandle handle = new PBSolverHandle(
                new PseudoOptDecorator(SolverFactory.newDefault()));
        solver = new OptToPBSATAdapter(handle);
        reader = new OPBReader2012(handle);
    }

    @Test
    public void testWBO1() throws ContradictionException, TimeoutException,
            ParseFormatException, IOException {
        String wbo = "* #variable= 1 #constraint= 2 #soft= 2 mincost= 2 maxcost= 3 sumcost= 5\nsoft: 6 ;\n [2] +1 x1 >= 1 ;\n[3] -1 x1 >= 0 ;\n";
        reader.parseInstance(new StringBufferInputStream(wbo));
        assertTrue(solver.isSatisfiable());
        IVecInt sol = new VecInt(solver.model());
        assertTrue(sol.contains(-1));
        assertEquals(BigInteger.valueOf(2), solver.getCurrentObjectiveValue());
    }

    @Test
    public void testWBO2() throws ContradictionException, TimeoutException,
            ParseFormatException, IOException {
        String wbo = "* #variable= 2 #constraint= 3 #soft= 2 mincost= 2 maxcost= 3 sumcost= 5\nsoft: 6 ;\n[2] +1 x1 >= 1 ;\n[3] +1 x2 >= 1 ;\n-1 x1 -1 x2 >= -1 ;\n";
        reader.parseInstance(new StringBufferInputStream(wbo));
        assertTrue(solver.isSatisfiable());
        IVecInt sol = new VecInt(solver.model());
        assertTrue(sol.contains(-1));
        assertTrue(sol.contains(2));
        assertEquals(BigInteger.valueOf(2), solver.getCurrentObjectiveValue());
    }

    @Test
    public void testWBO3() throws ContradictionException, TimeoutException,
            ParseFormatException, IOException {
        String wbo = "* #variable= 4 #constraint= 6 #soft= 4 mincost= 2 maxcost= 5 sumcost= 14\nsoft: 6 ;\n[2] +1 x1 >= 1 ;\n[3] +1 x2 >= 1 ;\n[4] +1 x3 >= 1 ;\n[5] +1 x4 >= 1 ;\n-1 x1 -1 x2 >= -1 ;\n-1 x3 -1 x4 >= -1 ;";
        reader.parseInstance(new StringBufferInputStream(wbo));
        assertFalse(solver.isSatisfiable());
    }
}
