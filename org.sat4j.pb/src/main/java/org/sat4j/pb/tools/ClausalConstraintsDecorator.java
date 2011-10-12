package org.sat4j.pb.tools;

import java.math.BigInteger;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.tools.ClausalCardinalitiesDecorator;

public class ClausalConstraintsDecorator extends
		ClausalCardinalitiesDecorator<IPBSolver> implements IPBSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final IPBSolver pbsolver;

	public ClausalConstraintsDecorator(IPBSolver pbsolver) {
		super(pbsolver);
		this.pbsolver = pbsolver;
	}

	public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
			boolean moreThan, BigInteger d) throws ContradictionException {
		return pbsolver.addPseudoBoolean(lits, coeffs, moreThan, d);
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		pbsolver.setObjectiveFunction(obj);
	}

	public ObjectiveFunction getObjectiveFunction() {
		return pbsolver.getObjectiveFunction();
	}

	public IConstr addAtMost(IVecInt literals, IVecInt coeffs, int degree)
			throws ContradictionException {
		return pbsolver.addAtMost(literals, coeffs, degree);
	}

	public IConstr addAtMost(IVecInt literals, IVec<BigInteger> coeffs,
			BigInteger degree) throws ContradictionException {
		return pbsolver.addAtMost(literals, coeffs, degree);
	}

	public IConstr addAtLeast(IVecInt literals, IVecInt coeffs, int degree)
			throws ContradictionException {
		return pbsolver.addAtLeast(literals, coeffs, degree);
	}

	public IConstr addAtLeast(IVecInt literals, IVec<BigInteger> coeffs,
			BigInteger degree) throws ContradictionException {
		return pbsolver.addAtLeast(literals, coeffs, degree);
	}

	public IConstr addExactly(IVecInt literals, IVecInt coeffs, int weight)
			throws ContradictionException {
		return pbsolver.addExactly(literals, coeffs, weight);
	}

	public IConstr addExactly(IVecInt literals, IVec<BigInteger> coeffs,
			BigInteger weight) throws ContradictionException {
		return pbsolver.addExactly(literals, coeffs, weight);
	}
}
