package org.sat4j.pb.tools;

import java.math.BigInteger;

import org.sat4j.pb.IPBSolverService;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;
import org.sat4j.tools.SolutionFoundListener;

public class SearchOptimizerListener implements
        SearchListener<IPBSolverService> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private IPBSolverService solverService;

    private ObjectiveFunction obj;

    private final SolutionFoundListener sfl;

    public SearchOptimizerListener(SolutionFoundListener sfl) {
        this.sfl = sfl;
    }

    public void init(IPBSolverService solverService) {
        this.obj = solverService.getObjectiveFunction();
        this.solverService = solverService;
    }

    public void assuming(int p) {
        // TODO Auto-generated method stub

    }

    public void propagating(int p, IConstr reason) {
        // TODO Auto-generated method stub

    }

    public void backtracking(int p) {
        // TODO Auto-generated method stub

    }

    public void adding(int p) {
        // TODO Auto-generated method stub

    }

    public void learn(IConstr c) {
        // TODO Auto-generated method stub

    }

    public void delete(int[] clause) {
        // TODO Auto-generated method stub

    }

    public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
        // TODO Auto-generated method stub

    }

    public void conflictFound(int p) {
        // TODO Auto-generated method stub

    }

    public void solutionFound(int[] model) {
        if (obj != null) {
            BigInteger modelDegree = obj.calculateDegree(model);
            System.out.println("c objective function value = " + modelDegree);
            this.solverService.addAtMostOnTheFly(obj.getVars(),
                    obj.getCoeffs(), modelDegree.subtract(BigInteger.ONE));
        }
        sfl.onSolutionFound(model);
    }

    public void beginLoop() {
        // TODO Auto-generated method stub

    }

    public void start() {
        // TODO Auto-generated method stub

    }

    public void end(Lbool result) {
        if (result == Lbool.FALSE)
            sfl.onUnsatTermination();
    }

    public void restarting() {
        // TODO Auto-generated method stub

    }

    public void backjump(int backjumpLevel) {
        // TODO Auto-generated method stub

    }

    public void cleaning() {
        // TODO Auto-generated method stub

    }
}
