package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.cnf.UnitClauses;
import org.sat4j.minisat.core.ILits;
import org.sat4j.specs.IVecInt;

public class UnitClausesPB extends UnitClauses implements PBConstr {

	public UnitClausesPB(IVecInt values) {
		super(values);
	}

	public BigInteger getCoef(int literal) {
		return BigInteger.ONE;
	}

	public BigInteger getDegree() {
		return BigInteger.ONE;
	}

	public ILits getVocabulary() {
		throw new UnsupportedOperationException();
	}

	public int[] getLits() {
		throw new UnsupportedOperationException();
	}

	public BigInteger[] getCoefs() {
		throw new UnsupportedOperationException();
	}

	public IVecInt computeAnImpliedClause() {
		throw new UnsupportedOperationException();
	}

}
