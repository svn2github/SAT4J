package org.sat4j.intervalorders;

public class NegRelation extends IRelation {

	public final IRelation relation;

	public NegRelation(IRelation relation) {
		this.relation = relation;
	}

	@Override
	public int getNumberOfVotes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNumberOfVotes(int nbvotes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toDot(int nbVoters) {
		throw new UnsupportedOperationException();
	}
}
