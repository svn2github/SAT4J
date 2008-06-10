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
* Based on the pseudo boolean algorithms described in:
* A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
* Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
* Volume 24, Issue 3, March 2005 Page(s): 305 - 317
* 
* and 
* Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
* Framework. Ph.D. Dissertation, University of Oregon.
*******************************************************************************/
package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class PuebloMinWatchPb extends MinWatchPb {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de base des contraintes
     * 
     * @param voc
     *            Informations sur le vocabulaire employ???
     * @param ps
     *            Liste des litt???raux
     * @param weightedLits
     *            Liste des coefficients
     * @param moreThan
     *            Indication sur le comparateur
     * @param degree
     *            Stockage du degr??? de la contrainte
     */
    private PuebloMinWatchPb(ILits voc, IDataStructurePB mpb) {

        super(voc, mpb);
    }

    /**
     * @param s
     *            outil pour la propagation des litt???raux
     * @param ps
     *            liste des litt???raux de la nouvelle contrainte
     * @param coefs
     *            liste des coefficients des litt???raux de la contrainte
     * @param moreThan
     *            d???termine si c'est une sup???rieure ou ???gal ??? l'origine
     * @param degree
     *            fournit le degr??? de la contrainte
     * @return une nouvelle clause si tout va bien, ou null si un conflit est
     *         d???tect???
     */
    public static PuebloMinWatchPb minWatchPbNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, IVecInt coefs, boolean moreThan, int degree)
            throws ContradictionException {
        return minWatchPbNew(s, voc, ps, toVecBigInt(coefs), moreThan,
                toBigInt(degree));
    }

    /**
     * @param s
     *            outil pour la propagation des litt???raux
     * @param ps
     *            liste des litt???raux de la nouvelle contrainte
     * @param coefs
     *            liste des coefficients des litt???raux de la contrainte
     * @param moreThan
     *            d???termine si c'est une sup???rieure ou ???gal ??? l'origine
     * @param degree
     *            fournit le degr??? de la contrainte
     * @return une nouvelle clause si tout va bien, ou null si un conflit est
     *         d???tect???
     */
    public static PuebloMinWatchPb minWatchPbNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, IVec<BigInteger> coefs, boolean moreThan,
            BigInteger degree) throws ContradictionException {
        // Il ne faut pas modifier les param?tres
        VecInt litsVec = new VecInt(ps.size());
        IVec<BigInteger> coefsVec = new Vec<BigInteger>(coefs.size());
        ps.copyTo(litsVec);
        coefs.copyTo(coefsVec);

        // Ajouter les simplifications quand la structure sera d???finitive
        IDataStructurePB mpb = niceParameters(litsVec, coefsVec, moreThan,
                degree, voc);
        PuebloMinWatchPb outclause = new PuebloMinWatchPb(voc, mpb);

        if (outclause.degree.signum() <= 0) {
            return null;
        }

        outclause.computeWatches();

        outclause.computePropagation(s);

        return outclause;

    }

    public static PuebloMinWatchPb minWatchPbNew(UnitPropagationListener s,
            ILits voc, IDataStructurePB mpb) throws ContradictionException {
        PuebloMinWatchPb outclause = new PuebloMinWatchPb(voc, mpb);

        if (outclause.degree.signum() <= 0) {
            return null;
        }

        outclause.computeWatches();

        outclause.computePropagation(s);

        return outclause;

    }

    /**
     * 
     */
    public static WatchPb watchPbNew(ILits voc, IVecInt lits, IVecInt coefs,
            boolean moreThan, int degree) {
        return watchPbNew(voc, lits, toVecBigInt(coefs), moreThan,
                toBigInt(degree));
    }

    /**
     * 
     */
    public static WatchPb watchPbNew(ILits voc, IVecInt lits,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree) {
        IDataStructurePB mpb = null;
        mpb = niceCheckedParameters(lits, coefs, moreThan, degree, voc);
        return new PuebloMinWatchPb(voc, mpb);
    }

    public static WatchPb normalizedWatchPbNew(ILits voc, IDataStructurePB mpb) {
        return new PuebloMinWatchPb(voc, mpb);
    }

    @Override
    protected BigInteger maximalCoefficient(int pIndice) {
        return coefs[0];
    }

    @Override
    protected BigInteger updateWatched(BigInteger mc, int pIndice) {
        BigInteger maxCoef = mc;
        if (watchingCount < size()) {
            BigInteger upWatchCumul = watchCumul.subtract(coefs[pIndice]);
            BigInteger borneSup = degree.add(maxCoef);
            for (int ind = 0; ind < lits.length
                    && upWatchCumul.compareTo(borneSup) < 0; ind++) {
                if (!voc.isFalsified(lits[ind]) && !watched[ind]) {
                    upWatchCumul = upWatchCumul.add(coefs[ind]);
                    watched[ind] = true;
                    assert watchingCount < size();
                    watching[watchingCount++] = ind;
                    voc.watch(lits[ind] ^ 1, this);
                }
            }
            watchCumul = upWatchCumul.add(coefs[pIndice]);
        }
        return maxCoef;
    }

}
