package org.sat4j.intervalorders;

import org.sat4j.pb.tools.INegator;

public class RelationNegator implements INegator<IRelation> {

	public boolean isNegated(IRelation thing) {
		return thing instanceof NegRelation;
	}

	public IRelation unNegate(IRelation thing) {
		return ((NegRelation) thing).relation;
	}

}
