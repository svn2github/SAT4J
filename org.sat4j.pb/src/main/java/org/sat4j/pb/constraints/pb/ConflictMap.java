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

import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.VarActivityListener;
import org.sat4j.specs.IteratorInt;

/**
 * @author parrain TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class ConflictMap extends MapPb implements IConflict {

	private final ILits voc;

	protected boolean hasBeenReduced = false;
	protected long numberOfReductions = 0;

	/**
	 * to store the slack of the current resolvant
	 */
	protected BigInteger currentSlack;

	protected int currentLevel;

	/**
	 * allows to access directly to all variables belonging to a particular
	 * level At index 0, unassigned literals are stored (usually level -1); so
	 * there is always a step between index and levels.
	 */
	protected VecInt[] byLevel;

	/**
	 * constructs the data structure needed to perform cutting planes
	 * 
	 * @param cpb
	 *            pseudo-boolean constraint which rosed the conflict
	 * @param level
	 *            current decision level
	 * @return a conflict on which cutting plane can be performed.
	 */
	public static IConflict createConflict(PBConstr cpb, int level) {
		return new ConflictMap(cpb, level);
	}

	ConflictMap(PBConstr cpb, int level) {
		super(cpb);
		this.voc = cpb.getVocabulary();
		this.currentLevel = level;
		initStructures();
	}

	private void initStructures() {
		currentSlack = BigInteger.ZERO;
		byLevel = new VecInt[levelToIndex(currentLevel) + 1];
		int ilit, litLevel, index;
		BigInteger tmp;
		for (int i = 0; i < size(); i++) {
			ilit = weightedLits.getLit(i);
			litLevel = voc.getLevel(ilit);
			// eventually add to slack
			tmp = weightedLits.getCoef(i);
			if ((tmp.signum() > 0)
					&& (((!voc.isFalsified(ilit)) || litLevel == currentLevel)))
				currentSlack = currentSlack.add(tmp);
			// add to byLevel structure
			index = levelToIndex(litLevel);
			if (byLevel[index] == null) {
				byLevel[index] = new VecInt();
			}
			byLevel[index].push(ilit);
		}
	}

	/**
	 * convert level into an index in the byLevel structure
	 * 
	 * @param level
	 * @return
	 */
	private static final int levelToIndex(int level) {
		return level + 1;
	}

	/**
	 * convert index in the byLevel structure into a level
	 * 
	 * @param indLevel
	 * @return
	 */
	private static final int indexToLevel(int indLevel) {
		return indLevel - 1;
	}

	/*
	 * coefficient to be computed.
	 */
	protected BigInteger coefMult = BigInteger.ZERO;

	protected BigInteger coefMultCons = BigInteger.ZERO;

	/**
	 * computes a cutting plane with a pseudo-boolean constraint. this method
	 * updates the current instance (of ConflictMap).
	 * 
	 * @param cpb
	 *            constraint to compute with the cutting plane
	 * @param litImplied
	 *            literal that must be resolved by the cutting plane
	 * @return an update of the degree of the current instance
	 */
	public BigInteger resolve(PBConstr cpb, int litImplied,
			VarActivityListener val) {
		assert litImplied > 1;
		int nLitImplied = litImplied ^ 1;
		if (cpb == null || !weightedLits.containsKey(nLitImplied)) {
			// no resolution
			// undo operation should be anticipated
			int litLevel = levelToIndex(voc.getLevel(litImplied));
			int lit = 0;
			if (byLevel[litLevel] != null) {
				if (byLevel[litLevel].contains(litImplied)) {
					lit = litImplied;
					assert weightedLits.containsKey(litImplied);
				} else if (byLevel[litLevel].contains(nLitImplied)) {
					lit = nLitImplied;
					assert weightedLits.containsKey(nLitImplied);
				}
			}

			if (lit > 0) {
				byLevel[litLevel].remove(lit);
				if (byLevel[0] == null)
					byLevel[0] = new VecInt();
				byLevel[0].push(lit);
			}
			return degree;
		}

		assert slackConflict().signum() <= 0;
		assert degree.signum() >= 0;

		// coefficients of the constraint must be copied in an other structure
		// in order to make reduction operations.
		BigInteger[] coefsCons = null;
		BigInteger degreeCons = cpb.getDegree();

		// search of the index of the implied literal
		int ind = 0;
		while (cpb.get(ind) != litImplied)
			ind++;

		assert cpb.get(ind) == litImplied;
		assert cpb.getCoef(ind) != BigInteger.ZERO;

		if (cpb.getCoef(ind).equals(BigInteger.ONE)) {
			// then we know that the resolvant will still be a conflict (cf.
			// Dixon's property)
			coefMultCons = weightedLits.get(nLitImplied);
			coefMult = BigInteger.ONE;
			// updating of the degree of the conflict
			degreeCons = degreeCons.multiply(coefMultCons);
		} else {
			if (weightedLits.get(nLitImplied).equals(BigInteger.ONE)) {
				// then we know that the resolvant will still be a conflict (cf.
				// Dixon's property)
				coefMult = cpb.getCoef(ind);
				coefMultCons = BigInteger.ONE;
				// updating of the degree of the conflict
				degree = degree.multiply(coefMult);
			} else {
				// pb-constraint has to be reduced
				// to obtain a conflictual result from the cutting plane
				WatchPb wpb = (WatchPb) cpb; // DLB Findbugs warning ok
				coefsCons = wpb.getCoefs();
				assert positiveCoefs(coefsCons);
				degreeCons = reduceUntilConflict(litImplied, ind, coefsCons,
						wpb);
				// updating of the degree of the conflict
				degreeCons = degreeCons.multiply(coefMultCons);
				degree = degree.multiply(coefMult);
			}

			// coefficients of the conflict must be multiplied by coefMult
			if (!coefMult.equals(BigInteger.ONE))
				for (int i = 0; i < size(); i++) {
					changeCoef(i, weightedLits.getCoef(i).multiply(coefMult));
				}
		}

		assert slackConflict().signum() <= 0;

		// cutting plane
		degree = cuttingPlane(cpb, degreeCons, coefsCons, coefMultCons, val);

		// neither litImplied nor nLitImplied is present in coefs structure
		assert !weightedLits.containsKey(litImplied);
		assert !weightedLits.containsKey(nLitImplied);
		// neither litImplied nor nLitImplied is present in byLevel structure
		assert getLevelByLevel(litImplied) == -1;
		assert getLevelByLevel(nLitImplied) == -1;
		assert degree.signum() > 0;
		assert slackConflict().signum() <= 0;

		// saturation
		degree = saturation();
		assert slackConflict().signum() <= 0;

		return degree;
	}

	protected BigInteger reduceUntilConflict(int litImplied, int ind,
			BigInteger[] reducedCoefs, WatchPb wpb) {
		BigInteger slackResolve = BigInteger.ONE.negate();
		BigInteger slackThis = BigInteger.ZERO;
		BigInteger slackIndex;
		BigInteger slackConflict = slackConflict();
		BigInteger ppcm;
		BigInteger reducedDegree = wpb.getDegree();
		BigInteger previousCoefLitImplied = BigInteger.ZERO;
		BigInteger tmp;
		BigInteger coefLitImplied = weightedLits.get(litImplied ^ 1);

		do {
			if (slackResolve.signum() >= 0) {
				assert slackThis.signum() > 0;
				tmp = reduceInConstraint(wpb, reducedCoefs, ind, reducedDegree);
				assert ((tmp.compareTo(reducedDegree) < 0) && (tmp
						.compareTo(BigInteger.ONE) >= 0));
				reducedDegree = tmp;
			}
			// search of the multiplying coefficients
			assert weightedLits.get(litImplied ^ 1).signum() > 0;
			assert reducedCoefs[ind].signum() > 0;

			if (!reducedCoefs[ind].equals(previousCoefLitImplied)) {
				assert coefLitImplied.equals(weightedLits.get(litImplied ^ 1));
				ppcm = ppcm(reducedCoefs[ind], coefLitImplied);
				assert ppcm.signum() > 0;
				coefMult = ppcm.divide(coefLitImplied);
				coefMultCons = ppcm.divide(reducedCoefs[ind]);

				assert coefMultCons.signum() > 0;
				assert coefMult.signum() > 0;
				assert coefMult.multiply(coefLitImplied).equals(
						coefMultCons.multiply(reducedCoefs[ind]));
				previousCoefLitImplied = reducedCoefs[ind];
			}

			// slacks computed for each constraint
			slackThis = wpb.slackConstraint(reducedCoefs, reducedDegree)
					.multiply(coefMultCons);
			assert slackConflict.equals(slackConflict());
			slackIndex = slackConflict.multiply(coefMult);
			assert slackIndex.signum() <= 0;
			// estimate of the slack after the cutting plane
			slackResolve = slackThis.add(slackIndex);
		} while (slackResolve.signum() >= 0);
		assert coefMult.multiply(weightedLits.get(litImplied ^ 1)).equals(
				coefMultCons.multiply(reducedCoefs[ind]));
		return reducedDegree;

	}

	/**
	 * computes the slack of the current instance
	 */
	public BigInteger slackConflict() {
		BigInteger poss = BigInteger.ZERO;
		BigInteger tmp;
		// for each literal
		for (int i = 0; i < size(); i++) {
			tmp = weightedLits.getCoef(i);
			if (tmp.signum() != 0 && !voc.isFalsified(weightedLits.getLit(i)))
				poss = poss.add(tmp);
		}
		return poss.subtract(degree);
	}

	public boolean oldIsAssertive(int dl) {
		BigInteger tmp;
		int lit;
		BigInteger slack = computeSlack(dl).subtract(degree);
		if (slack.signum() < 0)
			return false;
		for (int i = 0; i < size(); i++) {
			tmp = weightedLits.getCoef(i);
			lit = weightedLits.getLit(i);
			if ((tmp.signum() > 0)
					&& (voc.isUnassigned(lit) || voc.getLevel(lit) >= dl)
					&& (slack.compareTo(tmp) < 0))
				return true;
		}
		return false;
	}

	// computes a slack with respect to a particular decision level
	private BigInteger computeSlack(int dl) {
		BigInteger slack = BigInteger.ZERO;
		int lit;
		BigInteger tmp;
		for (int i = 0; i < size(); i++) {
			tmp = weightedLits.getCoef(i);
			lit = weightedLits.getLit(i);
			if ((tmp.signum() > 0)
					&& (((!voc.isFalsified(lit)) || voc.getLevel(lit) >= dl)))
				slack = slack.add(tmp);
		}
		return slack;
	}

	/**
	 * tests if the conflict is assertive (allows to imply a literal) at a
	 * particular decision level
	 * 
	 * @param dl
	 *            the decision level
	 * @return true if the conflict is assertive at the decision level
	 */
	public boolean isAssertive(int dl) {
		assert dl <= currentLevel;
		assert dl <= currentLevel;

		currentLevel = dl;
		// assert currentSlack.equals(computeSlack(dl));
		BigInteger slack = currentSlack.subtract(degree);
		if (slack.signum() < 0)
			return false;
		return isImplyingLiteral(slack);
	}

	// given the slack already computed, tests if a literal could be implied at
	// a particular level
	// uses the byLevel data structure to parse each literal by decision level
	private boolean isImplyingLiteral(BigInteger slack) {
		// unassigned literals are tried first
		int unassigned = levelToIndex(-1);
		int lit;
		if (byLevel[unassigned] != null) {
			for (IteratorInt iterator = byLevel[unassigned].iterator(); iterator
					.hasNext();) {
				lit = iterator.next();
				if (slack.compareTo(weightedLits.get(lit)) < 0) {
					assertiveLiteral = weightedLits.allLits.get(lit);
					return true;
				}
			}
		}
		// then we have to look at every literal at a decision level >=dl
		BigInteger tmp;
		int level = levelToIndex(currentLevel);
		if (byLevel[level] != null)
			for (IteratorInt iterator = byLevel[level].iterator(); iterator
					.hasNext();) {
				lit = iterator.next();
				tmp = weightedLits.get(lit);
				if (tmp != null && slack.compareTo(tmp) < 0) {
					assertiveLiteral = weightedLits.allLits.get(lit);
					return true;
				}
			}
		return false;
	}

	// given the slack already computed, tests if a literal could be implied at
	// a particular level
	// uses the coefs data structure (where coefficients are decreasing ordered)
	// to parse each literal
	private boolean isImplyingLiteralOrdered(int dl, BigInteger slack) {
		int ilit, litLevel;
		for (int i = 0; i < size(); i++) {
			ilit = weightedLits.getLit(i);
			litLevel = voc.getLevel(ilit);
			if ((litLevel >= dl || voc.isUnassigned(ilit))
					&& (slack.compareTo(weightedLits.getCoef(i)) < 0))
				return true;
		}
		return false;
	}

	/**
	 * computes the least common factor of two integers (Plus Petit Commun
	 * Multiple in french)
	 * 
	 * @param a
	 *            first integer
	 * @param b
	 *            second integer
	 * @return the least common factor
	 */
	protected static BigInteger ppcm(BigInteger a, BigInteger b) {
		return a.divide(a.gcd(b)).multiply(b);
	}

	/**
	 * constraint reduction : removes a literal of the constraint. The literal
	 * should be either unassigned or satisfied. The literal can not be the
	 * literal that should be resolved.
	 * 
	 * @param wpb
	 *            the initial constraint to reduce
	 * @param coefsBis
	 *            the coefficients of the constraint wrt which the reduction
	 *            will be proposed
	 * @param indLitImplied
	 *            index in wpb of the literal that should be resolved
	 * @param degreeBis
	 *            the degree of the constraint wrt which the reduction will be
	 *            proposed
	 * @return new degree of the reduced constraint
	 */
	public BigInteger reduceInConstraint(WatchPb wpb,
			final BigInteger[] coefsBis, final int indLitImplied,
			final BigInteger degreeBis) {
		// logger.entering(this.getClass().getName(),"reduceInConstraint");
		assert degreeBis.compareTo(BigInteger.ONE) > 0;
		// search of an unassigned literal
		int lit = -1;
		for (int ind = 0; (ind < wpb.lits.length) && (lit == -1); ind++)
			if (coefsBis[ind].signum() != 0 && voc.isUnassigned(wpb.lits[ind])) {
				assert coefsBis[ind].compareTo(degreeBis) < 0;
				lit = ind;
			}

		// else, search of a satisfied literal
		if (lit == -1)
			for (int ind = 0; (ind < wpb.lits.length) && (lit == -1); ind++)
				if ((coefsBis[ind].signum() != 0)
						&& (voc.isSatisfied(wpb.lits[ind]))
						&& (ind != indLitImplied))
					lit = ind;

		// a literal has been found
		assert lit != -1;

		assert lit != indLitImplied;
		// logger.finer("Found literal "+Lits.toString(lits[lit]));
		// reduction can be done
		BigInteger degUpdate = degreeBis.subtract(coefsBis[lit]);
		coefsBis[lit] = BigInteger.ZERO;

		// saturation of the constraint
		degUpdate = saturation(coefsBis, degUpdate);

		assert coefsBis[indLitImplied].signum() > 0;
		assert degreeBis.compareTo(degUpdate) > 0;
		return degUpdate;
	}

	static BigInteger saturation(BigInteger[] coefs, BigInteger degree) {
		assert degree.signum() > 0;
		BigInteger minimum = degree;
		for (int i = 0; i < coefs.length; i++) {
			if (coefs[i].signum() > 0)
				minimum = minimum.min(coefs[i]);
			coefs[i] = degree.min(coefs[i]);
		}
		if (minimum.equals(degree) && !degree.equals(BigInteger.ONE)) {
			// the result is a clause
			// there is no more possible reduction
			degree = BigInteger.ONE;
			for (int i = 0; i < coefs.length; i++)
				if (coefs[i].signum() > 0)
					coefs[i] = degree;
		}
		return degree;
	}

	private static boolean positiveCoefs(final BigInteger[] coefsCons) {
		for (int i = 0; i < coefsCons.length; i++) {
			if (coefsCons[i].signum() <= 0)
				return false;
		}
		return true;
	}

	/**
	 * computes the level for the backtrack : the highest decision level for
	 * which the conflict is assertive.
	 * 
	 * @param maxLevel
	 *            the lowest level for which the conflict is assertive
	 * @return the highest level (smaller int) for which the constraint is
	 *         assertive.
	 */
	public int getBacktrackLevel(int maxLevel) {
		// we are looking for a level higher than maxLevel
		// where the constraint is still assertive
		VecInt lits;
		int level;
		int indStop = levelToIndex(maxLevel) - 1;
		int indStart = levelToIndex(0);
		BigInteger slack = computeSlack(0).subtract(degree);
		int previous = 0;
		for (int indLevel = indStart; indLevel <= indStop; indLevel++) {
			if (byLevel[indLevel] != null) {
				level = indexToLevel(indLevel);
				assert computeSlack(level).subtract(degree).equals(slack);
				if (isImplyingLiteralOrdered(level, slack)) {
					break;
				}
				// updating the new slack
				lits = byLevel[indLevel];
				int lit;
				for (IteratorInt iterator = lits.iterator(); iterator.hasNext();) {
					lit = iterator.next();
					if (voc.isFalsified(lit)
							&& voc.getLevel(lit) == indexToLevel(indLevel))
						slack = slack.subtract(weightedLits.get(lit));
				}
				if (!lits.isEmpty())
					previous = level;
			}
		}
		assert previous == oldGetBacktrackLevel(maxLevel);
		return previous;
	}

	public int oldGetBacktrackLevel(int maxLevel) {
		int litLevel;
		int borneMax = maxLevel;
		assert oldIsAssertive(borneMax);
		int borneMin = -1;
		// borneMax is the highest level in the search tree where the constraint
		// is assertive
		for (int i = 0; i < size(); i++) {
			litLevel = voc.getLevel(weightedLits.getLit(i));
			if (litLevel < borneMax && litLevel > borneMin
					&& oldIsAssertive(litLevel))
				borneMax = litLevel;
		}
		// the level returned is the first level below borneMax
		// where there is a literal belonging to the constraint
		int retour = 0;
		for (int i = 0; i < size(); i++) {
			litLevel = voc.getLevel(weightedLits.getLit(i));
			if (litLevel > retour && litLevel < borneMax) {
				retour = litLevel;
			}
		}
		return retour;
	}

	public void updateSlack(int level) {
		int dl = levelToIndex(level);
		if (byLevel[dl] != null) {
			int lit;
			for (IteratorInt iterator = byLevel[dl].iterator(); iterator
					.hasNext();) {
				lit = iterator.next();
				if (voc.isFalsified(lit))
					currentSlack = currentSlack.add(weightedLits.get(lit));
			}
		}
	}

	@Override
	void increaseCoef(int lit, BigInteger incCoef) {
		if ((!voc.isFalsified(lit)) || voc.getLevel(lit) == currentLevel) {
			currentSlack = currentSlack.add(incCoef);
		}
		assert byLevel[levelToIndex(voc.getLevel(lit))].contains(lit);
		super.increaseCoef(lit, incCoef);
	}

	@Override
	void decreaseCoef(int lit, BigInteger decCoef) {
		if ((!voc.isFalsified(lit)) || voc.getLevel(lit) == currentLevel) {
			currentSlack = currentSlack.subtract(decCoef);
		}
		assert byLevel[levelToIndex(voc.getLevel(lit))].contains(lit);
		super.decreaseCoef(lit, decCoef);
	}

	@Override
	void setCoef(int lit, BigInteger newValue) {
		int litLevel = voc.getLevel(lit);
		if ((!voc.isFalsified(lit)) || litLevel == currentLevel) {
			if (weightedLits.containsKey(lit))
				currentSlack = currentSlack.subtract(weightedLits.get(lit));
			currentSlack = currentSlack.add(newValue);
		}
		int indLitLevel = levelToIndex(litLevel);
		if (!weightedLits.containsKey(lit)) {
			if (byLevel[indLitLevel] == null) {
				byLevel[indLitLevel] = new VecInt();
			}
			byLevel[indLitLevel].push(lit);

		}
		assert byLevel[indLitLevel] != null;
		assert byLevel[indLitLevel].contains(lit);
		super.setCoef(lit, newValue);
	}

	@Override
	void changeCoef(int indLit, BigInteger newValue) {
		int lit = weightedLits.getLit(indLit);
		int litLevel = voc.getLevel(lit);
		if ((!voc.isFalsified(lit)) || litLevel == currentLevel) {
			if (weightedLits.containsKey(lit))
				currentSlack = currentSlack.subtract(weightedLits.get(lit));
			currentSlack = currentSlack.add(newValue);
		}
		int indLitLevel = levelToIndex(litLevel);
		assert weightedLits.containsKey(lit);
		assert byLevel[indLitLevel] != null;
		assert byLevel[indLitLevel].contains(lit);
		super.changeCoef(indLit, newValue);
	}

	@Override
	void removeCoef(int lit) {
		int litLevel = voc.getLevel(lit);
		if ((!voc.isFalsified(lit)) || litLevel == currentLevel) {
			currentSlack = currentSlack.subtract(weightedLits.get(lit));
		}
		int indLitLevel = levelToIndex(litLevel);
		assert indLitLevel < byLevel.length;
		assert byLevel[indLitLevel] != null;
		assert byLevel[indLitLevel].contains(lit);
		byLevel[indLitLevel].remove(lit);
		super.removeCoef(lit);
	}

	private int getLevelByLevel(int lit) {
		for (int i = 0; i < byLevel.length; i++)
			if (byLevel[i] != null && byLevel[i].contains(lit))
				return i;
		return -1;
	}

	public boolean slackIsCorrect(int dl) {
		return currentSlack.equals(computeSlack(dl));
	}

	@Override
	public String toString() {
		int lit;
		StringBuffer stb = new StringBuffer();
		for (int i = 0; i < size(); i++) {
			lit = weightedLits.getLit(i);
			stb.append(weightedLits.getCoef(i));
			stb.append(".");
			stb.append(Lits.toString(lit));
			stb.append(" ");
			stb.append("[");
			stb.append(voc.valueToString(lit));
			stb.append("@");
			stb.append(voc.getLevel(lit));
			stb.append("]");
		}
		return stb.toString() + " >= " + degree; //$NON-NLS-1$
	}

	public boolean hasBeenReduced() {
		return hasBeenReduced;
	}

	public long getNumberOfReductions() {
		return numberOfReductions;
	}
}
