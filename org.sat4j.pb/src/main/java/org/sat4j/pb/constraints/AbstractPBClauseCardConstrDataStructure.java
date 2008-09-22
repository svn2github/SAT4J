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

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.pb.constraints.pb.IDataStructurePB;
import org.sat4j.pb.constraints.pb.PBConstr;
import org.sat4j.pb.constraints.pb.WatchPb;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public abstract class AbstractPBClauseCardConstrDataStructure extends
        AbstractPBDataStructureFactory {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static final BigInteger MAX_INT_VALUE = BigInteger
            .valueOf(Integer.MAX_VALUE);

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, boolean, int)
     */
    @Override
    protected PBConstr constraintFactory(IVecInt literals, IVecInt coefs,
            boolean moreThan, int degree) throws ContradictionException {
        return constraintFactory(literals, WatchPb.toVecBigInt(coefs),
                moreThan, WatchPb.toBigInt(degree));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, int)
     */
    @Override
    protected PBConstr constraintFactory(IVecInt literals, IVecInt coefs,
            int degree) {
        return constraintFactory(literals, WatchPb.toVecBigInt(coefs), WatchPb
                .toBigInt(degree));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, boolean, int)
     */
    @Override
    protected PBConstr constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException {
        IDataStructurePB mpb = WatchPb.niceParameters(literals, coefs,
                moreThan, degree, getVocabulary());
        if (mpb == null)
            return null;
        int size = mpb.size();
        int[] theLists = new int[size];
        BigInteger[] normCoefs = new BigInteger[size];
        mpb.buildConstraintFromMapPb(theLists, normCoefs);
        if (mpb.getDegree().equals(BigInteger.ONE)) {
            IVecInt v = WLClause.sanityCheck(new VecInt(theLists), getVocabulary(),
                    solver);
            if (v == null)
                return null;
            return constructClause(v);
        }
        if (coefficientsEqualToOne(new Vec<BigInteger>(normCoefs))) {
            assert mpb.getDegree().compareTo(MAX_INT_VALUE) < 0;
            return constructCard(new VecInt(theLists), mpb.getDegree().intValue());
        }
        //return constructPB(mpb);
        return constructPB(theLists,normCoefs,mpb.getDegree());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, int)
     */
    @Override
    protected PBConstr constraintFactory(IDataStructurePB dspb) {
        if (dspb.getDegree().equals(BigInteger.ONE)) {
            return constructLearntClause(dspb);
        }
        if (dspb.isCardinality()) {
            return constructLearntCard(dspb);
        }
        return constructLearntPB(dspb);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, int)
     */
    @Override
    protected PBConstr constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree) {
        if (degree.equals(BigInteger.ONE)) {
            return constructLearntClause(literals);
        }
        if (coefficientsEqualToOne(coefs)) {
            return constructLearntCard(literals, degree.intValue());
        }
        return constructLearntPB(literals, coefs, degree);
    }


    
    static boolean coefficientsEqualToOne(IVec<BigInteger> coeffs) {
        for (int i = 0; i < coeffs.size(); i++)
            if (!coeffs.get(i).equals(BigInteger.ONE))
                return false;
        return true;
    }

    abstract protected PBConstr constructClause(IVecInt v);

    abstract protected PBConstr constructCard(IVecInt theLits, int degree)
            throws ContradictionException;

    abstract protected PBConstr constructPB(IDataStructurePB mpb)
            throws ContradictionException;

    abstract protected PBConstr constructPB(int[] theLits, BigInteger[] coefs, BigInteger degree)
    throws ContradictionException;

    abstract protected PBConstr constructLearntClause(IVecInt literals);

    abstract protected PBConstr constructLearntCard(IVecInt literals, int degree);

    abstract protected PBConstr constructLearntPB(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree);

    abstract protected PBConstr constructLearntClause(IDataStructurePB dspb);

    abstract protected PBConstr constructLearntCard(IDataStructurePB dspb);

    abstract protected PBConstr constructLearntPB(IDataStructurePB dspb);


}
