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

import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.csp.Encoding;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Var;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class DirectEncoding implements Encoding {

    private final IVecInt clause = new VecInt();

    private static final Encoding instance = new DirectEncoding();

    private DirectEncoding() {
        // nothing here
    }

    public static Encoding instance() {
        return instance;
    }

    public void onFinish(ISolver solver, IVec<Var> scope) {
    }

    public void onInit(ISolver solver, IVec<Var> scope) {
    }

    public void onNogood(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) throws ContradictionException {
        clause.clear();
        Var v;
        for (int i = 0; i < scope.size(); i++) {
            v = scope.get(i);
            clause.push(-v.translate(tuple.get(v).intValue()));
        }
        solver.addClause(clause);

    }

    public void onSupport(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) {
    }

}
