package org.sat4j.tools.xplain;

import java.util.Collection;

import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public interface XplainStrategy {

	public IVecInt explain(ISolver solver, Collection<Integer> constrsIds, IVecInt assumps) throws TimeoutException;
}
