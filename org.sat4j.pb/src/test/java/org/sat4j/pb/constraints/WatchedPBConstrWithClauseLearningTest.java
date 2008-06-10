/*
 * Created on 20 mai 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.pb.constraints;

import junit.framework.TestCase;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;

/**
 * @author Propri?taire
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WatchedPBConstrWithClauseLearningTest extends
        AbstractPseudoBooleanAndPigeonHoleTest {

    /**
     * Cr?ation d'un test
     * 
     * @param arg
     *            argument ?ventuel
     */
    public WatchedPBConstrWithClauseLearningTest(String arg) {
        super(arg);
    }

    /**
     * @see TestCase#setUp()
     */
    @Override
    protected IPBSolver createSolver() {
        return SolverFactory.newPBResAllPBWL();
    }

}
