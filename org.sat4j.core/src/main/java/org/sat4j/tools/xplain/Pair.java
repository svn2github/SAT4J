package org.sat4j.tools.xplain;

public class Pair implements Comparable<Pair> {
	public final Integer key;
	public final double activity;

	public Pair(Integer key, double activity) {
		this.key = key;
		this.activity = activity;
	}

	public int compareTo(Pair arg0) {
		return activity > arg0.activity ? -1 : 1;
	}

}
