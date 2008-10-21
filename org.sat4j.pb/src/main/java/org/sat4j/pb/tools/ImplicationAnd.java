/**
 * 
 */
package org.sat4j.pb.tools;

import java.util.Iterator;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class ImplicationAnd<T> {
	private final DependencyHelper<T> helper;
	private final IVecInt clause;
	private final IVec<IConstr> toName = new Vec<IConstr>();

	public ImplicationAnd(DependencyHelper<T> helper, IVecInt clause) {
		this.clause = clause;
		this.helper = helper;
	}

	public ImplicationAnd<T> and(T thing) throws ContradictionException {
		IVecInt tmpClause = new VecInt();
		clause.copyTo(tmpClause);
		tmpClause.push(helper.getIntValue(thing));
		toName.push(helper.xplain.addClause(tmpClause));
		return this;
	}

	public ImplicationAnd<T> andNot(T thing) throws ContradictionException {
		IVecInt tmpClause = new VecInt();
		clause.copyTo(tmpClause);
		tmpClause.push(-helper.getIntValue(thing));
		toName.push(helper.xplain.addClause(tmpClause));
		return this;
	}

	public void named(String name) {
		for (Iterator<IConstr> it = toName.iterator(); it.hasNext();) {
			helper.constrs.push(it.next());
			helper.descs.push(name);
		}
	}
}