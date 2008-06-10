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
package org.sat4j.csp.constraints;

import java.util.HashMap;
import java.util.Map;

import org.sat4j.csp.Domain;
import org.sat4j.csp.Encoding;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Var;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;

public abstract class Supports implements Relation {

    private Encoding encoding;

    private final int arity;

    private int[][] tuples;

    private int lastmatch;

    private Map<Evaluable, Integer> mtuple;

    public Supports(int arity, int nbtuples) {
        this.arity = arity;
        tuples = new int[nbtuples][];
    }

    public void addTuple(int index, int[] tuple) {
        tuples[index] = tuple;
    }

    public int arity() {
        return arity;
    }

    public void toClause(ISolver solver, IVec<Var> scope, IVec<Evaluable> vars)
            throws ContradictionException {
        assert vars.size() == 0;
        assert scope.size() == arity;
        int[] tuple = new int[scope.size()];
        mtuple = new HashMap<Evaluable, Integer>();
        lastmatch = -1;
        encoding = chooseEncoding(scope);
        encoding.onInit(solver, scope);
        find(tuple, 0, scope, solver);
        encoding.onFinish(solver, scope);
    }

    protected abstract Encoding chooseEncoding(IVec<Var> scope);

    private void find(int[] tuple, int n, IVec<Var> scope, ISolver solver)
            throws ContradictionException {
        if (n == scope.size()) {
            assert mtuple.size() == n;
            if (notPresent(tuple)) {
                encoding.onNogood(solver, scope, mtuple);
            } else {
                encoding.onSupport(solver, scope, mtuple);
            }
        } else {
            Domain domain = scope.get(n).domain();
            for (int i = 0; i < domain.size(); i++) {
                tuple[n] = domain.get(i);
                mtuple.put(scope.get(n), tuple[n]);
                find(tuple, n + 1, scope, solver);
            }
            mtuple.remove(scope.get(n));

        }

    }

    private boolean notPresent(int[] tuple) {
        // System.out.println("Checking:" + Arrays.asList(tuple));
        // find the first tuple begining with the same
        // initial number
        int i = lastmatch + 1;
        int j = 0;
        final int[][] ltuples = tuples;
        int searchedvalue, currentvalue;
        while (i < ltuples.length && j < tuple.length) {
            searchedvalue = ltuples[i][j];
            currentvalue = tuple[j];
            if (searchedvalue < currentvalue) {
                i++;
                j = 0;
                continue;
            }
            if (searchedvalue > currentvalue)
                return true;
            j++;
        }
        if (j == tuple.length) {
            lastmatch = i;
            return false;
        }
        return true;
    }
}
