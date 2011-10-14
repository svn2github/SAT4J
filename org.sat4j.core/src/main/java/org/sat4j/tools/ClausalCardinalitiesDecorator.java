package org.sat4j.tools;

import org.sat4j.core.ConstrGroup;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public class ClausalCardinalitiesDecorator<T extends ISolver> extends
		SolverDecorator<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClausalCardinalitiesDecorator(T solver) {
		super(solver);
	}

	@Override
	public IConstr addAtLeast(IVecInt literals, int degree)
			throws ContradictionException {
		if (degree == 1) {
			return addClause(literals);
		}
		throw new UnsupportedOperationException("At least not managed yet");
	}

	@Override
	public IConstr addAtMost(IVecInt literals, int k)
			throws ContradictionException {
		if (k == 0 || literals.size() == 1) {
			// will propagate unit literals
			return super.addAtMost(literals, k);
		}
		if (literals.size() <= 1) {
			throw new UnsupportedOperationException(
					"requires at least 2 literals");
		}
		ConstrGroup group = new ConstrGroup(false);
		final int n = literals.size();

		int s[][] = new int[n][k];
		for (int j = 0; j < k; j++) {
			for (int i = 0; i < n - 1; i++) {
				s[i][j] = nextFreeVarId(true);
			}
		}
		IVecInt clause = new VecInt();
		clause.push(-literals.get(0));
		clause.push(s[0][0]);
		group.add(addClause(clause));
		clause.clear();
		for (int j = 1; j < k; j++) {
			clause.push(-s[0][j]);
			group.add(addClause(clause));
			clause.clear();
		}
		clause.push(-literals.get(n - 1));
		clause.push(-s[n - 2][k - 1]);
		group.add(addClause(clause));
		clause.clear();
		for (int i = 1; i < n - 1; i++) {
			clause.push(-literals.get(i));
			clause.push(s[i][0]);
			group.add(addClause(clause));
			clause.clear();
			clause.push(-s[i - 1][0]);
			clause.push(s[i][0]);
			group.add(addClause(clause));
			clause.clear();
			for (int j = 1; j < k; j++) {
				clause.push(-literals.get(i));
				clause.push(-s[i - 1][j - 1]);
				clause.push(s[i][j]);
				group.add(addClause(clause));
				clause.clear();
				clause.push(-s[i - 1][j]);
				clause.push(s[i][j]);
				group.add(addClause(clause));
				clause.clear();
			}
			clause.push(-literals.get(i));
			clause.push(-s[i - 1][k - 1]);
			group.add(addClause(clause));
			clause.clear();
		}
		return group;
	}

}
