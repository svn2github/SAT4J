package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

public class ConflictMapReduceToCard extends ConflictMap {

    public static final BigInteger MAXVALUE = BigInteger
            .valueOf(Long.MAX_VALUE);

    public ConflictMapReduceToCard(PBConstr cpb, int level) {
        super(cpb, level);
    }

    public ConflictMapReduceToCard(PBConstr cpb, int level, boolean noRemove) {
        super(cpb, level, noRemove);
    }

    public static IConflict createConflict(PBConstr cpb, int level) {
        return new ConflictMapReduceToCard(cpb, level);
    }

    /**
     * reduces the constraint defined by wpb until the result of the cutting
     * plane is a conflict. this reduction returns a cardinality.
     * 
     * @param litImplied
     * @param ind
     * @param reducedCoefs
     * @param wpb
     * @return degree of the cardinality constraint resulting
     */
    @Override
    protected BigInteger reduceUntilConflict(int litImplied, int ind,
            BigInteger[] reducedCoefs, BigInteger degreeReduced, IWatchPb wpb) {
        BigInteger coefLitImplied = this.weightedLits.get(litImplied ^ 1);

        if ((reducedCoefs[0].multiply(coefLitImplied).compareTo(MAXVALUE) > 0)
                || (reducedCoefs[ind].multiply(this.weightedLits.getCoef(0))
                        .compareTo(MAXVALUE) > 0)) {
            degreeReduced = reduceToCard(ind, wpb, reducedCoefs, degreeReduced);
            this.coefMultCons = this.weightedLits.get(litImplied ^ 1);
            this.coefMult = BigInteger.ONE;
            this.numberOfReductions++;
            return degreeReduced;
        } else
            return super.reduceUntilConflict(litImplied, ind, reducedCoefs,
                    degreeReduced, wpb);
    }

    private BigInteger reduceToCard(int ind, IWatchPb wpb,
            BigInteger[] reducedCoefs, BigInteger degreeReduced) {
        BigInteger somCoefs = BigInteger.ZERO;
        int cpt = 0;
        for (int i = 0; i < reducedCoefs.length; i++) {
            if (somCoefs.compareTo(degreeReduced) < 0) {
                somCoefs = somCoefs.add(reducedCoefs[i]);
                cpt++;
            }
            if (i == ind || wpb.getVocabulary().isFalsified(wpb.get(i))) {
                reducedCoefs[i] = BigInteger.ONE;
            } else {
                reducedCoefs[i] = BigInteger.ZERO;
            }
        }
        return BigInteger.valueOf(cpt);
    }

}
