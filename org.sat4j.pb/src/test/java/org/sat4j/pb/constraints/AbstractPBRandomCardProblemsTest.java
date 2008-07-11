package org.sat4j.pb.constraints;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.reader.PBInstanceReader;

public abstract class AbstractPBRandomCardProblemsTest extends
		AbstractRandomCardProblemsTest<IPBSolver> {

	@Override
    protected PBInstanceReader createInstanceReader(final IPBSolver solver){
    	return new PBInstanceReader(solver);
    }
    
    /**
     * 
     */
    public AbstractPBRandomCardProblemsTest(String name) {
        super(name);
    }

}
