/**
 * 
 */
package org.sat4j.tools.xplain;


class Pair implements Comparable<Pair> {
	int id;
	double activity;

	public Pair(int id, double activity) {
		this.id = id;
		this.activity = activity;
	}

	public int compareTo(Pair p) {
		if (activity > p.activity)
			return -1;
		if (activity < p.activity)
			return 1;
		return 0;
	}
	@Override
	public String toString() {
		return Integer.toString(id)+"("+activity+")";
	}
}