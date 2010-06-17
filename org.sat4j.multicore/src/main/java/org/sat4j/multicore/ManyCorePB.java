package org.sat4j.multicore;

import java.math.BigInteger;

import org.sat4j.core.ASolverFactory;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.tools.ConstrGroup;

public class ManyCorePB extends ManyCore<IPBSolver> implements IPBSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ManyCorePB(ASolverFactory<IPBSolver> factory, String... solverNames) {
		super(factory, solverNames);
	}

	public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
			boolean moreThan, BigInteger d) throws ContradictionException {
		ConstrGroup group = new ConstrGroup(false);
		for (int i = 0; i < numberOfSolvers; i++) {
			group.add(solvers.get(i)
					.addPseudoBoolean(lits, coeffs, moreThan, d));
		}
		return group;
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		for (int i = 0; i < numberOfSolvers; i++) {
			solvers.get(i).setObjectiveFunction(obj);
		}
	}

	public ObjectiveFunction getObjectiveFunction() {
		return solvers.get(0).getObjectiveFunction();
	}

}
