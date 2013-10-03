/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.sat;

import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.SolverStats;
import org.sat4j.minisat.orders.RSATPhaseSelectionStrategy;
import org.sat4j.minisat.restarts.NoRestarts;
import org.sat4j.specs.Constr;
import org.sat4j.specs.ILogAble;

/**
 * 
 * Strategy used by the solver when launched with the remote control.
 * 
 * @author sroussel
 * 
 */
public class RemoteControlStrategy implements RestartStrategy,
        IPhaseSelectionStrategy {

    private static final int SLEEP_TIME = 1000;

    private static final long serialVersionUID = 1L;

    private RestartStrategy restart;
    private IPhaseSelectionStrategy phaseSelectionStrategy;

    private ILogAble logger;

    private boolean isInterrupted;

    private boolean hasClickedOnRestart;
    private boolean hasClickedOnClean;

    private int conflictNumber;
    private int nbClausesAtWhichWeShouldClean;

    private boolean useTelecomStrategyAsLearnedConstraintsDeletionStrategy;

    private ICDCL<?> solver;

    public RemoteControlStrategy(ILogAble log) {
        this.hasClickedOnClean = false;
        this.hasClickedOnRestart = false;
        this.restart = new NoRestarts();
        this.phaseSelectionStrategy = new RSATPhaseSelectionStrategy();
        this.logger = log;
        this.isInterrupted = false;
        this.useTelecomStrategyAsLearnedConstraintsDeletionStrategy = false;
    }

    public RemoteControlStrategy() {
        this(null);
    }

    public boolean isHasClickedOnRestart() {
        return this.hasClickedOnRestart;
    }

    public void setHasClickedOnRestart(boolean hasClickedOnRestart) {
        this.hasClickedOnRestart = hasClickedOnRestart;
    }

    public boolean isHasClickedOnClean() {
        return this.hasClickedOnClean;
    }

    public void setHasClickedOnClean(boolean hasClickedOnClean) {
        this.hasClickedOnClean = hasClickedOnClean;
        clickedOnClean();
    }

    public boolean isUseTelecomStrategyAsLearnedConstraintsDeletionStrategy() {
        return this.useTelecomStrategyAsLearnedConstraintsDeletionStrategy;
    }

    public void setUseTelecomStrategyAsLearnedConstraintsDeletionStrategy(
            boolean useTelecomStrategyAsLearnedConstraintsDeletionStrategy) {
        this.useTelecomStrategyAsLearnedConstraintsDeletionStrategy = useTelecomStrategyAsLearnedConstraintsDeletionStrategy;
    }

    public void clickedOnClean() {
        if (this.hasClickedOnClean) {
            this.solver.setNeedToReduceDB(true);
            this.hasClickedOnClean = false;
        }
    }

    public RestartStrategy getRestartStrategy() {
        return this.restart;
    }

    public IPhaseSelectionStrategy getPhaseSelectionStrategy() {
        return this.phaseSelectionStrategy;
    }

    public void setPhaseSelectionStrategy(
            IPhaseSelectionStrategy phaseSelectionStrategy) {
        this.phaseSelectionStrategy = phaseSelectionStrategy;
    }

    public void setRestartStrategy(RestartStrategy restart) {
        this.restart = restart;
    }

    public int getNbClausesAtWhichWeShouldClean() {
        return this.nbClausesAtWhichWeShouldClean;
    }

    public void setNbClausesAtWhichWeShouldClean(
            int nbClausesAtWhichWeShouldClean) {
        this.nbClausesAtWhichWeShouldClean = nbClausesAtWhichWeShouldClean;
    }

    public ILogAble getLogger() {
        return this.logger;
    }

    public void setLogger(ILogAble logger) {
        this.logger = logger;
    }

    public void init(SearchParams params, SolverStats stats) {
        this.restart.init(params, stats);
    }

    @Deprecated
    public long nextRestartNumberOfConflict() {
        return this.restart.nextRestartNumberOfConflict();
    }

    public boolean shouldRestart() {
        if (this.hasClickedOnRestart) {
            this.hasClickedOnRestart = false;
            this.logger.log("Told the solver to restart");
            return true;
        }
        return this.restart.shouldRestart();
    }

    public void onRestart() {
        this.restart.onRestart();
    }

    public void onBackjumpToRootLevel() {
        this.restart.onBackjumpToRootLevel();
    }

    public SearchParams getSearchParams() {
        return this.solver.getSearchParams();
    }

    public SolverStats getSolverStats() {
        return this.solver.getStats();
    }

    public ICDCL<?> getSolver() {
        return this.solver;
    }

    public void setSolver(ICDCL<?> solver) {
        this.solver = solver;
    }

    public void reset() {
        this.restart.newConflict();
    }

    public void newConflict() {
        this.restart.newConflict();
        this.conflictNumber++;
        if (this.useTelecomStrategyAsLearnedConstraintsDeletionStrategy
                && this.conflictNumber > this.nbClausesAtWhichWeShouldClean) {
            this.conflictNumber = 0;
            this.solver.setNeedToReduceDB(true);
        }
    }

    public void updateVar(int p) {
        this.phaseSelectionStrategy.updateVar(p);
    }

    public void init(int nlength) {
        this.phaseSelectionStrategy.init(nlength);
    }

    public void init(int var, int p) {
        this.phaseSelectionStrategy.init(var, p);
    }

    public void assignLiteral(int p) {
        while (this.isInterrupted) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.log(e.getMessage());
            }
        }
        this.phaseSelectionStrategy.assignLiteral(p);
    }

    public int select(int var) {
        return this.phaseSelectionStrategy.select(var);
    }

    public void updateVarAtDecisionLevel(int q) {
        this.phaseSelectionStrategy.updateVarAtDecisionLevel(q);
    }

    @Override
    public String toString() {
        return "RemoteControlStrategy [restartStrategy = " + this.restart
                + ", learnedClausesDeletionStrategy = clean after "
                + this.nbClausesAtWhichWeShouldClean
                + " conflicts, phaseSelectionStrategy = "
                + this.phaseSelectionStrategy + "]";
    }

    public void setInterrupted(boolean b) {
        this.isInterrupted = b;
        if (this.isInterrupted) {
            this.logger.log("Solver paused");
        } else {
            this.logger.log("Resume solving");
        }
    }

    public void newLearnedClause(Constr learned, int trailLevel) {
        this.restart.newLearnedClause(learned, trailLevel);
    }

}
