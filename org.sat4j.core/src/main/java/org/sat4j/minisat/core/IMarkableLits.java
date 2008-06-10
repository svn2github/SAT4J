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

import java.util.Set;

import org.sat4j.specs.IVecInt;

/**
 * Vocabulary in which literals can be marked.
 * 
 * @author daniel
 * 
 */
public interface IMarkableLits extends ILits {
    int MARKLESS = 0;

    /**
     * Mark a given literal with a given mark.
     * 
     * @param p
     *            the literal
     * @param mark
     *            an integer used to mark the literal. The specific mark
     *            MARKLESS is used to denote that the literal is not marked. The
     *            marks are supposed to be positive in the most common cases.
     */
    void setMark(int p, int mark);

    /**
     * Mark a given literal.
     * 
     * @param p
     *            a literal
     */
    void setMark(int p);

    /**
     * To get the mark for a given literal.
     * 
     * @param p
     *            a literal
     * @return the mark associated with that literal, or MARKLESS if the literal
     *         is not marked.
     */
    int getMark(int p);

    /**
     * To know if a given literal is marked, i.e. has a mark different from
     * MARKLESS.
     * 
     * @param p
     *            a literal
     * @return true iif the literal is marked.
     */
    boolean isMarked(int p);

    /**
     * Set the mark of a given literal to MARKLESS.
     * 
     * @param p
     *            a literal
     */
    void resetMark(int p);

    /**
     * Set all the literal marks to MARKLESS
     * 
     */
    void resetAllMarks();

    /**
     * Returns the set of all marked literals.
     * 
     * @return a set of literals whose mark is different from MARKLESS.
     */
    IVecInt getMarkedLiterals();

    /**
     * Returns that set of all the literals having a specific mark.
     * 
     * @param mark
     *            a mark
     * @return a set of literals whose mark is mark
     */
    IVecInt getMarkedLiterals(int mark);

    /**
     * Returns the set of all marked variables. A variable is marked iff at
     * least one of its literal is marked.
     * 
     * @return a set of variables whose mark is different from MARKLESS.
     */
    IVecInt getMarkedVariables();

    /**
     * Returns the set of all variables having a specific mark. A variable is
     * marked iff at least one of its literal is marked.
     * 
     * @param mark
     *            a mark.
     * @return a set of variables whose mark is mark.
     */
    IVecInt getMarkedVariables(int mark);

    /**
     * 
     * @return a list of marks used to mark the literals.
     */
    Set<Integer> getMarks();
}
