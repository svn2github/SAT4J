package org.sat4j.csp.constraints3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.Var;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/** 
* @author Emmanuel Lonca - lonca@cril.fr
*/
public class ElementaryCtrBuilder {

	/** the solver in which the problem is encoded */
	private IPBSolver solver;

	/** a mapping from the CSP variable names to Sat4j CSP variables */
	private Map<String, Var> varmapping = new LinkedHashMap<String, Var>();

	private Map<Var, Integer> firstInternalVarMapping;

	public ElementaryCtrBuilder(IPBSolver solver, Map<String, Var> varmapping, Map<Var, Integer> firstInternalVarMapping) {
		this.solver = solver;
		this.varmapping = varmapping;
		this.firstInternalVarMapping = firstInternalVarMapping;
	}
	
	public boolean buildCtrInstantiation(String id, XVarInteger[] list, int[] values) {
		for(int i=0; i<list.length; ++i) {
			IVecInt unitCl = new VecInt(new int[]{CtrBuilderUtils.getDomainVar(list[i], this.firstInternalVarMapping.get(this.varmapping.get(list[i].id)), (long) values[i])});
			try {
				this.solver.addClause(unitCl);
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}

	public boolean buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg) {
		Predicate p = new Predicate();
		SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
		SortedSet<String> strVars = new TreeSet<>();
		int nPos = pos.length;
		StringBuffer exprBuff = new StringBuffer();
		boolean first = true;
		exprBuff.append("or(");
		for(int i=0; i<nPos; ++i) {
			if(!first) {
				exprBuff.append(',');
			} else {
				first = false;
			}
			String var = pos[i].id;
			String normVar = CtrBuilderUtils.normalizeCspVarName(var);
			exprBuff.append(normVar);
			strVars.add(normVar);
			vars.add(this.varmapping.get(var));
		}
		for(int i=0; i<neg.length; ++i) {
			if(!first) {
				exprBuff.append(',');
			} else {
				first = false;
			}
			String var = neg[i].id;
			String normVar = CtrBuilderUtils.normalizeCspVarName(var);
			exprBuff.append("not(");
			exprBuff.append(normVar);
			exprBuff.append(')');
			strVars.add(normVar);
			vars.add(this.varmapping.get(var));
		}
		exprBuff.append(')');
		p.setExpression(exprBuff.toString());
		for(String var : strVars) p.addVariable(var);
		try {
			p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}

}
