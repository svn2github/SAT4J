package org.sat4j.pb.core;

import java.math.BigInteger;

import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

/**
 * Handle bounded positive integer variables through a binary decomposition.
 * 
 * @author lonca
 * 
 */
public class IntegerVariable {

	private final IVecInt vars;
	private final IPBSolver solver;

	public IntegerVariable(IPBSolver solver, BigInteger maxValue) {
		if (maxValue.compareTo(BigInteger.ZERO) <= 0) {
			throw new IllegalArgumentException(
					"the integer variable maximum value must be at least 1");
		}
		this.solver = solver;
		int nbVars = maxValue.bitLength();
		this.vars = new VecInt(nbVars);
		for (int i = 0; i < nbVars; ++i) {
			this.vars.push(solver.nextFreeVarId(true));
		}
	}

	public IVecInt getLits() {
		return this.vars;
	}

	public int nVars() {
		return this.vars.size();
	}

	/**
	 * @return this variable value through the last model found.
	 */
	public BigInteger getIntegerValue() {
		BigInteger res = BigInteger.ZERO;
		BigInteger factor = BigInteger.ONE;
		for (IteratorInt it = this.vars.iterator(); it.hasNext();) {
			if (this.solver.model(it.next())) {
				res = res.add(factor);
			}
			factor = factor.shiftLeft(1);
		}
		return res;
	}
}
