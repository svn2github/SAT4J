package org.sat4j.pb.tools;

import java.math.BigInteger;
import java.util.Iterator;

import org.sat4j.core.Vec;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.tools.xplain.Xplain;

public class XplainPB extends Xplain<IPBSolver> implements IPBSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XplainPB(IPBSolver solver) {
		super(solver);
	}


	@Override
	public IConstr addAtMost(IVecInt literals, int degree)
			throws ContradictionException {
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		coeffs.growTo(literals.size(), BigInteger.ONE);
		int newvar = nborigvars + ++nbnewvar;
		literals.push(newvar);
		BigInteger coef = BigInteger.valueOf(degree-coeffs.size());
		coeffs.push(coef);
		return decorated().addPseudoBoolean(literals, coeffs, false, BigInteger.valueOf(degree));
	}


	public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
			boolean moreThan, BigInteger d) throws ContradictionException {
		int newvar = nborigvars + ++nbnewvar;		
		lits.push(newvar);
		if (moreThan && d.signum()>=0) {		
			coeffs.push(d);
		} else {
			BigInteger sum = BigInteger.ZERO;
			for (Iterator<BigInteger> ite = coeffs.iterator() ; ite.hasNext();)
				sum = sum.add(ite.next());
			sum = sum.subtract(d);
			coeffs.push(sum.negate());
		}
		IConstr constr =  decorated().addPseudoBoolean(lits, coeffs, moreThan, d);
		if (constr==null) {
			// constraint trivially satisfied
			nbnewvar--;
			// System.err.println(lits.toString()+"/"+coeffs+"/"+(moreThan?">=":"<=")+d);
		} else {
			constrs.push(constr);
		}
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
