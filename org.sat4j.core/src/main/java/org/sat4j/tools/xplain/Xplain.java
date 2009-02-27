package org.sat4j.tools.xplain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
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
 *                 Note that for the moment, QuickXplain does not work properly
 *                 in an optimization setting.
 * 
 * @author daniel
 * 
 * @param <T>
 *            a subinterface to ISolver.
 */
public class Xplain<T extends ISolver> extends SolverDecorator<T> {

	protected Map<Integer, IConstr> constrs = new HashMap<Integer, IConstr>();

	protected IVecInt assump;

	private int lastCreatedVar;
	private boolean pooledVarId = false;
	
	private static final XplainStrategy xplainStrategy = new ReplayXplainStrategy();

	public Xplain(T solver) {
		super(solver);
	}

	@Override
	public IConstr addClause(IVecInt literals) throws ContradictionException {
		int newvar = createNewVar(literals);
		literals.push(newvar);
		IConstr constr = super.addClause(literals);
		constrs.put(newvar, constr);
		return constr;
	}

	protected int createNewVar(IVecInt literals) {
		if (pooledVarId) {
			pooledVarId = false;
			return lastCreatedVar; 
		}
		lastCreatedVar = nextFreeVarId(true);
		return lastCreatedVar;
	}

	protected void discardLastestVar() {
		pooledVarId = true;
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

	public Collection<IConstr> explain() throws TimeoutException {
		assert !isSatisfiable(assump);
		ISolver solver = decorated();
		if (solver instanceof IOptimizationProblem) {
			solver = ((SolverDecorator<? extends ISolver>) solver).decorated();
		}
		IVecInt keys = xplainStrategy.explain(solver, constrs.keySet(), assump);
		Collection<IConstr> explanation = new ArrayList<IConstr>(keys.size());
		for (IteratorInt it = keys.iterator(); it.hasNext();) {
			explanation.add(constrs.get(it.next()));
		}
		return explanation;
	}

	public Collection<IConstr> getConstraints() {
		return constrs.values();
	}

	@Override
	public int[] findModel() throws TimeoutException {
		assump = VecInt.EMPTY;
		IVecInt extraVariables = new VecInt();
		for (Integer p : constrs.keySet()) {
			extraVariables.push(-p);
		}
		return super.findModel(extraVariables);
	}

	@Override
	public int[] findModel(IVecInt assumps) throws TimeoutException {
		assump = assumps;
		IVecInt extraVariables = new VecInt();
		assumps.copyTo(extraVariables);
		for (Integer p : constrs.keySet()) {
			extraVariables.push(-p);
		}
		return super.findModel(extraVariables);
	}

	@Override
	public boolean isSatisfiable() throws TimeoutException {
		assump = VecInt.EMPTY;
		IVecInt extraVariables = new VecInt();
		for (Integer p : constrs.keySet()) {
			extraVariables.push(-p);
		}
		return super.isSatisfiable(extraVariables);
	}

	@Override
	public boolean isSatisfiable(boolean global) throws TimeoutException {
		assump = VecInt.EMPTY;
		IVecInt extraVariables = new VecInt();
		for (Integer p : constrs.keySet()) {
			extraVariables.push(-p);
		}
		return super.isSatisfiable(extraVariables, global);
	}

	@Override
	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
		assump = assumps;
		IVecInt extraVariables = new VecInt();
		assumps.copyTo(extraVariables);
		for (Integer p : constrs.keySet()) {
			extraVariables.push(-p);
		}
		return super.isSatisfiable(extraVariables);
	}

	@Override
	public boolean isSatisfiable(IVecInt assumps, boolean global)
			throws TimeoutException {
		assump = assumps;
		IVecInt extraVariables = new VecInt();
		assumps.copyTo(extraVariables);
		for (Integer p : constrs.keySet()) {
			extraVariables.push(-p);
		}
		return super.isSatisfiable(extraVariables, global);
	}

	@Override
	public int[] model() {
		int[] fullmodel = super.model();
		// int[] model = new int[fullmodel.length - constrs.size()];
		IVecInt vmodel = new VecInt(fullmodel.length - constrs.size());
		for (int i = 0; i < fullmodel.length; i++) {
			if (constrs.get(Math.abs(fullmodel[i])) == null) {
				vmodel.push(fullmodel[i]);
			}
		}
		int[] model = new int[vmodel.size()];
		vmodel.copyTo(model);
		return model;
	}

}
