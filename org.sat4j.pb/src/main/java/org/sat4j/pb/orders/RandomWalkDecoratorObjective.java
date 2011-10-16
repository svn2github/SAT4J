package org.sat4j.pb.orders;

import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.pb.ObjectiveFunction;

public class RandomWalkDecoratorObjective extends RandomWalkDecorator implements
		IOrderObjective {

	private final IOrderObjective objorder;

	public RandomWalkDecoratorObjective(VarOrderHeapObjective order, double p) {
		super(order, p);
		objorder = order;
	}

	public void setObjectiveFunction(ObjectiveFunction obj) {
		objorder.setObjectiveFunction(obj);
	}

}
