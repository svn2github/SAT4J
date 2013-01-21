package org.sat4j.tools;

import java.util.ArrayList;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class AllMUSesOnePass {

    private final List<IVecInt> musList;
    private AbstractClauseSelectorSolver<ISolver> css;

    private final boolean group;

    public AllMUSesOnePass(boolean group) {
        if (!group) {
            this.css = new NegationDecorator<ISolver>(
                    new NegationDecorator<ISolver>(SolverFactory.newDefault()));
        } else {
            this.css = new GroupClauseSelectorSolver<ISolver>(
                    SolverFactory.newDefault());
        }
        musList = new ArrayList<IVecInt>();
        this.group = group;
    }

    public AllMUSesOnePass() {
        this(false);
    }

    /**
     * Gets an instance of ISolver that can be used to compute all MUSes
     * 
     * @return the instance of ISolver to which the clauses will be added
     */
    public <T extends ISolver> T getSolverInstance() {
        return (T) this.css;
    }

    public List<IVecInt> computeAllMUSes() {
        return computeAllMUSes(SolutionFoundListener.VOID);
    }

    public List<IVecInt> computeAllMUSes(SolutionFoundListener listener) {
        int nVar = css.nVars();

        IVecInt pLits = new VecInt();
        for (Integer i : css.getAddedVars()) {
            pLits.push(i);
        }

        Minimal4InclusionModel min4Inc = new Minimal4InclusionModel(css, pLits);

        IVecInt blockingClause;

        IVecInt secondPhaseClause;

        IVecInt fullMUS = new VecInt();
        IVecInt mus;

        int clause;

        for (int i = 0; i < css.getAddedVars().size(); i++) {
            fullMUS.push(i + 1);
        }

        // first phase
        try {

            while (min4Inc.isSatisfiable()) {
                int[] fullmodel = min4Inc.modelWithInternalVariables();

                mus = new VecInt();
                fullMUS.copyTo(mus);

                blockingClause = new VecInt();
                secondPhaseClause = new VecInt();
                for (int i = 0; i < pLits.size(); i++) {
                    clause = Math.abs(pLits.get(i));
                    if (fullmodel[clause - 1] > 0) {
                        blockingClause.push(-clause);
                        secondPhaseClause.push(clause - nVar);
                        mus.remove(clause - nVar);
                    }
                }

                musList.add(mus);

                listener.onSolutionFound(mus);

                css.addBlockingClause(blockingClause);

            }

        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ContradictionException e) {

        }

        System.out.println("MUS = " + musList);

        return musList;
    }
}
