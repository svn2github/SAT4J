package org.sat4j.pb.tools;

import java.math.BigInteger;
import java.util.Iterator;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.tools.QuickXplain;

public class QuickXplainPB extends QuickXplain<IPBSolver> implements IPBSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QuickXplainPB(IPBSolver solver) {
		super(solver);
	}

	public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
			boolean moreThan, BigInteger d) throws ContradictionException {
		int newvar = nborigvars + ++nbnewvar;
		lits.push(newvar);
		if (moreThan) {
			coeffs.push(d);
		} else {
			BigInteger sum = BigInteger.ZERO;
			for (Iterator<BigInteger> ite = coeffs.iterator() ; ite.hasNext();)
				sum = sum.add(ite.next().abs());
			sum = sum.subtract(d.abs());
			coeffs.push(sum);
			// throw new UnsupportedOperationException();
		}
		IConstr constr =  decorated().addPseudoBoolean(lits, coeffs, moreThan, d);
		constrs.push(constr);
		assert constrs.size() == nbnewvar;
		return constr;
	}

	public String getExplanation() {
		return decorated().getExplanation();
	}

	public void setListOfVariablesForExplanation(IVecInt listOfVariables) {
		decorated().setListOfVariablesForExplanation(listOfVariables);
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		decorated().setObjectiveFunction(obj);
	}


}
