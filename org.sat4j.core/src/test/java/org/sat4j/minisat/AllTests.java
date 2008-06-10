/*
 * Created on 15 juin 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.sat4j.minisat");
        // $JUnit-BEGIN$
        suite.addTestSuite(TestAssertion.class);
        suite.addTestSuite(M2MiniActiveLearningTest.class);
        suite.addTestSuite(VarOrderTest.class);
        suite.addTestSuite(M2MiniLearning23Test.class);
        suite.addTestSuite(M2MiniLearning2Test.class);
        suite.addTestSuite(M2MiniSATTest.class);
        suite.addTestSuite(TestsFonctionnels.class);
        suite.addTestSuite(M2MiniLearningTest.class);
        suite.addTestSuite(M2RelsatTest.class);
        suite.addTestSuite(M2CardMinYannTest.class);
        suite.addTestSuite(M2AtLeastTest.class);
        suite.addTestSuite(M2BackjumpingTest.class);
        suite.addTestSuite(M2MiniLearningCBTest.class);
        suite.addTest(GenericM2Test.suite());
        suite.addTestSuite(M2CardMaxYannTest.class);
        // $JUnit-END$
        return suite;
    }
}