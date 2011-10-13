package org.sat4j.pb.constraints;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.pb.constraints.pb.LearntBinaryClausePB;
import org.sat4j.pb.constraints.pb.LearntHTClausePB;
import org.sat4j.pb.constraints.pb.OriginalBinaryClausePB;
import org.sat4j.pb.constraints.pb.OriginalHTClausePB;
import org.sat4j.pb.constraints.pb.UnitClausePB;
import org.sat4j.specs.IVecInt;

public class UnitBinaryHTClausePBConstructor implements IClauseConstructor {

	public Constr constructClause(UnitPropagationListener solver, ILits voc,
			IVecInt v) {
		if (v == null) {
			// tautological clause
			return null;
		}
		if (v.size() == 1) {
			return new UnitClausePB(v.last(), voc);
		}
		if (v.size() == 2) {
			return OriginalBinaryClausePB.brandNewClause(solver, voc, v);
		}
		return OriginalHTClausePB.brandNewClause(solver, voc, v);
	}

	public Constr constructLearntClause(ILits voc, IVecInt literals) {
		if (literals.size() == 1) {
			return new UnitClausePB(literals.last(), voc);
		}
		if (literals.size() == 2) {
			return new LearntBinaryClausePB(literals, voc);
		}
		return new LearntHTClausePB(literals, voc);
	}

}
