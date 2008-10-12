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
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.csp.Encoding;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Var;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

public class GeneralizedSupportEncoding implements Encoding {

    private final Map<Set<Integer>, IVecInt> supports = new HashMap<Set<Integer>, IVecInt>();

    private static final Encoding instance = new GeneralizedSupportEncoding();

    private GeneralizedSupportEncoding() {

    }

    public static Encoding instance() {
        return instance;
    }

    public void onFinish(ISolver solver, IVec<Var> scope)
            throws ContradictionException {
        // TODO Auto-generated method stub

    }

    public void onInit(ISolver solver, IVec<Var> scope) {
        supports.clear();
        int[] acc = new int[scope.size()];
        fill(0, scope, acc, supports);
    }

    public void onNogood(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) throws ContradictionException {

    }

    public void onSupport(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) throws ContradictionException {
        for (int i = 0; i < scope.size(); i++) {
            Set<Integer> set = new TreeSet<Integer>();
            Var vari = scope.get(i);
            for (int j = 0; j < scope.size(); j++) {
                if (i != j) {
                    set.add(scope.get(j).translate(tuple.get(vari)));
                }
            }
            IVecInt support = supports.get(set);
            assert support != null;
            support.push(vari.translate(tuple.get(vari)));
        }

    }

    private void fill(int n, IVec<Var> scope, int[] acc,
            Map<Set<Integer>, IVecInt> theSupports) {
        if (n == scope.size()) {
            for (int j = 0; j < acc.length; j++) {
                Set<Integer> set = new TreeSet<Integer>();
                for (int i = 0; i < acc.length; i++)
                    if (i != j)
                        set.add(scope.get(i).translate(acc[i]));
                theSupports.put(set, new VecInt());
            }
        } else
            for (IteratorInt iterator = scope.get(n).domain().iterator() ; iterator.hasNext();) {
                acc[n] = iterator.next();
                fill(n + 1, scope, acc, theSupports);
            }

    }
}
