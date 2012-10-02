package org.sat4j.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class CheckMUSSolutionListener implements SolutionFoundListener {

    private List<IVecInt> clauses;

    private String explain;

    // public CheckThatItIsAMUS(List<IVecInt> clauses) {
    // this.clauses = clauses;
    // }

    public CheckMUSSolutionListener() {
        this.clauses = new ArrayList<IVecInt>();
    }

    // public void setOriginalClauses(List<IVecInt> clauses) {
    // this.clauses = clauses;
    // }

    public void addOriginalClause(IVecInt clause) {
        IVecInt newClause = new VecInt(clause.size());
        if (clauses == null) {
            this.clauses = new ArrayList<IVecInt>();
        }
        clause.copyTo(newClause);
        clauses.add(newClause);
    }

    /**
     * 
     * @param mus
     *            containing the clauses identifiers
     * @param clauses
     *            the original set of clauses
     * @return
     */
    public boolean checkThatItIsAMUS(IVecInt mus) {
        boolean result = false;

        ISolver solver = SolverFactory.newDefault();

        try {
            for (int i = 0; i < mus.size(); i++) {
                solver.addClause(clauses.get(mus.get(i) - 1));
            }

            result = !solver.isSatisfiable();

            if (!result) {
                explain = "The set of clauses in the MUS is SAT : "
                        + Arrays.toString(solver.model());
                return result;
            }

        } catch (ContradictionException e) {
            result = true;
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < mus.size(); i++) {
                solver = SolverFactory.newDefault();
                for (int j = 0; j < mus.size(); j++) {
                    if (j != i) {
                        solver.addClause(clauses.get(mus.get(j) - 1));
                    }
                }
                result = result && solver.isSatisfiable();
                if (!result) {
                    explain = "The subset of clauses in the MUS not containing clause "
                            + (i + 1)
                            + " is SAT : "
                            + Arrays.toString(solver.model());
                    return result;
                }
            }
        } catch (ContradictionException e) {
            result = false;
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return result;

    }

    public void onSolutionFound(int[] solution) {

    }

    public void onSolutionFound(IVecInt solution) {
        if (checkThatItIsAMUS(solution))
            System.out.println(solution + " is a MUS");
        else
            System.out.println(solution + " is not a MUS \n" + explain);
    }
}
