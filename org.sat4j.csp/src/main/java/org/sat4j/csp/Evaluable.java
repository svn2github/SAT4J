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
package org.sat4j.csp;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

public interface Evaluable {

    /**
     * Return the domain of the evaluable.
     * 
     * @return the domain of the evaluable.
     */
    Domain domain();

    /**
     * Translates a value from the domain into a SAT variable in Dimacs format.
     * 
     * @param key
     *            a value from domain()
     * @return the SAT variable associated with that value.
     */
    int translate(int key);

    /**
     * Translates a variable over a domain into a set a clauses enforcing that
     * exactly one value must be chosen in the domain.
     * 
     * @param solver
     *            a solver to feed with the clauses.
     * @throws ContradictionException
     *             if a trivial inconsistency is met.
     */
    void toClause(ISolver solver) throws ContradictionException;

}
