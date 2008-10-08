package org.sat4j.tools.xplain;

import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public interface XplainStrategy {

	public IVecInt explain(ISolver solver, int nbnewvar, int nborigvars,IVec<IConstr> constrs, IVecInt assumps) throws TimeoutException;
}
