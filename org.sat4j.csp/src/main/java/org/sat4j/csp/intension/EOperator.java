package org.sat4j.csp.intension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum EOperator {
	
	OPPOSITE("neg", EExpressionType.INTEGER, 1, 1, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return -v[0];
		}
	},
	
	ABSOLUTE_VALUE("abs", EExpressionType.INTEGER, 1, 1, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return Math.abs(v[0]);
		}
	},
	
	ADDITION("add", EExpressionType.INTEGER, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE) {
		public int evaluate(final int[] v) {
			int sum = 0;
			for(int i:v) sum+= i;
			return sum;
		}
	},
	
	SUBTRACTION("sub", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0]-v[1];
		}
	},
	
	MULTIPLICATION("mul", EExpressionType.INTEGER, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE) {
		public int evaluate(final int[] v) {
			int prod=1;
			for(int i:v) prod *= i;
			return prod;
		}
	},
	
	INTEGER_DIVISION("div", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0]/v[1];
		}
	},
	
	REMAINDER("mod", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0]%v[1];
		}
	},
	
	SQUARE("sqr", EExpressionType.INTEGER, 1, 1, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0]*v[0];
		}
	},
	
	POWER("pow", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return internalEvaluate(v[0], v[1]);
		}
		private int internalEvaluate(int a, int b) {
			if(b==1) return a;
			int halfPow = internalEvaluate(a, b/2);
			if((b&1)==0) return halfPow*halfPow;
			return a*halfPow*halfPow;
		}
	},
	
	MINIMUM("min", EExpressionType.INTEGER, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE) {
		public int evaluate(final int[] v) {
			int m = Integer.MAX_VALUE;
			for(int i:v) m = Math.min(m, i);
			return m;
		}
	},
	
	MAXIMUM("max", EExpressionType.INTEGER, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE) {
		public int evaluate(final int[] v) {
			int m = Integer.MIN_VALUE;
			for(int i:v) m = Math.max(m, i);
			return m;
		}
	},
	
	DISTANCE("dist", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return Math.abs(v[0]-v[1]);
		}
	},
	
	LESS_THAN("lt", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0] < v[1] ? 1 : 0;
		}
	},
	
	LESS_THAN_OR_EQUAL("le", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0] <= v[1] ? 1 : 0;
		}
	},
	
	GREATER_THAN("gt", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0] > v[1] ? 1 : 0;
		}
	},
	
	GREATER_THAN_OR_EQUAL("ge", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0] >= v[1] ? 1 : 0;
		}
	},
	
	DIFFERENT_FROM("ne", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0] != v[1] ? 1 : 0;
		}
	},
	
	EQUAL_TO("eq", EExpressionType.BOOLEAN, 1, EOperator.INFINITE_ARITY, EAssociativityState.AND_CHAIN_OF_TWO_WITH_FIRST_COMMON) {
		public int evaluate(final int[] v) {
			for(int i:v) if(i != v[0]) return 0;
			return 1;
		}
	},
	
	SET("set", EExpressionType.SET, 0, EOperator.INFINITE_ARITY, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return 0;
		}
	},
	
	MEMBERSHIP("in", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE) {
		// set members are v[1] ... v[n]
		public int evaluate(final int[] v) {
			for(int i=1; i<v.length; ++i) if(v[i] == v[0]) return 1;
			return 0;
		}
	},
	
	LOGICAL_NOT("not", EExpressionType.BOOLEAN, 1, 1, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0]==0 ? 1 : 0;
		}
	},
	
	LOGICAL_AND("and", EExpressionType.BOOLEAN, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE) {
		public int evaluate(final int[] v) {
			for(int i:v) if(i == 0) return 0;
			return 1;
		}
	},
	
	LOGICAL_OR("or", EExpressionType.BOOLEAN, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE) {
		public int evaluate(final int[] v) {
			for(int i:v) if(i != 0) return 1;
			return 0;
		}
	},
	
	LOGICAL_XOR("xor", EExpressionType.BOOLEAN, 2, EOperator.INFINITE_ARITY, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0]==0 && v[1]!=0 ? 1 : v[0]!=0 && v[1]==0 ? 1 : 0;
		}
	},
	
	LOGICAL_EQUIVALENCE("iff", EExpressionType.BOOLEAN, 1, EOperator.INFINITE_ARITY, EAssociativityState.AND_CHAIN_OF_TWO_WITH_FIRST_COMMON) {
		public int evaluate(final int[] v) {
			for(int i:v) if((i!=0 && v[0]==0) || (i==0 && v[0] != 0)) return 0;
			return 1;
		}
	},
	
	LOGICAL_IMPLICATION("imp", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0]==0 || v[1] != 0 ? 1 : 0;
		}
	},
	
	ALTERNATIVE("if", EExpressionType.SAME_AS_CHILDREN, 3, 3, EAssociativityState.NONE) {
		public int evaluate(final int[] v) {
			return v[0]!=0 ? v[1] : v[2];
		}
	},
	
	DISTINCT_VALUES("distinct", EExpressionType.INTEGER, 1, 2, EAssociativityState.NONE) {
		// values to check are v[0] ... v[n]
		public int evaluate(final int[] v) {
			Set<Integer> values = new HashSet<>();
			for(int i:v) values.add(i);
			return values.size();
		}
	};
	
	enum EAssociativityState {
		NONE, ASSOCIATIVE, AND_CHAIN_OF_TWO_WITH_FIRST_COMMON;
	}
	

	public static final int INFINITE_ARITY = Integer.MAX_VALUE;
	
	private final String op;
	private final int minArity;
	private final int maxArity;
	private EExpressionType resultType;
	private final EAssociativityState associativityState;

	private EOperator(final String op, final EExpressionType resultType, final int minArity, final int maxArity, final EAssociativityState associativity) {
		this.op = op;
		this.resultType = resultType;
		this.minArity = minArity;
		this.maxArity = maxArity;
		this.associativityState = associativity;
	}
	
	private static final Map<String, EOperator> strOpCache = new HashMap<>();
	
	public static EOperator operator(final String str) {
		EOperator operator = strOpCache.get(str);
		if(operator != null) return operator;
		for(EOperator op : EOperator.values()) {
			if(op.nameAsString().equals(str)) {
				operator = op;
				break;
			}
		}
		if(operator == null) {
			throw new IllegalArgumentException("\""+str+"\" is not a valid operator");
		}
		strOpCache.put(str, operator);
		return operator;
	}

	public String nameAsString() {
		return op;
	}
	
	public EExpressionType resultType() {
		return this.resultType;
	}

	public int minArity() {
		return minArity;
	}

	public int maxArity() {
		return maxArity;
	}
	
	public EAssociativityState associtivityState() {
		return this.associativityState;
	}

	public int evaluate(final int[] v) {
		throw new UnsupportedOperationException("method not implemented for operator "+this.name());
	}

	public int evaluate(final IExpression[] operands, final Map<String, Integer> bindings) {
		if(this == EOperator.MEMBERSHIP) {
			return evaluateMembership(operands, bindings);
		}
		if(this == EOperator.DISTINCT_VALUES) {
			return evaluateDistinctValues(operands, bindings);
		}
		final int[] childrenEvaluation = new int[operands.length];
		for(int i=0; i<operands.length; ++i) {
			childrenEvaluation[i] = operands[i].evaluate(bindings);
		}
		return this.evaluate(childrenEvaluation);
	}

	public int updateEvaluation(final IExpression[] operands, final Map<String, Integer> bindingsChange) {
		if(this == EOperator.MEMBERSHIP) {
			return updateMembershipEvaluation(operands, bindingsChange);
		}
		if(this == EOperator.DISTINCT_VALUES) {
			return updateDistinctValuesEvaluation(operands, bindingsChange);
		}
		final int[] childrenEvaluation = new int[operands.length];
		for(int i=0; i<operands.length; ++i) {
			childrenEvaluation[i] = operands[i].updateEvaluation(bindingsChange);
		}
		return this.evaluate(childrenEvaluation);
	}

	private int evaluateMembership(final IExpression[] operands, final Map<String, Integer> bindings) {
		final List<Integer> values = new ArrayList<>();
		values.add(operands[0].evaluate(bindings));
		for(IExpression expr : operands[1].operands()) {
			values.add(expr.evaluate(bindings));
		}
		final int[] valuesArray = new int[values.size()];
		for(int i=0; i<values.size(); ++i) valuesArray[i] = values.get(i);
		return this.evaluate(valuesArray);
	}
	
	private int updateMembershipEvaluation(final IExpression[] operands, final Map<String, Integer> bindingsChange) {
		final List<Integer> values = new ArrayList<>();
		values.add(operands[0].updateEvaluation(bindingsChange));
		for(IExpression expr : operands[1].operands()) {
			values.add(expr.updateEvaluation(bindingsChange));
		}
		final int[] valuesArray = new int[values.size()];
		for(int i=0; i<values.size(); ++i) valuesArray[i] = values.get(i);
		return this.evaluate(valuesArray);
	}
	
	private int evaluateDistinctValues(IExpression[] operands, Map<String, Integer> bindings) {
		Set<Integer> values = new HashSet<>();
		for(IExpression expr : operands[0].operands()) {
			values.add(expr.evaluate(bindings));
		}
		if(operands.length > 1) {
			for(IExpression expr : operands[1].operands()) {
				values.remove(expr.evaluate(bindings));
			}
		}
		final int[] valuesArray = new int[values.size()];
		int i=0;
		for(Integer value : values) valuesArray[i++] = value;
		return this.evaluate(valuesArray);
	}

	private int updateDistinctValuesEvaluation(IExpression[] operands, Map<String, Integer> bindingsChange) {
		Set<Integer> values = new HashSet<>();
		for(IExpression expr : operands[0].operands()) {
			values.add(expr.updateEvaluation(bindingsChange));
		}
		if(operands.length > 1) {
			for(IExpression expr : operands[1].operands()) {
				values.remove(expr.updateEvaluation(bindingsChange));
			}
		}
		final int[] valuesArray = new int[values.size()];
		int i=0;
		for(Integer value : values) valuesArray[i++] = value;
		return this.evaluate(valuesArray);
	}

}
