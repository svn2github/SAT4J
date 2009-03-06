package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.cnf.UnitClause;
import org.sat4j.minisat.core.ILits;
import org.sat4j.specs.IVecInt;

public class UnitClausePB extends UnitClause implements PBConstr {

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

	public BigInteger getCoef(int literal) {
		return BigInteger.ONE;
	}

	public BigInteger[] getCoefs() {
		BigInteger[] tmp = { BigInteger.ONE };
		return tmp;
	}

	public BigInteger getDegree() {
		return BigInteger.ONE;
	}

	public int[] getLits() {
		int[] tmp = { literal };
		return tmp;
	}

	public ILits getVocabulary() {
		return voc;
	}

}
