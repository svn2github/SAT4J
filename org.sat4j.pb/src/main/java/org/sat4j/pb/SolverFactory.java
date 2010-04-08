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
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.learning.ClauseOnlyLearning;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.learning.NoLearningButHeuristics;
import org.sat4j.minisat.orders.PhaseInLastLearnedClauseSelectionStrategy;
import org.sat4j.minisat.orders.RSATPhaseSelectionStrategy;
import org.sat4j.minisat.orders.UserFixedPhaseSelectionStrategy;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.restarts.ArminRestarts;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.minisat.uip.FirstUIP;
import org.sat4j.pb.constraints.AbstractPBDataStructureFactory;
import org.sat4j.pb.constraints.CompetMinHTmixedClauseCardConstrDataStructureFactory;
import org.sat4j.pb.constraints.CompetResolutionPBMixedHTClauseCardConstrDataStructure;
import org.sat4j.pb.constraints.CompetResolutionPBMixedWLClauseCardConstrDataStructure;
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
import org.sat4j.pb.core.PBSolverCautious;
import org.sat4j.pb.core.PBSolverClause;
import org.sat4j.pb.core.PBSolverResCP;
import org.sat4j.pb.core.PBSolverResolution;
import org.sat4j.pb.core.PBSolverWithImpliedClause;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.DimacsOutputSolver;

