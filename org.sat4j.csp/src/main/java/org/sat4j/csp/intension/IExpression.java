package org.sat4j.csp.intension;

import java.util.Map;
import java.util.Set;

public interface IExpression extends Comparable<IExpression> {
	
	String typeAsString();
	
	Set<String> involvedVars();
	
	IExpression[] getOperands();
	
	Map<Integer, Integer> encodeWithTseitin(ICspToSatEncoder solver);
}
