/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

public class ConflictMapRounding extends ConflictMap {

    /**
     * @param cpb
     * @param level
     */
    public ConflictMapRounding(PBConstr cpb, int level) {
        super(cpb, level);
    }

    /**
     * @param cpb
     * @param level
     * @param noRemove
     */
    public ConflictMapRounding(PBConstr cpb, int level, boolean noRemove) {
        super(cpb, level, noRemove);
    }

    public static IConflict createConflict(PBConstr cpb, int level) {
        return new ConflictMapRounding(cpb, level);
    }

    public static IConflict createConflict(PBConstr cpb, int level,
            boolean noRemove) {
        return new ConflictMapRounding(cpb, level, noRemove);
    }

    static BigInteger ceildiv(BigInteger p, BigInteger q) {
        return p.add(q).subtract(BigInteger.ONE).divide(q);
    }

    @Override
    protected BigInteger reduceUntilConflict(int x, int xindex,
            BigInteger[] abc, BigInteger t, IWatchPb xyz) {
        BigInteger a = abc[xindex];
        int n = xyz.size();
        BigInteger sprime = BigInteger.ZERO;
        for (int k = 0; k < n; k++) {
            if (!voc.isFalsified(xyz.get(k))) {
                sprime = sprime.add(ceildiv(abc[k], a));
            }
        }
        BigInteger tprime = ceildiv(t, a);
        sprime = sprime.subtract(tprime);
        BigInteger bigt = t;
        BigInteger tnewprime;
        for (int k = 0; k < n; k++) {
            if (!sprime.equals(BigInteger.ZERO) && !voc.isFalsified(xyz.get(k))
                    && xyz.get(k) != x && !abc[k].equals(BigInteger.ZERO)) {
                // incremental computation of the slack proposed by Jakob
                bigt = bigt.subtract(abc[k]);
                tnewprime = ceildiv(bigt, a);
                sprime = sprime.add(tprime).subtract(ceildiv(abc[k], a))
                        .subtract(tnewprime);
                tprime = tnewprime;
                abc[k] = BigInteger.ZERO;
            } else
                abc[k] = ceildiv(abc[k], a);
        }
        tprime = saturation(abc, tprime, xyz);
        this.coefMultCons = this.weightedLits.get(x ^ 1);
        this.coefMult = BigInteger.ONE;
        return tprime;
    }

}
