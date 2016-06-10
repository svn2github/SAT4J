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
import org.xcsp.parser.XDomains.XDomInteger;
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to channel constraints.
 * 
 * @author Emmanuel Lonca <lonca@cril.fr>
 *
 */
public class ChannelCtrBuilder {
	
	private IPBSolver solver;
	
	private Map<String, Var> varmapping;

	public ChannelCtrBuilder(IPBSolver solver, Map<String, Var> varmapping) {
		this.solver = solver;
		this.varmapping = varmapping;		
	}
	
	public boolean buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2) {
		if(list1.length != list2.length) {
			throw new IllegalArgumentException("lists of different sizes provided as arguments of channel constraint");
		}
		checkChannelPrerequisites(list1, startIndex2);
		checkChannelPrerequisites(list2, startIndex1);
		boolean contradictionFound = false;
		contradictionFound |= buildListCtrChannel(list2, startIndex2, list1, startIndex1);
		contradictionFound |= buildListCtrChannel(list1, startIndex1, list2, startIndex2);
		return contradictionFound;
	}
	
	public boolean buildCtrChannel(String id, XVarInteger[] list, int startIndex) {
		checkChannelPrerequisites(list, startIndex);
		return buildListCtrChannel(list, startIndex, list, startIndex);
	}
	
	public boolean buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value) {
		boolean contradictionFound = CtrBuilderUtils.buildSumEqOneCstr(this.solver, this.varmapping, list);
		for(int i=0; i<list.length; ++i) {
			XVarInteger var = list[i];
			Predicate p = new Predicate();
			Var[] varArray = new Var[]{varmapping.get(var.id)};
			Vec<Var> scope = new Vec<Var>(varArray);
			Vec<Evaluable> vars = new Vec<Evaluable>(varArray);
			String norm = CtrBuilderUtils.normalizeCspVarName(var.id);
			p.addVariable(norm);
			String expr = "iff(eq("+norm+",1),eq("+value+","+(i+startIndex)+"))";
			p.setExpression(expr);
			try {
				p.toClause(this.solver, scope, vars);
			} catch (ContradictionException e) {
				return true;
			}
		}
		return contradictionFound;
	}
	
	private void checkChannelPrerequisites(XVarInteger[] list, int startIndex) {
		if(startIndex < 0) {
			throw new IllegalArgumentException("negative startIndex ("+startIndex+") given for channel constraint");
		}
		for(XVarInteger var : list) {
			XDomInteger domain = (XDomInteger)(var.dom);
			if(domain.getFirstValue() < startIndex || domain.getLastValue() >= (list.length + startIndex)) {
				throw new IllegalArgumentException("incompatible variable domain in channel constraint");
			}
		}
	}
	
	private boolean buildListCtrChannel(XVarInteger[] list1, int startIndex1,
			XVarInteger[] list2, int startIndex2) {
		boolean contradictionFound = false;
		for(int i=0; i<list2.length; ++i) {
			XVarInteger var = list2[i];
			XDomInteger domain = (XDomInteger)(var.dom);
			for(long j=domain.getFirstValue(); j<=domain.getLastValue(); ++j) {
				if(!domain.contains(j)) {
					continue;
				}
				contradictionFound |= buildChannelImplCstr(var, i+startIndex2, list1[(int)j-startIndex1], (int)j);
			}
		}
		return contradictionFound;
	}
	
	private boolean buildChannelImplCstr(XVarInteger v1, int v1Index, XVarInteger v2, int v2Index) {
		Predicate p = new Predicate();
		Var[] varArray = new Var[]{varmapping.get(v1.id), varmapping.get(v2.id)};
		Vec<Var> scope = new Vec<Var>(varArray);
		Vec<Evaluable> vars = new Vec<Evaluable>(varArray);
		String norm1 = CtrBuilderUtils.normalizeCspVarName(v1.id);
		p.addVariable(norm1);
		String norm2 = CtrBuilderUtils.normalizeCspVarName(v2.id);
		p.addVariable(norm2);
		String expr = "or(not(eq("+norm1+","+v2Index+")),eq("+norm2+","+v1Index+"))";
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}

}
