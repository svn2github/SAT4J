package org.sat4j.pb.tools;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

public class DisjunctionRHS<T, C> {
	private final IVecInt literals;
	private final DependencyHelper<T, C> helper;

	private final IVec<IConstr> toName = new Vec<IConstr>();

	public DisjunctionRHS(DependencyHelper<T, C> helper, IVecInt literals) {
		this.literals = literals;
		this.helper = helper;
	}

	public ImplicationNamer<T, C> implies(T... things)
			throws ContradictionException {
		IVecInt clause = new VecInt();
		for (T t : things) {
			clause.push(helper.getIntValue(t));
		}
		int p;
		IConstr constr;
		for (IteratorInt it = literals.iterator(); it.hasNext();) {
			p = it.next();
			clause.push(p);
			constr = helper.solver.addClause(clause);
			if (constr == null) {
				throw new IllegalStateException(
						"Constraints are not supposed to be null when using the helper");
			}
			toName.push(constr);
			clause.remove(p);
		}
		return new ImplicationNamer<T, C>(helper, toName);
	}
}
