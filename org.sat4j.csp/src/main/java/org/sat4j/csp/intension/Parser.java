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

import java.util.ArrayList;
import java.util.List;

/**
 * Parses an intension constraint given as a string and returns it as a tree of {@link IExpression}.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
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
