/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2016 Daniel Le Berre
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *******************************************************************************/
package org.sat4j.csp.intension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An {@link IExpression} equal to an operator-rooted expression.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class OperatorExpression implements IExpression {
	
	private EOperator op;
	private IExpression[] operands;
	private EExpressionType expressionType;
	
	private final Set<String> involvedVars = new HashSet<>();
	
	public OperatorExpression(final EOperator op, IExpression[] operands) {
		for(IExpression expr : operands) this.involvedVars.addAll(expr.getInvolvedVars());
		if(handleMembershipCase(op, operands)) return;
		if(handleAssociativityCase(op, operands)) return;
		this.op = op;
		this.operands = operands;
		expressionType = op.resultType();
	}

	private boolean handleMembershipCase(EOperator op, IExpression[] operands) {
		if(op != EOperator.MEMBERSHIP) return false;
		IExpression inclTest = operands[0];
		IExpression[] setMembers = operands[1].getOperands();
		IExpression[] newOperands = new IExpression[setMembers.length];
		for(int i=0; i<setMembers.length; ++i) {
			newOperands[i] = new OperatorExpression(EOperator.EQUAL_TO, new IExpression[]{inclTest, setMembers[i]});
		}
		this.op = EOperator.LOGICAL_OR;
		this.operands = newOperands;
		handleAssociativityCase(this.op, this.operands);
		return true;
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
	
	public EOperator getOperator() {
		return this.op;
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
	public Set<String> getInvolvedVars() {
		return this.involvedVars;
	}
	
	@Override
	public IExpression[] getOperands() {
		return this.operands;
	}
	
	@Override
	public String typeAsString() {
		return "operator";
	}

}
