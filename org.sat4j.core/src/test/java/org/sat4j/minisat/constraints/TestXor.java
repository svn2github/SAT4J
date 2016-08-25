package org.sat4j.minisat.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.constraints.xor.Xor;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.restarts.NoRestarts;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

public class TestXor {

    private Solver<?> solver;

    @Before
    public void setUp() {
        solver = (Solver<?>) SolverFactory.newDefault();
        solver.setRestartStrategy(new NoRestarts());
    }

    @Test
    public void twoOppositeParity() throws TimeoutException {
        solver.newVar(5);
        IVecInt lits = new VecInt(new int[] { 1, 2, 3, 4, 5 });
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits), true, solver.getVocabulary()));
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits), false, solver.getVocabulary()));
        assertFalse(solver.isSatisfiable());
    }

    @Test
    public void twoLargeOppositeParity() throws TimeoutException {
        int size = 16;
        solver.newVar(size);
        IVecInt lits = new VecInt(size);
        for (int i = 0; i < size; i++) {
            lits.push(i + 1);
        }
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits), true, solver.getVocabulary()));
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits), false, solver.getVocabulary()));
        assertFalse(solver.isSatisfiable());
        solver.printStat(new PrintWriter(System.out, true));
    }

    @Test
    public void oneParityAndOneCard()
            throws TimeoutException, ContradictionException {
        solver.newVar(5);
        IVecInt lits = new VecInt(new int[] { 1, 2, 3, 4, 5 });
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits), true, solver.getVocabulary()));
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
                solver.dimacs2internal(lits), false, solver.getVocabulary()));
        solver.addAtMost(lits, 2);
        assertTrue(solver.isSatisfiable());
        System.out.println(new VecInt(solver.model()));
    }

    @Test
    public void checkNumberOfSolution()
            throws TimeoutException, ContradictionException {
        solver.newVar(3);
        IVecInt lits = new VecInt(new int[] { 1, 2, 3 });
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits), true, solver.getVocabulary()));
        ModelIterator iterator = new ModelIterator(solver);
        while (iterator.isSatisfiable()) {
            iterator.model(); // to go to the next model
        }
        assertEquals(4, iterator.numberOfModelsFoundSoFar());
        System.out.println(new VecInt(solver.model()));
    }

    @Test
    public void checkNumberOfSolutionWithClauses()
            throws TimeoutException, ContradictionException {
        solver.newVar(3);
        IVecInt lits = new VecInt(new int[] { 1, 2, 3 });
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits), true, solver.getVocabulary()));
        IVecInt clause = new VecInt(new int[] { 1, 2, 3 });
        solver.addClause(clause);
        clause.clear();
        clause.push(-1).push(-3);
        solver.addClause(clause);
        ModelIterator iterator = new ModelIterator(solver);
        while (iterator.isSatisfiable()) {
            iterator.model(); // to go to the next model
        }
        assertEquals(2, iterator.numberOfModelsFoundSoFar());
        System.out.println(new VecInt(solver.model()));
    }

    @Test
    public void checkNumberOfSolutionWithCards()
            throws TimeoutException, ContradictionException {
        solver.newVar(3);
        IVecInt lits = new VecInt(new int[] { 1, 2, 3 });
        solver.addConstr(Xor.createParityConstraint(
                solver.dimacs2internal(lits), true, solver.getVocabulary()));
        solver.addAtLeast(lits, 1);
        ModelIterator iterator = new ModelIterator(solver);
        while (iterator.isSatisfiable()) {
            iterator.model(); // to go to the next model
        }
        assertEquals(3, iterator.numberOfModelsFoundSoFar());
        System.out.println(new VecInt(solver.model()));
    }
}
