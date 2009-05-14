package org.sat4j.pb.constraints;

import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.card.MinWatchCard;
import org.sat4j.minisat.constraints.cnf.LearntBinaryClause;
import org.sat4j.minisat.constraints.cnf.LearntHTClause;
import org.sat4j.minisat.constraints.cnf.OriginalBinaryClause;
import org.sat4j.minisat.constraints.cnf.OriginalHTClause;
import org.sat4j.minisat.constraints.cnf.UnitClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.pb.constraints.pb.IDataStructurePB;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class CompetMinHTmixedClauseCardConstrDataStructureFactory extends
		PBMinClauseCardConstrDataStructure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CompetMinHTmixedClauseCardConstrDataStructureFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Constr constructClause(IVecInt v) {
		if (v == null)
			return null;
		if (v.size() == 2) {
			return OriginalBinaryClause.brandNewClause(solver, getVocabulary(),
					v);
		}
		return OriginalHTClause.brandNewClause(solver, getVocabulary(), v);
	}

	@Override
	protected Constr constructLearntClause(IVecInt resLits) {
		if (resLits.size() == 1) {
			return new UnitClause(resLits.last());
		}
		if (resLits.size() == 2) {
			return new LearntBinaryClause(resLits, getVocabulary());
		}
		return new LearntHTClause(resLits, getVocabulary());
	}

	@Override
	protected Constr constructCard(IVecInt theLits, int degree)
			throws ContradictionException {
		return MinWatchCard.minWatchCardNew(solver, getVocabulary(), theLits,
				MinWatchCard.ATLEAST, degree);
	}

	@Override
	protected Constr constructLearntCard(IDataStructurePB dspb) {
		IVecInt resLits = new VecInt();
		IVec<BigInteger> resCoefs = new Vec<BigInteger>();
		dspb.buildConstraintFromConflict(resLits, resCoefs);
		return new MinWatchCard(getVocabulary(), resLits, true, dspb
				.getDegree().intValue());
	}

}
