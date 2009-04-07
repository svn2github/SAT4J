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

import org.sat4j.pb.constraints.pb.IDataStructurePB;
import org.sat4j.pb.constraints.pb.PBConstr;
import org.sat4j.pb.constraints.pb.PuebloMinWatchPb;
import org.sat4j.specs.ContradictionException;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PuebloPBMinDataStructure extends AbstractPBDataStructureFactory {

	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sat4j.minisat.constraints.AbstractPBDataStructureFactory#
	 * constraintFactory(org.sat4j.specs.VecInt, org.sat4j.specs.VecInt,
	 * boolean, int)
	 */
	@Override
	protected PBConstr constraintFactory(int[] literals, BigInteger[] coefs,
			BigInteger degree) throws ContradictionException {
		return PuebloMinWatchPb.normalizedMinWatchPbNew(solver,
				getVocabulary(), literals, coefs, degree);
	}

	@Override
	protected PBConstr learntConstraintFactory(IDataStructurePB dspb) {
		return PuebloMinWatchPb.normalizedWatchPbNew(getVocabulary(), dspb);
	}

}
