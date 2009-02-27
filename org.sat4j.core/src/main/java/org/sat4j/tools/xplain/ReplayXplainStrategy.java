package org.sat4j.tools.xplain;

import java.util.Collection;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class ReplayXplainStrategy implements XplainStrategy {

	public IVecInt explain(ISolver solver, Collection<Integer> constrsIds, IVecInt assumps) throws TimeoutException {
		IVecInt encodingAssumptions = new VecInt(constrsIds.size() + assumps.size());
		assumps.copyTo(encodingAssumptions);
		for (Integer p : constrsIds) {
			encodingAssumptions.push(p);
		}
		boolean shouldContinue;
		int startingPoint = assumps.size();
		do {
			shouldContinue = false;
			int i = startingPoint;
			encodingAssumptions.set(i, -encodingAssumptions.get(i));
			assert encodingAssumptions.get(i) < 0;
			while (solver.isSatisfiable(encodingAssumptions)) {
				i++;
				assert encodingAssumptions.get(i) > 0;
				encodingAssumptions.set(i, -encodingAssumptions.get(i));
			}
			if (i > startingPoint) {
				assert !solver.isSatisfiable(encodingAssumptions);
				if (i < encodingAssumptions.size()) {
					// latest constraint is for sure responsible for the inconsistency.
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
		} while (shouldContinue&&solver.isSatisfiable(encodingAssumptions));
		IVecInt constrsKeys = new VecInt(startingPoint);
		for (int i = assumps.size(); i < startingPoint; i++) {
			constrsKeys.push(-encodingAssumptions.get(i));
		}
		return constrsKeys;
	}

}
