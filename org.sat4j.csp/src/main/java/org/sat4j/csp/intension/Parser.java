package org.sat4j.csp.intension;

import java.util.ArrayList;
import java.util.List;

/** ICE stands for Intension Constraint Encoder */
public class Parser {
	
	private static final String NULL_LENGTH_EXPR_MSG_PREFIX = "null length expression at index ";
	private final char[] charArray;
	private int currentCharIndex;
	
	private IExpression expression;
	
	private static final char ARGUMENT_LIST_BEGIN = '(';
	private static final char ARGUMENT_LIST_SEP = ',';
	private static final char ARGUMENT_LIST_END = ')';
	

	public Parser(final String strExpression) {
		this.charArray = strExpression.replaceAll(" ", "").toCharArray();
		this.currentCharIndex = 0;
	}
	
	public void parse() {
		this.expression = parseExpression();
	}
	
	public IExpression getExpression() {
		return this.expression;
	}

	private IExpression parseExpression() {
		final StringBuffer exprBuf = new StringBuffer();
		boolean expressionEnded = false;
		while(!expressionEnded && (this.currentCharIndex < this.charArray.length)) {
			final char current = this.charArray[this.currentCharIndex];
			switch(current) {
			case ARGUMENT_LIST_BEGIN:
				return parseOperatorExpression(exprBuf.toString());
			case ARGUMENT_LIST_SEP:
			case ARGUMENT_LIST_END:
				expressionEnded = true;
				break;
			default:
				exprBuf.append(current);
				++this.currentCharIndex;
			}
		}
		final String strValue = exprBuf.toString();
		if(strValue.length() == 0) throw new IllegalArgumentException(NULL_LENGTH_EXPR_MSG_PREFIX+this.currentCharIndex);
		Integer intValue = null;
		try {
			intValue = Integer.valueOf(strValue);
		} catch (NumberFormatException e) {
			return new VarExpression(strValue);
		}
		return new IntegerExpression(intValue);
	}

	private IExpression parseOperatorExpression(final String operatorName) {
		final EOperator operator = EOperator.operator(operatorName);
		final List<IExpression> operandsList = new ArrayList<>();
		if(this.charArray[this.currentCharIndex] != ARGUMENT_LIST_BEGIN) throw new IllegalArgumentException("Expected character \""+ARGUMENT_LIST_BEGIN+"\"");
		while(this.charArray[this.currentCharIndex] != ARGUMENT_LIST_END) {
			++this.currentCharIndex;
			try {
				final IExpression child = parseExpression();
				operandsList.add(child);
			} catch(IllegalArgumentException e) {
				if(!e.getMessage().startsWith(NULL_LENGTH_EXPR_MSG_PREFIX)) throw e;
			}
		}
		++this.currentCharIndex;
		if(operandsList.size() < operator.minArity() || operandsList.size() > operator.maxArity()) {
			throw new IllegalArgumentException("wrong arity for operator \""+operator.nameAsString()+"\" at index "+this.currentCharIndex);
		}
		IExpression[] operandsArray = new IExpression[operandsList.size()];
		operandsArray = operandsList.toArray(operandsArray);
		return new OperatorExpression(operator, operandsArray);
	}

}
