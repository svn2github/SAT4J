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

public class ReplayXplainStrategy implements XplainStrategy {

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

		boolean shouldContinue;
		int startingPoint = assumps.size();
		do {
			shouldContinue = false;
			int i = startingPoint;
			encodingAssumptions.set(i, -encodingAssumptions.get(i));
			assert encodingAssumptions.get(i) < 0;
			while (!computationCanceled
					&& solver.isSatisfiable(encodingAssumptions)) {
				i++;
				assert encodingAssumptions.get(i) > 0;
				encodingAssumptions.set(i, -encodingAssumptions.get(i));
			}
			if (!computationCanceled && i > startingPoint) {
				assert !solver.isSatisfiable(encodingAssumptions);
				if (i < encodingAssumptions.size()) {
					// latest constraint is for sure responsible for the
					// inconsistency.
					int tmp = encodingAssumptions.get(i);
					for (int j = i; j > startingPoint; j--) {
						encodingAssumptions.set(j, -encodingAssumptions
								.get(j - 1));
					}
					encodingAssumptions.set(startingPoint, tmp);
				}
				shouldContinue = true;
			}
			startingPoint++;
		} while (!computationCanceled && shouldContinue
				&& solver.isSatisfiable(encodingAssumptions));
		if (computationCanceled) {
			throw new TimeoutException();
		}
		IVecInt constrsKeys = new VecInt(startingPoint);
		for (int i = assumps.size(); i < startingPoint; i++) {
			constrsKeys.push(-encodingAssumptions.get(i));
		}
		return constrsKeys;
	}
}
