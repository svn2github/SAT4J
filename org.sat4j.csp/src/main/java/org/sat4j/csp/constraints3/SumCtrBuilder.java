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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sat4j.core.Vec;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.Var;
import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
import org.xcsp.parser.XParser.Condition;
import org.xcsp.parser.XParser.ConditionVal;
import org.xcsp.parser.XParser.ConditionVar;
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to sum constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class SumCtrBuilder {
	
	/** the solver in which the problem is encoded */
	private IPBSolver solver;
	
	/** a mapping from the CSP variable names to Sat4j CSP variables */
	private Map<String, Var> varmapping = new LinkedHashMap<String, Var>();

	public SumCtrBuilder(IPBSolver solver, Map<String, Var> varmapping) {
		this.solver = solver;
		this.varmapping = varmapping;		
	}
	
	public boolean buildCtrSum(String id, XVarInteger[] list, Condition condition) {
		int[] coeffs = new int[list.length];
		Arrays.fill(coeffs, 1);
		return buildCtrSum(id, list, coeffs, condition);
	}

	public boolean buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition) {
		Predicate p = new Predicate();
		Vec<Var> scope = new Vec<Var>();
		Vec<Evaluable> vars = new Vec<Evaluable>();
		String varId;
		StringBuffer exprBuf = new StringBuffer();
		exprBuf.append(condition.operator.toString().toLowerCase());
		exprBuf.append('(');
		for(int i=0; i<list.length-1; ++i) {
			exprBuf.append("add(");
			if(coeffs[i] != 1) {
				exprBuf.append("mul(");
				exprBuf.append(coeffs[i]);
				exprBuf.append(',');
			}
			varId = list[i].id;
			addVarToPredExprBuffer(varId, p, scope, vars, exprBuf);
			if(coeffs[i] != 1) {
				exprBuf.append(')');
			}
			exprBuf.append(',');
		}
		varId = list[list.length-1].id;
		addVarToPredExprBuffer(varId, p, scope, vars, exprBuf);
		for(int i=0; i<list.length-1; ++i) {
			exprBuf.append(')');
		}
		exprBuf.append(',');
		if(condition instanceof ConditionVar) {
			varId = ((ConditionVar) condition).x.id;
			addVarToPredExprBuffer(varId, p, scope, vars, exprBuf);
		} else if(condition instanceof ConditionVal) {
			exprBuf.append(((ConditionVal) condition).k);
		} else {
			throw new UnsupportedOperationException("this kind of condition is not supported yet.");
		}
		exprBuf.append(')');
		String expr = exprBuf.toString();
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}
	
	private void addVarToPredExprBuffer(String varId, Predicate p, Vec<Var> scope,
			Vec<Evaluable> vars, StringBuffer exprBuf) {
		scope.push(this.varmapping.get(varId));
		vars.push(this.varmapping.get(varId));
		String normalizeName = CtrBuilderUtils.normalizeCspVarName(varId);
		p.addVariable(normalizeName);
		exprBuf.append(normalizeName);
	}

}
