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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.opt.MaxSatDecorator;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.OptToSatAdapter;

public class TestDavid {

    @Test
    public void testMaxsat() throws ContradictionException, TimeoutException {
        MaxSatDecorator maxsat = new MaxSatDecorator(SolverFactory.newLight());
        maxsat.newVar(3);
        IVecInt literals = new VecInt();
        literals.push(1).push(-2).push(3);
        maxsat.addClause(literals);
        literals.clear();
        literals.push(-1).push(-2);
        maxsat.addClause(literals);
        literals.clear();
        literals.push(2);
        maxsat.addClause(literals);
        literals.clear();
        literals.push(-3);
        maxsat.addClause(literals);
        OptToSatAdapter opt = new OptToSatAdapter(maxsat);
        assertTrue(opt.isSatisfiable());
        assertEquals(1, maxsat.calculateObjective());
    }

    @Test
    public void testMaxsatBis() throws ContradictionException, TimeoutException {
        MaxSatDecorator maxsat = new MaxSatDecorator(SolverFactory.newLight());
        maxsat.newVar(3);
        IVecInt literals = new VecInt();
        literals.push(1).push(-2);
        maxsat.addClause(literals);
        literals.clear();
        literals.push(-1).push(-2);
        maxsat.addClause(literals);
        literals.clear();
        literals.push(2);
        maxsat.addClause(literals);
        literals.clear();
        literals.push(-3);
        maxsat.addClause(literals);
        OptToSatAdapter opt = new OptToSatAdapter(maxsat);
        assertTrue(opt.isSatisfiable());
        assertTrue(maxsat.calculateObjective().equals(1));
        assertFalse(opt.model(3));
    }

    @Test
    public void testPartialWeightedMaxsat() throws ContradictionException,
            TimeoutException {
        WeightedMaxSatDecorator maxsat = new WeightedMaxSatDecorator(
                SolverFactory.newLight());
        maxsat.newVar(3);
        IVecInt literals = new VecInt();
        literals.push(1).push(-2).push(3);
        maxsat.addHardClause(literals);
        literals.clear();
        literals.push(-1).push(-2);
        maxsat.addHardClause(literals);
        literals.clear();
        literals.push(2);
        maxsat.addSoftClause(10, literals);
        literals.clear();
        literals.push(-3);
        maxsat.addSoftClause(5, literals);
        OptToPBSATAdapter opt = new OptToPBSATAdapter(new PseudoOptDecorator(
                maxsat));
        assertTrue(opt.isSatisfiable());
        assertTrue(opt.model(2));
        assertTrue(opt.model(3));
    }

    @Test
    public void testWeightedMinimization() throws ContradictionException,
            TimeoutException {
        WeightedMaxSatDecorator maxsat = new WeightedMaxSatDecorator(
                SolverFactory.newLight());
        maxsat.newVar(3);
        IVecInt literals = new VecInt();
        literals.push(1).push(-2).push(3);
        maxsat.addHardClause(literals);
        literals.clear();
        literals.push(-1).push(-2);
        maxsat.addHardClause(literals);
        literals.clear();
        literals.push(-2).push(3);
        IVecInt coefs = new VecInt().push(10).push(5);
        maxsat.addWeightedLiteralsToMinimize(literals, coefs);
        OptToPBSATAdapter opt = new OptToPBSATAdapter(new PseudoOptDecorator(
                maxsat));
        assertTrue(opt.isSatisfiable());
        assertTrue(opt.model(2));
        assertTrue(opt.model(3));
    }

    @Test
    public void testExampleDavid() throws ContradictionException,
            TimeoutException {
        WeightedMaxSatDecorator maxsat = new WeightedMaxSatDecorator(
                SolverFactory.newLight());
        maxsat.newVar(3);
        IVecInt literals = new VecInt();
        literals.push(1).push(-2).push(3);
        maxsat.addHardClause(literals);
        literals.clear();
        literals.push(-1).push(-2);
        maxsat.addHardClause(literals);
        literals.clear();
        literals.push(1).push(2).push(3);
        maxsat.addLiteralsToMinimize(literals);
        OptToPBSATAdapter opt = new OptToPBSATAdapter(new PseudoOptDecorator(
                maxsat));
        assertTrue(opt.isSatisfiable());
        assertFalse(opt.model(1));
        assertFalse(opt.model(2));
        assertFalse(opt.model(3));
    }
}
