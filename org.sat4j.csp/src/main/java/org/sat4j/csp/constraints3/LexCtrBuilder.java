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
import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
import org.xcsp.parser.XEnums.TypeOperator;
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to lex constraints.
 * 
 * @author Emmanuel Lonca <lonca@cril.fr>
 *
 */
public class LexCtrBuilder {
	
	private IPBSolver solver;
	
	private Map<String, Var> varmapping;

	public LexCtrBuilder(IPBSolver solver, Map<String, Var> varmapping) {
		this.solver = solver;
		this.varmapping = varmapping;		
	}

	public boolean buildCtrLex(String id, XVarInteger[][] lists, TypeOperator operator) {
		boolean contradictionFound = false;
		for(int i=0; i<lists.length-1; ++i) {
			contradictionFound |= buildCtrLex(id, lists[0], lists[1], operator);
		}
		return contradictionFound;
	}
	
	public boolean buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperator operator) {
		boolean contradictionFound = false;
		contradictionFound |= buildCtrLex(id, matrix, operator);
		XVarInteger[][] tMatrix = CtrBuilderUtils.transposeMatrix(matrix);
		contradictionFound |= buildCtrLex(id, tMatrix, operator);
		return contradictionFound;
	}
	
	private boolean buildCtrLex(String id, XVarInteger[] list1,
			XVarInteger[] list2, TypeOperator operator) {
		TypeOperator strictOp = CtrBuilderUtils.strictTypeOperator(operator);
		Predicate p = new Predicate();
		Vec<Var> scope = new Vec<Var>();
		Vec<Evaluable> vars = new Vec<Evaluable>();
		for(int i=0; i<list1.length; ++i) {
			String id01 = list1[i].id;
			String id02 = list2[i].id;
			scope.push(this.varmapping.get(id01));
			scope.push(this.varmapping.get(id02));
			vars.push(this.varmapping.get(id01));
			vars.push(this.varmapping.get(id02));
			p.addVariable(CtrBuilderUtils.normalizeCspVarName(id01));
			p.addVariable(CtrBuilderUtils.normalizeCspVarName(id02));
		}
		String[] chains = new String[list1.length];
		String id01 = list1[0].id;
		String id02 = list2[0].id;
		chains[0] = list1.length == 1
				? operator.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(id01)+","+CtrBuilderUtils.normalizeCspVarName(id02)+")"
				: strictOp.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(id01)+","+CtrBuilderUtils.normalizeCspVarName(id02)+")";
		for(int i=1; i<list1.length; ++i) {
			String eqChain = "eq("+CtrBuilderUtils.normalizeCspVarName(id01)+","+CtrBuilderUtils.normalizeCspVarName(id02)+")";
			for(int j=1; j<i; ++j) {
				String idj1 = list1[j].id;
				String idj2 = list2[j].id;
				eqChain = "and("+eqChain+",eq("+CtrBuilderUtils.normalizeCspVarName(idj1)+","+CtrBuilderUtils.normalizeCspVarName(idj2)+"))";
			}
			String idi1 = list1[i].id;
			String idi2 = list2[i].id;
			String finalMember =  i == list1.length-1
					? operator.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(idi1)+","+CtrBuilderUtils.normalizeCspVarName(idi2)+")"
					: strictOp.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(idi1)+","+CtrBuilderUtils.normalizeCspVarName(idi2)+")";
			chains[i] = "and("+eqChain+","+finalMember+")";
		}
		p.setExpression(CtrBuilderUtils.chainExpressions(chains, "or"));
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}
}
