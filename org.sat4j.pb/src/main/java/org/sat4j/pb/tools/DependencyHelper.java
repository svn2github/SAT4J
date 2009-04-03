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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.GateTranslator;

/**
 * Helper class intended to make life easier to people to feed a sat solver
 * programmatically.
 * 
 * @author daniel
 * 
 * @param <T>
 *            The class of the objects to map into boolean variables.
 * @param <C>
 *            The class of the object to map to each constraint.
 */
public class DependencyHelper<T, C> {

	public final INegator<T> NO_NEGATION = new INegator<T>() {

		public boolean isNegated(T thing) {
			return false;
		}

		public T unNegate(T thing) {
			return thing;
		}
	};

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Map<T, Integer> mapToDimacs = new HashMap<T, Integer>();
	private final Map<Integer, T> mapToDomain = new HashMap<Integer, T>();
	final Map<IConstr, C> descs = new HashMap<IConstr, C>();

	final XplainPB xplain;
	private final GateTranslator gator;

	private INegator<T> negator = NO_NEGATION;

	/**
	 * 
	 * @param solver
	 *            the solver to be used to solve the problem.
	 */
	public DependencyHelper(IPBSolver solver) {
		this.xplain = new XplainPB(solver);
		this.gator = new GateTranslator(xplain);
	}

	public void setNegator(INegator<T> negator) {
		this.negator = negator;
	}

	/**
	 * Translate a domain object into a dimacs variable.
	 * 
	 * @param thing
	 *            a domain object
	 * @return the dimacs variable (an integer) representing that domain object.
	 */
	int getIntValue(T thing) {
		T myThing;
		boolean negated = negator.isNegated(thing);
		if (negated) {
			myThing = negator.unNegate(thing);
		} else {
			myThing = thing;
		}
		Integer intValue = mapToDimacs.get(myThing);
		if (intValue == null) {
			intValue = xplain.nextFreeVarId(true);
			mapToDomain.put(intValue, thing);
			mapToDimacs.put(thing, intValue);
		}
		if (negated) {
			return -intValue;
		}
		return intValue;
	}

	/**
	 * Retrieve the solution found.
	 * 
	 * THAT METHOD IS EXPECTED TO BE CALLED IF hasASolution() RETURNS TRUE.
	 * 
	 * @return the domain object that must be satisfied to satisfy the
	 *         constraints entered in the solver.
	 * @see {@link #hasASolution()}
	 */
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

	/**
	 * Retrieve the boolean value associated with a domain object in the
	 * solution found by the solver. THAT METHOD IS EXPECTED TO BE CALLED IF
	 * hasASolution() RETURNS TRUE.
	 * 
	 * @param t
	 *            a domain object
	 * @return true iff the domain object has been set to true in the current
	 *         solution.
	 */
	public boolean getBooleanValueFor(T t) {
		return xplain.model(getIntValue(t));
	}

	/**
	 * 
	 * @return true if the set of constraints entered inside the solver can be
	 *         satisfied.
	 * @throws TimeoutException
	 */
	public boolean hasASolution() throws TimeoutException {
		return xplain.isSatisfiable();
	}

	/**
	 * 
	 * @return true if the set of constraints entered inside the solver can be
	 *         satisfied.
	 * @throws TimeoutException
	 */
	public boolean hasASolution(IVec<T> assumps) throws TimeoutException {
		IVecInt assumptions = new VecInt();
		for (Iterator<T> it = assumps.iterator(); it.hasNext();) {
			assumptions.push(getIntValue(it.next()));
		}
		return xplain.isSatisfiable(assumptions);
	}

	/**
	 * 
	 * @return true if the set of constraints entered inside the solver can be
	 *         satisfied.
	 * @throws TimeoutException
	 */
	public boolean hasASolution(Collection<T> assumps) throws TimeoutException {
		IVecInt assumptions = new VecInt();
		for (T t : assumps) {
			assumptions.push(getIntValue(t));
		}
		return xplain.isSatisfiable(assumptions);
	}

