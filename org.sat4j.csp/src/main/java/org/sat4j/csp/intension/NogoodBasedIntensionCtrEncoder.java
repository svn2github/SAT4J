package org.sat4j.csp.intension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NogoodBasedIntensionCtrEncoder implements ICspToSatEncoder, IIntensionCtrEncoder {

	private final ICspToSatEncoder solver;

	private final Map<String, int[]> domains = new HashMap<>();

	private final Map<VarExpression, Integer> lastVarExprEvaluation = new HashMap<>();

	private final Map<OperatorExpression, Integer> lastOpExprEvaluation = new HashMap<>();

	public NogoodBasedIntensionCtrEncoder(ICspToSatEncoder solver) {
		this.solver = solver;
	}

	public boolean encode(String strExpression) {
		Parser parser = new Parser(strExpression);
		parser.parse();
		final IExpression expression = parser.getExpression();
		return encodeGlobalExpression(expression);
	}

	private boolean encodeGlobalExpression(IExpression expression) {
		if(expression instanceof OperatorExpression && ((OperatorExpression) expression).getOperator() == EOperator.LOGICAL_AND) {
			return encodeGlobalAndExpression(expression);
		}
		final Set<String> involved = expression.involvedVars();
		extractDomains(involved);
		InstantiationIterator instantiationIterator = new InstantiationIterator(involved, this.domains);
		for(Map<String, Integer> fullInstantiation : instantiationIterator) {
			final Map<String, Integer> bindingsChange = instantiationIterator.getInstantiationUpdate();
			int result = updateEvaluation(expression, bindingsChange);
			if(result == 0) {
				if(encodeNogood(fullInstantiation)) return true;
			}
		}
		return false;
	}
	
	private void extractDomains(Set<String> involved) {
		for(String var : involved) {
			if(domains.get(var) == null) {
				this.domains.put(var, this.solver.getCspVarDomain(var));
			}
		}
	}

	private boolean encodeGlobalAndExpression(IExpression expression) {
		boolean contradictionFound = false;
		for(int i=0; (!contradictionFound) && (i<expression.getOperands().length); ++i) {
			contradictionFound |= encodeGlobalExpression(expression.getOperands()[i]);
		}
		return contradictionFound;
	}
	
	private int updateEvaluation(IExpression expression, final Map<String, Integer> bindingsChange) {
		Method toCall;
		int result;
		try {
			final String methodNameSuffix = expression.typeAsString().substring(0,1).toUpperCase()+expression.typeAsString().substring(1);
			toCall = getClass().getMethod("updateEvaluationFor"+methodNameSuffix, IExpression.class, Map.class);
			result = (int) toCall.invoke(this, expression, bindingsChange);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
		return result;
	}

	public int updateEvaluationForInteger(IExpression expr, final Map<String, Integer> bindingsChange) {
		return ((IntegerExpression) expr).getValue();
	}

	public int updateEvaluationForVar(IExpression iexpr, final Map<String, Integer> bindingsChange) {
		VarExpression expr = (VarExpression) iexpr;
		final Integer value = bindingsChange.get(expr.getVar());
		if(value == null) return this.lastVarExprEvaluation.get(expr);
		this.lastVarExprEvaluation.put(expr, value);
		return value;
	}

	public int updateEvaluationForOperator(IExpression iexpr, final Map<String, Integer> bindingsChange) {
		OperatorExpression expr = (OperatorExpression) iexpr;
		boolean inCache = true;
		for(String var : bindingsChange.keySet()) {
			if(expr.involvedVars().contains(var)) {
				inCache = false;
				break;
			}
		}
		if(inCache) return this.lastOpExprEvaluation.get(expr);
		final int value = updateEvaluation(expr.getOperator(), expr.getOperands(), bindingsChange);
		this.lastOpExprEvaluation.put(expr, value);
		return value;
	}

	public int updateEvaluation(EOperator operator, final IExpression[] operands, final Map<String, Integer> bindingsChange) {
		final int[] childrenEvaluation = new int[operands.length];
		for(int i=0; i<operands.length; ++i) {
			childrenEvaluation[i] = updateEvaluation(operands[i], bindingsChange);
		}
		Method toCall;
		int result;
		try {
			toCall = getClass().getMethod("evaluateOperator"+operator.nameAsString().substring(0,1).toUpperCase()+operator.nameAsString().substring(1), int[].class);
			result = (int) toCall.invoke(this, childrenEvaluation);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
		return result;
	}
	
	public int evaluateOperatorNeg(final int[] v) {
		return -v[0];
	}

	public int evaluateOperatorAbs(final int[] v) {
		return Math.abs(v[0]);
	}

	public int evaluateOperatorAdd(final int[] v) {
		int sum = 0;
		for(int i:v) sum+= i;
		return sum;
	}

	public int evaluateOperatorSub(final int[] v) {
		return v[0]-v[1];
	}

	public int evaluateOperatorMul(final int[] v) {
		int prod=1;
		for(int i:v) prod *= i;
		return prod;
	}

	public int evaluateOperatorDiv(final int[] v) {
		return v[0]/v[1];
	}

	public int evaluateOperatorMod(final int[] v) {
		return v[0]%v[1];
	}

	public int evaluateOperatorSqr(final int[] v) {
		return v[0]*v[0];
	}

	public int evaluateOperatorPow(final int[] v) {
		return internalEvaluate(v[0], v[1]);
	}
	
	private int internalEvaluate(int a, int b) {
		if(b==1) return a;
		int halfPow = internalEvaluate(a, b/2);
		if((b&1)==0) return halfPow*halfPow;
		return a*halfPow*halfPow;
	}

	public int evaluateOperatorMin(final int[] v) {
		int m = Integer.MAX_VALUE;
		for(int i:v) m = Math.min(m, i);
		return m;
	}


	public int evaluateOperatorMax(final int[] v) {
		int m = Integer.MIN_VALUE;
		for(int i:v) m = Math.max(m, i);
		return m;
	}

	public int evaluateOperatorDist(final int[] v) {
		return Math.abs(v[0]-v[1]);
	}


	public int evaluateOperatorLt(final int[] v) {
		return v[0] < v[1] ? 1 : 0;
	}

	public int evaluateOperatorLe(final int[] v) {
		return v[0] <= v[1] ? 1 : 0;
	}

	public int evaluateOperatorGt(final int[] v) {
		return v[0] > v[1] ? 1 : 0;
	}

	public int evaluateOperatorGe(final int[] v) {
		return v[0] >= v[1] ? 1 : 0;
	}

	public int evaluateOperatorNe(final int[] v) {
		return v[0] != v[1] ? 1 : 0;
	}

	public int evaluateOperatorEq(final int[] v) {
		for(int i:v) if(i != v[0]) return 0;
		return 1;
	}

	public int evaluateOperatorNot(final int[] v) {
		return v[0]==0 ? 1 : 0;
	}

	public int evaluateOperatorAnd(final int[] v) {
		for(int i:v) if(i == 0) return 0;
		return 1;
	}

	public int evaluateOperatorOr(final int[] v) {
		for(int i:v) if(i != 0) return 1;
		return 0;
	}

	public int evaluateOperatorXor(final int[] v) {
		return v[0]==0 && v[1]!=0 ? 1 : v[0]!=0 && v[1]==0 ? 1 : 0;
	}

	public int evaluateOperatorIff(final int[] v) {
		for(int i:v) if((i!=0 && v[0]==0) || (i==0 && v[0] != 0)) return 0;
		return 1;
	}

	public int evaluateOperatorImp(final int[] v) {
		return v[0]==0 || v[1] != 0 ? 1 : 0;
	}

	public int evaluateOperatorIf(final int[] v) {
		return v[0]!=0 ? v[1] : v[2];
	}

	private boolean encodeNogood(Map<String, Integer> fullInstantiation) {
		int[] clause = new int[fullInstantiation.size()];
		int i=0;
		for(Map.Entry<String, Integer> assignment : fullInstantiation.entrySet()) {
			clause[i++] = -this.solver.getSolverVar(assignment.getKey(), assignment.getValue());
		}
		return this.solver.addClause(clause);
	}

	private class InstantiationIterator implements Iterable<Map<String, Integer>>, Iterator<Map<String, Integer>> {

		private final Map<String, int[]> domainMap;

		private final String[] vars;
		private final int[][] domains;
		private final int[] domainSizes;
		private final int[] domainIndexes;

		private Map<String, Integer> fullInstantiation;
		private Map<String, Integer> lastFullInstantiation;
		private Map<String, Integer> lastInstantiationUpdate;
		private Map<String, Integer> instantiationUpdate;

		public InstantiationIterator(final Set<String> involved, final Map<String, int[]> domainMap) {
			this.domainMap = domainMap;
			final int nVars = involved.size();
			if(nVars == 0) throw new IllegalArgumentException("number of involved vars must be higher than zero");
			this.vars = new String[nVars];
			this.domains = new int[nVars][];
			this.domainSizes = new int[nVars];
			this.domainIndexes = new int[nVars];
			initDataStructures(involved);
			computeFirst();
		}

		private void initDataStructures(final Set<String> involved) {
			int i=0;
			for(String var : involved) {
				this.vars[i] = var;
				final int[] domain = this.domainMap.get(var);
				this.domains[i] = domain;
				this.domainSizes[i] = domain.length;
				this.domainIndexes[i] = 0;
				++i;
			}
		}

		@Override
		public Iterator<Map<String, Integer>> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return instantiationUpdate != null;
		}

		@Override
		public Map<String, Integer> next() {
			this.lastFullInstantiation = this.fullInstantiation;
			this.lastInstantiationUpdate = this.instantiationUpdate;
			computeNext();
			return this.lastFullInstantiation;
		}

		public Map<String, Integer> getInstantiationUpdate() {
			return this.lastInstantiationUpdate;
		}

		private void computeFirst() {
			this.fullInstantiation = new HashMap<>();
			this.instantiationUpdate = new HashMap<>();
			for(int i=0; i<this.vars.length; ++i) {
				this.fullInstantiation.put(this.vars[i], this.domains[i][0]);
				this.instantiationUpdate.put(this.vars[i], this.domains[i][0]);
			}
		}

		private void computeNext() {
			this.fullInstantiation = new HashMap<>(this.lastFullInstantiation);
			this.instantiationUpdate = new HashMap<>();
			int i;
			for(i=this.vars.length-1; i>=0; --i) {
				if(this.domainIndexes[i] < this.domainSizes[i]-1) {
					++this.domainIndexes[i];
					this.fullInstantiation.put(this.vars[i], this.domains[i][this.domainIndexes[i]]);
					this.instantiationUpdate.put(this.vars[i], this.domains[i][this.domainIndexes[i]]);
					break;
				}
				this.domainIndexes[i] = 0;
				this.fullInstantiation.put(this.vars[i], this.domains[i][0]);
				this.instantiationUpdate.put(this.vars[i], this.domains[i][0]);
			}
			if(i == -1) {
				this.fullInstantiation = null;
				this.instantiationUpdate = null;
			}
		}
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
