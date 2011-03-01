package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.core.ILits;

public class MinWatchPbLongPB extends MinWatchPbLong implements PBConstr {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final BigInteger[] bigCoefs;
	private final BigInteger bigDegree;

	public MinWatchPbLongPB(ILits voc, IDataStructurePB mpb) {
		super(voc, mpb);
		bigCoefs = new BigInteger[mpb.size()];
		mpb.buildConstraintFromMapPb(lits, bigCoefs);
		bigDegree = mpb.getDegree();
	}

	public MinWatchPbLongPB(ILits voc, int[] lits, BigInteger[] coefs,
			BigInteger degree) {
		super(voc, lits, coefs, degree);
		bigCoefs = coefs;
		bigDegree = degree;
	}

	/**
	 * to obtain the coefficient of the i-th literal of the constraint
	 * 
	 * @param i
	 *            index of the literal
	 * @return coefficient of the literal
	 */
	public BigInteger getCoef(int i) {
		return bigCoefs[i];
	}

	/**
	 * @return Returns the degree.
	 */
	public BigInteger getDegree() {
		return bigDegree;
	}

	/**
	 * to obtain the coefficients of the constraint.
	 * 
	 * @return a copy of the array of the coefficients
	 */
	public BigInteger[] getCoefs() {
		BigInteger[] coefsBis = new BigInteger[bigCoefs.length];
		System.arraycopy(bigCoefs, 0, coefsBis, 0, bigCoefs.length);
		return coefsBis;
	}

}
