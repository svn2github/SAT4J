/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2006 Daniel Le
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
*******************************************************************************/
package org.sat4j.minisat;

import java.io.Serializable;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.constraints.CardinalityDataStructure;
import org.sat4j.minisat.constraints.ClausalDataStructureCB;
import org.sat4j.minisat.constraints.ClausalDataStructureCBWL;
import org.sat4j.minisat.constraints.MixedDataStructureDaniel;
import org.sat4j.minisat.constraints.MixedDataStructureWithBinary;
import org.sat4j.minisat.constraints.MixedDataStructureWithBinaryAndTernary;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.ILits2;
import org.sat4j.minisat.core.ILits23;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.learning.ActiveLearning;
import org.sat4j.minisat.learning.FixedLengthLearning;
import org.sat4j.minisat.learning.LimitedLearning;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.learning.NoLearningButHeuristics;
import org.sat4j.minisat.learning.PercentLengthLearning;
import org.sat4j.minisat.orders.JWOrder;
import org.sat4j.minisat.orders.MyOrder;
import org.sat4j.minisat.orders.PureOrder;
import org.sat4j.minisat.orders.RSATPhaseSelectionStrategy;
import org.sat4j.minisat.orders.VarOrder;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.restarts.ArminRestarts;
import org.sat4j.minisat.restarts.LubyRestarts;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.minisat.uip.DecisionUIP;
import org.sat4j.minisat.uip.FirstUIP;
import org.sat4j.opt.MinOneDecorator;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.DimacsOutputSolver;
import org.sat4j.tools.OptToSatAdapter;

/**
 * User friendly access to pre-constructed solvers.
 * 
 * @author leberre
 */
