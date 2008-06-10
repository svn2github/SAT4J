package org.sat4j.pb.constraints;

import junit.framework.TestCase;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;

public class CounterPBConstrWithClauseCardConstrLearningTest extends
        CounterPBConstrWithPBConstrLearningTest {

    public CounterPBConstrWithClauseCardConstrLearningTest(String arg) {
        super(arg);
        // TODO Auto-generated constructor stub
    }

    /**
     * @see TestCase#setUp()
     */
    @Override
    protected IPBSolver createSolver() {
        return SolverFactory.newPBCPMixedConstraints();
    }

}
