package org.sat4j.csp.intension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OperatorExpression implements IExpression {
	
	private EOperator op;
	private IExpression[] operands;
	private EExpressionType expressionType;
	
	private final Set<String> involvedVars = new HashSet<>();
	
	private int lastEvaluation;

	public OperatorExpression(final EOperator op, IExpression[] operands) {
		for(IExpression expr : operands) this.involvedVars.addAll(expr.involvedVars());
		if(handleAssociativityCase(op, operands)) return;
		this.op = op;
		this.operands = operands;
		expressionType = op.resultType();
	}

	private boolean handleAssociativityCase(EOperator tmpOp, IExpression[] tmpOperands) {
		switch(tmpOp.associtivityState()) {
		case ASSOCIATIVE:
			if(tmpOperands.length > 2) {
				handleAssociativity(tmpOp, tmpOperands);
				return true;
			}
			return false;
		case AND_CHAIN_OF_TWO_WITH_FIRST_COMMON:
			if(tmpOperands.length > 2) {
				handleAndChainOfTwoWithFirstCommon(tmpOp, tmpOperands);
				return true;
			}
			return false;
		default:
			return false;
		}
	}

	private void handleAssociativity(EOperator tmpOp, IExpression[] tmpOperands) {
		IExpression operand1 = null;
		if(tmpOperands.length == 3) {
			operand1 = tmpOperands[0];
		} else {
			IExpression[] subArray1 = new IExpression[tmpOperands.length/2];
			System.arraycopy(tmpOperands, 0, subArray1, 0, tmpOperands.length/2);
			operand1 = new OperatorExpression(tmpOp, subArray1);
		}
		int arraySize = tmpOperands.length - tmpOperands.length/2;
		IExpression[] subArray2 = new IExpression[arraySize];
		System.arraycopy(tmpOperands, tmpOperands.length/2, subArray2, 0, arraySize);
		IExpression operand2 = new OperatorExpression(tmpOp, subArray2);
		this.op = tmpOp;
		this.operands = new IExpression[]{operand1, operand2};
	}
	
	private void handleAndChainOfTwoWithFirstCommon(EOperator tmpOp, IExpression[] tmpOperands) {
		IExpression[] andOperands = new IExpression[tmpOperands.length-1];
		for(int i=1; i<tmpOperands.length; ++i) {
			andOperands[i-1] = new OperatorExpression(tmpOp, new IExpression[]{tmpOperands[0], tmpOperands[i]});
		}
		this.op = EOperator.LOGICAL_AND;
		OperatorExpression tmpExpression = new OperatorExpression(this.op, andOperands);
		this.operands = tmpExpression.operands;
	}

	@Override
	public String toString() {
		final StringBuffer sbuf = new StringBuffer();
		sbuf.append(this.op.nameAsString()).append('(');
		if(operands.length > 0) sbuf.append(operands[0].toString());
		for(int i=1; i<this.operands.length; ++i) sbuf.append(',').append(operands[i].toString());
		sbuf.append(')');
		return sbuf.toString();
	}
	
	@Override
	public int compareTo(final IExpression o) {
		return this.toString().compareTo(o.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expressionType == null) ? 0 : expressionType.hashCode());
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + Arrays.hashCode(operands);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OperatorExpression))
			return false;
		OperatorExpression other = (OperatorExpression) obj;
		if (expressionType != other.expressionType)
			return false;
		if (op != other.op)
			return false;
		if (!Arrays.equals(operands, other.operands))
			return false;
		return true;
	}

	@Override
	public Set<String> involvedVars() {
		return this.involvedVars;
	}
	
	@Override
	public IExpression[] operands() {
		return this.operands;
	}

	@Override
	public int evaluate(final Map<String, Integer> bindings) {
		final int value = this.op.evaluate(this.operands, bindings);
		this.lastEvaluation = value;
		return value;
	}

	@Override
	public int updateEvaluation(final Map<String, Integer> bindingsChange) {
		boolean inCache = true;
		for(String var : bindingsChange.keySet()) {
			if(this.involvedVars.contains(var)) {
				inCache = false;
				break;
			}
		}
		if(inCache) return this.lastEvaluation;
		final int value = this.op.updateEvaluation(this.operands, bindingsChange);
		this.lastEvaluation = value;
		return value;
	}

	@Override
	public boolean isAndOperator() {
		return this.op == EOperator.LOGICAL_AND;
	}

}
