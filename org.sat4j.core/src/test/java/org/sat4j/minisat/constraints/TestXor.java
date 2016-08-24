package org.sat4j.minisat.constraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.constraints.xor.Xor;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class TestXor {

    private Solver<?> solver;

    @Before
    public void setUp() {
        solver = (Solver<?>) SolverFactory.newDefault();
    }

    @Test
    public void twoOppositeParity() throws TimeoutException {
        solver.newVar(5);
        IVecInt lits = new VecInt(new int[] { 1, 2, 3, 4, 5 });
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits).toArray(), true,
                solver.getVocabulary()));
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits).toArray(), false,
                solver.getVocabulary()));
        assertFalse(solver.isSatisfiable());
    }

    @Test
    public void oneParityAndOneCard()
            throws TimeoutException, ContradictionException {
        solver.newVar(5);
        IVecInt lits = new VecInt(new int[] { 1, 2, 3, 4, 5 });
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits).toArray(), true,
                solver.getVocabulary()));
        solver.addAtMost(lits, 2);
        assertTrue(solver.isSatisfiable());
        System.out.println(new VecInt(solver.model()));
    }

    @Test
    public void clauseParityAndCard()
            throws TimeoutException, ContradictionException {
        solver.newVar(5);
        IVecInt clause = new VecInt(new int[] { -1, -2, -3 });
        solver.addClause(clause);
        clause.clear();
        clause.push(-4).push(-5);
        solver.addClause(clause);
        clause.clear();
        clause.push(-1).push(-5);
        solver.addClause(clause);
        IVecInt lits = new VecInt(new int[] { 1, 2, 3, 4, 5 });
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits).toArray(), false,
                solver.getVocabulary()));
        solver.addAtMost(lits, 2);
        assertTrue(solver.isSatisfiable());
        System.out.println(new VecInt(solver.model()));
    }
}
