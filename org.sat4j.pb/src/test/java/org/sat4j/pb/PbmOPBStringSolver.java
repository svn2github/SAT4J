package org.sat4j.pb;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class PbmOPBStringSolver {

	private static final String STRING1 = "* #variable= 3 #constraint= 1\n+1 x1 +1 x2 +1 x3 >= 1 ;\n";
	private static final String STRING2 = "* #variable= 3 #constraint= 1\n"
			+ "min: +10 x2 +32 x3 ;\n+1 x1 +1 x2 +1 x3 >= 1 ;\n";

	@Test
	public void testNoMin() throws ContradictionException {
		IPBSolver solver = new OPBStringSolver();
		solver.newVar(3);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3);
		solver.addClause(clause);
		assertEquals(STRING1, solver.toString());
	}

	@Test
	public void testWithMin() throws ContradictionException {
		IPBSolver solver = new OPBStringSolver();
		solver.newVar(3);
		IVecInt clause = new VecInt();
		clause.push(1).push(2).push(3);
		solver.addClause(clause);
		IVecInt vars = new VecInt();
		vars.push(2).push(3);
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.push(BigInteger.TEN).push(BigInteger.valueOf(32));
		ObjectiveFunction obj = new ObjectiveFunction(vars, coeffs);
		solver.setObjectiveFunction(obj);
		assertEquals(STRING2, solver.toString());

	}
}
