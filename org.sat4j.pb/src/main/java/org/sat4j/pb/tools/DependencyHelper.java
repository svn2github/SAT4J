/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
*******************************************************************************/

package org.sat4j.pb.tools;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
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
public class DependencyHelper<T,C> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Map<T, Integer> mapToDimacs = new HashMap<T, Integer>();
	private final IVec<T> mapToDomain;
	final IVec<IConstr> constrs = new Vec<IConstr>();
	final IVec<C> descs = new Vec<C>();

	final XplainPB xplain;
	
	public DependencyHelper(IPBSolver solver, int maxvarid) {
		this.xplain = new XplainPB(solver);
		xplain.newVar(maxvarid);
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

	public void setObjectiveFunction(WeightedObject<T> ... wobj) {
		IVecInt literals = new VecInt(wobj.length);
		IVec<BigInteger> coefs = new Vec<BigInteger>(wobj.length);
		for (WeightedObject<T> wo : wobj) {
			literals.push(getIntValue(wo.thing));
			coefs.push(wo.getWeight());
		}
		ObjectiveFunction obj = new ObjectiveFunction(literals,coefs);
		xplain.setObjectiveFunction(obj);
	}
}
