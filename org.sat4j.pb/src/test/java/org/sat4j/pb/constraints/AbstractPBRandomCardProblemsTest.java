package org.sat4j.pb.constraints;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.InstanceReader;

public abstract class AbstractPBRandomCardProblemsTest extends
		AbstractRandomCardProblemsTest<IPBSolver> {

	@Override
    protected InstanceReader createInstanceReader(IPBSolver solver){
    	return new PBInstanceReader(solver);
    }
    
    /**
     * 
     */
    public AbstractPBRandomCardProblemsTest(String name) {
        super(name);
    }

}
