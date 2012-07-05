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
package org.sat4j.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;

/**
 * Allow to feed the solver with several SearchListener.
 * 
 * @author leberre
 * 
 */
public class MultiTracing implements SearchListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private final Collection<SearchListener> listeners = new ArrayList<SearchListener>();

    public MultiTracing(SearchListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
    }

    public MultiTracing(List<SearchListener> listenersList) {
        this.listeners.addAll(listenersList);
    }

    public void assuming(int p) {
        for (SearchListener sl : this.listeners) {
            sl.assuming(p);
        }

    }

    public void propagating(int p, IConstr reason) {
        for (SearchListener sl : this.listeners) {
            sl.propagating(p, reason);
        }

    }

    public void backtracking(int p) {
        for (SearchListener sl : this.listeners) {
            sl.backtracking(p);
        }
    }

    public void adding(int p) {
        for (SearchListener sl : this.listeners) {
            sl.adding(p);
        }

    }

    public void learn(IConstr c) {
        for (SearchListener sl : this.listeners) {
            sl.learn(c);
        }

    }

    public void delete(int[] clause) {
        for (SearchListener sl : this.listeners) {
            sl.delete(clause);
        }

    }

    public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
        for (SearchListener sl : this.listeners) {
            sl.conflictFound(confl, dlevel, trailLevel);
        }

    }

    public void conflictFound(int p) {
        for (SearchListener sl : this.listeners) {
            sl.conflictFound(p);
        }

    }

    public void solutionFound(int[] model) {
        for (SearchListener sl : this.listeners) {
            sl.solutionFound(model);
        }

    }

    public void beginLoop() {
        for (SearchListener sl : this.listeners) {
            sl.beginLoop();
        }
    }

    public void start() {
        for (SearchListener sl : this.listeners) {
            sl.start();
        }

    }

    public void end(Lbool result) {
        for (SearchListener sl : this.listeners) {
            sl.end(result);
        }
    }

    public void restarting() {
        for (SearchListener sl : this.listeners) {
            sl.restarting();
        }

    }

    public void backjump(int backjumpLevel) {
        for (SearchListener sl : this.listeners) {
            sl.backjump(backjumpLevel);
        }

    }

    public void init(ISolverService solverService) {
        for (SearchListener sl : this.listeners) {
            sl.init(solverService);
        }
    }

    public void cleaning() {
        for (SearchListener sl : this.listeners) {
            sl.cleaning();
        }
    }

}
