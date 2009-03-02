package org.sat4j.tools.xplain;

import org.sat4j.specs.IConstr;

public class Pair implements Comparable<Pair> {
	public final Integer key;
	public final IConstr constr;
	public static final double NOTHING = 0.0001;

	public Pair(Integer key, IConstr constr) {
		this.key = key;
		this.constr = constr;
	}

	public int compareTo(Pair arg0) {
		if (arg0.constr == constr) {
			return 0;
		}
		if (arg0.constr == null) {
			return -1;
		}
		if (constr == null) {
			return 1;
		}
		double deltaActivity = constr.getActivity() - arg0.constr.getActivity();
		if (Math.abs(deltaActivity) < NOTHING) {
			int deltaSize = constr.size() - arg0.constr.size();
			if (deltaSize == 0) {
				return key - arg0.key;
			}
			return deltaSize;
		}
		return deltaActivity > 0 ? -1 : 1;
	}

}
