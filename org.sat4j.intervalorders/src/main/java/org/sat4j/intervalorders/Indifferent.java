package org.sat4j.intervalorders;

public class Indifferent extends IRelation {

	private final String a, b;

	public Indifferent(String a, String b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public String toString() {
		return a + "~" + b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * a.hashCode() + prime * b.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Indifferent)) {
			return false;
		}
		Indifferent other = (Indifferent) obj;
		if (a.equals(other.a) && b.equals(other.b) || a.equals(other.b)
				&& b.equals(other.a)) {
			return true;
		}
		return false;
	}

	@Override
	public String toDot(int nbVoters) {
		if (a.equals(b)) {
			return "";
		}
		String style = getNumberOfVotes() == nbVoters ? "bold" : "dashed";
		return a + " -> " + b + " [label=\"" + getPenalty() + "\" style="
				+ style + ", color=green, dir=none, rank=same]";
	}
}
