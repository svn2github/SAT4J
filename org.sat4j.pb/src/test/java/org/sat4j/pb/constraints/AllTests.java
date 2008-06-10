package org.sat4j.pb.constraints;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for org.sat4j.pb.constraints");
        // $JUnit-BEGIN$
        // suite.addTestSuite(LitTest.class);
        // suite.addTestSuite(AbstractPigeonHoleWithCardinalityTest.class);
        suite.addTestSuite(CounterPBConstrClauseImpliedWithClauseCardConstrLearning.class);
        suite.addTestSuite(CounterPBConstrOnRandomCardProblemsTest.class);
        suite.addTestSuite(CounterPBConstrWithCBClauseCardConstrLearningTest.class);
        suite.addTestSuite(CounterPBConstrWithClauseAtLeastConstrLearningTest.class);
        suite.addTestSuite(CounterPBConstrWithClauseCardConstrLearningTest.class);
        suite.addTestSuite(CounterPBConstrWithClauseLearningTest.class);
        suite.addTestSuite(CounterPBConstrWithPBConstrLearningTest.class);
        suite.addTestSuite(CounterPBWithClauseCardConstrLearningReduceToClauseTest.class);
        suite.addTestSuite(PuebloWatchedPbClauseAtLeastConstrWithPBConstrLearningTest.class);
        suite.addTestSuite(PuebloWatchedPBClauseCardConstrWithPBConstrLearningTest.class);
        suite.addTestSuite(PuebloWatchedPBConstrOnRandomCardProblemsTest.class);
        suite.addTestSuite(PuebloWatchedPBConstrWithClauseLearningTest.class);
        suite.addTestSuite(PuebloWatchedPBConstrWithPBConstrLearningTest.class);
        suite.addTestSuite(WatchedPBConstrOnRandomCardProblemsTest.class);
        suite.addTestSuite(WatchedPBConstrWithClauseLearningTest.class);
        suite.addTestSuite(WatchedPBConstrWithPBConstrLearningTest.class);
        // $JUnit-END$
        return suite;
    }

}