public class SolverFactory extends ASolverFactory<ISolver> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // thread safe implementation of the singleton design pattern
    private static SolverFactory instance;

    /**
     * Private constructor. Use singleton method instance() instead.
     * 
     * @see #instance()
     */
    private SolverFactory() {
        super();
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
     * @return a "default" "minilearning" solver learning clauses of size
     *         smaller than 10 % of the total number of variables
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearning() {
        return newMiniLearning(10);
    }

    /**
     * @return a "default" "minilearning" solver learning clauses of size
     *         smaller than 10 % of the total number of variables with a heap
     *         based var order.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningHeap() {
        return newMiniLearningHeap(new MixedDataStructureDaniel());
    }

    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningHeapEZSimp() {
        Solver<ILits,DataStructureFactory<ILits>> solver = newMiniLearningHeap();
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningHeapExpSimp() {
        Solver<ILits,DataStructureFactory<ILits>> solver = newMiniLearningHeap();
        solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
        return solver;
    }

    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningHeapRsatExpSimp() {
        Solver<ILits,DataStructureFactory<ILits>> solver = newMiniLearningHeapExpSimp();
        solver.setOrder(new VarOrderHeap<ILits>(new RSATPhaseSelectionStrategy()));
        return solver;
    }

    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningHeapRsatExpSimpBiere() {
        Solver<ILits,DataStructureFactory<ILits>> solver = newMiniLearningHeapRsatExpSimp();
        solver.setRestartStrategy(new ArminRestarts());
        solver.setSearchParams(new SearchParams(1.1, 100));
        return solver;
    }

    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningHeapRsatExpSimpLuby() {
        Solver<ILits,DataStructureFactory<ILits>> solver = newMiniLearningHeapRsatExpSimp();
        solver.setRestartStrategy(new LubyRestarts());
        return solver;
    }

    /**
     * @param n
     *                the maximal size of the clauses to learn as a percentage
     *                of the initial number of variables
     * @return a "minilearning" solver learning clauses of size smaller than n
     *         of the total number of variables
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearning(int n) {
        return newMiniLearning(new MixedDataStructureDaniel(), n);
    }

    /**
     * @param dsf
     *                a specific data structure factory
     * @return a default "minilearning" solver using a specific data structure
     *         factory, learning clauses of length smaller or equals to 10 % of
     *         the number of variables.
     */
    public static <L extends ILits> Solver<L,DataStructureFactory<L>> newMiniLearning(
            DataStructureFactory<L> dsf) {
        return newMiniLearning(dsf, 10);
    }

    /**
     * @param dsf
     *                a specific data structure factory
     * @return a default "minilearning" solver using a specific data structure
     *         factory, learning clauses of length smaller or equals to 10 % of
     *         the number of variables and a heap based VSIDS heuristics
     */
    public static <L extends ILits> Solver<L,DataStructureFactory<L>> newMiniLearningHeap(
            DataStructureFactory<L> dsf) {
        return newMiniLearning(dsf, new VarOrderHeap<L>());
    }

    /**
     * @return a default minilearning solver using a specific data structure
     *         described in Lawrence Ryan thesis to handle binary clauses.
     * @see #newMiniLearning
     */
    public static Solver<ILits2,DataStructureFactory<ILits2>> newMiniLearning2() {
        return newMiniLearning(new MixedDataStructureWithBinary());
    }

    public static Solver<ILits2,DataStructureFactory<ILits2>> newMiniLearning2Heap() {
        return newMiniLearningHeap(new MixedDataStructureWithBinary());
    }

    /**
     * @return a default minilearning solver using a specific data structures
     *         described in Lawrence Ryan thesis to handle binary and ternary
     *         clauses.
     * @see #newMiniLearning
     */
    public static Solver<ILits23,DataStructureFactory<ILits23>> newMiniLearning23() {
        return newMiniLearning(new MixedDataStructureWithBinaryAndTernary());
    }

    /**
     * @return a default minilearning SAT solver using counter-based clause
     *         representation (i.e. all the literals of a clause are watched)
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningCB() {
        return newMiniLearning(new ClausalDataStructureCB());
    }

    /**
     * @return a default minilearning SAT solver using counter-based clause
     *         representation (i.e. all the literals of a clause are watched)
     *         for the ORIGINAL clauses and watched-literals clause
     *         representation for learnt clauses.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningCBWL() {
        return newMiniLearning(new ClausalDataStructureCBWL());
    }

    public static Solver<ILits2,DataStructureFactory<ILits2>> newMiniLearning2NewOrder() {
        return newMiniLearning(new MixedDataStructureWithBinary(),
                new MyOrder());
    }

    /**
     * @return a default minilearning SAT solver choosing periodically to branch
     *         on "pure watched" literals if any. (a pure watched literal l is a
     *         literal that is watched on at least one clause such that its
     *         negation is not watched at all. It is not necessarily a watched
     *         literal.)
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningPure() {
        return newMiniLearning(new MixedDataStructureDaniel(), new PureOrder());
    }

    /**
     * @return a default minilearning SAT solver choosing periodically to branch
     *         on literal "pure in the original set of clauses" if any.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningCBWLPure() {
        return newMiniLearning(new ClausalDataStructureCBWL(), new PureOrder());
    }

    /**
     * @param dsf
     *                the data structure factory used to represent literals and
     *                clauses
     * @param n
     *                the maximum size of learnt clauses as percentage of the
     *                original number of variables.
     * @return a SAT solver with learning limited to clauses of length smaller
     *         or equal to n, the dsf data structure, the FirstUIP clause
     *         generator and a sort of VSIDS heuristics.
     */
    public static <L extends ILits> Solver<L,DataStructureFactory<L>> newMiniLearning(
            DataStructureFactory<L> dsf, int n) {
        LimitedLearning<L,DataStructureFactory<L>> learning = new PercentLengthLearning<L,DataStructureFactory<L>>(n);
        Solver<L,DataStructureFactory<L>> solver = new Solver<L,DataStructureFactory<L>>(new FirstUIP(), learning, dsf,
                new VarOrder<L>(), new MiniSATRestarts());
        learning.setSolver(solver);
        return solver;
    }

    /**
     * @param dsf
     *                the data structure factory used to represent literals and
     *                clauses
     * @param order
     *                the heuristics
     * @return a SAT solver with learning limited to clauses of length smaller
     *         or equal to 10 percent of the total number of variables, the dsf
     *         data structure, the FirstUIP clause generator and order as
     *         heuristics.
     */
    public static <L extends ILits> Solver<L,DataStructureFactory<L>> newMiniLearning(
            DataStructureFactory<L> dsf, IOrder<L> order) {
        LimitedLearning<L,DataStructureFactory<L>> learning = new PercentLengthLearning<L,DataStructureFactory<L>>(10);
        Solver<L,DataStructureFactory<L>> solver = new Solver<L,DataStructureFactory<L>>(new FirstUIP(), learning, dsf, order,
                new MiniSATRestarts());
        learning.setSolver(solver);
        return solver;
    }

    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningEZSimp() {
        return newMiniLearningEZSimp(new MixedDataStructureDaniel());
    }

    // public static ISolver newMiniLearning2EZSimp() {
    // return newMiniLearningEZSimp(new MixedDataStructureWithBinary());
    // }

    public static <L extends ILits> Solver<L,DataStructureFactory<L>> newMiniLearningEZSimp(
            DataStructureFactory<L> dsf) {
        LimitedLearning<L,DataStructureFactory<L>> learning = new PercentLengthLearning<L,DataStructureFactory<L>>(10);
        Solver<L,DataStructureFactory<L>> solver = new Solver<L,DataStructureFactory<L>>(new FirstUIP(), learning, dsf,
                new VarOrder<L>(), new MiniSATRestarts());
        learning.setSolver(solver);
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    /**
     * @return a default MiniLearning without restarts.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningHeapEZSimpNoRestarts() {
        LimitedLearning<ILits,DataStructureFactory<ILits>> learning = new PercentLengthLearning<ILits,DataStructureFactory<ILits>>(10);
        Solver<ILits,DataStructureFactory<ILits>> solver = new Solver<ILits,DataStructureFactory<ILits>>(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new SearchParams(
                        Integer.MAX_VALUE), new VarOrderHeap<ILits>(),
                new MiniSATRestarts());
        learning.setSolver(solver);
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    /**
     * @return a default MiniLearning with restarts beginning at 1000 conflicts.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniLearningHeapEZSimpLongRestarts() {
        LimitedLearning<ILits,DataStructureFactory<ILits>> learning = new PercentLengthLearning<ILits,DataStructureFactory<ILits>>(10);
        Solver<ILits,DataStructureFactory<ILits>> solver = new Solver<ILits,DataStructureFactory<ILits>>(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new SearchParams(1000),
                new VarOrderHeap<ILits>(), new MiniSATRestarts());
        learning.setSolver(solver);
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    /**
     * @return a SAT solver using First UIP clause generator, watched literals,
     *         VSIDS like heuristics learning only clauses having a great number
     *         of active variables, i.e. variables with an activity strictly
     *         greater than one.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newActiveLearning() {
        ActiveLearning<ILits,DataStructureFactory<ILits>> learning = new ActiveLearning<ILits,DataStructureFactory<ILits>>();
        Solver<ILits,DataStructureFactory<ILits>> s = new Solver<ILits,DataStructureFactory<ILits>>(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new VarOrder<ILits>(),
                new MiniSATRestarts());
        learning.setOrder(s.getOrder());
        learning.setSolver(s);
        return s;
    }

    /**
     * @return a SAT solver very close to the original MiniSAT sat solver.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniSAT() {
        return newMiniSAT(new MixedDataStructureDaniel());
    }

    /**
     * @return MiniSAT without restarts.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniSATNoRestarts() {
        MiniSATLearning<ILits,DataStructureFactory<ILits>> learning = new MiniSATLearning<ILits,DataStructureFactory<ILits>>();
        Solver<ILits,DataStructureFactory<ILits>> solver = new Solver<ILits,DataStructureFactory<ILits>>(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new SearchParams(
                        Integer.MAX_VALUE), new VarOrder<ILits>(),
                new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;

    }

    /**
     * @return MiniSAT with a special data structure from Lawrence Ryan thesis
     *         for managing binary clauses.
     */
    public static Solver<ILits2,DataStructureFactory<ILits2>> newMiniSAT2() {
        return newMiniSAT(new MixedDataStructureWithBinary());
    }

    /**
     * @return MiniSAT with a special data structure from Lawrence Ryan thesis
     *         for managing binary and ternary clauses.
     */
    public static Solver<ILits23,DataStructureFactory<ILits23>> newMiniSAT23() {
        return newMiniSAT(new MixedDataStructureWithBinaryAndTernary());
    }

    /**
     * @param dsf
     *                the data structure used for representing clauses and lits
     * @return MiniSAT the data structure dsf.
     */
    public static <L extends ILits> Solver<L,DataStructureFactory<L>> newMiniSAT(
            DataStructureFactory<L> dsf) {
        MiniSATLearning<L,DataStructureFactory<L>> learning = new MiniSATLearning<L,DataStructureFactory<L>>();
        Solver<L,DataStructureFactory<L>> solver = new Solver<L,DataStructureFactory<L>>(new FirstUIP(), learning, dsf,
                new VarOrder<L>(), new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return a SAT solver very close to the original MiniSAT sat solver.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniSATHeap() {
        return newMiniSATHeap(new MixedDataStructureDaniel());
    }

    /**
     * @return a SAT solver very close to the original MiniSAT sat solver
     *         including easy reason simplification.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniSATHeapEZSimp() {
        Solver<ILits,DataStructureFactory<ILits>> solver = newMiniSATHeap();
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    public static Solver<ILits,DataStructureFactory<ILits>> newMiniSATHeapExpSimp() {
        Solver<ILits,DataStructureFactory<ILits>> solver = newMiniSATHeap();
        solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
        return solver;
    }

    /**
     * @return MiniSAT with a special data structure from Lawrence Ryan thesis
     *         for managing binary clauses.
     */
    public static Solver<ILits2,DataStructureFactory<ILits2>> newMiniSAT2Heap() {
        return newMiniSATHeap(new MixedDataStructureWithBinary());
    }

    /**
     * @return MiniSAT with a special data structure from Lawrence Ryan thesis
     *         for managing binary and ternary clauses.
     */
    public static Solver<ILits23,DataStructureFactory<ILits23>> newMiniSAT23Heap() {
        return newMiniSATHeap(new MixedDataStructureWithBinaryAndTernary());
    }

    public static <L extends ILits> Solver<L,DataStructureFactory<L>> newMiniSATHeap(
            DataStructureFactory<L> dsf) {
        MiniSATLearning<L,DataStructureFactory<L>> learning = new MiniSATLearning<L,DataStructureFactory<L>>();
        Solver<L,DataStructureFactory<L>> solver = new Solver<L,DataStructureFactory<L>>(new FirstUIP(), learning, dsf,
                new VarOrderHeap<L>(), new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with data structures to handle cardinality constraints.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniCard() {
        return newMiniSAT(new CardinalityDataStructure());
    }


    /**
     * @return MiniSAT with decision UIP clause generator.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newRelsat() {
        MiniSATLearning<ILits,DataStructureFactory<ILits>> learning = new MiniSATLearning<ILits,DataStructureFactory<ILits>>();
        Solver<ILits,DataStructureFactory<ILits>> solver = new Solver<ILits,DataStructureFactory<ILits>>(new DecisionUIP(), learning,
                new MixedDataStructureDaniel(), new VarOrderHeap<ILits>(),
                new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with VSIDS heuristics, FirstUIP clause generator for
     *         backjumping but no learning.
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newBackjumping() {
        NoLearningButHeuristics<ILits,DataStructureFactory<ILits>> learning = new NoLearningButHeuristics<ILits,DataStructureFactory<ILits>>();
        Solver<ILits,DataStructureFactory<ILits>> solver = new Solver<ILits,DataStructureFactory<ILits>>(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new VarOrderHeap<ILits>(),
                new MiniSATRestarts());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return a SAT solver with learning limited to clauses of length smaller
     *         or equals to 3, with a specific data structure for binary and
     *         ternary clauses as found in Lawrence Ryan thesis, without
     *         restarts, with a Jeroslow/Wang kind of heuristics.
     */
    public static Solver<ILits23,DataStructureFactory<ILits23>> newMini3SAT() {
        LimitedLearning<ILits23,DataStructureFactory<ILits23>> learning = new FixedLengthLearning<ILits23,DataStructureFactory<ILits23>>(3);
        Solver<ILits23,DataStructureFactory<ILits23>> solver = new Solver<ILits23,DataStructureFactory<ILits23>>(new FirstUIP(), learning,
                new MixedDataStructureWithBinaryAndTernary(), new SearchParams(
                        Integer.MAX_VALUE), new JWOrder(),
                new MiniSATRestarts());
        learning.setSolver(solver);
        return solver;
    }

    /**
     * @return a Mini3SAT with full learning.
     * @see #newMini3SAT()
     */
    public static Solver<ILits23,DataStructureFactory<ILits23>> newMini3SATb() {
        MiniSATLearning<ILits23,DataStructureFactory<ILits23>> learning = new MiniSATLearning<ILits23,DataStructureFactory<ILits23>>();
        Solver<ILits23,DataStructureFactory<ILits23>> solver = new Solver<ILits23,DataStructureFactory<ILits23>>(new FirstUIP(), learning,
                new MixedDataStructureWithBinaryAndTernary(), new SearchParams(
                        Integer.MAX_VALUE), new JWOrder(),
                new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return a solver computing models with a minimum number of satisfied literals.
     */
    public static ISolver newMinOneSolver() {
        return new OptToSatAdapter(new MinOneDecorator(newDefault()));
    }
    
    /**
     * Default solver of the SolverFactory. This solver is meant to be used on
     * challenging SAT benchmarks.
     * 
     * @return the best "general purpose" SAT solver available in the factory.
     * @see #defaultSolver() the same method, polymorphic, to be called from an
     *      instance of ASolverFactory.
     */
    public static ISolver newDefault() {
        return newMiniLearningHeapRsatExpSimpBiere();
    }

    @Override
    public ISolver defaultSolver() {
        return newDefault();
    }

    /**
     * Small footprint SAT solver.
     * 
     * @return a SAT solver suitable for solving small/easy SAT benchmarks.
     * @see #lightSolver() the same method, polymorphic, to be called from an
     *      instance of ASolverFactory.
     */
    public static ISolver newLight() {
        return newMini3SAT();
    }

    @Override
    public ISolver lightSolver() {
        return newLight();
    }

    public static ISolver newDimacsOutput() {
        return new DimacsOutputSolver();
    }

}
