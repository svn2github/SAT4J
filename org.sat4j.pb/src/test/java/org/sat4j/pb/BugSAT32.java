package org.sat4j.pb;

import java.math.BigInteger;

import org.junit.Test;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.ILits;
import org.sat4j.pb.constraints.pb.Pseudos;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class BugSAT32 {

	@Test
	public void testClassicalCase() throws ContradictionException {
		IVecInt literals = VecInt.EMPTY;
		IVec<BigInteger> coefs = new Vec<BigInteger>();
		ILits voc = new Lits();
		Pseudos.niceParameters(literals, coefs, true, BigInteger.ZERO, voc);
		Pseudos.niceParameters(literals, coefs, false, BigInteger.ZERO, voc);
		Pseudos.niceParameters(literals, coefs, true, BigInteger.ONE.negate(),
				voc);
		Pseudos.niceParameters(literals, coefs, false, BigInteger.ONE.negate(),
				voc);
	}

	@Test
	public void testCompetitionCase() throws ContradictionException {
		int[] literals = {};
		BigInteger[] coefs = {};
		Pseudos.niceParametersForCompetition(literals, coefs, true,
				BigInteger.ZERO);
		Pseudos.niceParametersForCompetition(literals, coefs, false,
				BigInteger.ZERO);
		Pseudos.niceParametersForCompetition(literals, coefs, true,
				BigInteger.ONE.negate());
		Pseudos.niceParametersForCompetition(literals, coefs, false,
				BigInteger.ONE.negate());
	}

}
