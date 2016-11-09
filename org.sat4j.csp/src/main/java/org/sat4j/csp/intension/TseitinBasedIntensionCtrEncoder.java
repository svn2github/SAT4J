package org.sat4j.csp.intension;

import java.util.Map;

public class TseitinBasedIntensionCtrEncoder implements IIntensionCtrEncoder {
	
	private final ICspToSatEncoder solver;

	public TseitinBasedIntensionCtrEncoder(ICspToSatEncoder solver) {
		this.solver = solver;
	}
	
	public boolean encode(String strExpression) {
		Parser parser = new Parser(strExpression);
		parser.parse();
		final IExpression expression = parser.getExpression();
		return encodeExpression(expression);
	}

	private boolean encodeExpression(IExpression expression) {
		Map<Integer, Integer> map = expression.encodeWithTseitin(this.solver);
		for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
			this.solver.addClause(new int[]{entry.getKey() == 0 ? -entry.getValue() : entry.getValue()});
		}
		return false;
	}

	@Override
	public int[] getCspVarDomain(String var) {
		return this.solver.getCspVarDomain(var);
	}

	@Override
	public int getSolverVar(String var, Integer value) {
		return this.solver.getSolverVar(var, value);
	}

	@Override
	public boolean addClause(int[] clause) {
		return this.solver.addClause(clause);
	}

	@Override
	public Integer newVar() {
		return this.solver.newVar();
	}

}
