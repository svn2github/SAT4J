package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.RemiUtils;

public class BugSAT16 {

	@Test
	public void testCNFCase() throws ContradictionException, TimeoutException {

		IPBSolver solver = SolverFactory.newDefault();

		// +1 x6 >= 1 [+1 x6 >= 1]
		solver.addClause(transform1(new int[] { 6 }));

		// +1 x6 +1 ~x5 >= 1 [+1 x6 -1 x5 >= 0]
		solver.addClause(transform1(new int[] { 6, -5 }));

		// +1 ~x6 +1 x5 >= 1 [-1 x6 +1 x5 >= 0]
		solver.addClause(transform1(new int[] { -6, 5 }));

		// +1 x6 +1 ~x4 >= 1 [+1 x6 -1 x4 >= 0]
		solver.addClause(transform1(new int[] { 6, -4 }));

		// +1 ~x6 +1 x4 >= 1 [-1 x6 +1 x4 >= 0]
		solver.addClause(transform1(new int[] { -6, 4 }));

		// +1 x4 +1 ~x2 +1 ~x1 >= 2 [+1 x4 -1 x2 -1 x1 >= 0]
		solver.addClause(transform1(new int[] { 4, -2, -1 }));

		// +1 ~x4 +1 x2 +1 x1 >= 1 [-1 x4 +1 x2 +1 x1 >= 0]
		solver.addClause(transform1(new int[] { -4, 2, 1 }));

		IVecInt backbone = RemiUtils.backbone(solver);
		assertEquals(3, backbone.size());
		assertTrue(backbone.contains(6));
		assertTrue(backbone.contains(5));
		assertTrue(backbone.contains(4));
	}

	@Test
	public void testPBCase() throws ContradictionException, TimeoutException {

		IPBSolver solver = SolverFactory.newDefault();

		// +1 x6 >= 1 [+1 x6 >= 1]
		solver.addPseudoBoolean(transform1(new int[] { 6 }),
				transform2(new int[] { 1 }), true, BigInteger.valueOf(1));

		// +1 x6 +1 ~x5 >= 1 [+1 x6 -1 x5 >= 0]
		solver.addPseudoBoolean(transform1(new int[] { 6, -5 }),
				transform2(new int[] { 1, 1 }), true, BigInteger.valueOf(1));

		// +1 ~x6 +1 x5 >= 1 [-1 x6 +1 x5 >= 0]
		solver.addPseudoBoolean(transform1(new int[] { -6, 5 }),
				transform2(new int[] { 1, 1 }), true, BigInteger.valueOf(1));

		// +1 x6 +1 ~x4 >= 1 [+1 x6 -1 x4 >= 0]
		solver.addPseudoBoolean(transform1(new int[] { 6, -4 }),
				transform2(new int[] { 1, 1 }), true, BigInteger.valueOf(1));

		// +1 ~x6 +1 x4 >= 1 [-1 x6 +1 x4 >= 0]
		solver.addPseudoBoolean(transform1(new int[] { -6, 4 }),
				transform2(new int[] { 1, 1 }), true, BigInteger.valueOf(1));

		// +1 x4 +1 ~x2 +1 ~x1 >= 2 [+1 x4 -1 x2 -1 x1 >= 0]
		solver.addPseudoBoolean(transform1(new int[] { 4, -2, -1 }),
				transform2(new int[] { 1, 1, 1 }), true, BigInteger.valueOf(2));

		// +1 ~x4 +1 x2 +1 x1 >= 1 [-1 x4 +1 x2 +1 x1 >= 0]
		solver.addPseudoBoolean(transform1(new int[] { -4, 2, 1 }),
				transform2(new int[] { 1, 1, 1 }), true, BigInteger.valueOf(1));

		IVecInt backbone = RemiUtils.backbone(solver);
		assertEquals(3, backbone.size());
		assertTrue(backbone.contains(6));
		assertTrue(backbone.contains(5));
		assertTrue(backbone.contains(4));
	}

	static IVecInt transform1(int[] intArray) {
		return new VecInt(intArray);
	}

	static IVec<BigInteger> transform2(int[] intArray) {
		BigInteger[] result = new BigInteger[intArray.length];
		for (int i = 0; i < intArray.length; i++) {
			result[i] = BigInteger.valueOf(intArray[i]);
		}
		return new Vec<BigInteger>(result);
	}
}
