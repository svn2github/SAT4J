package org.sat4j.csp.intension;

import java.util.HashMap;
import java.util.Map;

public enum EOperator {
	
	OPPOSITE("neg", EExpressionType.INTEGER, 1, 1, EAssociativityState.NONE),
	ABSOLUTE_VALUE("abs", EExpressionType.INTEGER, 1, 1, EAssociativityState.NONE),
	ADDITION("add", EExpressionType.INTEGER, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE),
	SUBTRACTION("sub", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE),
	MULTIPLICATION("mul", EExpressionType.INTEGER, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE),
	INTEGER_DIVISION("div", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE),
	REMAINDER("mod", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE),
	SQUARE("sqr", EExpressionType.INTEGER, 1, 1, EAssociativityState.NONE),
	POWER("pow", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE),
	MINIMUM("min", EExpressionType.INTEGER, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE),
	MAXIMUM("max", EExpressionType.INTEGER, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE),
	DISTANCE("dist", EExpressionType.INTEGER, 2, 2, EAssociativityState.NONE),
	LESS_THAN("lt", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE),
	LESS_THAN_OR_EQUAL("le", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE),
	GREATER_THAN("gt", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE),
	GREATER_THAN_OR_EQUAL("ge", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE),
	DIFFERENT_FROM("ne", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE),
	EQUAL_TO("eq", EExpressionType.BOOLEAN, 1, EOperator.INFINITE_ARITY, EAssociativityState.AND_CHAIN_OF_TWO_WITH_FIRST_COMMON),
	SET("set", EExpressionType.SET, 0, EOperator.INFINITE_ARITY, EAssociativityState.NONE),
	MEMBERSHIP("in", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE),
	LOGICAL_NOT("not", EExpressionType.BOOLEAN, 1, 1, EAssociativityState.NONE),
	LOGICAL_AND("and", EExpressionType.BOOLEAN, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE),
	LOGICAL_OR("or", EExpressionType.BOOLEAN, 1, EOperator.INFINITE_ARITY, EAssociativityState.ASSOCIATIVE),
	LOGICAL_XOR("xor", EExpressionType.BOOLEAN, 2, EOperator.INFINITE_ARITY, EAssociativityState.NONE),
	LOGICAL_EQUIVALENCE("iff", EExpressionType.BOOLEAN, 1, EOperator.INFINITE_ARITY, EAssociativityState.AND_CHAIN_OF_TWO_WITH_FIRST_COMMON),
	LOGICAL_IMPLICATION("imp", EExpressionType.BOOLEAN, 2, 2, EAssociativityState.NONE),
	ALTERNATIVE("if", EExpressionType.SAME_AS_CHILDREN, 3, 3, EAssociativityState.NONE);
	
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
	
}
