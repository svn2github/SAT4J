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
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to noOverlap constraints.
 * 
 * @author Emmanuel Lonca <lonca@cril.fr>
 *
 */
public class NoOverlapCtrBuilder {
	
	private IPBSolver solver;
	
	private Map<String, Var> varmapping;

	public NoOverlapCtrBuilder(IPBSolver solver, Map<String, Var> varmapping) {
		this.solver = solver;
		this.varmapping = varmapping;		
	}
	
	public boolean buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		if(!zeroIgnored) {
			throw new UnsupportedOperationException("not implemented yet: zeroIgnored=false in buildCtrNoOverlap");
		}
		boolean contradictionFound = false;
		for(int i=0; i<origins.length-1; ++i) {
			for(int j=i+1; j<origins.length; ++j) {
				XVarInteger var1 = origins[i];
				XVarInteger var2 = origins[j];
				int length1 = lengths[i];
				int length2 = lengths[j];
				Vec<Var> scope = new Vec<Var>(new Var[]{this.varmapping.get(var1.id), this.varmapping.get(var2.id)});
				Vec<Evaluable> vars = new Vec<Evaluable>(new Evaluable[]{this.varmapping.get(var1.id), this.varmapping.get(var2.id)});
				contradictionFound |= buildDirectionalNoOverlapCstr(var1, var2, length1, scope, vars);
				contradictionFound |= buildDirectionalNoOverlapCstr(var2, var1, length2, scope, vars);
			}
		}
		return contradictionFound;
	}
	
	public boolean buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		boolean contradictionFound = false;
		for(int i=0; i<origins.length; ++i) {
			contradictionFound |= buildCtrNoOverlap(id, origins[i], lengths[i], zeroIgnored);
		}
		return contradictionFound;
	}
	
	private boolean buildDirectionalNoOverlapCstr(XVarInteger var1,
			XVarInteger var2, int length1, Vec<Var> scope, Vec<Evaluable> vars) {
		Predicate p = new Predicate();
		String normalize2 = CtrBuilderUtils.normalizeCspVarName(var2.id);
		p.addVariable(normalize2);
		String normalized1 = CtrBuilderUtils.normalizeCspVarName(var1.id);
		p.addVariable(normalized1);
		String expr = "ge(sub("+normalize2+","+normalized1+"),"+length1+")";
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}

}
