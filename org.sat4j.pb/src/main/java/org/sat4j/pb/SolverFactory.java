/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
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
* Based on the pseudo boolean algorithms described in:
* A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
* Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
* Volume 24, Issue 3, March 2005 Page(s): 305 - 317
* 
* and 
* Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
* Framework. Ph.D. Dissertation, University of Oregon.
*******************************************************************************/
package org.sat4j.pb;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.learning.ClauseOnlyLearning;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.learning.NoLearningButHeuristics;
import org.sat4j.minisat.orders.PhaseInLastLearnedClauseSelectionStrategy;
import org.sat4j.minisat.orders.RSATPhaseSelectionStrategy;
import org.sat4j.minisat.orders.UserFixedPhaseSelectionStrategy;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.minisat.uip.FirstUIP;
import org.sat4j.pb.constraints.CompetPBMaxClauseCardConstrDataStructure;
import org.sat4j.pb.constraints.PBMaxCBClauseCardConstrDataStructure;
import org.sat4j.pb.constraints.PBMaxClauseAtLeastConstrDataStructure;
import org.sat4j.pb.constraints.PBMaxClauseCardConstrDataStructure;
import org.sat4j.pb.constraints.PBMaxDataStructure;
import org.sat4j.pb.constraints.PBMinClauseCardConstrDataStructure;
import org.sat4j.pb.constraints.PBMinDataStructure;
import org.sat4j.pb.constraints.PuebloPBMinClauseAtLeastConstrDataStructure;
import org.sat4j.pb.constraints.PuebloPBMinClauseCardConstrDataStructure;
import org.sat4j.pb.constraints.PuebloPBMinDataStructure;
import org.sat4j.pb.core.PBDataStructureFactory;
import org.sat4j.pb.core.PBSolverCP;
import org.sat4j.pb.core.PBSolverClause;
import org.sat4j.pb.core.PBSolverResolution;
import org.sat4j.pb.core.PBSolverWithImpliedClause;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.DimacsOutputSolver;

/**
 * User friendly access to pre-constructed solvers.
 * 
 * @author leberre
 */
