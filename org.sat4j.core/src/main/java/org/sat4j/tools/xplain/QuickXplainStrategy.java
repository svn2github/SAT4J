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
package org.sat4j.tools.xplain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * @since 2.1
 */
public class QuickXplainStrategy implements XplainStrategy {

	private boolean computationCanceled;

	public void cancelExplanationComputation() {
		computationCanceled = true;
	}

	public IVecInt explain(ISolver solver, Map<Integer, IConstr> constrs,
			IVecInt assumps) throws TimeoutException {
		computationCanceled = false;
		IVecInt encodingAssumptions = new VecInt(constrs.size()
				+ assumps.size());
		List<Pair> pairs = new ArrayList<Pair>(constrs.size());
		IConstr constr;
		for (Map.Entry<Integer, IConstr> entry : constrs.entrySet()) {
			constr = entry.getValue();
			pairs.add(new Pair(entry.getKey(), constr));
		}
		Collections.sort(pairs);

		assumps.copyTo(encodingAssumptions);
		// for (Integer p : constrsIds) {
		// encodingAssumptions.push(p);
		// }
		for (Pair p : pairs) {
			encodingAssumptions.push(p.key);
		}
		IVecInt results = new VecInt(encodingAssumptions.size());
		computeExplanation(solver, encodingAssumptions, assumps.size(),
				encodingAssumptions.size() - 1, results);
		return results;
	}

	private void computeExplanation(ISolver solver,
			IVecInt encodingAssumptions, int start, int end, IVecInt result)
			throws TimeoutException {
		if (!solver.isSatisfiable(encodingAssumptions)) {
			return;
		}
		int i = start;
		encodingAssumptions.set(i, -encodingAssumptions.get(i));
		assert encodingAssumptions.get(i) < 0;
		while (!computationCanceled
				&& solver.isSatisfiable(encodingAssumptions)) {
			if (i == end) {
				for (int j = start; j <= end; j++) {
					encodingAssumptions.set(j, -encodingAssumptions.get(j));
				}
				return;
			}
			i++;
			assert encodingAssumptions.get(i) > 0;
			encodingAssumptions.set(i, -encodingAssumptions.get(i));
		}
		result.push(-encodingAssumptions.get(i));
		if (start == i) {
			return;
		}
		int newend = i - 1;
		int split = (newend + start) / 2;
		if (split < newend) {
			for (int j = split + 1; j < i; j++) {
				encodingAssumptions.set(j, -encodingAssumptions.get(j));
			}
			computeExplanation(solver, encodingAssumptions, split + 1, newend,
					result);
		}
		if (start <= split) {
			for (int j = start; j <= split; j++) {
				encodingAssumptions.set(j, -encodingAssumptions.get(j));
			}
			computeExplanation(solver, encodingAssumptions, start, split,
					result);
		}
		if (computationCanceled) {
			throw new TimeoutException();
		}
	}
}
