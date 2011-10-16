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
package org.sat4j.minisat.orders;

import java.io.PrintWriter;
import java.util.Random;

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;

/**
 * @since 2.2
 */
public class RandomWalkDecorator implements IOrder {

	private final VarOrderHeap decorated;

	private final double p;

	private final Random rand = new Random(123456789);
	private ILits voc;
	private int nbRandomWalks;

	public RandomWalkDecorator(VarOrderHeap order) {
		this(order, 0.01);
	}

	public RandomWalkDecorator(VarOrderHeap order, double p) {
		decorated = order;
		this.p = p;
	}

	public void assignLiteral(int q) {
		decorated.assignLiteral(q);
	}

	public IPhaseSelectionStrategy getPhaseSelectionStrategy() {
		return decorated.getPhaseSelectionStrategy();
	}

	public void init() {
		decorated.init();
	}

	public void printStat(PrintWriter out, String prefix) {
		out.println(prefix + "random walks\t: " + nbRandomWalks);
		decorated.printStat(out, prefix);
	}

	public int select() {
		if (rand.nextDouble() < p) {
			int var, lit, max;

			while (!decorated.heap.empty()) {
				max = decorated.heap.size();
				var = decorated.heap.get(rand.nextInt(max) + 1);
				lit = getPhaseSelectionStrategy().select(var);
				if (voc.isUnassigned(lit)) {
					nbRandomWalks++;
					return lit;
				}
			}
		}
		return decorated.select();
	}

	public void setLits(ILits lits) {
		decorated.setLits(lits);
		voc = lits;
		nbRandomWalks = 0;
	}

	public void setPhaseSelectionStrategy(IPhaseSelectionStrategy strategy) {
		decorated.setPhaseSelectionStrategy(strategy);
	}

	public void setVarDecay(double d) {
		decorated.setVarDecay(d);
	}

	public void undo(int x) {
		decorated.undo(x);
	}

	public void updateVar(int q) {
		decorated.updateVar(q);
	}

	public double varActivity(int q) {
		return decorated.varActivity(q);
	}

	public void varDecayActivity() {
		decorated.varDecayActivity();
	}

	public void updateVarAtDecisionLevel(int q) {
		decorated.updateVarAtDecisionLevel(q);
	}

	@Override
	public String toString() {
		return decorated.toString() + " with random walks " + p;
	}

}
