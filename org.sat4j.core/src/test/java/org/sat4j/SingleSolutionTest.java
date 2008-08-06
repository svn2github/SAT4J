package org.sat4j;

import junit.framework.TestCase;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SingleSolutionDetector;

public class SingleSolutionTest extends TestCase {

    public SingleSolutionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        solver = SolverFactory.newMiniSAT();
        detector = new SingleSolutionDetector(solver);
        detector.newVar(3);
    }

    /*
     * Test method for
     * 'org.sat4j.tools.SingleSolutionDetector.hasASingleSolution()'
     */
    public void testHasASingleSolution() throws ContradictionException,
            TimeoutException {
        IVecInt clause = new VecInt();
        clause.push(1).push(2);
        detector.addClause(clause);
        clause.clear();
        clause.push(-1).push(-2);
        detector.addClause(clause);
        assertTrue(detector.isSatisfiable());
        assertFalse(detector.hasASingleSolution());
    }

    /*
     * Test method for
     * 'org.sat4j.tools.SingleSolutionDetector.hasASingleSolution()'
     */
    public void testHasNoSingleSolution() throws ContradictionException,
            TimeoutException {
        IVecInt clause = new VecInt();
        clause.push(1).push(2);
        detector.addClause(clause);
        clause.clear();
        clause.push(-1).push(-2);
        detector.addClause(clause);
        assertTrue(detector.isSatisfiable());
        clause.clear();
        clause.push(-1).push(2);
        detector.addClause(clause);
        assertTrue(detector.isSatisfiable());
        assertTrue(detector.hasASingleSolution());
        clause.clear();
        clause.push(1).push(-2);
        detector.addClause(clause);
        assertFalse(detector.isSatisfiable());
        try {
            assertFalse(detector.hasASingleSolution());
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }
    
    /*
     * Test method for
     * 'org.sat4j.tools.SingleSolutionDetector.hasASingleSolution()'
     */
    public void testHasNoSingleSolutionUNSAT() throws ContradictionException,
            TimeoutException {
        IVecInt clause = new VecInt();
        clause.push(1).push(2);
        detector.addClause(clause);
        clause.clear();
        clause.push(-1).push(-2);
        detector.addClause(clause);
        assertTrue(detector.isSatisfiable());
        clause.clear();
        clause.push(-1).push(2);
        detector.addClause(clause);
        assertTrue(detector.isSatisfiable());
        clause.clear();
        clause.push(1).push(-2);
        detector.addClause(clause);
        assertFalse(detector.isSatisfiable());
        try {
            assertFalse(detector.hasASingleSolution());
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }
    /*
     * Test method for
     * 'org.sat4j.tools.SingleSolutionDetector.hasASingleSolution(IVecInt)'
     */
    public void testHasASingleSolutionIVecInt() throws ContradictionException,
            TimeoutException {
        IVecInt clause = new VecInt();
        clause.push(1).push(2);
        detector.addClause(clause);
        IVecInt assumptions = new VecInt();
        assumptions.push(1);
        assertTrue(detector.isSatisfiable(assumptions));
        assertFalse(detector.hasASingleSolution(assumptions));
        clause.clear();
        clause.push(-1).push(2);
        detector.addClause(clause);
        assertTrue(detector.isSatisfiable(assumptions));
        assertTrue(detector.hasASingleSolution(assumptions));
        clause.clear();
        clause.push(-1).push(-2);
        detector.addClause(clause);
        assertFalse(detector.isSatisfiable(assumptions));
        try {
            assertFalse(detector.hasASingleSolution(assumptions));
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }

    private ISolver solver;

    private SingleSolutionDetector detector;
}
