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

import java.util.Collection;
import java.util.Map;

import org.sat4j.core.Vec;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.Var;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.xcsp.common.Types.TypeOperator;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * Utility class for XCSP3 constraint builders.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class CtrBuilderUtils {
	
	public static XVarInteger[][] transposeMatrix(XVarInteger[][] matrix) {
		XVarInteger[][] tMatrix = new XVarInteger[matrix[0].length][matrix.length];
		for(int i=0; i<matrix[0].length; ++i) {
			for(int j=0; j<matrix.length; ++j) {
				tMatrix[i][j] = matrix[j][i];
			}
		}
		return tMatrix;
	}
	
	public static String normalizeCspVarName(String name) { // TODO: remove unnecessary calls to this method
		return name;
	}
	
	public static IVec<Var> toVarVec(Collection<Var> vars) {
		IVec<Var> vec = new Vec<>(vars.size());
		for(Var v : vars) {
			vec.push(v);
		}
		return vec;
	}
	
	public static IVec<Evaluable> toEvaluableVec(Collection<Var> vars) {
		IVec<Evaluable> vec = new Vec<>(vars.size());
		for(Var v : vars) {
			vec.push(v);
		}
		return vec;
	}
	
	public static String chainExpressionsForAssociativeOp(String[] exprs, String op) {
		StringBuffer exprBuf = new StringBuffer();
		exprBuf.append(op).append('(').append(exprs[0]);
		for(int i=1; i<exprs.length; ++i) exprBuf.append(',').append(exprs[i]);
		exprBuf.append(')');
		return exprBuf.toString();
	}
	
	public static String chainExpressionsAssociative(String[] exprs, String op) {
		StringBuffer exprBuff = new StringBuffer();
		exprBuff.append(exprs[0]);
		for(int i=1; i<exprs.length; ++i) {
			exprBuff.append(op);
			exprBuff.append(exprs[i]);
		}
		return exprBuff.toString();
	}
	
	public static boolean buildSumEqOneCstr(IPBSolver solver, Map<String, Var> varmapping, XVarInteger[] list) {
		Predicate p = new Predicate();
		Vec<Var> scope = new Vec<Var>(list.length);
		Vec<Evaluable> vars = new Vec<Evaluable>(list.length);
		String[] toChain = new String[list.length];
		for(int i=0; i<list.length; ++i) {
			XVarInteger var = list[i];
			scope.push(varmapping.get(var.id));
			vars.push(varmapping.get(var.id));
			String norm = normalizeCspVarName(var.id);
			p.addVariable(norm);
			toChain[i] = norm;
		}
		p.setExpression("eq("+chainExpressionsForAssociativeOp(toChain, "add")+",1)");
		try {
			p.toClause(solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}
	
	public static TypeOperator strictTypeOperator(TypeOperator op) {
		switch(op) {
		case GE: return TypeOperator.GT;
		case LE: return TypeOperator.LT;
		case SUBSEQ: return TypeOperator.SUBSET;
		case SUPSEQ: return TypeOperator.SUPSET;
		default: return op;
		}
	}

}
