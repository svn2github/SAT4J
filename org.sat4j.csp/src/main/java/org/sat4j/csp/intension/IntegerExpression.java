package org.sat4j.csp.intension;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IntegerExpression implements IExpression {
	
	private final Integer value;
	
	private final Set<String> involvedVars = new HashSet<>();
	
	public IntegerExpression(final int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

	@Override
	public int compareTo(final IExpression o) {
		return this.toString().compareTo(o.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IntegerExpression))
			return false;
		IntegerExpression other = (IntegerExpression) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
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
		return this.value;
	}

	@Override
	public int updateEvaluation(final Map<String, Integer> bindingsChange) {
		return this.value;
	}

	@Override
	public boolean isAndOperator() {
		return false;
	}

}
