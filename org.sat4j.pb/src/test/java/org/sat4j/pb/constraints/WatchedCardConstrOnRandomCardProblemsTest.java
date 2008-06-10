/*
 * Created on 17 mars 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.pb.constraints;

import junit.framework.TestCase;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.constraints.CardinalityDataStructureYanMin;
import org.sat4j.specs.ISolver;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class WatchedCardConstrOnRandomCardProblemsTest extends
        AbstractRandomCardProblemsTest<ISolver> {

    /**
     * @param arg0
     */
    public WatchedCardConstrOnRandomCardProblemsTest(String arg0) {
        super(arg0);
    }

    /**
     * @see TestCase#setUp()
     */
    @Override
    protected ISolver createSolver() {
        return SolverFactory
                .newMiniLearning(new CardinalityDataStructureYanMin());
    }

}
