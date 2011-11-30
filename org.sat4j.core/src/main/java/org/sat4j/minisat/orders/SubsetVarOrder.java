package org.sat4j.minisat.orders;

import org.sat4j.minisat.core.Heap;

public class SubsetVarOrder extends VarOrderHeap {

	private final int[] varsToTest;

	public SubsetVarOrder(int[] varsToTest) {
		this.varsToTest = new int[varsToTest.length];
		System.arraycopy(varsToTest, 0, this.varsToTest, 0, varsToTest.length);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init() {
		int nlength = lits.nVars() + 1;
		if (activity == null || activity.length < nlength) {
			activity = new double[nlength];
		}
		phaseStrategy.init(nlength);
		activity[0] = -1;
		heap = new Heap(activity);
		heap.setBounds(nlength);
		for (int var : varsToTest) {
			assert var > 0;
			assert var <= lits.nVars() : "" + lits.nVars() + "/" + var; //$NON-NLS-1$ //$NON-NLS-2$
			activity[var] = 0.0;
			if (lits.belongsToPool(var)) {
				heap.insert(var);
			}
		}
	}
}
