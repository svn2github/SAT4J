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

import java.util.LinkedHashMap;
import java.util.Map;

import org.sat4j.core.Vec;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.Var;
import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.xcsp.parser.XNodeExpr;
import org.xcsp.parser.XEnums.TypeArithmeticOperator;
import org.xcsp.parser.XEnums.TypeConditionOperatorRel;
import org.xcsp.parser.XNodeExpr.XNodeLeaf;
import org.xcsp.parser.XNodeExpr.XNodeParent;
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to intension (including "primitive" special cases) constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class IntensionCtrBuilder {
	
	/** the solver in which the problem is encoded */
	private IPBSolver solver;
	
	/** a mapping from the CSP variable names to Sat4j CSP variables */
	private Map<String, Var> varmapping = new LinkedHashMap<String, Var>();

	public IntensionCtrBuilder(IPBSolver solver, Map<String, Var> varmapping) {
		this.solver = solver;
		this.varmapping = varmapping;		
	}
	
	public boolean buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorRel op, int k) {
		String expr = op.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(x.id)+","+k+")";
		IVec<Var> scope = new Vec<Var>(1);
		scope.push(this.varmapping.get(x.id));
		IVec<Evaluable> vars = new Vec<Evaluable>(1);
		vars.push(this.varmapping.get(x.id));
		Predicate p = new Predicate();
		p.addVariable(CtrBuilderUtils.normalizeCspVarName(x.id));
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}
	
	public boolean buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, int k) {
		String expr = op.name().toLowerCase()+"("+opa.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(x.id)+","+CtrBuilderUtils.normalizeCspVarName(y.id)+"),"+k+")";
		Vec<Var> scope = new Vec<Var>(new Var[]{this.varmapping.get(x.id), this.varmapping.get(y.id)});
		Vec<Evaluable> vars = new Vec<Evaluable>(new Evaluable[]{this.varmapping.get(x.id), this.varmapping.get(y.id)});
		Predicate p = new Predicate();
		p.addVariable(CtrBuilderUtils.normalizeCspVarName(x.id));
		p.addVariable(CtrBuilderUtils.normalizeCspVarName(y.id));
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}
	
	public boolean buildCtrIntension(String id, XVarInteger[] xscope, XNodeParent syntaxTreeRoot) {
		syntaxTreeRootToString(syntaxTreeRoot);
		Vec<Var> scope = new Vec<Var>(xscope.length);
		Vec<Evaluable> vars = new Vec<Evaluable>(xscope.length);
		Predicate p = new Predicate();
		for(XVarInteger vxscope : xscope) {
			String strVar = vxscope.toString();
			p.addVariable(CtrBuilderUtils.normalizeCspVarName(strVar));
			scope.push(varmapping.get(strVar));
			vars.push(varmapping.get(strVar));
		}
		String expr = syntaxTreeRootToString(syntaxTreeRoot);
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}
	
	private String syntaxTreeRootToString(XNodeParent syntaxTreeRoot) {
		StringBuffer treeToString = new StringBuffer();
		fillSyntacticStrBuffer(syntaxTreeRoot, treeToString);
		return treeToString.toString();
	}

	private void fillSyntacticStrBuffer(XNodeExpr root,
			StringBuffer treeToString) {
		if(root instanceof XNodeLeaf) {
			treeToString.append(CtrBuilderUtils.normalizeCspVarName(root.toString()));
			return;
		}
		treeToString.append(root.getType().toString().toLowerCase());
		XNodeExpr[] sons = ((XNodeParent) root).sons;
		treeToString.append('(');
		fillSyntacticStrBuffer(sons[0], treeToString);
		for(int i=1; i<sons.length; ++i) {
			treeToString.append(',');
			fillSyntacticStrBuffer(sons[i], treeToString);
		}
		treeToString.append(')');
	}

}
