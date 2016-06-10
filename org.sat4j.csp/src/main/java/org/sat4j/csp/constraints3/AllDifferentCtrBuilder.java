/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2016 Daniel Le Berre
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
package org.sat4j.csp.constraints3;

import java.util.Map;

import org.sat4j.core.Vec;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.Var;
import org.sat4j.csp.constraints.AllDiffCard;
import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to allDifferent constraints.
 * 
 * @author Emmanuel Lonca <lonca@cril.fr>
 *
 */
public class AllDifferentCtrBuilder {
	
	private IPBSolver solver;
	
	private Map<String, Var> varmapping;

	public AllDifferentCtrBuilder(IPBSolver solver, Map<String, Var> varmapping) {
		this.solver = solver;
		this.varmapping = varmapping;		
	}
	
	public boolean buildCtrAllDifferent(String id, XVarInteger[] list) {
		Vec<Var> scope = new Vec<Var>(list.length);
		Vec<Evaluable> vars = new Vec<Evaluable>(list.length);
		for(XVarInteger vxscope : list) {
			String strVar = vxscope.toString();
			scope.push(varmapping.get(strVar));
			vars.push(varmapping.get(strVar));
		}
		AllDiffCard card = new AllDiffCard();
		try {
			card.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}
	
	public boolean buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		boolean contradictionFound = false;
		for(XVarInteger[] list : lists) {
			contradictionFound |= buildCtrAllDifferent(id, list);
		}
		return contradictionFound;
	}

	public boolean buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		if(except.length == 0) {
			return buildCtrAllDifferent(id, list);
		}
		Vec<Var> scope = new Vec<Var>(list.length);
		Vec<Evaluable> vars = new Vec<Evaluable>(list.length);
		Predicate p = new Predicate();
		String exceptBase = "eq(X,"+except[0]+")";
		for(int i=1; i<except.length; ++i) {
			exceptBase = "or("+exceptBase+",eq(X,"+except[i]+"))";
		}
		String[] exprs = new String[list.length-1];
		for(int i=0; i<list.length-1; ++i) {
			scope.push(varmapping.get(list[i].id));
			vars.push(varmapping.get(list[i].id));
			String normalizedCurVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			p.addVariable(normalizedCurVar);
			String exceptExpr = exceptBase.replaceAll("X", normalizedCurVar);
			String neExpr = "ne("+normalizedCurVar+","+CtrBuilderUtils.normalizeCspVarName(list[i+1].id)+")";
			for(int j=i+2; j<list.length; ++j) {
				neExpr = "and("+neExpr+",ne("+normalizedCurVar+","+CtrBuilderUtils.normalizeCspVarName(list[j].id)+"))";
			}
			exprs[i] = "or("+exceptExpr+","+neExpr+")";
		}
		XVarInteger lastVar = list[list.length-1];
		scope.push(varmapping.get(lastVar.id));
		vars.push(varmapping.get(lastVar.id));
		String normalizedCurVar = CtrBuilderUtils.normalizeCspVarName(lastVar.id);
		p.addVariable(normalizedCurVar);
		p.setExpression(CtrBuilderUtils.chainExpressions(exprs, "and"));
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}

	public boolean buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		boolean contradictionFound = false;
		XVarInteger[][] tMatrix = CtrBuilderUtils.transposeMatrix(matrix);
		for(int i=0; i<matrix.length; ++i) {
			contradictionFound |= buildCtrAllDifferent(id, matrix[i]);
			contradictionFound |= buildCtrAllDifferent(id, tMatrix[i]);
		}
		return contradictionFound;
	}

}
