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
package org.sat4j.minisat.constraints.cnf;

import org.sat4j.minisat.core.ILits2;

/**
 * @author leberre To change the template for this generated type comment go to
 * 	Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Lits2 extends Lits implements ILits2 {

	private static final long serialVersionUID = 1L;

	private BinaryClauses[] binclauses = null;

	/**
     * 
     */
	public Lits2() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * To know the number of binary clauses in which the literal occurs. Please
	 * note that this method should only be used in conjunction with the
	 * BinaryClauses data structure.
	 * 
	 * @param p
	 * @return the number of binary clauses in which the literal occurs.
	 */
	public int nBinaryClauses(int p) {
		if (binclauses == null) {
			return 0;
		}
		if (binclauses[p] == null) {
			return 0;
		}
		return binclauses[p].size();
	}

	public void binaryClauses(int lit1, int lit2) {
		register(lit1, lit2);
		register(lit2, lit1);
	}

	private void register(int p, int q) {
		if (binclauses == null) {
			binclauses = new BinaryClauses[2 * capacity() + 2];
		} else {
			int maxid = Math.max(p, q);
			if (binclauses.length <= maxid) {
				ensurePool(maxid);
				BinaryClauses[] nbinClauses = new BinaryClauses[2 * capacity() + 2];
				System.arraycopy(binclauses, 0, nbinClauses, 0,
						binclauses.length);
				binclauses = nbinClauses;
			}
		}
		if (binclauses[p] == null) {
			binclauses[p] = new BinaryClauses(this, p);
			watches[p ^ 1].insertFirstWithShifting(binclauses[p]);
		}
		binclauses[p].addBinaryClause(q);
	}

}
