/*
 * Created on 2 avr. 2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j;

import junit.framework.TestCase;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.Minimal4CardinalityModel;
import org.sat4j.tools.Minimal4InclusionModel;
import org.sat4j.tools.ModelIterator;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ModelIteratorTest extends TestCase {
    /**
     * Constructor for ModelIteratorTest.
     * 
     * @param arg0
     */
    public ModelIteratorTest(String arg0) {
        super(arg0);
    }

    public void testModelIterator() {
        try {
            ISolver solver = new ModelIterator(SolverFactory.newMiniLearning());
            solver.newVar(3);
            IVecInt clause = new VecInt();
            clause.push(1);
            clause.push(2);
            clause.push(3);
            solver.addClause(clause);
            clause.clear();
            clause.push(-1);
            clause.push(-2);
            clause.push(-3);
            solver.addClause(clause);
            int counter = 0;
            while (solver.isSatisfiable()) {
                solver.model();
                counter++;
            }
            assertEquals(6, counter);
        } catch (ContradictionException e) {
            fail();
        } catch (TimeoutException e) {
            fail();
        }
    }

    // Is not Implemented Yet. We need a Backup/Restore solution to do so.
    // public void testIncMinModel() {
    // try {
    // ISolver solver = new ModelIterator(new
    // Minimal4InclusionModel(SolverFactory.newMiniLearning()));
    // solver.newVar(3);
    // VecInt clause = new VecInt();
    // clause.push(1);
    // clause.push(2);
    // clause.push(3);
    // solver.addClause(clause);
    // clause.clear();
    // clause.push(-1);
    // clause.push(-2);
    // clause.push(-3);
    // solver.addClause(clause);
    // int counter = 0;
    // while (solver.isSatisfiable()) {
    // int[] model = solver.model();
    // counter++;
    // }
    // assertEquals(3, counter);
    // } catch (ContradictionException e) {
    // fail();
    // } catch (TimeoutException e) {
    // fail();
    // }
    // }
    //    
    // public void testCardMinModel() {
    // try {
    // ISolver solver = new ModelIterator(new
    // Minimal4CardinalityModel(SolverFactory.newMiniLearning()));
    // solver.newVar(3);
    // VecInt clause = new VecInt();
    // clause.push(1);
    // clause.push(2);
    // clause.push(3);
    // solver.addClause(clause);
    // clause.clear();
    // clause.push(-1);
    // clause.push(-2);
    // clause.push(-3);
    // solver.addClause(clause);
    // int counter = 0;
    // while (solver.isSatisfiable()) {
    // int[] model = solver.model();
    // counter++;
    // }
    // assertEquals(3, counter);
    // } catch (ContradictionException e) {
    // fail();
    // } catch (TimeoutException e) {
    // fail();
    // }
    // }

    public void testCardModel() {
        try {
            ISolver solver = new Minimal4CardinalityModel(SolverFactory
                    .newMiniLearning());
            solver.newVar(3);
            IVecInt clause = new VecInt();
            clause.push(1);
            clause.push(-2);
            clause.push(3);
            solver.addClause(clause);
            clause.clear();
            clause.push(-1);
            clause.push(2);
            clause.push(-3);
            solver.addClause(clause);
            int counter = 0;
            while (solver.isSatisfiable() && (counter < 10)) {
                solver.model();
                counter++;
            }
            assertEquals(10, counter);
        } catch (ContradictionException e) {
            fail();
        } catch (TimeoutException e) {
            fail();
        }
    }

    public void testIncModel() {
        try {
            ISolver solver = new Minimal4InclusionModel(SolverFactory
                    .newMiniLearning());
            solver.newVar(3);
            IVecInt clause = new VecInt();
            clause.push(1);
            clause.push(-2);
            clause.push(3);
            solver.addClause(clause);
            clause.clear();
            clause.push(-1);
            clause.push(2);
            clause.push(-3);
            solver.addClause(clause);
            int counter = 0;
            while (solver.isSatisfiable() && (counter < 10)) {
                solver.model();
                counter++;
            }
            assertEquals(10, counter);
        } catch (ContradictionException e) {
            fail();
        } catch (TimeoutException e) {
            fail();
        }
    }

    public void testIsSatisfiableVecInt() {
        try {
            ISolver solver = SolverFactory.newMiniLearning();
            solver.newVar(3);
            IVecInt clause = new VecInt();
            clause.push(1);
            clause.push(2);
            clause.push(3);
            solver.addClause(clause);
            clause.clear();
            clause.push(-1);
            clause.push(-2);
            clause.push(-3);
            solver.addClause(clause);
            assertTrue(solver.isSatisfiable());
            IVecInt cube = new VecInt();
            cube.push(1);
            assertTrue(solver.isSatisfiable(cube));
            printModel(solver.model());
            cube.push(2);
            assertEquals(2, cube.size());
            assertTrue(solver.isSatisfiable(cube));
            printModel(solver.model());
            cube.push(-3);
            assertEquals(3, cube.size());
            assertTrue(solver.isSatisfiable(cube));
            printModel(solver.model());
            cube.pop();
            cube.push(3);
            assertEquals(3, cube.size());
            System.out.println(" cube " + cube);
            boolean sat = solver.isSatisfiable(cube);
            if (sat) {
                printModel(solver.model());
            }
            assertFalse(sat);
        } catch (ContradictionException e) {
            fail();
        } catch (TimeoutException e) {
            fail();
        }
    }

    private void printModel(int[] model) {
        for (int i = 0; i < model.length; i++) {
            System.out.print(model[i] + " ");
        }
        System.out.println();
    }
}