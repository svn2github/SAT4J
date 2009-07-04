package org.sat4j.intervalorders;

public class Prefer extends IRelation {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Prefer other = (Prefer) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	private final String from, to;

	public Prefer(String from, String to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public String toString() {
		return from + ">" + to;
	}

	@Override
	public String toDot(int nbVoters) {
		String color = getNumberOfVotes() == 0 ? ", color=red "
				: getNumberOfVotes() == nbVoters ? ", color=blue " : "";
		return from + " -> " + to + "[ label=\"" + getPenalty() + "\"" + color
				+ "]";
	}

}
