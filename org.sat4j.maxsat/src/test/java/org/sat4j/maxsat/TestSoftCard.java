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
package org.sat4j.maxsat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class TestSoftCard {

    private WeightedMaxSatDecorator wms;

    @Before
    public void setUp() {
        this.wms = new WeightedMaxSatDecorator(SolverFactory.newDefault());
    }

    @Test
    public void test() throws ContradictionException, TimeoutException {
        this.wms.newVar(5);
        IVecInt clause = new VecInt();
        clause.push(1).push(2).push(3);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(1).push(-2);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(1).push(2);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(-1).push(3);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(1).push(2).push(3);
        this.wms.addAtMost(clause, 1);
        clause.clear();
        assertFalse(this.wms.isSatisfiable());
    }

    @Test
    public void test2() throws ContradictionException, TimeoutException {
        this.wms.newVar(5);
        IVecInt clause = new VecInt();
        clause.push(1).push(2).push(3);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(1).push(-2);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(1).push(2);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(-1).push(3);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(1).push(2).push(3);
        this.wms.addSoftAtMost(clause, 1);
        clause.clear();
        assertTrue(this.wms.isSatisfiable());
    }

    @Test
    public void test3() throws ContradictionException, TimeoutException {
        this.wms.newVar(6);
        IVecInt clause = new VecInt();
        clause.push(1).push(2).push(3);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(4).push(5).push(6);
        this.wms.addHardClause(clause);
        clause.clear();
        clause.push(1).push(2).push(3).push(4).push(5).push(6);
        this.wms.addSoftAtMost(clause, 1);
        clause.clear();
        assertTrue(this.wms.isSatisfiable());
    }

}
