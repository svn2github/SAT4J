package org.sat4j.csp.constraints3;

import java.util.ArrayList;
import java.util.List;

import org.sat4j.csp.Predicate;
import org.xcsp.parser.XParser.Condition;
import org.xcsp.parser.XParser.ConditionIntvl;
import org.xcsp.parser.XParser.ConditionVal;
import org.xcsp.parser.XParser.ConditionVar;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class StringCondition {
	
	public static final String LEFT_OPERAND = "__LEFT_OPERAND__";

	private String asString;
	
	private List<String> vars = new ArrayList<String>();

	public StringCondition(ConditionVar condition) {
		String varId = ((ConditionVar)condition).x.id;
		vars.add(varId);
		this.asString = condition.operator.name().toLowerCase()+"("+LEFT_OPERAND+","+CtrBuilderUtils.normalizeCspVarName(varId)+")";
	}
	
	public StringCondition(ConditionVal condition) {
		this.asString = condition.operator.name().toLowerCase()+"("+LEFT_OPERAND+","+Integer.toString(((ConditionVal)condition).k)+")";
	}
	
	public StringCondition(ConditionIntvl condition) {
		this.asString = "and(ge("+LEFT_OPERAND+","+Integer.toString(condition.min)+"),le("+LEFT_OPERAND+","+Integer.toString(condition.max)+"))";
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
	
	private String asString(String leftOperand) {
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
