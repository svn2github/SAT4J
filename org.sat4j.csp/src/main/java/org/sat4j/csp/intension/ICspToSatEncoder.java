package org.sat4j.csp.intension;

public interface ICspToSatEncoder {

	int[] getCspVarDomain(String var);

	int getSolverVar(String var, Integer value);

	boolean addClause(int[] clause);

}
