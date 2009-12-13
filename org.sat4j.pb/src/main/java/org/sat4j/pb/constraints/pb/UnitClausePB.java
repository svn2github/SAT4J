package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.cnf.UnitClause;
import org.sat4j.minisat.core.ILits;
import org.sat4j.specs.IVecInt;

public final class UnitClausePB extends UnitClause implements PBConstr {

	private final ILits voc;

	public UnitClausePB(int value, ILits voc) {
		super(value);
		this.voc = voc;
	}

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	public IVecInt computeAnImpliedClause() {
		return null;
	}

	public BigInteger getCoef(int p) {
		return BigInteger.ONE;
	}

	public BigInteger[] getCoefs() {
		return new BigInteger[] { BigInteger.ONE };
	}

	public BigInteger getDegree() {
		return BigInteger.ONE;
	}

	public int[] getLits() {
		return new int[] { literal };
	}

	public ILits getVocabulary() {
		return voc;
	}

}
