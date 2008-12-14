package org.sat4j.pb.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.core.LiteralsUtils;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.Solver;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;
/**
 * Helper class intended to make life easier to people to feed a 
 * sat solver programmatically.
 * 
 * @author daniel
 *
 * @param <T> The class of the objects to map into boolean variables.
 * @param <C> The class of the object to map to each constraint.
 */
public class DependencyHelper<T,C> implements SearchListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Map<T, Integer> mapToDimacs = new HashMap<T, Integer>();
	private final IVec<T> mapToDomain;
	final IVec<IConstr> constrs = new Vec<IConstr>();
	final IVec<C> descs = new Vec<C>();

	private int conflictingVariable;

	final XplainPB xplain;

	public DependencyHelper(IPBSolver solver, int maxvarid) {
		this.xplain = new XplainPB(solver);
		xplain.newVar(maxvarid);
		solver.setSearchListener(this);
		mapToDomain = new Vec<T>();
		mapToDomain.push(null);
	}

	int getIntValue(T thing) {
		Integer intValue = mapToDimacs.get(thing);
		if (intValue == null) {
			intValue = mapToDomain.size();
			mapToDomain.push(thing);
			mapToDimacs.put(thing, intValue);
		}
		return intValue;
	}

	public IVec<T> getSolution() {
		int[] model = xplain.model();
		IVec<T> toInstall = new Vec<T>();
		for (int i : model) {
			if (i > 0) {
				toInstall.push(mapToDomain.get(i));
			}
		}
		return toInstall;
	}

	public T getConflictingElement() {
		return mapToDomain.get(conflictingVariable);
	}

	public boolean hasASolution() throws TimeoutException {
		return xplain.isSatisfiable();
	}

	public Set<C> why() throws TimeoutException {
		IVecInt explanation = xplain.explain();
		Set<C> ezexplain = new TreeSet<C>();
		for (IteratorInt it = explanation.iterator(); it.hasNext();) {
			ezexplain.add(descs.get(it.next() - 1));
		}
		return ezexplain;
	}

	public Set<C> why(T thing) throws TimeoutException {
		IVecInt assumps = new VecInt();
		assumps.push(-getIntValue(thing));
		return why(assumps);
	}
	
	public Set<C> whyNot(T thing) throws TimeoutException {
		IVecInt assumps = new VecInt();
		assumps.push(getIntValue(thing));
		return why(assumps);
	}
	
	private Set<C> why(IVecInt assumps) throws TimeoutException {
		if (xplain.isSatisfiable(assumps)) {
			return Collections.emptySet();
		}
		IVecInt explanation = xplain.explain();
		Set<C> ezexplain = new TreeSet<C>();
		for (IteratorInt it = explanation.iterator(); it.hasNext();) {
			ezexplain.add(descs.get(it.next() - 1));
		}
		return ezexplain;
	}

	public void setTrue(T thing, C name) throws ContradictionException {
		IVecInt clause = new VecInt();
		clause.push(getIntValue(thing));
		constrs.push(xplain.addClause(clause));
		descs.push(name);
	}

	public void setFalse(T thing, C name) throws ContradictionException {
		IVecInt clause = new VecInt();
		clause.push(-getIntValue(thing));
		constrs.push(xplain.addClause(clause));
		descs.push(name);
	}

	public ImplicationRHS<T,C> implication(T... lhs) {
		IVecInt clause = new VecInt();
		for (T t : lhs) {
			clause.push(-getIntValue(t));
		}
		return new ImplicationRHS<T,C>(this, clause);
	}

	public ImplicationNamer<T,C> atMost(int i, T... things)
			throws ContradictionException {
		IVec<IConstr> toName = new Vec<IConstr>();
		IVecInt literals = new VecInt();
		for (T t : things) {
			literals.push(getIntValue(t));
		}
		toName.push(xplain.addAtMost(literals, i));
		return new ImplicationNamer<T,C>(this, toName);
	}

	public void adding(int p) {
		// TODO Auto-generated method stub

	}

	public void assuming(int p) {
		// TODO Auto-generated method stub

	}

	public void backtracking(int p) {
		// TODO Auto-generated method stub

	}

	public void beginLoop() {
	}

	public void conflictFound(IConstr confl) {
		conflictingVariable = Math.abs(LiteralsUtils.toDimacs(confl.get(0)));
	}

	public void conflictFound(int p) {
		conflictingVariable = Math.abs(LiteralsUtils.toDimacs(p));
	}

	public void delete(int[] clause) {
	}

	public void end(Lbool result) {
	}

	public void learn(IConstr c) {
	}

	public void propagating(int p, IConstr reason) {
	}

	public void solutionFound() {
	}

	public void start() {
	}
}
