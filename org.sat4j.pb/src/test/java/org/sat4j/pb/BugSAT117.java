package org.sat4j.pb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.GateTranslator;
import org.sat4j.tools.ModelIterator;

public class BugSAT117 {

    private ISolver solver;

    @Before
    public void setUp() throws ContradictionException {
        solver = SolverFactory.newEclipseP2().getSolvingEngine();
        IVecInt clause = new VecInt();
        clause.push(1).push(2).push(3);
        solver.addClause(clause);
    }

    @Test
    public void testTimeoutOnConflict() throws ContradictionException,
            TimeoutException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {

        solver.setTimeoutOnConflicts(100);
        assertTrue(solver.isSatisfiable());
        Field field = solver.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("timer");
        field.setAccessible(true);
        assertNull(field.get(solver));
    }

    @Test
    public void testTimeoutOnConflictGlobal() throws ContradictionException,
            TimeoutException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        solver.setTimeoutOnConflicts(100);
        assertTrue(solver.isSatisfiable(true));
        Field field = solver.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("timer");
        field.setAccessible(true);
        assertNull(field.get(solver));
    }

    @Test
    public void testTimeoutSeconds() throws ContradictionException,
            TimeoutException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        solver.setTimeout(10);
        assertTrue(solver.isSatisfiable());
        Field field = solver.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("timer");
        field.setAccessible(true);
        assertNull(field.get(solver));
    }

    @Test
    public void testTimeoutSecondsGlobal() throws ContradictionException,
            TimeoutException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        solver.setTimeout(10);
        assertTrue(solver.isSatisfiable(true));
        Field field = solver.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("timer");
        field.setAccessible(true);
        assertNotNull(field.get(solver));
    }

    @Test
    public void testTimeoutSecondsLoop() throws ContradictionException,
            TimeoutException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {

        for (int i = 0; i < 1000; i++) {
            solver = SolverFactory.newEclipseP2();
            IVecInt clause = new VecInt();
            clause.push(1).push(2).push(3);
            solver.addClause(clause);
            solver.setTimeout(10);
            assertTrue(solver.isSatisfiable());
            Field field = solver.getSolvingEngine().getClass().getSuperclass()
                    .getSuperclass().getDeclaredField("timer");
            field.setAccessible(true);
            assertNull(field.get(solver.getSolvingEngine()));
        }
    }

    @Test
    public void testTimeoutConflictsLoop() throws ContradictionException,
            TimeoutException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {

        for (int i = 0; i < 1000; i++) {
            solver = SolverFactory.newEclipseP2();
            IVecInt clause = new VecInt();
            clause.push(1).push(2).push(3);
            solver.addClause(clause);
            solver.setTimeoutOnConflicts(10);
            assertTrue(solver.isSatisfiable());
            Field field = solver.getSolvingEngine().getClass().getSuperclass()
                    .getSuperclass().getDeclaredField("timer");
            field.setAccessible(true);
            assertNull(field.get(solver.getSolvingEngine()));
        }
    }

    @Test
    public void testFromPaul() {
        // This test will run for 60s, may want to disable it for fast unit
        // testing.
        final ISolver solver = SolverFactory.newLight();
        GateTranslator gateTranslator = new GateTranslator(solver);
        ModelIterator modelIterator = new ModelIterator(solver);

        Date begin = new Date();
        long beginTime = begin.getTime();
        Date now = new Date();
        while (now.getTime() - beginTime < 60000) {
            solve(gateTranslator, modelIterator);
            now = new Date();
            solver.reset();
        }
    }

    private List<List<Integer>> solve(GateTranslator gateTranslator,
            ModelIterator modelIterator) {
        List<List<Integer>> solution = new ArrayList<List<Integer>>();
        solver.newVar(9);

        // (x1 ∨ ¬x5 ∨ x4) ∧ (¬x1 ∨ x5 ∨ x3 ∨ x4)
        try {
            gateTranslator.not(6, 5);
            IVecInt disjuncts1 = new VecInt(new int[] { 1, 6, 4 });
            gateTranslator.or(7, disjuncts1);
            gateTranslator.not(8, 1);
            IVecInt disjuncts2 = new VecInt(new int[] { 8, 5, 3, 4 });
            gateTranslator.or(9, disjuncts2);
            gateTranslator.gateTrue(9);
            while (modelIterator.isSatisfiable()) {
                final int[] model = modelIterator.model();
                List<Integer> modelList = new ArrayList<Integer>(model.length);
                for (int literal : model) {
                    modelList.add(literal);
                }
                solution.add(modelList);
            }
        } catch (ContradictionException ex) {
            ex.printStackTrace();
        } catch (TimeoutException ex) {
            ex.printStackTrace();
        } finally {
            gateTranslator.reset();
            solver.reset();
            modelIterator.reset();
        }

        return solution;
    }
}
