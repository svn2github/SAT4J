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

import org.sat4j.core.Vec;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * That class represents the RHS of an implication.
 * 
 * @author daniel
 * 
 * @param <T>
 * @param <C>
 */
public class ImplicationRHS<T, C> {

	private final IVecInt clause;
	private final DependencyHelper<T, C> helper;

	private final IVec<IConstr> toName = new Vec<IConstr>();

	public ImplicationRHS(DependencyHelper<T, C> helper, IVecInt clause) {
		this.clause = clause;
		this.helper = helper;
	}

	/**
	 * Build an implication with a conjunction of literals in the RHS.
	 * 
	 * @param thing
	 *            a domain object that will appear positively.
	 * @return a RHS conjunction of literals.
	 * @throws ContradictionException
	 */
	public ImplicationAnd<T, C> implies(T thing) throws ContradictionException {
		ImplicationAnd<T, C> and = new ImplicationAnd<T, C>(helper, clause);
		and.and(thing);
		return and;
	}

	/**
	 * Build an implication with a disjunction of literals in the RHS.
	 * 
	 * @param thing
	 *            a domain object
	 * @return an object used to name the constraint. The constraint MUST BE
	 *         NAMED.
	 * @throws ContradictionException
	 */
	public ImplicationNamer<T, C> implies(T... things)
			throws ContradictionException {
		for (T t : things) {
			clause.push(helper.getIntValue(t));
		}
		toName.push(helper.solver.addClause(clause));
		return new ImplicationNamer<T, C>(helper, toName);
	}

	/**
	 * Build an implication with a conjunction of literals in the RHS.
	 * 
	 * @param thing
	 *            a domain object that will appear negatively.
	 * @return a RHS conjunction of literals.
	 * @throws ContradictionException
	 */
	public ImplicationAnd<T, C> impliesNot(T thing)
			throws ContradictionException {
		ImplicationAnd<T, C> and = new ImplicationAnd<T, C>(helper, clause);
		and.andNot(thing);
		return and;
	}

}