/**
 * User friendly access to pre-constructed solvers.
 * 
 * @author leberre
 * @since 2.0
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
	public static PBSolverCP newPBCPAllPB() {
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
	public static PBSolverCP newPBCPMixedConstraints() {
		return newPBCP(new PBMaxClauseCardConstrDataStructure());
	}

	/**
	 * @return MiniSAT with Counter-based pseudo boolean constraints and
	 *         constraint learning. Clauses and cardinalities with watched
	 *         literals are also handled (and learnt). A specific heuristics
	 *         taking into account the objective value is used.
	 */
	public static PBSolverCP newPBCPMixedConstraintsObjective() {
		return newPBCP(new PBMaxClauseCardConstrDataStructure(),
				new VarOrderHeapObjective());
	}

	public static PBSolverCP newCompetPBCPMixedConstraintsObjective() {
		return newPBCP(new PBMaxClauseCardConstrDataStructure(),
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
	public static PBSolverCP newPBCPMixedConstraintsObjectiveLearnJustClauses() {
		ClauseOnlyLearning<PBDataStructureFactory> learning = new ClauseOnlyLearning<PBDataStructureFactory>();
		PBSolverCP solver = new PBSolverCP(new FirstUIP(), learning,
				new PBMaxClauseCardConstrDataStructure(),
				new VarOrderHeapObjective());
		learning.setSolver(solver);
		return solver;
	}

	public static PBSolverCP newCompetPBCPMixedConstraintsObjectiveLearnJustClauses() {
		ClauseOnlyLearning<PBDataStructureFactory> learning = new ClauseOnlyLearning<PBDataStructureFactory>();
		PBSolverCP solver = new PBSolverCP(new FirstUIP(), learning,
				new PBMaxClauseCardConstrDataStructure(),
				new VarOrderHeapObjective());
		learning.setSolver(solver);
		return solver;
	}

	private static PBSolverCP newPBKiller(IPhaseSelectionStrategy phase) {
		ClauseOnlyLearning<PBDataStructureFactory> learning = new ClauseOnlyLearning<PBDataStructureFactory>();
		PBSolverCP solver = new PBSolverCP(new FirstUIP(), learning,
				new PBMaxClauseCardConstrDataStructure(),
				new VarOrderHeapObjective(phase));
		learning.setSolver(solver);
		return solver;
	}

	public static PBSolverCP newPBKillerRSAT() {
		return newPBKiller(new RSATPhaseSelectionStrategy());
	}

	public static PBSolverCP newPBKillerClassic() {
		return newPBKiller(new PhaseInLastLearnedClauseSelectionStrategy());
	}

	public static PBSolverCP newPBKillerFixed() {
		return newPBKiller(new UserFixedPhaseSelectionStrategy());
	}

	private static PBSolverCP newCompetPBKiller(IPhaseSelectionStrategy phase) {
		ClauseOnlyLearning<PBDataStructureFactory> learning = new ClauseOnlyLearning<PBDataStructureFactory>();
		PBSolverCP solver = new PBSolverCP(new FirstUIP(), learning,
				new PBMaxClauseCardConstrDataStructure(),
				new VarOrderHeapObjective(phase));
		learning.setSolver(solver);
		return solver;
	}

	public static PBSolverCP newCompetPBKillerRSAT() {
		return newCompetPBKiller(new RSATPhaseSelectionStrategy());
	}

	public static PBSolverCP newCompetPBKillerClassic() {
		return newCompetPBKiller(new PhaseInLastLearnedClauseSelectionStrategy());
	}

	public static PBSolverCP newCompetPBKillerFixed() {
		return newCompetPBKiller(new UserFixedPhaseSelectionStrategy());
	}

	/**
	 * @return MiniLearning with Counter-based pseudo boolean constraints and
	 *         constraint learning. Clauses and cardinalities with watched
	 *         literals are also handled (and learnt). A specific heuristics
	 *         taking into account the objective value is used. Conflict
	 *         analysis reduces to clauses to avoid computations
	 */
	public static PBSolverCP newMiniLearningOPBClauseCardConstrMaxSpecificOrderIncrementalReductionToClause() {
		// LimitedLearning learning = new LimitedLearning(10);
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		// LearningStrategy learning = new NoLearningButHeuristics();
		PBSolverCP solver = new PBSolverClause(new FirstUIP(), learning,
				new PBMaxClauseCardConstrDataStructure(),
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
	public static PBSolverCP newPBCPMixedConstraintsObjectiveNoLearning() {
		NoLearningButHeuristics<PBDataStructureFactory> learning = new NoLearningButHeuristics<PBDataStructureFactory>();
		// SearchParams params = new SearchParams(1.1,100);
		PBSolverCP solver = new PBSolverCP(new FirstUIP(), learning,
				new PBMaxClauseCardConstrDataStructure(),
				new VarOrderHeapObjective());
		learning.setSolver(solver);
		learning.setVarActivityListener(solver);
		return solver;
	}

	public static PBSolverResolution newPBResMixedConstraintsObjective() {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
				learning, new PBMaxClauseCardConstrDataStructure(),
				new VarOrderHeapObjective(), new MiniSATRestarts());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		return solver;
	}

	public static PBSolverResolution newCompetPBResWLMixedConstraintsObjective() {
		return newCompetPBResMixedConstraintsObjective(new CompetResolutionPBMixedWLClauseCardConstrDataStructure());
	}

	public static PBSolverResolution newCompetPBResHTMixedConstraintsObjective() {
		return newCompetPBResMixedConstraintsObjective(new CompetResolutionPBMixedHTClauseCardConstrDataStructure());
	}

	public static PBSolverResolution newCompetPBResMixedConstraintsObjective(
			PBDataStructureFactory dsf) {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
				learning, dsf, new VarOrderHeapObjective(
						new RSATPhaseSelectionStrategy()), new ArminRestarts());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		return solver;
	}

	public static PBSolverResolution newPBResHTMixedConstraintsObjective() {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		AbstractPBDataStructureFactory ds = new CompetResolutionPBMixedHTClauseCardConstrDataStructure();
		ds.setNormalizer(AbstractPBDataStructureFactory.NO_COMPETITION);
		PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
				learning, ds, new VarOrderHeapObjective(),
				new MiniSATRestarts());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		return solver;
	}

	public static PBSolverResolution newCompetPBResMinHTMixedConstraintsObjective() {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
				learning,
				new CompetMinHTmixedClauseCardConstrDataStructureFactory(),
				new VarOrderHeapObjective(), new MiniSATRestarts());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		return solver;
	}

	public static PBSolverResolution newPBResMinHTMixedConstraintsObjective() {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		AbstractPBDataStructureFactory ds = new CompetMinHTmixedClauseCardConstrDataStructureFactory();
		ds.setNormalizer(AbstractPBDataStructureFactory.NO_COMPETITION);
		PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
				learning, ds, new VarOrderHeapObjective(),
				new MiniSATRestarts());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		return solver;
	}

	public static PBSolverResolution newCompetPBResMixedConstraintsObjectiveExpSimp() {
		PBSolverResolution solver = newPBResMixedConstraintsObjective();
		solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
		return solver;
	}

	public static PBSolverResolution newCompetPBResWLMixedConstraintsObjectiveExpSimp() {
		PBSolverResolution solver = newCompetPBResWLMixedConstraintsObjective();
		solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
		return solver;
	}

	public static PBSolverResolution newPBResHTMixedConstraintsObjectiveExpSimp() {
		PBSolverResolution solver = newPBResHTMixedConstraintsObjective();
		solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
		return solver;
	}

	public static PBSolverResolution newCompetPBResMinHTMixedConstraintsObjectiveExpSimp() {
		PBSolverResolution solver = newCompetPBResMinHTMixedConstraintsObjective();
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
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverClause solver = new PBSolverClause(new FirstUIP(), learning,
				new PBMaxClauseCardConstrDataStructure(), new VarOrderHeap());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		return solver;
	}

	/**
	 * @return MiniSAT with Counter-based pseudo boolean constraints and
	 *         constraint learning. Clauses and cardinalities with watched
	 *         literals are also handled (and learnt). A reduction of
	 *         PB-constraints to clauses is made in order to simplify cutting
	 *         planes (if coefficients are larger than bound).
	 */
	public static PBSolverCautious newPBCPMixedConstraintsCautious(int bound) {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverCautious solver = new PBSolverCautious(new FirstUIP(),
				learning, new PBMaxClauseCardConstrDataStructure(),
				new VarOrderHeapObjective(), bound);
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		return solver;
	}

	public static PBSolverCautious newPBCPMixedConstraintsCautious() {
		return newPBCPMixedConstraintsCautious(PBSolverCautious.BOUND);
	}

	/**
	 * @return MiniSAT with Counter-based pseudo boolean constraints and
	 *         constraint learning. Clauses and cardinalities with watched
	 *         literals are also handled (and learnt). A reduction of
	 *         PB-constraints to clauses is made in order to simplify cutting
	 *         planes (if coefficients are larger than bound).
	 */
	public static PBSolverResCP newPBCPMixedConstraintsResCP(long bound) {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverResCP solver = new PBSolverResCP(new FirstUIP(), learning,
				new PBMaxClauseCardConstrDataStructure(),
				new VarOrderHeapObjective(), bound);
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
		return solver;
	}

	public static PBSolverResCP newPBCPMixedConstraintsResCP() {
		return newPBCPMixedConstraintsResCP(PBSolverResCP.MAXCONFLICTS);
	}

	/**
	 * @return MiniSAT with Counter-based pseudo boolean constraints and
	 *         constraint learning. Clauses and cardinalities with watched
	 *         literals are also handled (and learnt). a pre-processing is
	 *         applied which adds implied clauses from PB-constraints.
	 */
	public static PBSolverWithImpliedClause newPBCPMixedConstrainsImplied() {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverWithImpliedClause solver = new PBSolverWithImpliedClause(
				new FirstUIP(), learning,
				new PBMaxClauseCardConstrDataStructure(), new VarOrderHeap());
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
	public static PBSolverCP newMiniOPBClauseAtLeastConstrMax() {
		return newPBCP(new PBMaxClauseAtLeastConstrDataStructure());
	}

	/**
	 * @return MiniSAT with Counter-based pseudo boolean constraints and
	 *         clauses, watched cardinalities, and constraint learning.
	 */
	public static PBSolverCP newMiniOPBCounterBasedClauseCardConstrMax() {
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
	public static PBSolverCP newPBCPAllPBWL() {
		return newPBCP(new PBMinDataStructure());
	}

	/**
	 * @return MiniSAT with WL-based pseudo boolean constraints and clause
	 *         learning.
	 */
	public static PBSolverResolution newPBResAllPBWLPueblo() {
		return newPBRes(new PuebloPBMinDataStructure());
	}

	private static PBSolverResolution newPBRes(PBDataStructureFactory dsf) {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
				learning, dsf, new VarOrderHeap(), new MiniSATRestarts());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		return solver;
	}

	/**
	 * @return MiniSAT with WL-based pseudo boolean constraints and constraint
	 *         learning.
	 */
	public static PBSolverCP newPBCPAllPBWLPueblo() {
		return newPBCP(new PuebloPBMinDataStructure());
	}

	/**
	 * @return MiniSAT with WL-based pseudo boolean constraints and clauses,
	 *         cardinalities, and constraint learning.
	 */
	public static PBSolverCP newMiniOPBClauseCardMinPueblo() {
		return newPBCP(new PuebloPBMinClauseCardConstrDataStructure());
	}

	/**
	 * @return MiniSAT with WL-based pseudo boolean constraints and clauses,
	 *         cardinalities, and constraint learning.
	 */
	public static PBSolverCP newMiniOPBClauseCardMin() {
		return newPBCP(new PBMinClauseCardConstrDataStructure());
	}

	/**
	 * @return MiniSAT with WL-based pseudo boolean constraints and clauses,
	 *         counter-based cardinalities, and constraint learning.
	 */
	public static PBSolverCP newMiniOPBClauseAtLeastMinPueblo() {
		return newPBCP(new PuebloPBMinClauseAtLeastConstrDataStructure());
	}

	private static PBSolverCP newPBCP(PBDataStructureFactory dsf, IOrder order) {
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverCP solver = new PBSolverCP(new FirstUIP(), learning, dsf, order);
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		solver.setRestartStrategy(new ArminRestarts());
		solver.setLearnedConstraintsDeletionStrategy(solver.glucose);
		return solver;
	}

	private static PBSolverCP newPBCP(PBDataStructureFactory dsf) {
		return newPBCP(dsf, new VarOrderHeap());
	}

	/**
	 * Cutting Planes based solver. The inference during conflict analysis is
	 * based on cutting planes instead of resolution as in a SAT solver.
	 * 
	 * @return the best available cutting planes based solver of the library.
	 */
	public static IPBSolver newCuttingPlanes() {
		return newCompetPBCPMixedConstraintsObjective();
	}

	/**
	 * Resolution based solver (i.e. classic SAT solver able to handle generic
	 * constraints. No specific inference mechanism.
	 * 
	 * @return the best available resolution based solver of the library.
	 */
	public static IPBSolver newResolution() {
		return newResolutionGlucose();
	}

	/**
	 * Resolution based solver (i.e. classic SAT solver able to handle generic
	 * constraints. No specific inference mechanism). Uses glucose based memory
	 * management.
	 * 
	 * @return the best available resolution based solver of the library.
	 */
	public static IPBSolver newResolutionGlucose() {
		PBSolverResolution solver = newCompetPBResWLMixedConstraintsObjectiveExpSimp();
		solver.setLearnedConstraintsDeletionStrategy(solver.glucose);
		return solver;
	}

	/**
	 * Resolution based solver (i.e. classic SAT solver able to handle generic
	 * constraints. No specific inference mechanism). Uses glucose based memory
	 * management. Uses a simple restart strategy (original Minisat's one).
	 * 
	 * @return the best available resolution based solver of the library.
	 */
	public static IPBSolver newResolutionSimpleRestarts() {
		PBSolverResolution solver = newCompetPBResWLMixedConstraintsObjectiveExpSimp();
		solver.setLearnedConstraintsDeletionStrategy(solver.glucose);
		solver.setRestartStrategy(new MiniSATRestarts());
		return solver;
	}

	/**
	 * Resolution based solver (i.e. classic SAT solver able to handle generic
	 * constraints. No specific inference mechanism).
	 * 
	 * Keeps the constraints as long as there is enough memory available.
	 * 
	 * @return the best available resolution based solver of the library.
	 */
	public static IPBSolver newResolutionMaxMemory() {
		return newCompetPBResWLMixedConstraintsObjectiveExpSimp();
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
		return newResolutionGlucose();
	}

	/**
	 * Default solver of the SolverFactory for instances not normalized. This
	 * solver is meant to be used on challenging SAT benchmarks.
	 * 
	 * @return the best "general purpose" SAT solver available in the factory.
	 * @see #defaultSolver() the same method, polymorphic, to be called from an
	 *      instance of ASolverFactory.
	 */
	public static IPBSolver newDefaultNonNormalized() {
		return newPBResHTMixedConstraintsObjectiveExpSimp();
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
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverResolution solver = new PBSolverResolution(new FirstUIP(),
				learning,
				new CompetResolutionPBMixedHTClauseCardConstrDataStructure(),
				new VarOrderHeapObjective(new RSATPhaseSelectionStrategy()),
				new ArminRestarts());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		solver.setTimeoutOnConflicts(300);
		solver.setVerbose(false);
		return new OptToPBSATAdapter(new PseudoOptDecorator(solver));
	}

}