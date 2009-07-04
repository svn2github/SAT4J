package org.sat4j.intervalorders;

public abstract class IRelation {
	private int numberOfVotes;
	private int penalty;

	public int getNumberOfVotes() {
		return numberOfVotes;
	}

	public void setNumberOfVotes(int nbvotes) {
		this.numberOfVotes = nbvotes;
	}

	public int getPenalty() {
		return penalty;
	}

	public void setPenalty(int penalty) {
		this.penalty = penalty;
	}

	abstract public String toDot(int nbVoters);
}
