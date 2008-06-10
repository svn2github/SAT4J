package org.sat4j.pb.constraints;

import junit.framework.TestCase;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;

public class CounterPBWithClauseCardConstrLearningReduceToClauseTest extends
        CounterPBConstrWithCBClauseCardConstrLearningTest {

    public CounterPBWithClauseCardConstrLearningReduceToClauseTest(String arg) {
        super(arg);
    }

    /**
     * @see TestCase#setUp()
     */
    @Override
    protected IPBSolver createSolver() {
        return SolverFactory.newPBCPMixedConstraintsReduceToClause();
    }

}
