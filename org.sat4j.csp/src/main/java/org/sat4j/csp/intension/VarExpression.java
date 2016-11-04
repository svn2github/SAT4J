package org.sat4j.csp.intension;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VarExpression implements IExpression {
	
	private final String var;
	
	private final Set<String> involvedVars = new HashSet<>();
	
	private int lastEvaluation;
	
	public VarExpression(final String value) {
		this.var = value;
		this.involvedVars.add(value);
	}

	@Override
	public String toString() {
		return this.var;
	}
	
	@Override
	public int compareTo(final IExpression o) {
		return this.toString().compareTo(o.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((var == null) ? 0 : var.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VarExpression))
			return false;
		VarExpression other = (VarExpression) obj;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}

	@Override
	public Set<String> involvedVars() {
		return this.involvedVars;
	}
	
	@Override
	public IExpression[] operands() {
		return null;
	}

	@Override
	public int evaluate(final Map<String, Integer> bindings) {
		final Integer value = bindings.get(this.var);
		if(value == null) throw new IllegalArgumentException("missing varaible in provided bindings: "+this.var);
		this.lastEvaluation = value;
		return value;
	}

	@Override
	public int updateEvaluation(final Map<String, Integer> bindingsChange) {
		final Integer value = bindingsChange.get(this.var);
		if(value == null) return this.lastEvaluation;
		this.lastEvaluation = value;
		return value;
	}

	@Override
	public boolean isAndOperator() {
		return false;
	}

}
