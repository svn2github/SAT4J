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

import org.sat4j.core.VecInt;
import org.sat4j.csp.Clausifiable;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Var;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

public class AllDiff implements Clausifiable {

    public void toClause(ISolver solver, IVec<Var> scope, IVec<Evaluable> vars)
            throws ContradictionException {
        int n = scope.size();
        IVecInt clause = new VecInt();
        int vali,valj;
        for (int i = 0; i < n; i++) {
            Var vi = scope.get(i);
            for (int j = i + 1; j < n; j++) {
                // make all values differents
                Var vj = scope.get(j);
                for (IteratorInt iti = vi.domain().iterator(); iti.hasNext();) {
                    vali = iti.next();
                    for (IteratorInt itj = vj.domain().iterator(); itj.hasNext();) {
                        valj = itj.next();
                        // the two variables share a common value
                        if (vali == valj) {
                            clause.push(-vi.translate(vali));
                            clause.push(-vj.translate(valj));
                            solver.addClause(clause);
                            clause.clear();
                        }
                    }
                }
            }
        }
    }

}
