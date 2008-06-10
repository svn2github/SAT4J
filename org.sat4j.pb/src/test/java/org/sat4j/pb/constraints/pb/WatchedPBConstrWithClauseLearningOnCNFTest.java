/*
 * Created on 17 mars 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.AbstractM2Test;
import org.sat4j.minisat.core.ILits;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.core.PBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class WatchedPBConstrWithClauseLearningOnCNFTest extends AbstractM2Test<IPBSolver> {

    /**
     * @param arg0
     */
    public WatchedPBConstrWithClauseLearningOnCNFTest(String arg0) {
        super(arg0);
    }

    /**
     * @see TestCase#setUp()
     */
    @Override
    protected IPBSolver createSolver() {
        mysolver = SolverFactory.newPBResAllPBWL();
        return mysolver;
    }

    private PBSolver<ILits> mysolver;
    
    public void testPropagation() {
        solver.newVar(3);
        IVecInt lits = new VecInt();
        lits.push(1);
        lits.push(2);
        lits.push(3);
        IVec<BigInteger> coefs = new Vec<BigInteger>();
        coefs.growTo(lits.size(), BigInteger.ONE);
        try {
            mysolver.addPseudoBoolean(lits, coefs, true, BigInteger.ONE);
            mysolver.assume(3); // assume -1
            assertNull(mysolver.propagate());
            assertTrue(mysolver.getVocabulary().isSatisfied(3));
            mysolver.assume(5); // assume -2
            assertNull(mysolver.propagate());
            assertTrue(mysolver.getVocabulary().isSatisfied(5));
            assertTrue(mysolver.getVocabulary().isSatisfied(6));
        } catch (ContradictionException e) {
            e.printStackTrace();
            fail();
        }

    }

    public void testPropagation2() {
        solver.newVar(3);
        IVecInt lits = new VecInt();
        lits.push(1);
        lits.push(2);
        lits.push(3);
        IVec<BigInteger> coefs = new Vec<BigInteger>();
        coefs.growTo(lits.size(), BigInteger.ONE);
        try {
            mysolver.addPseudoBoolean(lits, coefs, true, BigInteger.valueOf(4));
            fail();
        } catch (ContradictionException e) {
            // everything is fine here.
        }
    }

}
