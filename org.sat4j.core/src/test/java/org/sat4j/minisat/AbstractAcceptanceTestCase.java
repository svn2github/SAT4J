/*
 * Created on 1 sept. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public abstract class AbstractAcceptanceTestCase<T extends ISolver> extends TestCase {

    /**
     * 
     */
    public AbstractAcceptanceTestCase() {
        super();
    }

    /**
     * @param arg0
     */
    public AbstractAcceptanceTestCase(String arg0) {
        super(arg0);
    }

    protected T solver;

    protected InstanceReader reader;

    protected abstract T createSolver();

    /**
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() {
        solver = createSolver();
        reader = createInstanceReader(solver);
    }

    
    protected InstanceReader createInstanceReader(T solver){
    	return new InstanceReader(solver);
    }
    
    @Override
    protected void tearDown() {
        solver.reset();
    }

    protected boolean solveInstance(String filename)
            throws FileNotFoundException, ParseFormatException, IOException {
        try {
            reader.parseInstance(filename);
            solver.setTimeout(300); // set timeout to 5mn
            return solver.isSatisfiable();
        } catch (ContradictionException ce) {
            return false;
        } catch (TimeoutException ce) {
            fail("Timeout: need more time to complete!");
            return false;
        }
    }

}
