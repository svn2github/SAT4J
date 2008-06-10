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
package org.sat4j.minisat.orders;

import static org.sat4j.core.LiteralsUtils.neg;
import static org.sat4j.core.LiteralsUtils.posLit;
import static org.sat4j.core.LiteralsUtils.var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sat4j.minisat.core.ILits23;

/**
 * @author leberre Heuristique du prouveur. Changement par rapport au MiniSAT
 *         original : la gestion activity est faite ici et non plus dans Solver.
 */
public class JWOrder extends VarOrder<ILits23> {

    private static final long serialVersionUID = 1L;

    private int computeWeight(int var) {
        final int p = posLit(var);
        int pos2 = lits.nBinaryClauses(p);
        int neg2 = lits.nBinaryClauses(neg(p));
        int pos3 = lits.nTernaryClauses(p);
        int neg3 = lits.nTernaryClauses(neg(p));
        long weight = (pos2 * neg2 * 100L + pos2 + neg2) * 5L + pos3 * neg3 * 10L
                + pos3 + neg3;
        assert weight <= Integer.MAX_VALUE;
        if (weight == 0) {
            int pos = lits.watches(p).size();
            int neg = lits.watches(neg(p)).size();
            weight = pos + neg;
        }
        return (int) weight;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.IHeuristics#init()
     */
    @Override
    public void init() {
        super.init();
        List<ValuedLit> v = new ArrayList<ValuedLit>(order.length);

        for (int i = 1; i < order.length; i++) {
            ValuedLit t = new ValuedLit(order[i],computeWeight(order[i]>> 1));
            v.add(t);
        }
        Collections.sort(v);
        // System.out.println(v);
        for (int i = 0; i < v.size(); i++) {
            ValuedLit t = v.get(i);
            order[i + 1] = t.id;
            int index = var(t.id);
            varpos[index] = i + 1;
            activity[index] = t.count;
        }
        lastVar = 1;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.VarOrder#updateActivity(int)
     */
    @Override
    protected void updateActivity(int var) {
        activity[var] = computeWeight(var);
    }

    @Override
    public String toString() {
        return "Jeroslow-Wang static like heuristics updated when new clauses are learnt"; //$NON-NLS-1$
    }
}
