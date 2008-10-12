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
package org.sat4j.csp;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.constraints.MixedDataStructureDaniel;
import org.sat4j.minisat.constraints.MixedDataStructureWithBinary;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.minisat.uip.FirstUIP;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.DimacsOutputSolver;

/**
 * User friendly access to pre-constructed solvers.
 * 
 * @author leberre
 */
public class SolverFactory extends ASolverFactory<ISolver> {

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
     * @param dsf
     *                the data structure used for representing clauses and lits
     * @return MiniSAT the data structure dsf.
     */
    public static <L extends ILits> Solver<L,DataStructureFactory<L>> newMiniSAT(
            DataStructureFactory<L> dsf) {
        MiniSATLearning<L,DataStructureFactory<L>> learning = new MiniSATLearning<L,DataStructureFactory<L>>();
        Solver<L,DataStructureFactory<L>> solver = new Solver<L,DataStructureFactory<L>>(new FirstUIP(), learning, dsf,
                new VarOrderHeap<L>(), new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
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
        Solver<ILits,DataStructureFactory<ILits>> solver = newMiniSAT(new MixedDataStructureDaniel());
        solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
        return solver;
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
    	return newMiniSAT(new MixedDataStructureWithBinary());
    }

    @Override
    public ISolver lightSolver() {
        return newLight();
    }

    public static ISolver newDimacsOutput() {
        return new DimacsOutputSolver();
    }

}