public class SolverFactory extends ASolverFactory<IPBSolver> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // thread safe implementation of the singleton design pattern
    private static SolverFactory instance;

    /**
     * Private contructor. Use singleton method instance() instead.
     * 
     * @see #instance()
     */
    private SolverFactory() {

    }

    private static synchronized void createInstance() {
        if (instance == null) {
            instance = new SolverFactory();
        }
    }

    /**
     * Access to the single instance of the factory.
     * 
     * @return the singleton of that class.
     */
    public static SolverFactory instance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and clause
     *         learning.
     */
    public static PBSolverResolution newPBResAllPB() {
        return newPBRes(new PBMaxDataStructure());
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning.
     */
    public static PBSolverCP<ILits> newPBCPAllPB() {
        return newPBCP(new PBMaxDataStructure());
    }


    /**
     * @return Solver used to display in a string the pb-instance in OPB format.
     */
    public static IPBSolver newOPBStringSolver() {
        return new OPBStringSolver();
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt).
     */
    public static PBSolverCP<ILits> newPBCPMixedConstraints() {
        return newPBCP(new PBMaxClauseCardConstrDataStructure());
    }

    public static PBSolverCP<ILits> newCompetPBCPMixedConstraints() {
        return newPBCP(new CompetPBMaxClauseCardConstrDataStructure());
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). A specific heuristics
     *         taking into account the objective value is used.
     */
    public static PBSolverCP<ILits> newPBCPMixedConstraintsObjective() {
        return newPBCP(new CompetPBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
    }

    public static PBSolverCP<ILits> newCompetPBCPMixedConstraintsObjective() {
        return newPBCP(new CompetPBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
    }

    /**
     * @return MiniLearning with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). A specific heuristics
     *         taking into account the objective value is used. Conflict
     *         analysis with full cutting plane inference. Only clauses are
     *         recorded.
     */
    public static PBSolverCP<ILits> newPBCPMixedConstraintsObjectiveLearnJustClauses() {
        ClauseOnlyLearning<ILits, PBDataStructureFactory<ILits>> learning = new ClauseOnlyLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverCP<ILits> solver = new PBSolverCP<ILits>(new FirstUIP(),
                learning, new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
        learning.setSolver(solver);
        return solver;
    }

    public static PBSolverCP<ILits> newCompetPBCPMixedConstraintsObjectiveLearnJustClauses() {
        ClauseOnlyLearning<ILits, PBDataStructureFactory<ILits>> learning = new ClauseOnlyLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverCP<ILits> solver = new PBSolverCP<ILits>(new FirstUIP(),
                learning, new CompetPBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
        learning.setSolver(solver);
        return solver;
    }

    private static PBSolverCP<ILits> newPBKiller(IPhaseSelectionStrategy phase) {
        ClauseOnlyLearning<ILits, PBDataStructureFactory<ILits>> learning = new ClauseOnlyLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverCP<ILits> solver = new PBSolverCP<ILits>(new FirstUIP(),
                learning, new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective(phase));
        learning.setSolver(solver);
        return solver;
    }

    public static PBSolverCP<ILits> newPBKillerRSAT() {
        return newPBKiller(new RSATPhaseSelectionStrategy());
    }

    public static PBSolverCP<ILits> newPBKillerClassic() {
        return newPBKiller(new PhaseInLastLearnedClauseSelectionStrategy());
    }

    public static PBSolverCP<ILits> newPBKillerFixed() {
        return newPBKiller(new UserFixedPhaseSelectionStrategy());
    }

    private static PBSolverCP<ILits> newCompetPBKiller(IPhaseSelectionStrategy phase) {
        ClauseOnlyLearning<ILits, PBDataStructureFactory<ILits>> learning = new ClauseOnlyLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverCP<ILits> solver = new PBSolverCP<ILits>(new FirstUIP(),
                learning, new CompetPBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective(phase));
        learning.setSolver(solver);
        return solver;
    }

    public static PBSolverCP<ILits> newCompetPBKillerRSAT() {
        return newCompetPBKiller(new RSATPhaseSelectionStrategy());
    }

    public static PBSolverCP<ILits> newCompetPBKillerClassic() {
        return newCompetPBKiller(new PhaseInLastLearnedClauseSelectionStrategy());
    }

    public static PBSolverCP<ILits> newCompetPBKillerFixed() {
        return newCompetPBKiller(new UserFixedPhaseSelectionStrategy());
    }

    /**
     * @return MiniLearning with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). A specific heuristics
     *         taking into account the objective value is used. Conflict
     *         analysis reduces to clauses to avoid computations
     */
    public static PBSolverCP<ILits> newMiniLearningOPBClauseCardConstrMaxSpecificOrderIncrementalReductionToClause() {
        // LimitedLearning learning = new LimitedLearning(10);
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        // LearningStrategy learning = new NoLearningButHeuristics();
        PBSolverCP<ILits> solver = new PBSolverClause(new FirstUIP(), learning,
                new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    public static PBSolverCP<ILits> newCompetMiniLearningOPBClauseCardConstrMaxSpecificOrderIncrementalReductionToClause() {
        // LimitedLearning learning = new LimitedLearning(10);
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        // LearningStrategy learning = new NoLearningButHeuristics();
        PBSolverCP<ILits> solver = new PBSolverClause(new FirstUIP(), learning,
                new CompetPBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniLearning with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). A specific heuristics
     *         taking into account the objective value is used. The PB
     *         constraints are not learnt (watched), just used for backjumping.
     */
    public static PBSolverCP<ILits> newPBCPMixedConstraintsObjectiveNoLearning() {
        NoLearningButHeuristics<ILits, PBDataStructureFactory<ILits>> learning = new NoLearningButHeuristics<ILits, PBDataStructureFactory<ILits>>();
        // SearchParams params = new SearchParams(1.1,100);
        PBSolverCP<ILits> solver = new PBSolverCP<ILits>(new FirstUIP(),
                learning, new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
        learning.setSolver(solver);
        learning.setVarActivityListener(solver);
        return solver;
    }

    public static PBSolverCP<ILits> newCompetPBCPMixedConstraintsObjectiveNoLearning() {
        NoLearningButHeuristics<ILits, PBDataStructureFactory<ILits>> learning = new NoLearningButHeuristics<ILits, PBDataStructureFactory<ILits>>();
        // SearchParams params = new SearchParams(1.1,100);
        PBSolverCP<ILits> solver = new PBSolverCP<ILits>(new FirstUIP(),
                learning, new CompetPBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
        learning.setSolver(solver);
        learning.setVarActivityListener(solver);
        return solver;
    }

    public static PBSolverResolution newPBResMixedConstraintsObjective() {
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
                learning, new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective(), new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    public static PBSolverResolution newCompetPBResMixedConstraintsObjective() {
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
                learning, new CompetPBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective(), new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    public static PBSolverResolution newCompetPBResMixedConstraintsObjectiveExpSimp() {
    	PBSolverResolution solver = newCompetPBResMixedConstraintsObjective();
    	solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
    	return solver;
    }
    
    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). A reduction of
     *         PB-constraints to clauses is made in order to simplify cutting
     *         planes.
     */
    public static PBSolverClause newPBCPMixedConstraintsReduceToClause() {
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverClause solver = new PBSolverClause(new FirstUIP(), learning,
                new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeap<ILits>());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    public static PBSolverClause newCompetPBCPMixedConstraintsReduceToClause() {
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverClause solver = new PBSolverClause(new FirstUIP(), learning,
                new CompetPBMaxClauseCardConstrDataStructure(),
                new VarOrderHeap<ILits>());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). a pre-processing is
     *         applied which adds implied clauses from PB-constraints.
     */
    public static PBSolverWithImpliedClause newPBCPMixedConstrainsImplied() {
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverWithImpliedClause solver = new PBSolverWithImpliedClause(
                new FirstUIP(), learning,
                new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeap<ILits>());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    public static PBSolverWithImpliedClause newCompetPBCPMixedConstrainsImplied() {
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverWithImpliedClause solver = new PBSolverWithImpliedClause(
                new FirstUIP(), learning,
                new CompetPBMaxClauseCardConstrDataStructure(),
                new VarOrderHeap<ILits>());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints,
     *         counter-based cardinalities, watched clauses and constraint
     *         learning. methods isAssertive() and getBacktrackLevel() are
     *         totally incremental. Conflicts for PB-constraints use a Map
     *         structure
     */
    public static PBSolverCP<ILits> newMiniOPBClauseAtLeastConstrMax() {
        return newPBCP(new PBMaxClauseAtLeastConstrDataStructure());
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         clauses, watched cardinalities, and constraint learning.
     */
    public static PBSolverCP<ILits> newMiniOPBCounterBasedClauseCardConstrMax() {
        return newPBCP(new PBMaxCBClauseCardConstrDataStructure());
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clause
     *         learning.
     */
    public static PBSolverResolution newPBResAllPBWL() {
        return newPBRes(new PBMinDataStructure());
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and constraint
     *         learning.
     */
    public static PBSolverCP<ILits> newPBCPAllPBWL() {
        return newPBCP(new PBMinDataStructure());
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clause
     *         learning.
     */
    public static PBSolverResolution newPBResAllPBWLPueblo() {
        return newPBRes(new PuebloPBMinDataStructure());
    }

    private static PBSolverResolution newPBRes(PBDataStructureFactory<ILits> dsf) {
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
                learning, dsf, new VarOrderHeap<ILits>(), new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and constraint
     *         learning.
     */
    public static PBSolverCP<ILits> newPBCPAllPBWLPueblo() {
        return newPBCP(new PuebloPBMinDataStructure());
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clauses,
     *         cardinalities, and constraint learning.
     */
    public static PBSolverCP<ILits> newMiniOPBClauseCardMinPueblo() {
        return newPBCP(new PuebloPBMinClauseCardConstrDataStructure());
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clauses,
     *         cardinalities, and constraint learning.
     */
    public static PBSolverCP<ILits> newMiniOPBClauseCardMin() {
        return newPBCP(new PBMinClauseCardConstrDataStructure());
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clauses,
     *         counter-based cardinalities, and constraint learning.
     */
    public static PBSolverCP<ILits> newMiniOPBClauseAtLeastMinPueblo() {
        return newPBCP(new PuebloPBMinClauseAtLeastConstrDataStructure());
    }

    private static PBSolverCP<ILits> newPBCP(PBDataStructureFactory<ILits> dsf,
            IOrder<ILits> order) {
        MiniSATLearning<ILits, PBDataStructureFactory<ILits>> learning = new MiniSATLearning<ILits, PBDataStructureFactory<ILits>>();
        PBSolverCP<ILits> solver = new PBSolverCP<ILits>(new FirstUIP(),
                learning, dsf, order);
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    private static PBSolverCP<ILits> newPBCP(PBDataStructureFactory<ILits> dsf) {
        return newPBCP(dsf, new VarOrderHeap<ILits>());
    }

    /**
     * Default solver of the SolverFactory. This solver is meant to be used on
     * challenging SAT benchmarks.
     * 
     * @return the best "general purpose" SAT solver available in the factory.
     * @see #defaultSolver() the same method, polymorphic, to be called from an
     *      instance of ASolverFactory.
     */
    public static IPBSolver newDefault() {
        return newCompetPBCPMixedConstraintsObjective();
        //return newCompetPBKillerClassic();
    }

    @Override
    public IPBSolver defaultSolver() {
        return newDefault();
    }

    /**
     * Small footprint SAT solver.
     * 
     * @return a SAT solver suitable for solving small/easy SAT benchmarks.
     * @see #lightSolver() the same method, polymorphic, to be called from an
     *      instance of ASolverFactory.
     */
    public static IPBSolver newLight() {
        return newCompetPBResMixedConstraintsObjectiveExpSimp();
    }

    @Override
    public IPBSolver lightSolver() {
        return newLight();
    }

    public static ISolver newDimacsOutput() {
        return new DimacsOutputSolver();
    }
    
    public static IPBSolver newEclipseP2() {
        PBSolverResolution solver = newCompetPBResMixedConstraintsObjective();
        solver.setTimeoutOnConflicts(300);
        return new OptToPBSATAdapter(new PseudoOptDecorator(solver));
    }

}