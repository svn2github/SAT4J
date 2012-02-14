package org.sat4j.pb.core;

import org.sat4j.minisat.core.ICDCL;
import org.sat4j.pb.IPBSolver;

public interface IPBCDCLSolver<D extends PBDataStructureFactory> extends
		IPBSolver, ICDCL<D> {

}
