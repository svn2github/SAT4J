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
package org.sat4j.sat;

import org.sat4j.minisat.core.ConflictTimer;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.LearnedConstraintsDeletionStrategy;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.IVec;

/**
 * 
 * Strategy used by the solver when launched with the remote control. 
 * 
 * @author sroussel
 *
 */
public class TelecommandeStrategy implements RestartStrategy, LearnedConstraintsDeletionStrategy{


	private static final long serialVersionUID = 1L;


	private boolean hasClickedOnRestart;
	private boolean hasClickedOnClean;

	private ICDCL solver;


	public TelecommandeStrategy(){
		hasClickedOnClean = false;
		hasClickedOnRestart = false;
	}


	public boolean isHasClickedOnRestart() {
		return hasClickedOnRestart;
	}

	public void setHasClickedOnRestart(boolean hasClickedOnRestart) {
		this.hasClickedOnRestart = hasClickedOnRestart;
	}


	public boolean isHasClickedOnClean() {
		return hasClickedOnClean;
	}


	public void setHasClickedOnClean(boolean hasClickedOnClean) {
		this.hasClickedOnClean = hasClickedOnClean;
		solver.setNeedToReduceDB(true);
	}


	public void init(SearchParams params) {
	}

	public long nextRestartNumberOfConflict() {
		return 0;
	}

	public boolean shouldRestart() {
		if(hasClickedOnRestart){
			hasClickedOnRestart=false;
			System.out.println("Told the solver to restart");
			return true;
		}
		return false;
	}

	public void onRestart() {
	}

	public void onBackjumpToRootLevel() {
	}


	public ICDCL getSolver() {
		return solver;
	}


	public void setSolver(ICDCL solver) {
		this.solver = solver;
	}


	public void reset() {
	}

	public void newConflict() {
	}

	public void init() {

	}

	public ConflictTimer getTimer() {
		return this;
	}

	public void reduce(IVec<Constr> learnts) {
		//System.out.println("je suis lˆ ??");
		assert hasClickedOnClean;

		System.out.println("Told the solver to clean");
		int i, j;
		for (i = j = 0; i < learnts.size() / 2; i++) {
			Constr c = learnts.get(i);
			if (c.locked() || c.size() == 2) {
				learnts.set(j++, learnts.get(i));
			} else {
				c.remove(getSolver());
			}
		}
		for (; i < learnts.size(); i++) {
			learnts.set(j++, learnts.get(i));
		}
		if (true) {
			System.out.println("c "
					+ "cleaning " + (learnts.size() - j) //$NON-NLS-1$
					+ " clauses out of " + learnts.size()); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.flush();
		}
		learnts.shrinkTo(j);

		hasClickedOnClean=false;
	}

	public void onConflict(Constr outLearnt) {
		// TODO Auto-generated method stub

	}

	public void onConflictAnalysis(Constr reason) {
		// TODO Auto-generated method stub

	}


}
