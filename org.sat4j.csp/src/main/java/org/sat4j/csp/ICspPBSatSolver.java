package org.sat4j.csp;

import org.sat4j.pb.IPBSolver;

public interface ICspPBSatSolver extends IPBSolver {
	
	boolean shouldOnlyDisplayEncoding();
	
	void setShouldOnlyDisplayEncoding(boolean b);

}
