package org.sat4j.tools.xplain;

import java.util.Map;

import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public interface XplainStrategy {

	public IVecInt explain(ISolver solver, Map<Integer, IConstr> constrs,
			IVecInt assumps) throws TimeoutException;
}
