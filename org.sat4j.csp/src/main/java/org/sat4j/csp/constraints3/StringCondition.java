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
package org.sat4j.csp.constraints3;

import java.util.ArrayList;
import java.util.List;

import org.sat4j.csp.Predicate;
import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionIntvl;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Condition.ConditionVar;

/**
 * A class which aims at representing any constraint of xcsp3parser as a string.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class StringCondition {
	
	public static final String LEFT_OPERAND = "__LEFT_OPERAND__";

	private String asString;
	
	private List<String> vars = new ArrayList<String>();

	public StringCondition(ConditionVar condition) {
		String varId = ((ConditionVar)condition).x.id();
		vars.add(varId);
		this.asString = condition.operator.name().toLowerCase()+"("+LEFT_OPERAND+","+CtrBuilderUtils.normalizeCspVarName(varId)+")";
	}
	
	public StringCondition(ConditionVal condition) {
		this.asString = condition.operator.name().toLowerCase()+"("+LEFT_OPERAND+","+Long.toString(((ConditionVal)condition).k)+")";
	}
	
	public StringCondition(ConditionIntvl condition) {
		this.asString = "and(ge("+LEFT_OPERAND+","+Long.toString(condition.min)+"),le("+LEFT_OPERAND+","+Long.toString(condition.max)+"))";
	}
	
	public StringCondition(String str) {
		this.asString = str;
	}
	
	public StringCondition(String leftOperandPrefix, String leftOperandSuffix) {
		this(leftOperandPrefix+LEFT_OPERAND+leftOperandSuffix);
	}
	
	public static StringCondition buildStringCondition(Condition condition) {
		if(condition instanceof ConditionVar) return new StringCondition((ConditionVar) condition);
		if(condition instanceof ConditionVal) return new StringCondition((ConditionVal) condition);
		if(condition instanceof ConditionIntvl) return new StringCondition((ConditionIntvl) condition);
		throw new IllegalArgumentException();
	}
	
	public void addVariable(String var) {
		this.vars.add(var);
	}
	
	public List<String> getVarIds() {
		return this.vars;
	}
	
	public boolean hasVariables() {
		return this.vars.size() > 0;
	}
	
	public String asString(String leftOperand) {
		return this.asString.replaceAll(LEFT_OPERAND, leftOperand);
	}
	
	public void setPredicateExpression(Predicate p, String leftOperand) {
		setPredicateExpression(p, leftOperand, true);
	}

	public void setPredicateExpression(Predicate p, String leftOperand, boolean addVariables) {
		if(addVariables) {
			for(String var : this.vars) {
				if(!p.containsVariable(var)) p.addVariable(var);
			}
		}
		p.setExpression(asString(leftOperand));
	}
	
}
