package org.sat4j.tools;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IGroupSolver;
import org.sat4j.specs.IVecInt;

public class TestAllMUSesGroupTest {
    private IGroupSolver solver;

    @Before
    public void setUp() throws Exception {
        this.solver = AllMUSesGroup.getSolverInstance();
    }

    @Test
    public void testSimpleCase() {
        IVecInt c1 = new VecInt();
        IVecInt c2 = new VecInt();
        IVecInt c3 = new VecInt();
        IVecInt c4 = new VecInt();
        IVecInt c5 = new VecInt();

        c1.push(1);
        c2.push(2);
        c3.push(-1).push(-2);
        c4.push(3);
        c5.push(-3);

        this.solver.newVar(3);

        try {
            this.solver.addClause(c1, 1);
            this.solver.addClause(c2, 2);
            this.solver.addClause(c3, 3);
            this.solver.addClause(c4, 4);
            this.solver.addClause(c5, 5);

            List<IVecInt> muses = AllMUSes.computeAllMUSes(this.solver);

            assertEquals(muses.size(), 2);

        } catch (ContradictionException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testVerySimpleCase() {
        IVecInt c1 = new VecInt();
        IVecInt c2 = new VecInt();

        c1.push(1);
        c2.push(-1);

        this.solver.newVar(1);

        try {
            this.solver.addClause(c1, 1);
            this.solver.addClause(c2, 2);

            List<IVecInt> muses = AllMUSes.computeAllMUSes(this.solver);

            assertEquals(muses.size(), 1);

        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }
}
