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
* Based on the original MiniSat specification from:
* 
* An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
* Sixth International Conference on Theory and Applications of Satisfiability
* Testing, LNCS 2919, pp 502-518, 2003.
*
* See www.minisat.se for the original solver in C++.
* 
*******************************************************************************/
package org.sat4j.minisat.core;

import java.io.Serializable;

/**
 * Interface to the solver main steps. Useful for integrating search
 * visualization or debugging.
 * 
 * @author daniel
 * 
 */
public interface SearchListener extends Serializable {

    /**
     * decision variable
     * 
     * @param p
     */
    void assuming(int p);

    /**
     * Unit propagation
     * 
     * @param p
     */
    void propagating(int p);

    /**
     * backtrack on a decision variable
     * 
     * @param p
     */
    void backtracking(int p);

    /**
     * adding forced variable (conflict driven assignment)
     */
    void adding(int p);

    /**
     * learning a new clause
     * 
     * @param c
     */
    void learn(Constr c);

    /**
     * delete a clause
     */
    void delete(int[] clause);

    /**
     * a conflict has been found.
     * 
     */
    void conflictFound();

    /**
     * a solution is found.
     * 
     */
    void solutionFound();

    /**
     * starts a propagation
     */
    void beginLoop();

    /**
     * Start the search.
     * 
     */
    void start();

    /**
     * End the search.
     * 
     * @param result
     *            the result of the search.
     */
    void end(Lbool result);
}
