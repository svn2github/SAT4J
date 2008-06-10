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
import static org.sat4j.core.LiteralsUtils.var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sat4j.minisat.core.ILits2;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MyOrder extends VarOrder<ILits2> {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.IHeuristics#init()
     */
    @Override
    public void init() {
        super.init();
        List<ValuedLit> v = new ArrayList<ValuedLit>(order.length);
        int id;
        for (int i = 1; i < order.length; i++) {
            id = order[i];
            v.add(new ValuedLit(id,lits.nBinaryClauses(id) + lits.nBinaryClauses(neg(id))));
        }
        Collections.sort(v);
        // System.out.println(v);
        for (int i = 0; i < v.size(); i++) {
            ValuedLit t = v.get(i);
            order[i + 1] = t.id;
            int index = var(t.id);
            varpos[index] = i + 1;
        }
        lastVar = 1;
    }

    @Override
    public String toString() {
        return "Init VSIDS order with binary clause occurrences."; //$NON-NLS-1$
    }
}
