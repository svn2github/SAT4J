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

package org.sat4j.tools.encoding;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * This class allows the use of different encodings for different cardinality
 * constraints.
 * 
 * @author stephanieroussel
 * @since 2.3.1
 */
public class Policy extends EncodingStrategyAdapter {

	private final Sequential seq = new Sequential();
	private final Binary binary = new Binary();
	private final Product product = new Product();
	private final Commander commander = new Commander();

	@Override
	public IConstr addAtMost(ISolver solver, IVecInt literals, int k)
			throws ContradictionException {

		return super.addAtMost(solver, literals, k);
		// Commander commander = new Commander();
		// Product product = new Product();
		// if (k == 0 || literals.size() == 1) {
		// // will propagate unit literals
		// return super.addAtMost(solver, literals, k);
		// }
		// if (literals.size() <= 1) {
		// throw new UnsupportedOperationException(
		// "requires at least 2 literals");
		// }
		// if (k == 1) {
		// // return ladder.addAtMostOne(solver, literals);
		// // return binary.addAtMostOne(solver, literals);
		// return commander.addAtMostOne(solver, literals);
		// // return product.addAtMostOne(solver, literals);
		// }
		// return seq.addAtMost(solver, literals, k);
		// return product.addAtMost(solver, literals, k);
		// return commander.addAtMost(solver, literals, k);
	}

	@Override
	public IConstr addExactly(ISolver solver, IVecInt literals, int n)
			throws ContradictionException {
		// Ladder ladder = new Ladder();
		// if (n == 1) {
		// return ladder.addExactlyOne(solver, literals);
		// }

		return super.addExactly(solver, literals, n);
	}

}
