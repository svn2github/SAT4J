package org.sat4j.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

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
public class QuickXplain<T extends ISolver> extends SolverDecorator<T> {

	protected int nborigvars;
	protected int nbnewvar;

	protected IVec<IConstr> constrs = new Vec<IConstr>();

	public QuickXplain(T solver) {
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
		assert constr.size() == nbnewvar;
		return constr;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static class Pair implements Comparable<Pair> {
		int id;
		double activity;

		public Pair(int id, double activity) {
			this.id = id;
			this.activity = activity;
		}

		public int compareTo(Pair p) {
			if (activity > p.activity)
				return -1;
			if (activity < p.activity)
				return 1;
			return 0;
		}
		@Override
		public String toString() {
			return Integer.toString(id)+"("+activity+")";
		}
	}

	public IVecInt explain() throws TimeoutException {
		assert !isSatisfiable();
		List<Pair> pairs = new ArrayList<Pair>(nbnewvar);
		IConstr constr;
		for (int i = 0; i < nbnewvar; i++) {
			constr = constrs.get(i);
			if (constr != null) {
				pairs.add(new Pair(i + 1, constr.getActivity()));
			} else {
				pairs.add(new Pair(i + 1, 0.0));
			}
		}
		Collections.sort(pairs);
		IVecInt extraVariables = new VecInt(nbnewvar);
		// for (int p = 1; p <=nbnewvar; p++) {
		// extraVariables.push(p + nborigvars);
		// }
		for (Pair p : pairs) {
			extraVariables.push(p.id + nborigvars);
		}
		boolean shouldContinue;
		int startingPoint = 0;
		do {
			shouldContinue = false;
			int i = startingPoint;
			extraVariables.set(i, -extraVariables.get(i));
			assert extraVariables.get(i) < 0;
			while (super.isSatisfiable(extraVariables)) {
				i++;
				assert extraVariables.get(i) > 0;
				extraVariables.set(i, -extraVariables.get(i));
			}
			if (i > startingPoint) {
				assert !super.isSatisfiable(extraVariables);
				if (i < extraVariables.size()) {
					int tmp = extraVariables.get(i);
					for (int j = i; j >= startingPoint + 1; j--) {
						extraVariables.set(j, -extraVariables.get(j-1));
					}
					extraVariables.set(startingPoint,tmp);
				}
				startingPoint++;
				shouldContinue = true;
			} else {
				assert i == startingPoint;
				if (!super.isSatisfiable(extraVariables)) {
					startingPoint++;
				}
			}
		} while (shouldContinue);
		IVecInt clauseNumbers = new VecInt(startingPoint);
		for (int i = 0; i < startingPoint; i++) {
			clauseNumbers.push(-extraVariables.get(i) - nborigvars);
		}
		return clauseNumbers;
	}

	@Override
	public void reset() {
		super.reset();
		nbnewvar = 0;
	}

	@Override
	public int[] findModel() throws TimeoutException {
		IVecInt extraVariables = new VecInt();
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.findModel(extraVariables);
	}

	@Override
	public int[] findModel(IVecInt assumps) throws TimeoutException {
		IVecInt extraVariables = new VecInt();
		assumps.copyTo(extraVariables);
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.findModel(extraVariables);
	}

	@Override
	public boolean isSatisfiable() throws TimeoutException {
		IVecInt extraVariables = new VecInt();
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.isSatisfiable(extraVariables);
	}

	@Override
	public boolean isSatisfiable(boolean global) throws TimeoutException {
		IVecInt extraVariables = new VecInt();
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.isSatisfiable(extraVariables, global);
	}

	@Override
	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
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
		IVecInt extraVariables = new VecInt();
		assumps.copyTo(extraVariables);
		for (int p = 0; p < nbnewvar; p++) {
			extraVariables.push(-(p + nborigvars + 1));
		}
		return super.isSatisfiable(extraVariables, global);
	}
}
