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
package org.sat4j.pb.constraints;

import java.lang.reflect.Field;
import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.AbstractDataStructureFactory;
import org.sat4j.minisat.constraints.cnf.Clauses;
import org.sat4j.minisat.constraints.cnf.LearntBinaryClause;
import org.sat4j.minisat.constraints.cnf.LearntHTClause;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.constraints.cnf.OriginalBinaryClause;
import org.sat4j.minisat.constraints.cnf.OriginalHTClause;
import org.sat4j.minisat.constraints.cnf.UnitClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.pb.constraints.pb.AtLeastPB;
import org.sat4j.pb.constraints.pb.IDataStructurePB;
import org.sat4j.pb.constraints.pb.Pseudos;
import org.sat4j.pb.core.PBDataStructureFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractPBDataStructureFactory extends
		AbstractDataStructureFactory implements PBDataStructureFactory {

	interface INormalizer {
		PBContainer nice(IVecInt ps, IVec<BigInteger> bigCoefs,
				boolean moreThan, BigInteger bigDeg, ILits voc)
				throws ContradictionException;
	}

	public static final INormalizer FOR_COMPETITION = new INormalizer() {

		private static final long serialVersionUID = 1L;

		public PBContainer nice(IVecInt literals, IVec<BigInteger> coefs,
				boolean moreThan, BigInteger degree, ILits voc)
				throws ContradictionException {
			if (literals.size() != coefs.size())
				throw new IllegalArgumentException(
						"Number of coeff and literals are different!!!");
			IVecInt cliterals = new VecInt(literals.size());
			literals.copyTo(cliterals);
			IVec<BigInteger> ccoefs = new Vec<BigInteger>(literals.size());
			coefs.copyTo(ccoefs);
			for (int i = 0; i < cliterals.size();) {
				if (ccoefs.get(i).equals(BigInteger.ZERO)) {
					cliterals.delete(i);
					ccoefs.delete(i);
				} else {
					i++;
				}
			}
			int[] theLits = new int[cliterals.size()];
			cliterals.copyTo(theLits);
			BigInteger[] normCoefs = new BigInteger[ccoefs.size()];
			ccoefs.copyTo(normCoefs);
			BigInteger degRes = Pseudos.niceParametersForCompetition(theLits,
					normCoefs, moreThan, degree);
			return new PBContainer(theLits, normCoefs, degRes);

		}

	};

	public static final INormalizer NO_COMPETITION = new INormalizer() {

		private static final long serialVersionUID = 1L;

		public PBContainer nice(IVecInt literals, IVec<BigInteger> coefs,
				boolean moreThan, BigInteger degree, ILits voc)
				throws ContradictionException {
			IDataStructurePB res = Pseudos.niceParameters(literals, coefs,
					moreThan, degree, voc);
			int size = res.size();
			int[] theLits = new int[size];
			BigInteger[] theCoefs = new BigInteger[size];
			res.buildConstraintFromMapPb(theLits, theCoefs);
			BigInteger theDegree = res.getDegree();
			return new PBContainer(theLits, theCoefs, theDegree);
		}
	};

	private INormalizer norm = FOR_COMPETITION;

	protected INormalizer getNormalizer() {
		return norm;
	}

	public void setNormalizer(String simp) {
		Field f;
		try {
			f = AbstractPBDataStructureFactory.class.getDeclaredField(simp);
			norm = (INormalizer) f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			norm = FOR_COMPETITION;
		}
	}

	public void setNormalizer(INormalizer normalizer) {
		norm = normalizer;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Constr createClause(IVecInt literals) throws ContradictionException {
		IVecInt v = Clauses.sanityCheck(literals, getVocabulary(), solver);
		if (v == null)
			return null;
		if (v.size() == 2) {
			return OriginalBinaryClause.brandNewClause(solver, getVocabulary(),
					v);
		}
		return OriginalHTClause.brandNewClause(solver, getVocabulary(), v);
	}

	public Constr createUnregisteredClause(IVecInt literals) {
		if (literals.size() == 1) {
			return new UnitClause(literals.last());
		}
		if (literals.size() == 2) {
			return new LearntBinaryClause(literals, getVocabulary());
		}
		return new LearntHTClause(literals, getVocabulary());
	}

	@Override
	public Constr createCardinalityConstraint(IVecInt literals, int degree)
			throws ContradictionException {
		return AtLeastPB.atLeastNew(solver, getVocabulary(), literals, degree);
	}

	public Constr createPseudoBooleanConstraint(IVecInt literals,
			IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
			throws ContradictionException {
		PBContainer res = getNormalizer().nice(literals, coefs, moreThan,
				degree, getVocabulary());
		return constraintFactory(res.lits, res.coefs, res.degree);
	}

	public Constr createUnregisteredPseudoBooleanConstraint(
			IDataStructurePB dspb) {
		return learntConstraintFactory(dspb);
	}

	protected abstract Constr constraintFactory(int[] literals,
			BigInteger[] coefs, BigInteger degree)
			throws ContradictionException;

	protected abstract Constr learntConstraintFactory(IDataStructurePB dspb);

	@Override
	protected ILits createLits() {
		return new Lits();
	}

}
