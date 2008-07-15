/*
 * Created on 6 juil. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat;

import junit.framework.TestSuite;

import org.sat4j.specs.ISolver;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class GenericM2Test extends AbstractM2Test<ISolver> {

    private String solvername;

    private static final SolverFactory factory = SolverFactory.instance();

    /**
     * @param arg0
     */
    public GenericM2Test(String arg0) {
        setName("AbstractM2Test" + arg0);
        solvername = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected ISolver createSolver() {
        return factory.createSolverByName(solvername);
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        String[] names = factory.solverNames();
        for (int i = 0; i < names.length; i++) {
            suite.addTest(new GenericM2Test(names[i]));
        }
        return suite;
    }

    @Override
    protected void runTest() throws Throwable {
        assertFalse(solveInstance(PREFIX + "pigeons/hole6.cnf"));
    }
}
