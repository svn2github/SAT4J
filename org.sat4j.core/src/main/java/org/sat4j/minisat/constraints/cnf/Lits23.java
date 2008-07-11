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

import org.sat4j.minisat.core.ILits23;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Lits23 extends Lits2 implements ILits23 {

    private static final long serialVersionUID = 1L;

    private TernaryClauses[] ternclauses = null;

    /**
     * 
     */
    public Lits23() {
        super();
    }

    private void register(int p, int q, int r) {
        assert p > 1;
        assert q > 1;
        assert r > 1;

        if (ternclauses == null) {
            ternclauses = new TernaryClauses[2 * capacity() + 2];
        } else {
			int maxid = Math.max(p, q);
			if (ternclauses.length <= maxid) {
				TernaryClauses[] nternclauses = new TernaryClauses[2 * capacity() + 2];
				System.arraycopy(ternclauses, 0, nternclauses, 0,
						ternclauses.length);
				ternclauses = nternclauses;
			}
		}
        if (ternclauses[p] == null) {
            ternclauses[p] = new TernaryClauses(this, p);
            watches[p ^ 1].push(ternclauses[p]);
        }
        ternclauses[p].addTernaryClause(q, r);
    }

    public void ternaryClauses(int lit1, int lit2, int lit3) {
        register(lit1, lit2, lit3);
        register(lit2, lit1, lit3);
        register(lit3, lit1, lit2);
    }

    public int nTernaryClauses(int p) {
        if (ternclauses == null) {
            return 0;
        }
        if (ternclauses[p] == null) {
            return 0;
        }
        return ternclauses[p].size();
    }
}
