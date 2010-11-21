package org.sat4j.pb.tools;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.core.Vec;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.LexicoDecorator;

public class LexicoDecoratorPB extends LexicoDecorator<IPBSolver> implements
		IPBSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final List<ObjectiveFunction> objs = new ArrayList<ObjectiveFunction>();

	public LexicoDecoratorPB(IPBSolver solver) {
		super(solver);
	}

	public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
			boolean moreThan, BigInteger d) throws ContradictionException {
		return decorated().addPseudoBoolean(lits, coeffs, moreThan, d);
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		throw new UnsupportedOperationException();

	}

	public ObjectiveFunction getObjectiveFunction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean admitABetterSolution(IVecInt assumps)
			throws TimeoutException {
		decorated().setObjectiveFunction(objs.get(currentCriterion));
		return super.admitABetterSolution(assumps);
	}

	@Override
	public void addCriterion(IVecInt literals) {
		objs.add(new ObjectiveFunction(literals, new Vec<BigInteger>(literals
				.size(), BigInteger.TEN)));
		super.addCriterion(literals);
	}

}
