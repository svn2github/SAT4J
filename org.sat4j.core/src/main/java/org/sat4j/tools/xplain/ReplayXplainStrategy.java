package org.sat4j.tools.xplain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class ReplayXplainStrategy implements XplainStrategy {

	public IVecInt explain(ISolver solver, int nbnewvar, int nborigvars,
			IVec<IConstr> constrs, IVecInt assumps) throws TimeoutException {
		List<Pair> pairs = new ArrayList<Pair>(nbnewvar);
		IConstr constr;
		for (int i = 0; i < nbnewvar; i++) {
			constr = constrs.get(i);
			if (constr != null) {
				pairs.add(new Pair(i + 1, constr.getActivity()));
			} else {
				pairs.add(new Pair(i + 1, 0.0));
			}
		}
		Collections.sort(pairs);
		IVecInt extraVariables = new VecInt(nbnewvar+assumps.size());
		assumps.copyTo(extraVariables);
		// for (int p = 1; p <=nbnewvar; p++) {
		// extraVariables.push(p + nborigvars);
		// }
		for (Pair p : pairs) {
			extraVariables.push(p.id + nborigvars);
		}
		boolean shouldContinue;
		int startingPoint = assumps.size();
		do {
			shouldContinue = false;
			int i = startingPoint;
			extraVariables.set(i, -extraVariables.get(i));
			assert extraVariables.get(i) < 0;
			while (solver.isSatisfiable(extraVariables)) {
				i++;
				assert extraVariables.get(i) > 0;
				extraVariables.set(i, -extraVariables.get(i));
			}
			if (i > startingPoint) {
				assert !solver.isSatisfiable(extraVariables);
				if (i < extraVariables.size()) {
					int tmp = extraVariables.get(i);
					for (int j = i; j >= startingPoint + 1; j--) {
						extraVariables.set(j, -extraVariables.get(j - 1));
					}
					extraVariables.set(startingPoint, tmp);
				}
				shouldContinue = true;
			}
			startingPoint++;
		} while (shouldContinue);
		IVecInt clauseNumbers = new VecInt(startingPoint);
		for (int i = assumps.size(); i < startingPoint; i++) {
			clauseNumbers.push(-extraVariables.get(i) - nborigvars);
		}
		return clauseNumbers;
	}

}
