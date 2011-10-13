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
 * Based on the original MiniSat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 * 
 *******************************************************************************/
package org.sat4j.pb.constraints;

import java.math.BigInteger;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.pb.constraints.pb.IDataStructurePB;
import org.sat4j.pb.constraints.pb.MaxWatchPb;
import org.sat4j.pb.constraints.pb.MaxWatchPbLongCP;
import org.sat4j.specs.ContradictionException;

public class MaxLongWatchPBConstructor implements IPBConstructor {

	public Constr constructLearntPB(ILits voc, IDataStructurePB dspb) {
		if (dspb.isLongSufficient()) {
			return MaxWatchPbLongCP.normalizedWatchPbNew(voc, dspb);
		}
		return MaxWatchPb.normalizedWatchPbNew(voc, dspb);
	}

	public Constr constructPB(UnitPropagationListener solver, ILits voc,
			int[] theLits, BigInteger[] coefs, BigInteger degree)
			throws ContradictionException {
		if (isLongSufficient(coefs, degree)) {
			return MaxWatchPbLongCP.normalizedMaxWatchPbNew(solver, voc,
					theLits, coefs, degree);
		}
		return MaxWatchPb.normalizedMaxWatchPbNew(solver, voc, theLits, coefs,
				degree);
	}

	public static boolean isLongSufficient(BigInteger[] coefs, BigInteger degree) {
		BigInteger somCoefs = BigInteger.ZERO;
		for (int i = 0; somCoefs.bitLength() < Long.SIZE && i < coefs.length; i++)
			somCoefs = somCoefs.add(coefs[i]);
		return somCoefs.bitLength() < Long.SIZE;
	}

}
