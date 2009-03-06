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

import java.math.BigInteger;

import org.sat4j.minisat.constraints.AbstractDataStructureFactory;
import org.sat4j.minisat.constraints.cnf.Clauses;
import org.sat4j.minisat.constraints.cnf.LearntWLClause;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.pb.constraints.pb.AtLeastPB;
import org.sat4j.pb.constraints.pb.IDataStructurePB;
import org.sat4j.pb.constraints.pb.IInternalPBConstraintCreator;
import org.sat4j.pb.constraints.pb.WLClausePB;
import org.sat4j.pb.core.PBDataStructureFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractPBDataStructureFactory extends
		AbstractDataStructureFactory implements PBDataStructureFactory,
		IInternalPBConstraintCreator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Constr createClause(IVecInt literals) throws ContradictionException {
		IVecInt v = Clauses.sanityCheck(literals, getVocabulary(), solver);
		if (v == null)
			return null;
		return WLClausePB.brandNewClause(solver, getVocabulary(), v);
	}

	public Constr createUnregisteredClause(IVecInt literals) {
		return new LearntWLClause(literals, getVocabulary());
	}

	@Override
	public Constr createCardinalityConstraint(IVecInt literals, int degree)
			throws ContradictionException {
		return AtLeastPB.atLeastNew(solver, getVocabulary(), literals, degree);
	}

	public Constr createPseudoBooleanConstraint(IVecInt literals,
			IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
			throws ContradictionException {
		return constraintFactory(literals, coefs, moreThan, degree);
	}

	/**
	 * @param literals
	 *            the literals
	 * @param coefs
	 *            the coefficients
	 * @param moreThan
	 * @param degree
	 *            the degree of the constraint
	 * @return a new PB constraint
	 */
	protected abstract Constr constraintFactory(IVecInt literals,
			IVecInt coefs, boolean moreThan, int degree)
			throws ContradictionException;

	protected abstract Constr constraintFactory(IDataStructurePB dspb);

	protected abstract Constr constraintFactory(IVecInt literals,
			IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
			throws ContradictionException;

	public Constr createUnregisteredPseudoBooleanConstraint(IVecInt literals,
			IVec<BigInteger> coefs, BigInteger degree) {
		return constraintFactory(literals, coefs, degree);
	}

	public Constr createUnregisteredPseudoBooleanConstraint(
			IDataStructurePB dspb) {
		return constraintFactory(dspb);
	}

	public IConstr createUnregisteredPseudoBooleanConstraint(IVecInt literals,
			IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
			throws ContradictionException {
		return constraintFactory(literals, coefs, moreThan, degree);
	}

	/**
	 * @param literals
	 * @param coefs
	 * @param degree
	 * @return a new PB constraint
	 */
	protected abstract Constr constraintFactory(IVecInt literals,
			IVecInt coefs, int degree);

	protected abstract Constr constraintFactory(IVecInt literals,
			IVec<BigInteger> coefs, BigInteger degree);

	@Override
	protected ILits createLits() {
		return new Lits();
	}

}
