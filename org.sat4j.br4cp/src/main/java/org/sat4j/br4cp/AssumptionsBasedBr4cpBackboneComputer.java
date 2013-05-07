package org.sat4j.br4cp;

import java.util.Iterator;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.Backbone;


/**
 * A class used to compile propagation in configuration problem due to
 * assumptions. This class uses assumptions to keep informations. Although it is
 * efficient to chain assumptions ("scenarios"), this class should get
 * efficiency issues while canceling first assumptions.
 * 
 * @author lonca
 */
public class AssumptionsBasedBr4cpBackboneComputer extends DefaultBr4cpBackboneComputer {

	public AssumptionsBasedBr4cpBackboneComputer(ISolver solver,
			ConfigVarMap idMap) throws TimeoutException {
		super(solver, idMap);
	}

	@Override
	protected IVecInt computeBackbone(ISolver solver) throws TimeoutException {
		IVecInt assumps = new VecInt();
		for (Iterator<Set<Integer>> it = solverAssumptions.iterator(); it
				.hasNext();) {
			for (Iterator<Integer> it2 = it.next().iterator(); it2.hasNext();)
				assumps.push(it2.next());
		}
		if (!this.backbonesStack.isEmpty()) {
			for (IteratorInt it = this.backbonesStack.peek().iterator(); it
					.hasNext();) {
				int next = it.next();
				assumps.push(next);
			}
		}
		return Backbone.compute(solver, assumps);
	}

}
