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
 * Based on the pseudo boolean algorithms described in:
 * A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
 * Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
 * Volume 24, Issue 3, March 2005 Page(s): 305 - 317
 * 
 * and 
 * Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
 * Framework. Ph.D. Dissertation, University of Oregon.
 *******************************************************************************/
package org.sat4j.pb.orders;

import static org.sat4j.core.LiteralsUtils.neg;
import static org.sat4j.core.LiteralsUtils.var;

import java.math.BigInteger;

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.orders.PhaseInLastLearnedClauseSelectionStrategy;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class VarOrderHeapObjective extends VarOrderHeap<ILits> {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	private ObjectiveFunction obj;

	public VarOrderHeapObjective() {
		this(new PhaseInLastLearnedClauseSelectionStrategy());
	}

	public VarOrderHeapObjective(IPhaseSelectionStrategy strategy) {
		super(strategy);
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		this.obj = obj;
	}

	@Override
	public void init() {
		super.init();
		if (obj != null) {
			IVecInt vars = obj.getVars();
			IVec<BigInteger> coefs = obj.getCoeffs();
			for (int i = 0; i < vars.size(); i++) {
				int dimacsLiteral = vars.get(i);
				if (lits.belongsToPool(Math.abs(dimacsLiteral))) {
					int p = lits.getFromPool(dimacsLiteral);
					BigInteger c = coefs.get(i);
					if (c.signum() < 0) {
						p = neg(p);
					}
					int var = var(p);
					activity[var] = c.abs().doubleValue();
					if (heap.inHeap(var))
						heap.increase(var);
					else
						heap.insert(var);
					phaseStrategy.init(var, neg(p));
				}
			}
		}
	}

	@Override
	public String toString() {
		return super.toString() + " taking into account the objective function";
	}

}
