package org.sat4j.maxsat;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class BugSAT84 {

    @Test
    public void test() throws ContradictionException, TimeoutException {
        WeightedMaxSatDecorator wms = new WeightedMaxSatDecorator(SolverFactory.newDefault());
        wms.newVar(401432);
        wms.setExpectedNumberOfClauses(1);
        wms.setTopWeight(BigInteger.valueOf(729));
        IVecInt clause = new VecInt().push(-378671).push(59559);
        wms.addSoftClause(BigInteger.valueOf(729),clause);
        IProblem problem = new OptToPBSATAdapter(new PseudoOptDecorator(wms,false,true));
        assertTrue(problem.isSatisfiable());
        
    }

}
