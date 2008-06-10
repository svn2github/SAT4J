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
*******************************************************************************/
package org.sat4j.maxsat;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.constraints.MixedDataStructureDaniel;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.minisat.uip.FirstUIP;
import org.sat4j.pb.IPBSolver;

public class SolverFactory extends ASolverFactory<IPBSolver> {

    private static final long serialVersionUID = 1L;

    /**
     * Builds a SAT solver for the MAX sat evaluation. Full clause learning, no
     * restarts,
     * 
     * @return a
     */
    public static Solver<ILits,DataStructureFactory<ILits>> newMiniMaxSAT() {
        MiniSATLearning<ILits,DataStructureFactory<ILits>> learning = new MiniSATLearning<ILits,DataStructureFactory<ILits>>();
        Solver<ILits,DataStructureFactory<ILits>> solver = new Solver<ILits,DataStructureFactory<ILits>>(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new SearchParams(1.2,
                        100000), new VarOrderHeap<ILits>(),
                new MiniSATRestarts());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }
    
    @Override
    public IPBSolver defaultSolver() {
        return newDefault();
    }

    @Override
    public IPBSolver lightSolver() {
        return newLight();
    }

    public static IPBSolver newDefault() {
        return org.sat4j.pb.SolverFactory.newDefault();
    }
 
    public static IPBSolver newLight() {
        return org.sat4j.pb.SolverFactory.newLight();
    }
}
