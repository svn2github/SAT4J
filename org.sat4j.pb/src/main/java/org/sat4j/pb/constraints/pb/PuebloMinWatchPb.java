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

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;

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
	private PuebloMinWatchPb(ILits voc, int[] lits, BigInteger[] coefs,
			BigInteger degree) {
		super(voc, lits, coefs, degree);
	}

	private PuebloMinWatchPb(ILits voc, IDataStructurePB mpb) {

		super(voc, mpb);
	}

	/**
	 * @param s
	 *            a unit propagation listener
	 * @param voc
	 *            the vocabulary
	 * @param lits
	 *            the literals
	 * @param coefs
	 *            the coefficients
	 * @param degree
	 *            the degree of the constraint to normalize.
	 * @return a new PB constraint or null if a trivial inconsistency is
	 *         detected.
	 */
	public static PuebloMinWatchPb normalizedMinWatchPbNew(
			UnitPropagationListener s, ILits voc, int[] lits,
			BigInteger[] coefs, BigInteger degree)
			throws ContradictionException {
		// Il ne faut pas modifier les param?tres
		PuebloMinWatchPb outclause = new PuebloMinWatchPb(voc, lits, coefs,
				degree);

		if (outclause.degree.signum() <= 0) {
			return null;
		}

		outclause.computeWatches();

		outclause.computePropagation(s);

		return outclause;

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
