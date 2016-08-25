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
    protected BigInteger reduceUntilConflict(int litImplied, int ind,
            BigInteger[] reducedCoefs, BigInteger degreeReduced, IWatchPb wpb) {
        BigInteger coefLit = wpb.getCoef(ind);
        int size = wpb.size();
        BigInteger slack = BigInteger.ZERO;
        for (int i = 0; i < size; i++) {
            if (!voc.isFalsified(wpb.get(i))) {
                slack = slack.add(ceildiv(reducedCoefs[i], coefLit));
            }
        }
        BigInteger dividedDegree = ceildiv(degreeReduced, coefLit);
        slack = slack.subtract(dividedDegree);
        BigInteger updatedDegree = degreeReduced;
        for (int i = 0; i < size; i++) {
            if (!slack.equals(BigInteger.ZERO) && !voc.isFalsified(wpb.get(i))
                    && wpb.get(i) != litImplied
                    && !reducedCoefs[i].equals(BigInteger.ZERO)) {
                // incremental computation of the slack proposed by Jakob
                slack = slack
                        .subtract(ceildiv(reducedCoefs[i], coefLit))
                        .add(dividedDegree)
                        .subtract(
                                ceildiv(degreeReduced.subtract(reducedCoefs[i]),
                                        coefLit));
                updatedDegree = updatedDegree.subtract(reducedCoefs[i]);
                reducedCoefs[i] = BigInteger.ZERO;
            } else
                reducedCoefs[i] = ceildiv(reducedCoefs[i], coefLit);
        }
        degreeReduced = ceildiv(updatedDegree, coefLit);
        degreeReduced = saturation(reducedCoefs, degreeReduced, wpb);
        this.coefMultCons = this.weightedLits.get(litImplied ^ 1);
        this.coefMult = BigInteger.ONE;
        return degreeReduced;
    }

}
