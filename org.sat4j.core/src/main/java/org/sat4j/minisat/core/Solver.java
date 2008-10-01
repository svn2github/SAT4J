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
 * The reason simplification methods are coming from MiniSAT 1.14 released under 
 * the MIT license:
 * MiniSat -- Copyright (c) 2003-2005, Niklas Een, Niklas Sorensson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 *******************************************************************************/
package org.sat4j.minisat.core;

import static org.sat4j.core.LiteralsUtils.var;
import static org.sat4j.core.LiteralsUtils.toDimacs;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.TimeoutException;

/**
 * The backbone of the library providing the modular implementation of a MiniSAT
 * (Chaff) like solver.
 * 
 * @author leberre
 */
public class Solver<L extends ILits, D extends DataStructureFactory<L>>
		implements ISolver, UnitPropagationListener, ActivityListener, Learner {

	private static final long serialVersionUID = 1L;

	private static final double CLAUSE_RESCALE_FACTOR = 1e-20;

	private static final double CLAUSE_RESCALE_BOUND = 1 / CLAUSE_RESCALE_FACTOR;

	/**
	 * List des contraintes du probl?me.
	 */
	private final IVec<Constr> constrs = new Vec<Constr>(); // Constr

	/**
	 * Liste des clauses apprises.
	 */
	private final IVec<Constr> learnts = new Vec<Constr>(); // Clause

	/**
	 * incr?ment pour l'activit? des clauses.
	 */
	private double claInc = 1.0;

	/**
	 * decay factor pour l'activit? des clauses.
	 */
	private double claDecay = 1.0;

	/**
	 * Queue de propagation
	 */
	// private final IntQueue propQ = new IntQueue(); // Lit
	// head of the queue in trail ... (taken from MiniSAT 1.14)
	private int qhead = 0;

	// queue

	/**
	 * affectation en ordre chronologique
	 */
	protected final IVecInt trail = new VecInt(); // lit

	// vector

	/**
	 * indice des s?parateurs des diff?rents niveau de d?cision dans trail
	 */
	protected final IVecInt trailLim = new VecInt(); // int

	// vector

	/**
	 * S?pare les hypoth?ses incr?mentale et recherche
	 */
	protected int rootLevel;

	private int[] model = null;

	protected L voc;

	private IOrder<L> order;

	private final ActivityComparator comparator = new ActivityComparator();

	private final SolverStats stats = new SolverStats();

	private final LearningStrategy<L, D> learner;

	protected final AssertingClauseGenerator analyzer;

	private volatile boolean undertimeout;

	private long timeout = Integer.MAX_VALUE;

	private boolean timeBasedTimeout = true;

	protected D dsfactory;

	private SearchParams params;

	private final IVecInt __dimacs_out = new VecInt();

	private SearchListener slistener = new NullSearchListener();

	private RestartStrategy restarter;

	private final Map<String, Integer> constrTypes = new HashMap<String, Integer>();

	private boolean isDBSimplificationAllowed = false;

	private int learnedLiterals = 0;
	
	protected IVecInt dimacs2internal(IVecInt in) {
		// if (voc.nVars() == 0) {
		// throw new RuntimeException(
		// "Please set the number of variables (solver.newVar() or solver.newVar(maxvar)) before adding constraints!"
		// );
		// }
		__dimacs_out.clear();
		__dimacs_out.ensure(in.size());
		for (int i = 0; i < in.size(); i++) {
			assert (in.get(i) != 0); // && (Math.abs(in.get(i)) <= voc.nVars());
			__dimacs_out.unsafePush(voc.getFromPool(in.get(i)));
		}
		return __dimacs_out;
	}

	/**
	 * creates a Solver without LearningListener. A learningListener must be
	 * added to the solver, else it won't backtrack!!! A data structure factory
	 * must be provided, else it won't work either.
	 * 
	 * @param acg
	 * 		an asserting clause generator
	 */

	public Solver(AssertingClauseGenerator acg, LearningStrategy<L, D> learner,
			D dsf, IOrder<L> order, RestartStrategy restarter) {
		this(acg, learner, dsf, new SearchParams(), order, restarter);
	}

	public Solver(AssertingClauseGenerator acg, LearningStrategy<L, D> learner,
			D dsf, SearchParams params, IOrder<L> order,
			RestartStrategy restarter) {
		analyzer = acg;
		this.learner = learner;
		this.order = order;
		this.params = params;
		setDataStructureFactory(dsf);
		this.restarter = restarter;
	}

	/**
	 * Change the internal representation of the contraints. Note that the
	 * heuristics must be changed prior to calling that method.
	 * 
	 * @param dsf
	 * 		the internal factory
	 */
	public final void setDataStructureFactory(D dsf) {
		dsfactory = dsf;
		dsfactory.setUnitPropagationListener(this);
		dsfactory.setLearner(this);
		voc = dsf.getVocabulary();
		order.setLits(voc);
	}

	public void setSearchListener(SearchListener sl) {
		slistener = sl;
	}

	public void setTimeout(int t) {
		timeout = t * 1000L;
		timeBasedTimeout = true;
	}

	public void setTimeoutMs(long t) {
		timeout = t;
		timeBasedTimeout = true;
	}

	public void setTimeoutOnConflicts(int count) {
		timeout = count;
		timeBasedTimeout = false;
	}

	public void setSearchParams(SearchParams sp) {
		params = sp;
	}

	public void setRestartStrategy(RestartStrategy restarter) {
		this.restarter = restarter;
	}

	public void expireTimeout() {
		undertimeout = false;
	}

	protected int nAssigns() {
		return trail.size();
	}

	public int nConstraints() {
		return constrs.size()+trail.size()-learnedLiterals;
	}

	public void learn(Constr c) {
		learnts.push(c);
		c.setLearnt();
		c.register();
		stats.learnedclauses++;
		switch (c.size()) {
		case 2:
			stats.learnedbinaryclauses++;
			break;
		case 3:
			stats.learnedternaryclauses++;
			break;
		default:
			// do nothing
		}
	}

	public int decisionLevel() {
		return trailLim.size();
	}

	@Deprecated
	public int newVar() {
		int index = voc.nVars() + 1;
		voc.ensurePool(index);
		return index;
	}

	public int newVar(int howmany) {
		voc.ensurePool(howmany);
		return voc.nVars();
	}

	public IConstr addClause(IVecInt literals) throws ContradictionException {
		IVecInt vlits = dimacs2internal(literals);
		return addConstr(dsfactory.createClause(vlits));
	}

	public boolean removeConstr(IConstr co) {
		if (co == null) {
			throw new IllegalArgumentException(
					"Reference to the constraint to remove needed!"); //$NON-NLS-1$
		}
		Constr c = (Constr) co;
		c.remove();
		constrs.remove(c);
		clearLearntClauses();
		cancelLearntLiterals();
		return true;
	}

	public void addAllClauses(IVec<IVecInt> clauses)
			throws ContradictionException {
		for (Iterator<IVecInt> iterator = clauses.iterator(); iterator
				.hasNext();) {
			addClause(iterator.next());
		}
	}

	public IConstr addAtMost(IVecInt literals, int degree)
			throws ContradictionException {
		int n = literals.size();
		IVecInt opliterals = new VecInt(n);
		for (IteratorInt iterator = literals.iterator(); iterator.hasNext();) {
			opliterals.push(-iterator.next());
		}
		return addAtLeast(opliterals, n - degree);
	}

	public IConstr addAtLeast(IVecInt literals, int degree)
			throws ContradictionException {
		IVecInt vlits = dimacs2internal(literals);
		return addConstr(dsfactory.createCardinalityConstraint(vlits, degree));
	}

	@SuppressWarnings("unchecked")
	public boolean simplifyDB() {
		// aucune raison de recommencer un propagate?
		// if (propagate() != null) {
		// // Un conflit est d?couvert, la base est inconsistante
		// return false;
		// }

		// Simplifie la base de clauses apres la premiere propagation des
		// clauses unitaires
		IVec<Constr>[] cs = new IVec[] { constrs, learnts };
		for (int type = 0; type < 2; type++) {
			int j = 0;
			for (int i = 0; i < cs[type].size(); i++) {
				if (cs[type].get(i).simplify()) {
					// enleve les contraintes satisfaites de la base
					cs[type].get(i).remove();
				} else {
					cs[type].moveTo(j++, i);
				}
			}
			cs[type].shrinkTo(j);
		}
		return true;
	}

	/**
	 * Si un mod?le est trouv?, ce vecteur contient le mod?le.
	 * 
	 * @return un mod?le de la formule.
	 */
	public int[] model() {
		if (model == null) {
			throw new UnsupportedOperationException(
					"Call the solve method first!!!"); //$NON-NLS-1$
		}
		int[] nmodel = new int[model.length];
		System.arraycopy(model, 0, nmodel, 0, model.length);
		return nmodel;
	}

	/**
	 * Satisfait un litt?ral
	 * 
	 * @param p
	 * 		le litt?ral
	 * @return true si tout se passe bien, false si un conflit appara?t.
	 */
	public boolean enqueue(int p) {
		return enqueue(p, null);
	}

	/**
	 * Put the literal on the queue of assignments to be done.
	 * 
	 * @param p
	 * 		the literal.
	 * @param from
	 * 		the reason to propagate that literal, else null
	 * @return true if the asignment can be made, false if a conflict is
	 * 	detected.
	 */
	public boolean enqueue(int p, Constr from) {
		assert p > 1;
		if (voc.isSatisfied(p)) {
			// literal is already satisfied. Skipping.
			return true;
		}
		if (voc.isFalsified(p)) {
			// conflicting enqueued assignment
			return false;
		}
		// new fact, store it
		voc.satisfies(p);
		voc.setLevel(p, decisionLevel());
		voc.setReason(p, from);
		trail.push(p);
		return true;
	}

	private boolean[] mseen = new boolean[0];

	private final IVecInt preason = new VecInt();

	private final IVecInt outLearnt = new VecInt();

	public void analyze(Constr confl, Pair results) {
		assert confl != null;
		outLearnt.clear();

		final boolean[] seen = mseen;

		assert outLearnt.size() == 0;
		for (int i = 0; i < seen.length; i++) {
			seen[i] = false;
		}

		analyzer.initAnalyze();
		int p = ILits.UNDEFINED;

		outLearnt.push(ILits.UNDEFINED);
		// reserve de la place pour le litteral falsifie
		int outBtlevel = 0;

		do {
			preason.clear();
			assert confl != null;
			confl.calcReason(p, preason);
			if (confl.learnt())
				claBumpActivity(confl);
			// Trace reason for p
			for (int j = 0; j < preason.size(); j++) {
				int q = preason.get(j);
				order.updateVar(q);
				if (!seen[q >> 1]) {
					// order.updateVar(q); // MINISAT
					seen[q >> 1] = true;
					if (voc.getLevel(q) == decisionLevel()) {
						analyzer.onCurrentDecisionLevelLiteral(q);
					} else if (voc.getLevel(q) > 0) {
						// ajoute les variables depuis le niveau de d?cision 0
						outLearnt.push(q ^ 1);
						outBtlevel = Math.max(outBtlevel, voc.getLevel(q));
					}
				}
			}

			// select next reason to look at
			do {
				p = trail.last();
				// System.err.print((Clause.lastid()+1)+"
				// "+((Clause)confl).getId()+"" );
				confl = voc.getReason(p);
				// System.err.println(((Clause)confl).getId());
				// assert(confl != null) || counter == 1;
				undoOne();
			} while (!seen[p >> 1]);
			// seen[p.var] indique que p se trouve dans outLearnt ou dans
			// le dernier niveau de d?cision
		} while (analyzer.clauseNonAssertive(confl));

		outLearnt.set(0, p ^ 1);
		simplifier.simplify(outLearnt);

		Constr c = dsfactory.createUnregisteredClause(outLearnt);
		slistener.learn(c);

		results.reason = c;

		assert outBtlevel > -1;
		results.backtrackLevel = outBtlevel;
	}

	interface ISimplifier extends Serializable {
		void simplify(IVecInt outLearnt);
	}

	public static final ISimplifier NO_SIMPLIFICATION = new ISimplifier() {
		/**
         * 
         */
		private static final long serialVersionUID = 1L;

		public void simplify(IVecInt outLearnt) {
		}

		@Override
		public String toString() {
			return "No reason simplification"; //$NON-NLS-1$
		}
	};

	public final ISimplifier SIMPLE_SIMPLIFICATION = new ISimplifier() {
		/**
         * 
         */
		private static final long serialVersionUID = 1L;

		public void simplify(IVecInt conflictToReduce) {
			simpleSimplification(conflictToReduce);
		}

		@Override
		public String toString() {
			return "Simple reason simplification"; //$NON-NLS-1$
		}
	};

	public final ISimplifier EXPENSIVE_SIMPLIFICATION = new ISimplifier() {

		/**
         * 
         */
		private static final long serialVersionUID = 1L;

		public void simplify(IVecInt conflictToReduce) {
			expensiveSimplification(conflictToReduce);
		}

		@Override
		public String toString() {
			return "Expensive reason simplification"; //$NON-NLS-1$
		}
	};

	private ISimplifier simplifier = NO_SIMPLIFICATION;

	/**
	 * Setup the reason simplification strategy. By default, there is no reason
	 * simplification. NOTE THAT REASON SIMPLIFICATION DOES NOT WORK WITH 
	 * SPECIFIC DATA STRUCTURE FOR HANDLING BOTH BINARY AND TERNARY CLAUSES. 
	 * 
	 * @param simp
	 * 		the name of the simplifier (one of NO_SIMPLIFICATION,
	 * 		SIMPLE_SIMPLIFICATION, EXPENSIVE_SIMPLIFICATION).
	 */
	public void setSimplifier(String simp) {
		Field f;
		try {
			f = Solver.class.getDeclaredField(simp);
			simplifier = (ISimplifier) f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			simplifier = NO_SIMPLIFICATION;
		}
	}

	/**
	 * Setup the reason simplification strategy. By default, there is no reason
	 * simplification. NOTE THAT REASON SIMPLIFICATION IS ONLY ALLOWED FOR WL
	 * CLAUSAL data structures. USING REASON SIMPLIFICATION ON CB CLAUSES,
	 * CARDINALITY CONSTRAINTS OR PB CONSTRAINTS MIGHT RESULT IN INCORRECT
	 * RESULTS.
	 * 
	 * @param simp
	 */
	public void setSimplifier(ISimplifier simp) {
		simplifier = simp;
	}

	// MiniSat -- Copyright (c) 2003-2005, Niklas Een, Niklas Sorensson
	//
	// Permission is hereby granted, free of charge, to any person obtaining a
	// copy of this software and associated documentation files (the
	// "Software"), to deal in the Software without restriction, including
	// without limitation the rights to use, copy, modify, merge, publish,
	// distribute, sublicense, and/or sell copies of the Software, and to
	// permit persons to whom the Software is furnished to do so, subject to
	// the following conditions:
	//
	// The above copyright notice and this permission notice shall be included
	// in all copies or substantial portions of the Software.
	//
	// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
	// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
	// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
	// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
	// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
	// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
	// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

	// Taken from MiniSAT 1.14: Simplify conflict clause (a little):
	private void simpleSimplification(IVecInt conflictToReduce) {
		int i, j;
		final boolean[] seen = mseen;
		for (i = j = 1; i < conflictToReduce.size(); i++) {
			IConstr r = voc.getReason(conflictToReduce.get(i));
			if (r == null) {
				conflictToReduce.moveTo(j++, i);
			} else {
				for (int k = 0; k < r.size(); k++)
					if (voc.isFalsified(r.get(k)) && !seen[r.get(k) >> 1]
							&& (voc.getLevel(r.get(k)) != 0)) {
						conflictToReduce.moveTo(j++, i);
						break;
					}
			}
		}
		conflictToReduce.shrink(i - j);
		stats.reducedliterals += (i - j);
	}

	private final IVecInt analyzetoclear = new VecInt();

	private final IVecInt analyzestack = new VecInt();

	// Taken from MiniSAT 1.14
	private void expensiveSimplification(IVecInt conflictToReduce) {
		// Simplify conflict clause (a lot):
		//
		int i, j;
		// (maintain an abstraction of levels involved in conflict)
		analyzetoclear.clear();
		conflictToReduce.copyTo(analyzetoclear);
		for (i = 1, j = 1; i < conflictToReduce.size(); i++)
			if (voc.getReason(conflictToReduce.get(i)) == null
					|| !analyzeRemovable(conflictToReduce.get(i)))
				conflictToReduce.moveTo(j++, i);
		conflictToReduce.shrink(i - j);
		stats.reducedliterals += (i - j);
	}

	// Check if 'p' can be removed.' min_level' is used to abort early if
	// visiting literals at a level that cannot be removed.
	//
	private boolean analyzeRemovable(int p) {
		assert voc.getReason(p) != null;
		analyzestack.clear();
		analyzestack.push(p);
		final boolean[] seen = mseen;
		int top = analyzetoclear.size();
		while (analyzestack.size() > 0) {
			int q = analyzestack.last();
			assert voc.getReason(q) != null;
			Constr c = voc.getReason(q);
			analyzestack.pop();
			for (int i = 0; i < c.size(); i++) {
				int l = c.get(i);
				if (voc.isFalsified(l) && !seen[var(l)] && voc.getLevel(l) != 0) {
					if (voc.getReason(l) == null) {
						for (int j = top; j < analyzetoclear.size(); j++)
							seen[analyzetoclear.get(j) >> 1] = false;
						analyzetoclear.shrink(analyzetoclear.size() - top);
						return false;
					}
					seen[l >> 1] = true;
					analyzestack.push(l);
					analyzetoclear.push(l);
				}
			}
		}

		return true;
	}

	// END Minisat 1.14 cut and paste

	/**
     * 
     */
	protected void undoOne() {
		// recupere le dernier litteral affecte
		int p = trail.last();
		assert p > 1;
		assert voc.getLevel(p) >= 0;
		int x = p >> 1;
		// desaffecte la variable
		voc.unassign(p);
		voc.setReason(p, null);
		voc.setLevel(p, -1);
		// met a jour l'heuristique
		order.undo(x);
		// depile le litteral des affectations
		trail.pop();
		// met a jour les contraintes apres desaffectation du litteral :
		// normalement, il n'y a rien a faire ici pour les prouveurs de type
		// Chaff??
		IVec<Undoable> undos = voc.undos(p);
		assert undos != null;
		while (undos.size() > 0) {
			undos.last().undo(p);
			undos.pop();
		}
	}

	/**
	 * Propagate activity to a constraint
	 * 
	 * @param confl
	 * 		a constraint
	 */
	public void claBumpActivity(Constr confl) {
		confl.incActivity(claInc);
		if (confl.getActivity() > CLAUSE_RESCALE_BOUND)
			claRescalActivity();
		// for (int i = 0; i < confl.size(); i++) {
		// varBumpActivity(confl.get(i));
		// }
	}

	public void varBumpActivity(int p) {
		order.updateVar(p);
	}

	private void claRescalActivity() {
		for (int i = 0; i < learnts.size(); i++) {
			learnts.get(i).rescaleBy(CLAUSE_RESCALE_FACTOR);
		}
		claInc *= CLAUSE_RESCALE_FACTOR;
	}

	/**
	 * @return null if not conflict is found, else a conflicting constraint.
	 */
	public Constr propagate() {
		while (qhead < trail.size()) {
			stats.propagations++;
			int p = trail.get(qhead++);
			slistener.propagating(toDimacs(p));
			order.assignLiteral(p);
			// p is the literal to propagate
			// Moved original MiniSAT code to dsfactory to avoid
			// watches manipulation in counter Based clauses for instance.
			assert p > 1;
			IVec<Propagatable> watched = dsfactory.getWatchesFor(p);

			final int size = watched.size();
			for (int i = 0; i < size; i++) {
				stats.inspects++;
				if (!watched.get(i).propagate(this, p)) {
					// Constraint is conflicting: copy remaining watches to
					// watches[p]
					// and return constraint
					dsfactory.conflictDetectedInWatchesFor(p, i);
					qhead = trail.size(); // propQ.clear();
					// FIXME enlever le transtypage
					return (Constr) watched.get(i);
				}
			}
		}
		return null;
	}

	void record(Constr constr) {
		constr.assertConstraint(this);
		slistener.adding(toDimacs(constr.get(0)));
		if (constr.size() == 1) {
			stats.learnedliterals++;
			learnedLiterals++;
		} else {
			learner.learns(constr);
		}
	}

	/**
	 * @return false ssi conflit imm?diat.
	 */
	public boolean assume(int p) {
		// Precondition: assume propagation queue is empty
		assert trail.size() == qhead;
		trailLim.push(trail.size());
		return enqueue(p);
	}

	/**
	 * Revert to the state before the last push()
	 */
	private void cancel() {
		// assert trail.size() == qhead || !undertimeout;
		int decisionvar = trail.unsafeGet(trailLim.last());
		slistener.backtracking(toDimacs(decisionvar));
		for (int c = trail.size() - trailLim.last(); c > 0; c--) {
			undoOne();
		}
		trailLim.pop();
	}

	/**
	 * Restore literals
	 */
	private void cancelLearntLiterals() {
		// assert trail.size() == qhead || !undertimeout;
		for (int c = learnedLiterals; c > 0; c--) {
			undoOne();
		}
		qhead = trail.size();
	}

	/**
	 * Cancel several levels of assumptions
	 * 
	 * @param level
	 */
	protected void cancelUntil(int level) {
		while (decisionLevel() > level) {
			cancel();
		}
		qhead = trail.size();
	}

	private final Pair analysisResult = new Pair();

	private boolean[] fullmodel;

	Lbool search(long nofConflicts) {
		assert rootLevel == decisionLevel();
		stats.starts++;
		int conflictC = 0;

		// varDecay = 1 / params.varDecay;
		order.setVarDecay(1 / params.getVarDecay());
		claDecay = 1 / params.getClaDecay();

		do {
			slistener.beginLoop();
			// propage les clauses unitaires
			Constr confl = propagate();
			assert trail.size() == qhead;

			if (confl == null) {
				// No conflict found
				// simpliFYDB() prevents a correct use of
				// constraints removal.
				if (decisionLevel() == 0 && isDBSimplificationAllowed) {
					// // Simplify the set of problem clause
					// // iff rootLevel==0
					stats.rootSimplifications++;
					boolean ret = simplifyDB();
					assert ret;
				}
				// was learnts.size() - nAssigns() > nofLearnts
				// if (nofLearnts.obj >= 0 && learnts.size() > nofLearnts.obj) {
				assert nAssigns() <= voc.realnVars();
				if (nAssigns() == voc.realnVars()) {
					slistener.solutionFound();
					modelFound();
					return Lbool.TRUE;
				}
				if (conflictC >= nofConflicts) {
					// Reached bound on number of conflicts
					// Force a restart
					cancelUntil(rootLevel);
					return Lbool.UNDEFINED;
				}
				if (needToReduceDB) {
					reduceDB();
					needToReduceDB = false;
					// Runtime.getRuntime().gc();
				}
				// New variable decision
				stats.decisions++;
				int p = order.select();
				assert p > 1;
				slistener.assuming(toDimacs(p));
				boolean ret = assume(p);
				assert ret;
			} else {
				// un conflit apparait
				stats.conflicts++;
				conflictC++;
				slistener.conflictFound();
				conflictCount.newConflict();
				if (decisionLevel() == rootLevel) {
					// on est a la racine, la formule est inconsistante
					return Lbool.FALSE;
				}
				// analyze conflict
				analyze(confl, analysisResult);
				assert analysisResult.backtrackLevel < decisionLevel();
				cancelUntil(Math.max(analysisResult.backtrackLevel, rootLevel));
				assert (decisionLevel() >= rootLevel)
						&& (decisionLevel() >= analysisResult.backtrackLevel);
				if (analysisResult.reason == null) {
					return Lbool.FALSE;
				}
				record(analysisResult.reason);
				analysisResult.reason = null;
				decayActivities();
			}
		} while (undertimeout);
		return Lbool.UNDEFINED; // timeout occured
	}

	protected void analyzeAtRootLevel(Constr conflict) {
	}

	/**
     * 
     */
	void modelFound() {
		model = new int[trail.size()];
		fullmodel = new boolean[nVars()];
		int index = 0;
		for (int i = 1; i <= voc.nVars(); i++) {
			if (voc.belongsToPool(i)) {
				int p = voc.getFromPool(i);
				if (!voc.isUnassigned(p)) {
					model[index++] = voc.isSatisfied(p) ? i : -i;
					fullmodel[i - 1] = voc.isSatisfied(p);
				}
			}
		}
		assert index == model.length;
		cancelUntil(rootLevel);
	}

	public boolean model(int var) {
		if (var <= 0 || var > nVars()) {
			throw new IllegalArgumentException(
					"Use a valid Dimacs var id as argument!"); //$NON-NLS-1$
		}
		if (fullmodel == null) {
			throw new UnsupportedOperationException(
					"Call the solve method first!!!"); //$NON-NLS-1$
		}
		return fullmodel[var - 1];
	}

	/**
     * 
     */
	protected void reduceDB() {
		reduceDB(claInc / learnts.size());
	}

	public void clearLearntClauses() {
		for (Iterator<Constr> iterator = learnts.iterator(); iterator.hasNext();)
			iterator.next().remove();
		learnts.clear();
	}

	protected void reduceDB(double lim) {
		int i, j;
		sortOnActivity();
		stats.reduceddb++;
		for (i = j = 0; i < learnts.size() / 2; i++) {
			Constr c = learnts.get(i);
			if (c.locked()) {
				learnts.set(j++, learnts.get(i));
			} else {
				c.remove();
			}
		}
		for (; i < learnts.size(); i++) {
			// Constr c = learnts.get(i);
			// if (!c.locked() && (c.getActivity() < lim)) {
			// c.remove();
			// } else {
			learnts.set(j++, learnts.get(i));
			// }
		}
		System.out.println("c cleaning " + (learnts.size() - j) //$NON-NLS-1$
				+ " clauses out of " + learnts.size() + " for limit " + lim); //$NON-NLS-1$ //$NON-NLS-2$
		learnts.shrinkTo(j);
	}

	/**
	 * @param learnts
	 */
	private void sortOnActivity() {
		learnts.sort(comparator);
	}

	/**
     * 
     */
	protected void decayActivities() {
		order.varDecayActivity();
		claDecayActivity();
	}

	/**
     * 
     */
	private void claDecayActivity() {
		claInc *= claDecay;
	}

	/**
	 * @return true iff the set of constraints is satisfiable, else false.
	 */
	public boolean isSatisfiable() throws TimeoutException {
		return isSatisfiable(VecInt.EMPTY);
	}

	/**
	 * @return true iff the set of constraints is satisfiable, else false.
	 */
	public boolean isSatisfiable(boolean global) throws TimeoutException {
		return isSatisfiable(VecInt.EMPTY, global);
	}

	private double timebegin = 0;

	private boolean needToReduceDB;

	private ConflictTimer conflictCount;

	private transient Timer timer;

	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
		return isSatisfiable(assumps, false);
	}

	public boolean isSatisfiable(IVecInt assumps, boolean global)
			throws TimeoutException {
		Lbool status = Lbool.UNDEFINED;

		final int howmany = voc.nVars();
		if (mseen.length < howmany) {
			mseen = new boolean[howmany + 1];
		}
		trail.ensure(howmany);
		trailLim.ensure(howmany);
		learnedLiterals = 0;
		order.init();
		learner.init();
		restarter.init(params);
		timebegin = System.currentTimeMillis();
		slistener.start();
		model = null; // forget about previous model
		fullmodel = null;

		// propagate constraints
		Constr confl = propagate();
		if (confl != null) {
			analyzeAtRootLevel(confl);
			slistener.end(Lbool.FALSE);
			cancelUntil(0);
			return false;
		}

		// push incremental assumptions
		for (IteratorInt iterator = assumps.iterator(); iterator.hasNext();) {
			if (!assume(voc.getFromPool(iterator.next()))
					|| (propagate() != null)) {
				slistener.end(Lbool.FALSE);
				cancelUntil(0);
				return false;
			}
		}
		rootLevel = decisionLevel();

		final long memorybound = Runtime.getRuntime().freeMemory() / 10;

		ConflictTimer freeMem = new ConflictTimerAdapter(500) {
			private static final long serialVersionUID = 1L;

			@Override
			void run() {
				long freemem = Runtime.getRuntime().freeMemory();
				// System.out.println("c Free memory "+freemem);
				if (freemem < memorybound) {
					// Reduce the set of learnt clauses
					needToReduceDB = true;
				}
			}
		};

		if (timeBasedTimeout) {
			if (!global || timer == null) {
				TimerTask stopMe = new TimerTask() {
					@Override
					public void run() {
						undertimeout = false;
					}
				};
				timer = new Timer(true);
				timer.schedule(stopMe, timeout);
				conflictCount = freeMem;
			}
		} else {
			if (!global || conflictCount == null) {
				ConflictTimer conflictTimeout = new ConflictTimerAdapter(
						(int) timeout) {
					private static final long serialVersionUID = 1L;

					@Override
					public void run() {
						undertimeout = false;
					}
				};
				conflictCount = new ConflictTimerContainer().add(
						conflictTimeout).add(freeMem);
			}
		}
		needToReduceDB = false;
		undertimeout = true;

		// Solve
		while ((status == Lbool.UNDEFINED) && undertimeout) {
			status = search(restarter.nextRestartNumberOfConflict());
			// System.out.println("c speed
			// "+(stats.decisions/((System.currentTimeMillis()-timebegin)/1000))+"
			// dec/s, "+stats.starts+"/"+stats.conflicts);
			restarter.onRestart();
		}

		cancelUntil(0);
		if (!global && timeBasedTimeout) {
			timer.cancel();
			timer = null;
		}
		slistener.end(status);
		if (!undertimeout) {
			throw new TimeoutException(" Timeout (" + timeout + "s) exceeded"); //$NON-NLS-1$//$NON-NLS-2$
		}
		return status == Lbool.TRUE;
	}

	public void printInfos(PrintWriter out, String prefix) {
		out.print(prefix);
		out.println("constraints type ");
		for (Map.Entry<String, Integer> entry : constrTypes.entrySet()) {
			out.println(prefix + entry.getKey() + " => " + entry.getValue());
		}
	}

	public SolverStats getStats() {
		return stats;
	}

	public IOrder<L> getOrder() {
		return order;
	}

	public void setOrder(IOrder<L> h) {
		order = h;
		order.setLits(voc);
	}

	public L getVocabulary() {
		return voc;
	}

	public void reset() {
		// FIXME verify that cleanup is OK
		voc.resetPool();
		dsfactory.reset();
		constrs.clear();
		learnts.clear();
		stats.reset();
		constrTypes.clear();
	}

	public int nVars() {
		return voc.nVars();
	}

	/**
	 * @param constr
	 * 		a constraint implementing the Constr interface.
	 * @return a reference to the constraint for external use.
	 */
	protected IConstr addConstr(Constr constr) {
		if (constr != null) {
			constrs.push(constr);
			String type = constr.getClass().getName();
			Integer count = constrTypes.get(type);
			if (count == null) {
				constrTypes.put(type, 1);
			} else {
				constrTypes.put(type, count + 1);
			}
		}
		return constr;
	}

	public DataStructureFactory<L> getDSFactory() {
		return dsfactory;
	}

	public IVecInt getOutLearnt() {
		return outLearnt;
	}

	/**
	 * returns the ith constraint in the solver.
	 * 
	 * @param i
	 * 		the constraint number (begins at 0)
	 * @return the ith constraint
	 */
	public IConstr getIthConstr(int i) {
		return constrs.get(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.specs.ISolver#printStat(java.io.PrintStream,
	 * java.lang.String)
	 */
	public void printStat(PrintStream out, String prefix) {
		printStat(new PrintWriter(out), prefix);
	}

	public void printStat(PrintWriter out, String prefix) {
		stats.printStat(out, prefix);
		double cputime = (System.currentTimeMillis() - timebegin) / 1000;
		out.println(prefix
				+ "speed (assignments/second)\t: " + stats.propagations //$NON-NLS-1$
				/ cputime);
		order.printStat(out, prefix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString(String prefix) {
		StringBuffer stb = new StringBuffer();
		Object[] objs = { analyzer, dsfactory, learner, params, order,
				simplifier, restarter };
		stb.append(prefix);
		stb.append("--- Begin Solver configuration ---"); //$NON-NLS-1$
		stb.append("\n"); //$NON-NLS-1$
		for (Object o : objs) {
			stb.append(prefix);
			stb.append(o.toString());
			stb.append("\n"); //$NON-NLS-1$
		}
		stb.append(prefix);
		stb.append("timeout=");
		if (timeBasedTimeout) {
			stb.append(timeout / 1000);
			stb.append("s\n");
		} else {
			stb.append(timeout);
			stb.append(" conflicts\n");
		}
		stb.append(prefix);
		stb.append("DB Simplification allowed=");
		stb.append(isDBSimplificationAllowed);
		stb.append("\n"); 
		stb.append(prefix);
		stb.append("--- End Solver configuration ---"); //$NON-NLS-1$
		return stb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(""); //$NON-NLS-1$
	}

	public int getTimeout() {
		return (int) (timeBasedTimeout ? timeout / 1000 : timeout);
	}

	public void setExpectedNumberOfClauses(int nb) {
		constrs.ensure(nb);
	}

	public Map<String, Number> getStat() {
		return stats.toMap();
	}

	public int[] findModel() throws TimeoutException {
		if (isSatisfiable()) {
			return model();
		}
		// DLB findbugs ok
		// A zero length array would mean that the formula is a tautology.
		return null;
	}

	public int[] findModel(IVecInt assumps) throws TimeoutException {
		if (isSatisfiable(assumps)) {
			return model();
		}
		// DLB findbugs ok
		// A zero length array would mean that the formula is a tautology.
		return null;
	}

	public boolean isDBSimplificationAllowed() {
		return isDBSimplificationAllowed;
	}

	public void setDBSimplificationAllowed(boolean status) {
		isDBSimplificationAllowed = status;
	}

}

class ActivityComparator implements Comparator<Constr>, Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Constr c1, Constr c2) {
		return (int) Math.round(c1.getActivity() - c2.getActivity());
	}
}

interface ConflictTimer {

	void reset();

	void newConflict();
}

abstract class ConflictTimerAdapter implements Serializable, ConflictTimer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int counter;

	private final int bound;

	ConflictTimerAdapter(final int bound) {
		this.bound = bound;
		counter = 0;
	}

	public void reset() {
		counter = 0;
	}

	public void newConflict() {
		counter++;
		if (counter == bound) {
			run();
			counter = 0;
		}
	}

	abstract void run();
}

class ConflictTimerContainer implements Serializable, ConflictTimer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final IVec<ConflictTimer> timers = new Vec<ConflictTimer>();

	ConflictTimerContainer add(ConflictTimer timer) {
		timers.push(timer);
		return this;
	}

	public void reset() {
		Iterator<ConflictTimer> it = timers.iterator();
		while (it.hasNext()) {
			it.next().reset();
		}
	}

	public void newConflict() {
		Iterator<ConflictTimer> it = timers.iterator();
		while (it.hasNext()) {
			it.next().newConflict();
		}
	}
}
