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
 * 
 * Based on the original MiniSat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 * 
 *******************************************************************************/
package org.sat4j.minisat.core;

import org.sat4j.specs.ISolver;
import org.sat4j.specs.SearchListener;

/**
 * Abstraction for Conflict Driven Clause Learning Solver.
 * 
 * Allows to easily access the various options available to setup the solver.
 * 
 * @author daniel
 * 
 * @param <D>
 */
public interface ICDCL<D extends DataStructureFactory> extends ISolver,
		UnitPropagationListener, ActivityListener, Learner {

	/**
	 * Change the internal representation of the constraints. Note that the
	 * heuristics must be changed prior to calling that method.
	 * 
	 * @param dsf
	 *            the internal factory
	 */
	public abstract void setDataStructureFactory(D dsf);

	/**
	 * @since 2.1
	 */
	public abstract void setSearchListener(SearchListener sl);

	/**
	 * @since 2.2
	 */
	public abstract SearchListener getSearchListener();

	/**
	 * @since 2.2
	 */
	public abstract void setLearner(LearningStrategy<D> learner);

	public abstract void setSearchParams(SearchParams sp);

	public abstract void setRestartStrategy(RestartStrategy restarter);

	/**
	 * Setup the reason simplification strategy. By default, there is no reason
	 * simplification. NOTE THAT REASON SIMPLIFICATION DOES NOT WORK WITH
	 * SPECIFIC DATA STRUCTURE FOR HANDLING BOTH BINARY AND TERNARY CLAUSES.
	 * 
	 * @param simp
	 *            the name of the simplifier (one of NO_SIMPLIFICATION,
	 *            SIMPLE_SIMPLIFICATION, EXPENSIVE_SIMPLIFICATION).
	 */
	public abstract void setSimplifier(String simp);

	/**
	 * Setup the reason simplification strategy. By default, there is no reason
	 * simplification. NOTE THAT REASON SIMPLIFICATION IS ONLY ALLOWED FOR WL
	 * CLAUSAL data structures. USING REASON SIMPLIFICATION ON CB CLAUSES,
	 * CARDINALITY CONSTRAINTS OR PB CONSTRAINTS MIGHT RESULT IN INCORRECT
	 * RESULTS.
	 * 
	 * @param simp
	 */
	public abstract void setSimplifier(ISimplifier simp);

	/**
	 * @param lcds
	 * @since 2.1
	 */
	public abstract void setLearnedConstraintsDeletionStrategy(
			LearnedConstraintsDeletionStrategy lcds);

	public abstract IOrder getOrder();

	public abstract void setOrder(IOrder h);

	public abstract void setNeedToReduceDB(boolean needToReduceDB);

}