package org.sat4j.br4cp;

import org.sat4j.br4cp.AraliaParser.LogicFormulaNode;
import org.sat4j.br4cp.AraliaParser.LogicFormulaNodeType;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IGroupSolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

/**
 * This class is used to encode a tree-formatted formula into a solver.
 * 
 * @author lonca
 * 
 */
public class FormulaToSolver {

	private ConfigVarMap varMap;
	private IGroupSolver solver;

	public FormulaToSolver(IGroupSolver solver, ConfigVarMap varMap) {
		this.solver = solver;
		this.varMap = varMap;
	}

	/**
	 * Encodes a formula into the solver
	 * 
	 * @param formula
	 *            the formula
	 * @param groupId the identifier of the group 
	 */
	public void encode(LogicFormulaNode formula, int groupId) {
		try {
			processNode(this.solver, formula, true,groupId);
		} catch (ContradictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Integer processNode(IGroupSolver solver, LogicFormulaNode formula, int groupId)
			throws ContradictionException {
		IVecInt sonsId = new VecInt(formula.getSons().size());
		if (!isFlatFormula(formula)) {
			for (LogicFormulaNode son : formula.getSons()) {
				sonsId.push(processNode(solver, son,groupId));
			}
		} else {
			for (LogicFormulaNode son : formula.getSons()) {
				sonsId.push(getTermId(solver, son));
			}
		}
		return processFlatFormula(solver, formula, sonsId,groupId);
	}

	private void processNode(IGroupSolver solver, LogicFormulaNode formula,
			boolean isFormulaRoot,int groupId) throws ContradictionException {
		Integer toPropagate = processNode(solver, formula,groupId);
		IVecInt unitCl = new VecInt(1);
		unitCl.push(toPropagate.intValue());
		this.solver.addClause(unitCl,groupId);
	}

	private Integer processFlatFormula(IGroupSolver solver,
			LogicFormulaNode formula, IVecInt sonsId,int groupId)
			throws ContradictionException {
		if (isTerm(formula)) {
			return getTermId(solver, formula);
		}
		int tseitinVar = solver.nextFreeVarId(true);
		if (formula.getNodeType() == LogicFormulaNodeType.CONJ) {
			IVecInt all = new VecInt();
			all.push(tseitinVar);
			for (IteratorInt it = sonsId.iterator(); it.hasNext();) {
				Integer sonId = it.next();
				all.push(-sonId);
				IVecInt cl = new VecInt(2);
				cl.push(sonId);
				cl.push(-tseitinVar);
				solver.addClause(cl,groupId);
			}
			solver.addClause(all);
			return tseitinVar;
		}
		if (formula.getNodeType() == LogicFormulaNodeType.DISJ) {
			IVecInt all = new VecInt();
			all.push(-tseitinVar);
			for (IteratorInt it = sonsId.iterator(); it.hasNext();) {
				Integer sonId = it.next();
				all.push(sonId);
				IVecInt cl = new VecInt(2);
				cl.push(-sonId);
				cl.push(tseitinVar);
				solver.addClause(cl,groupId);
			}
			solver.addClause(all,groupId);
			return tseitinVar;
		}
		throw new IllegalArgumentException();
	}

	private Integer getTermId(IGroupSolver solver, LogicFormulaNode formula) {
		String label = (formula.getNodeType() == LogicFormulaNodeType.TERM) ? (formula
				.getLabel()) : (formula.getSons().iterator().next().getLabel());
		if (!this.varMap.configVarExists(label)) {
			throw new IllegalArgumentException("var \"" + label
					+ "\" has not been defined");
		}
		Integer id = this.varMap.getSolverVar(label);
		return (formula.getNodeType() == LogicFormulaNodeType.TERM) ? (id)
				: (-id);
	}

	private boolean isFlatFormula(LogicFormulaNode formula) {
		if (formula.getNodeType() == LogicFormulaNodeType.CONJ)
			return areAllSonsTerms(formula);
		if (formula.getNodeType() == LogicFormulaNodeType.DISJ)
			return areAllSonsTerms(formula);
		if (formula.getNodeType() == LogicFormulaNodeType.NEG)
			return isTerm(formula.getSons().iterator().next());
		return true;
	}

	private boolean areAllSonsTerms(LogicFormulaNode formula) {
		for (LogicFormulaNode son : formula.getSons()) {
			if (!isTerm(son)) {
				return false;
			}
		}
		return true;
	}

	private boolean isTerm(LogicFormulaNode node) {
		if (node.getNodeType() == LogicFormulaNodeType.CONJ)
			return false;
		if (node.getNodeType() == LogicFormulaNodeType.DISJ)
			return false;
		if (node.getNodeType() == LogicFormulaNodeType.NEG)
			return isTerm(node.getSons().iterator().next());
		return true;
	}

}
