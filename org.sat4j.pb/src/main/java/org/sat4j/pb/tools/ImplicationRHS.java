/**
 * 
 */
package org.sat4j.pb.tools;

import org.sat4j.core.Vec;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class ImplicationRHS<T> {

	private IVecInt clause;
	private final DependencyHelper<T> helper;

	private IVec<IConstr> toName = new Vec<IConstr>();

	public ImplicationRHS(DependencyHelper<T> helper, IVecInt clause) {
		this.clause = clause;
		this.helper = helper;
	}

	public ImplicationAnd<T> implies(T thing) throws ContradictionException {
		ImplicationAnd<T> and = new ImplicationAnd<T>(helper, clause);
		and.and(thing);
		return and;
	}

	public ImplicationNamer<T> implies(T... things)
			throws ContradictionException {
		for (T t : things) {
			clause.push(helper.getIntValue(t));
		}
		toName.push(helper.xplain.addClause(clause));
		return new ImplicationNamer<T>(helper, toName);
	}

	public ImplicationAnd<T> impliesNot(T thing) throws ContradictionException {
		ImplicationAnd<T> and = new ImplicationAnd<T>(helper, clause);
		and.andNot(thing);
		return and;
	}

}