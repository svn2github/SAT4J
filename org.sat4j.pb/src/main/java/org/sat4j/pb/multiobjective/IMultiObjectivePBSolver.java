package org.sat4j.pb.multiobjective;

import java.util.List;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;

public interface IMultiObjectivePBSolver extends IPBSolver {

    void addObjectiveFunction(ObjectiveFunction obj);

    List<Integer> getObjectiveValues();

}
