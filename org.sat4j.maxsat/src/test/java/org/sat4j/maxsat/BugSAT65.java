package org.sat4j.maxsat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class BugSAT65 {

    @Test
    public void testOneSatisfiedConstraint() throws TimeoutException, ContradictionException {
        WeightedMaxSatDecorator wms = new WeightedMaxSatDecorator(SolverFactory.newDefault());
        wms.newVar(2);
        wms.setExpectedNumberOfClauses(3);
        IVecInt clause = new VecInt().push(-1).push(1);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(-2);
        wms.addSoftClause(clause);
        OptToPBSATAdapter problem = new OptToPBSATAdapter(new PseudoOptDecorator(wms,false,false));
        assertTrue(problem.isSatisfiable());
        assertEquals(BigInteger.ONE,problem.getCurrentObjectiveValue());
    }
    
    @Test
    public void testOneSatisfiedConstraintImplicant() throws TimeoutException, ContradictionException {
        WeightedMaxSatDecorator wms = new WeightedMaxSatDecorator(SolverFactory.newDefault());
        wms.newVar(2);
        wms.setExpectedNumberOfClauses(3);
        IVecInt clause = new VecInt().push(-1).push(1);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(-2);
        wms.addSoftClause(clause);
        OptToPBSATAdapter problem = new OptToPBSATAdapter(new PseudoOptDecorator(wms,false,true));
        assertTrue(problem.isSatisfiable());
        assertEquals(BigInteger.ONE,problem.getCurrentObjectiveValue());
    }
    
    @Test
    public void testTwoSatisfiedConstraint() throws TimeoutException, ContradictionException {
        WeightedMaxSatDecorator wms = new WeightedMaxSatDecorator(SolverFactory.newDefault());
        wms.newVar(2);
        wms.setExpectedNumberOfClauses(4);
        IVecInt clause = new VecInt().push(-1).push(1);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(2).push(-2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(-2);
        wms.addSoftClause(clause);
        OptToPBSATAdapter problem = new OptToPBSATAdapter(new PseudoOptDecorator(wms,false,false));
        assertTrue(problem.isSatisfiable());
        assertEquals(BigInteger.ONE,problem.getCurrentObjectiveValue());
    }
    
    @Test
    public void testTwoSatisfiedConstraintImplicant() throws TimeoutException, ContradictionException {
        WeightedMaxSatDecorator wms = new WeightedMaxSatDecorator(SolverFactory.newDefault());
        wms.newVar(2);
        wms.setExpectedNumberOfClauses(4);
        IVecInt clause = new VecInt().push(-1).push(1);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(2).push(-2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(-2);
        wms.addSoftClause(clause);
        OptToPBSATAdapter problem = new OptToPBSATAdapter(new PseudoOptDecorator(wms,false,true));
        assertTrue(problem.isSatisfiable());
        assertEquals(BigInteger.ONE,problem.getCurrentObjectiveValue());
    }
    
    @Test
    public void testOneSatisfiedConstraintMSTwoImplicant() throws TimeoutException, ContradictionException {
        WeightedMaxSatDecorator wms = new WeightedMaxSatDecorator(SolverFactory.newDefault());
        wms.newVar(2);
        wms.setExpectedNumberOfClauses(4);
        IVecInt clause = new VecInt().push(-1).push(1);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(1);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(-2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(-1);
        wms.addSoftClause(clause);
        OptToPBSATAdapter problem = new OptToPBSATAdapter(new PseudoOptDecorator(wms,false,true));
        assertTrue(problem.isSatisfiable());
        assertEquals(BigInteger.valueOf(2),problem.getCurrentObjectiveValue());
    }
    
    @Test
    public void testOneSatisfiedConstraintMSTwo() throws TimeoutException, ContradictionException {
        WeightedMaxSatDecorator wms = new WeightedMaxSatDecorator(SolverFactory.newDefault());
        wms.newVar(2);
        wms.setExpectedNumberOfClauses(4);
        IVecInt clause = new VecInt().push(-1).push(1);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(1);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(-2);
        wms.addSoftClause(clause);
        clause.clear();
        clause.push(-1);
        wms.addSoftClause(clause);
        OptToPBSATAdapter problem = new OptToPBSATAdapter(new PseudoOptDecorator(wms,false,false));
        assertTrue(problem.isSatisfiable());
        assertEquals(BigInteger.valueOf(2),problem.getCurrentObjectiveValue());
    }
}
