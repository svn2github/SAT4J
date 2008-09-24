package org.sat4j.tools;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * An implementation of the QuickXplain algorithm as explained by Ulrich Junker in the following
 * paper:
 * 
 * @inproceedings{ junker01:quickxplain:inp,
 *	author={Ulrich Junker},
 *	title={QUICKXPLAIN: Conflict Detection for Arbitrary Constraint Propagation Algorithms},
 *	booktitle={IJCAI'01 Workshop on Modelling and Solving problems with constraints (CONS-1)},
 *	year={2001},
 *	month={August},
 *	address={Seattle, WA, USA},
 *	url={citeseer.ist.psu.edu/junker01quickxplain.html},
 *	url={http://www.lirmm.fr/~bessiere/ws_ijcai01/junker.ps.gz} }
 * 
 * The algorithm has been adapted to work properly in a context where we can afford to
 * add a selector variable to each clause to enable or disable each constraint.
 * 
 * @author daniel
 *
 * @param <T> a subinterface to ISolver.
 */
public class QuickXplain<T extends ISolver> extends SolverDecorator<T> {

	protected int nborigvars;
	protected int nbnewvar;

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
		return super.addClause(literals);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IVecInt explain() throws TimeoutException {
		assert !isSatisfiable();
		IVecInt extraVariables = new VecInt();
		for (int p = 1; p <=nbnewvar; p++) {
			extraVariables.push(p + nborigvars);
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
					int tmp = extraVariables.get(startingPoint);
					extraVariables.moveTo(startingPoint, i);
					extraVariables.set(i, tmp);
					for (int j = startingPoint + 1; j <= i; j++) {
						extraVariables.set(j, -extraVariables.get(j));
					}
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
		IVecInt clauseNumbers = new VecInt();
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
}
