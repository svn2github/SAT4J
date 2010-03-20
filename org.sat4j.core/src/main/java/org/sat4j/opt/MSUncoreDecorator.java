package org.sat4j.opt;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolverDecorator;

public class MSUncoreDecorator extends SolverDecorator<ISolver> implements
		IOptimizationProblem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int nborigvars;

	private int nbexpectedclauses;

	private int nbnewvar;

	public MSUncoreDecorator(ISolver solver) {
		super(solver);
	}

	@Override
	public int newVar(int howmany) {
		nborigvars = super.newVar(howmany);
		return nborigvars;
	}

	@Override
	public void setExpectedNumberOfClauses(int nb) {
		nbexpectedclauses = nb;
		super.setExpectedNumberOfClauses(nb);
		super.newVar(nborigvars + nbexpectedclauses);
		lits.ensure(nb);

	}

	public int getExpectedNumberOfClauses() {
		return nbexpectedclauses;
	}

	@Override
	public IConstr addClause(IVecInt literals) throws ContradictionException {
		int newvar = nborigvars + ++nbnewvar;
		lits.push(newvar);
		literals.push(newvar);
		return super.addClause(literals);
	}

	@Override
	public void reset() {
		nbnewvar = 0;
		lits.clear();
		super.reset();
	}

	public boolean hasNoObjectiveFunction() {
		return false;
	}

	public boolean nonOptimalMeansSatisfiable() {
		return false;
	}

	public Number calculateObjective() {
		return counter;
	}

	private final IVecInt lits = new VecInt();

	private final IVecInt negLits = new VecInt();

	private int counter;

	public Number getObjectiveValue() {
		return counter;
	}

	public void forceObjectiveValueTo(Number forcedValue)
			throws ContradictionException {
		throw new UnsupportedOperationException();

	}

	public void discard() throws ContradictionException {
		discardCurrentSolution();
	}

	public void discardCurrentSolution() throws ContradictionException {
		IVecInt explanation = unsatExplanation();
		IVecInt literals = new VecInt(explanation.size());
		int p;
		for (int i = 0; i < explanation.size(); i++) {
			p = explanation.get(i);
			literals.push(-p);
			negLits.remove(-p);
		}
		addAtMost(literals, 1);
		counter++;
	}

	public boolean admitABetterSolution() throws TimeoutException {
		if (negLits.isEmpty()) {
			for (int i = 0; i < lits.size(); i++) {
				negLits.push(-lits.get(i));
			}
		}
		return admitABetterSolution(negLits);
	}

	public boolean admitABetterSolution(IVecInt assumps)
			throws TimeoutException {
		return !isSatisfiable(assumps, true);
	}

}
