package org.sat4j.intervalorders;

public class Interval implements Comparable<Interval> {
	public final int high;
	public final int low;

	public Interval(int high, int low) {
		this.high = high;
		this.low = low;
	}

	@Override
	public String toString() {
		return "[" + low + "," + high + "]";
	}

	public int compareTo(Interval o) {
		if (low > o.high) {
			return -1;
		}
		if (o.low > high) {
			return 1;
		}
		return 0;
	}
}
