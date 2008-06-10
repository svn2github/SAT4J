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
package org.sat4j.csp.encodings;

import java.util.HashMap;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.csp.Encoding;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Var;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

public class BinarySupportEncoding implements Encoding {

    private final Map<Integer, IVecInt> supportsa = new HashMap<Integer, IVecInt>();

    private final Map<Integer, IVecInt> supportsb = new HashMap<Integer, IVecInt>();

    private static final Encoding instance = new BinarySupportEncoding();

    private BinarySupportEncoding() {
        // nothing here
    }

    public static Encoding instance() {
        return instance;
    }

    public void onFinish(ISolver solver, IVec<Var> scope)
            throws ContradictionException {
        generateClauses(scope.get(0), supportsa, solver);
        generateClauses(scope.get(1), supportsb, solver);

    }

    public void onInit(ISolver solver, IVec<Var> scope) {
        supportsa.clear();
        supportsb.clear();
    }

    public void onNogood(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) throws ContradictionException {
    }

    public void onSupport(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) throws ContradictionException {
        Var vara = scope.get(0);
        Integer va = tuple.get(vara);
        Var varb = scope.get(1);
        Integer vb = tuple.get(varb);
        addSupport(va, varb, vb, supportsa);
        addSupport(vb, vara, va, supportsb);
    }

    private void addSupport(Integer head, Evaluable v, Integer value,
            Map<Integer, IVecInt> supports) {
        IVecInt sup = supports.get(head);
        if (sup == null) {
            sup = new VecInt();
            supports.put(head, sup);
        }
        sup.push(v.translate(value.intValue()));
    }

    private void generateClauses(Evaluable v, Map<Integer, IVecInt> supports,
            ISolver solver) throws ContradictionException {
        IVecInt clause = new VecInt();
        for (IteratorInt  it =  v.domain().iterator() ; it.hasNext();) {
            Integer key = new Integer(it.next());
            clause.clear();
            IVecInt support = supports.get(key);
            clause.push(-v.translate(key.intValue()));
            if (support != null) {
                for (IteratorInt  iterator = support.iterator() ; iterator.hasNext();)
                    clause.push(iterator.next());
            }
            solver.addClause(clause);
        }
    }

}