	/**
	 * Explain the reason of the inconsistency of the set of constraints.
	 * 
	 * THAT METHOD IS EXPECTED TO BE CALLED IF hasASolution() RETURNS FALSE.
	 * 
	 * @return a set of objects used to "name" each constraint entered in the
	 *         solver.
	 * @throws TimeoutException
	 * @see {@link #hasASolution()}
	 */
	public Set<C> why() throws TimeoutException {
		Collection<IConstr> explanation = xplain.explain();
		Set<C> ezexplain = new TreeSet<C>();
		for (IConstr constr : explanation) {
			C desc = descs.get(constr);
			if (desc != null) {
				ezexplain.add(desc);
			}
		}
		return ezexplain;
	}

	/**
	 * Explain a domain object has been set to true in a solution.
	 * 
	 * @return a set of objects used to "name" each constraint entered in the
	 *         solver.
	 * @throws TimeoutException
	 * @see {@link #hasASolution()}
	 */
	public Set<C> why(T thing) throws TimeoutException {
		IVecInt assumps = new VecInt();
		assumps.push(-getIntValue(thing));
		return why(assumps);
	}

	/**
	 * Explain a domain object has been set to false in a solution.
	 * 
	 * @return a set of objects used to "name" each constraint entered in the
	 *         solver.
	 * @throws TimeoutException
	 * @see {@link #hasASolution()}
	 */
	public Set<C> whyNot(T thing) throws TimeoutException {
		IVecInt assumps = new VecInt();
		assumps.push(getIntValue(thing));
		return why(assumps);
	}

	private Set<C> why(IVecInt assumps) throws TimeoutException {
		if (xplain.isSatisfiable(assumps)) {
			return Collections.emptySet();
		}
		return why();
	}

	/**
	 * Add a constraint to set the value of a domain object to true.
	 * 
	 * @param thing
	 *            the domain object
	 * @param name
	 *            the name of the constraint, to be used in an explanation if
	 *            needed.
	 * @throws ContradictionException
	 *             if the set of constraints appears to be trivially
	 *             inconsistent.
	 */
	public void setTrue(T thing, C name) throws ContradictionException {
		descs.put(gator.gateTrue(getIntValue(thing)), name);
	}

	/**
	 * Add a constraint to set the value of a domain object to false.
	 * 
	 * @param thing
	 *            the domain object
	 * @param name
	 *            the name of the constraint, to be used in an explanation if
	 *            needed.
	 * @throws ContradictionException
	 *             if the set of constraints appears to be trivially
	 *             inconsistent.
	 */
	public void setFalse(T thing, C name) throws ContradictionException {
		descs.put(gator.gateFalse(getIntValue(thing)), name);
	}

	/**
	 * Create a logical implication of the form lhs -> rhs
	 * 
	 * @param lhs
	 *            some domain objects. They form a conjunction in the left hand
	 *            side of the implication.
	 * @return the right hand side of the implication.
	 */
	public ImplicationRHS<T, C> implication(T... lhs) {
		IVecInt clause = new VecInt();
		for (T t : lhs) {
			clause.push(-getIntValue(t));
		}
		return new ImplicationRHS<T, C>(this, clause);
	}

	public DisjunctionRHS<T, C> disjunction(T... lhs) {
		IVecInt literals = new VecInt();
		for (T t : lhs) {
			literals.push(-getIntValue(t));
		}
		return new DisjunctionRHS<T, C>(this, literals);
	}

	/**
	 * Create a constraint stating that at most i domain object should be set to
	 * true.
	 * 
	 * @param i
	 *            the maximum number of domain object to set to true.
	 * @param things
	 *            the domain objects.
	 * @return an object used to name the constraint. The constraint MUST BE
	 *         NAMED.
	 * @throws ContradictionException
	 */
	public ImplicationNamer<T, C> atMost(int i, T... things)
			throws ContradictionException {
		IVec<IConstr> toName = new Vec<IConstr>();
		IVecInt literals = new VecInt();
		for (T t : things) {
			literals.push(getIntValue(t));
		}
		toName.push(xplain.addAtMost(literals, i));
		return new ImplicationNamer<T, C>(this, toName);
	}

