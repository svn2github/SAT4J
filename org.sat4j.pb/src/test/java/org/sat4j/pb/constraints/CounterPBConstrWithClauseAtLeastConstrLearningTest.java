package org.sat4j.pb.constraints;

import junit.framework.TestCase;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;

public class CounterPBConstrWithClauseAtLeastConstrLearningTest extends
        AbstractPseudoBooleanAndPigeonHoleTest {
    /**
     * Cr?ation d'un test
     * 
     * @param arg
     *            argument ?ventuel
     */
    public CounterPBConstrWithClauseAtLeastConstrLearningTest(String arg) {
        super(arg);
    }

    /**
     * @see TestCase#setUp()
     */
    @Override
    protected IPBSolver createSolver() {
        return SolverFactory.newMiniOPBClauseAtLeastConstrMax();
    }

}
