/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
* 
* Based on the pseudo boolean algorithms described in:
* A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
* Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
* Volume 24, Issue 3, March 2005 Page(s): 305 - 317
* 
* and 
* Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
* Framework. Ph.D. Dissertation, University of Oregon.
*******************************************************************************/
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
