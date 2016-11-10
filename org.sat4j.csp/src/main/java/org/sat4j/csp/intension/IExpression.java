package org.sat4j.csp.intension;

import java.util.Set;

public interface IExpression extends Comparable<IExpression> {
	
	String typeAsString();
	
	Set<String> getInvolvedVars();
	
	IExpression[] getOperands();
}
