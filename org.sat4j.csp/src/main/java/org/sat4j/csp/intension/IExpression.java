package org.sat4j.csp.intension;

import java.util.Map;
import java.util.Set;

public interface IExpression extends Comparable<IExpression> {
	
	boolean isAndOperator();
	
	Set<String> involvedVars();
	
	IExpression[] operands();
	
	int evaluate(Map<String, Integer> bindings);
	
	int updateEvaluation(Map<String, Integer> bindingsChange);
	
	Map<Integer, Integer> encodeWithTseitin(ICspToSatEncoder solver);
}
