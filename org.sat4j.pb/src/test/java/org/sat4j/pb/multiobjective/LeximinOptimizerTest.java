package org.sat4j.pb.multiobjective;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class LeximinOptimizerTest {

    private LeximinOptimizer solver = null;

    @Before
    public void setUp() {
        this.solver = new LeximinOptimizer(SolverFactory.newDefault());
    }

    @Test
    public void test1() throws ContradictionException, TimeoutException {
        this.solver.addClause(new VecInt(new int[] { -1, -2 }));
        this.solver.addClause(new VecInt(new int[] { -2, 4, 5 }));
        this.solver.addClause(new VecInt(new int[] { -3, -4 }));
        this.solver.addClause(new VecInt(new int[] { -3, -5 }));
        this.solver.addClause(new VecInt(new int[] { -4, -5 }));
        this.solver.addClause(new VecInt(new int[] { 2 }));
        this.solver.addClause(new VecInt(new int[] { 3, 4, 6 }));
        this.solver.addClause(new VecInt(new int[] { -3, -6 }));
        this.solver.addClause(new VecInt(new int[] { -4, -6 }));
        this.solver.addObjectiveFunction(new ObjectiveFunction(new VecInt(
                new int[] { -1, 6 }), new Vec<BigInteger>(new BigInteger[] {
                BigInteger.ONE, BigInteger.ONE })));
        this.solver.addObjectiveFunction(new ObjectiveFunction(new VecInt(
                new int[] { 4 }), new Vec<BigInteger>(
                new BigInteger[] { BigInteger.ONE })));
        assertEquals(true, this.solver.isSatisfiable());
        int[] lastModel = null;
        while (this.solver.admitABetterSolution()) {
            lastModel = this.solver.model();
            this.solver.getObjectiveFunction().calculateDegree(this.solver)
                    .intValue();
            this.solver.discardCurrentSolution();
        }
        assertEquals(-1, lastModel[0]);
        assertEquals(2, lastModel[1]);
        assertEquals(-3, lastModel[2]);
        assertEquals(4, lastModel[3]);
        assertEquals(-5, lastModel[4]);
    }
}
