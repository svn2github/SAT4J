package org.sat4j.pb.constraints;

import java.math.BigInteger;

import org.sat4j.minisat.core.Constr;
import org.sat4j.pb.constraints.pb.IDataStructurePB;
import org.sat4j.pb.constraints.pb.MinWatchPb;
import org.sat4j.pb.constraints.pb.MinWatchPbLong;
import org.sat4j.specs.ContradictionException;

public class CompetResolutionMinPBLongMixedWLClauseCardConstrDataStructure
		extends CompetResolutionPBLongMixedWLClauseCardConstrDataStructure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Constr constructPB(int[] theLits, BigInteger[] coefs,
			BigInteger degree) throws ContradictionException {
		return MinWatchPb.normalizedMinWatchPbNew(solver, getVocabulary(),
				theLits, coefs, degree);
	}

	@Override
	protected Constr constructLearntPB(IDataStructurePB dspb) {
		return MinWatchPb.normalizedWatchPbNew(getVocabulary(), dspb);
	}

	@Override
	protected Constr constructLongPB(int[] theLits, BigInteger[] coefs,
			BigInteger degree) throws ContradictionException {
		return MinWatchPbLong.normalizedMinWatchPbNew(solver, getVocabulary(),
				theLits, coefs, degree);
	}

	@Override
	protected Constr constructLearntLongPB(IDataStructurePB dspb) {
		return MinWatchPbLong.normalizedWatchPbNew(getVocabulary(), dspb);
	}

	public static boolean isLongSufficient(BigInteger[] coefs, BigInteger degree) {
		return (degree.add(coefs[0].add(coefs[1]))).bitLength() < Long.SIZE;
	}

}
