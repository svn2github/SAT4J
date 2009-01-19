package org.sat4j.tools.xplain;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolverDecorator;

/**
 * An implementation of the QuickXplain algorithm as explained by Ulrich Junker
 * in the following paper:
 * 
 * @inproceedings{ junker01:quickxplain:inp, author={Ulrich Junker},
 *                 title={QUICKXPLAIN: Conflict Detection for Arbitrary
 *                 Constraint Propagation Algorithms}, booktitle={IJCAI'01
 *                 Workshop on Modelling and Solving problems with constraints
 *                 (CONS-1)}, year={2001}, month={August}, address={Seattle, WA,
 *                 USA}, url={citeseer.ist.psu.edu/junker01quickxplain.html},
 *                 url={http://www.lirmm.fr/~bessiere/ws_ijcai01/junker.ps.gz} }
 * 
 *                 The algorithm has been adapted to work properly in a context
 *                 where we can afford to add a selector variable to each clause
 *                 to enable or disable each constraint.
 * 
 * Note that for the moment, QuickXplain does not work properly in an optimization setting.
 * 
 * @author daniel
 * 
 * @param <T>
 *            a subinterface to ISolver.
 */
public class Xplain<T extends ISolver> extends SolverDecorator<T> {

	protected int nborigvars;
	protected int nbnewvar;

	protected IVec<IConstr> constrs = new Vec<IConstr>();

	protected IVecInt assump;
	
	private static final XplainStrategy xplainStrategy = new ReplayXplainStrategy();
	
	public Xplain(T solver) {
		super(solver);
	}

	@Override
	public int newVar(int howmany) {
		nborigvars = super.newVar(howmany);
		return nborigvars;
	}

	@Override
	public IConstr addClause(IVecInt literals) throws ContradictionException {
		int newvar = nborigvars + ++nbnewvar;
		literals.push(newvar);
		IConstr constr = super.addClause(literals);
		constrs.push(constr);
		assert constrs.size() == nbnewvar : ""+constrs.size()+"!="+nbnewvar;
		return constr;
	}

	@Override
	public IConstr addAtLeast(IVecInt literals, int degree)
			throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IConstr addAtMost(IVecInt literals, int degree)
			throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IVecInt explain() throws TimeoutException {
		assert !isSatisfiable(assump);
		return xplainStrategy.explain(decorated(),nbnewvar,nborigvars,constrs, assump);
	}

	public IVec<IConstr> getConstraints() {
		return constrs;
	}
	
	public int getMaxOriginalVarId() {
		return nborigvars;
	}
	
	@Override
	public void reset() {
		super.reset();
		nbnewvar = 0;
	}

	@Override
	public int[] findModel() throws TimeoutException {
		assump = VecInt.EMPTY;
		IVecInt extraVariables = new VecInt();
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.findModel(extraVariables);
	}

	@Override
	public int[] findModel(IVecInt assumps) throws TimeoutException {
		assump = assumps;
		IVecInt extraVariables = new VecInt();
		assumps.copyTo(extraVariables);
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.findModel(extraVariables);
	}

	@Override
	public boolean isSatisfiable() throws TimeoutException {
		assump = VecInt.EMPTY;
		IVecInt extraVariables = new VecInt();
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.isSatisfiable(extraVariables);
	}

	@Override
	public boolean isSatisfiable(boolean global) throws TimeoutException {
		assump = VecInt.EMPTY;
		IVecInt extraVariables = new VecInt();
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.isSatisfiable(extraVariables, global);
	}

	@Override
	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
		assump = assumps;
		IVecInt extraVariables = new VecInt();
		assumps.copyTo(extraVariables);
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.isSatisfiable(extraVariables);
	}

	@Override
	public boolean isSatisfiable(IVecInt assumps, boolean global)
			throws TimeoutException {
		assump = assumps;
		IVecInt extraVariables = new VecInt();
		assumps.copyTo(extraVariables);
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.isSatisfiable(extraVariables, global);
	}

	@Override
	public int[] model() {
		int [] fullmodel = super.model();
		int end = Math.min(nborigvars,fullmodel.length)-1;
        while (Math.abs(fullmodel[end]) > nborigvars)
            end--;
        int [] model = new int[end + 1];
        for (int i = 0; i <= end; i++) {
            model[i] = fullmodel[i];
        }
        return model;
	}
	
	
}