	/**
	 * Create a constraint using equivalency chains thing <=> (thing1 <=> thing2
	 * <=> ... <=> thingn)
	 * 
	 * @param thing
	 *            a domain object
	 * @param things
	 *            other domain objects.
	 * @throws ContradictionException
	 */
	public void iff(C name, T thing, T... otherThings)
			throws ContradictionException {
		IVecInt literals = new VecInt(otherThings.length);
		for (T t : otherThings) {
			literals.push(getIntValue(t));
		}
		IConstr[] constrs = gator.iff(getIntValue(thing), literals);
		for (IConstr constr : constrs) {
			descs.put(constr, name);
		}
	}

	/**
	 * Create a constraint of the form thing <=> (thing1 and thing 2 ... and
	 * thingn)
	 * 
	 * @param name
	 * @param thing
	 * @param otherThings
	 * @throws ContradictionException
	 */
	public void and(C name, T thing, T... otherThings)
			throws ContradictionException {
		IVecInt literals = new VecInt(otherThings.length);
		for (T t : otherThings) {
			literals.push(getIntValue(t));
		}
		IConstr[] constrs = gator.and(getIntValue(thing), literals);
		for (IConstr constr : constrs) {
			descs.put(constr, name);
		}
	}

	/**
	 * Create a constraint of the form thing <=> (thing1 and thing 2 ... and
	 * thingn)
	 * 
	 * @param name
	 * @param thing
	 * @param otherThings
	 * @throws ContradictionException
	 */
	public void or(C name, T thing, T... otherThings)
			throws ContradictionException {
		IVecInt literals = new VecInt(otherThings.length);
		for (T t : otherThings) {
			literals.push(getIntValue(t));
		}
		IConstr[] constrs = gator.or(getIntValue(thing), literals);
		for (IConstr constr : constrs) {
			descs.put(constr, name);
		}
	}

	/**
	 * Create a constraint of the form thing <=> (if conditionThing then
	 * thenThing else elseThing)
	 * 
	 * @param name
	 * @param thing
	 * @param otherThings
	 * @throws ContradictionException
	 */
	public void ifThenElse(C name, T thing, T conditionThing, T thenThing,
			T elseThing) throws ContradictionException {
		IConstr[] constrs = gator.ite(getIntValue(thing),
				getIntValue(conditionThing), getIntValue(thenThing),
				getIntValue(elseThing));
		for (IConstr constr : constrs) {
			descs.put(constr, name);
		}
	}

	/**
	 * Add an objective function to ask for a solution that minimize the
	 * objective function.
	 * 
	 * @param wobj
	 *            a set of weighted objects (pairs of domain object and
	 *            BigInteger).
	 */
	public void setObjectiveFunction(WeightedObject<T>... wobj) {
		IVecInt literals = new VecInt(wobj.length);
		IVec<BigInteger> coefs = new Vec<BigInteger>(wobj.length);
		for (WeightedObject<T> wo : wobj) {
			literals.push(getIntValue(wo.thing));
			coefs.push(wo.getWeight());
		}
		ObjectiveFunction obj = new ObjectiveFunction(literals, coefs);
		xplain.setObjectiveFunction(obj);
	}

	/**
	 * Stop the SAT solver that is looking for a solution. The solver will throw
	 * a TimeoutException.
	 */
	public void stopSolver() {
		xplain.expireTimeout();
	}

	/**
	 * Stop the explanation computation. A TimeoutException will be thrown by
	 * the explanation algorithm.
	 */
	public void stopExplanation() {
		xplain.cancelExplanation();
	}
}
