package org.sat4j.csp.intension;

import java.util.Map;

import org.xcsp.parser.entries.XVariables.XVarInteger;

public interface ICspToSatEncoder {

	int[] getCspVarDomain(String var);

	int getSolverVar(String var, Integer value);

	Integer newSatSolverVar();
	
	Map<Integer, String> getMapping();
	
	void newCspVar(XVarInteger var, int[] dom);
	
	void newCspVar(XVarInteger var, int minDom, int maxDom);
	
	boolean addClause(int[] clause);

}
