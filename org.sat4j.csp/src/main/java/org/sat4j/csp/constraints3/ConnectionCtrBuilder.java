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
import java.util.SortedSet;
import java.util.TreeSet;

import org.sat4j.core.Vec;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.Var;
import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
import org.xcsp.common.Condition;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to channel constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class ConnectionCtrBuilder {
	
	/** the solver in which the problem is encoded */
	private IPBSolver solver;
	
	/** a mapping from the CSP variable names to Sat4j CSP variables */
	private Map<String, Var> varmapping = new LinkedHashMap<String, Var>();

	public ConnectionCtrBuilder(IPBSolver solver, Map<String, Var> varmapping) {
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
	
	private void checkChannelPrerequisites(XVarInteger[] list, int startIndex) {
		if(startIndex < 0) {
			throw new IllegalArgumentException("negative startIndex ("+startIndex+") given for channel constraint");
		}
	}
	
	private boolean buildListCtrChannel(XVarInteger[] list1, int startIndex1,
			XVarInteger[] list2, int startIndex2) {
		boolean contradictionFound = false;
		for(int i=0; i<list2.length; ++i) {
			XVarInteger var = list2[i];
			XDomInteger domain = (XDomInteger)(var.dom);
			for(long j=domain.getFirstValue(); j<=Math.min(domain.getLastValue(), list1.length+startIndex1-1); ++j) {
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
		Var[] varArray = v1.id == v2.id ? new Var[]{varmapping.get(v1.id)} : new Var[]{varmapping.get(v1.id), varmapping.get(v2.id)};
		Vec<Var> scope = new Vec<Var>(varArray);
		Vec<Evaluable> vars = new Vec<Evaluable>(varArray);
		String norm1 = CtrBuilderUtils.normalizeCspVarName(v1.id);
		p.addVariable(norm1);
		String norm2 = CtrBuilderUtils.normalizeCspVarName(v2.id);
		if(v1 != v2) p.addVariable(norm2);
		String expr = "or(not(eq("+norm1+","+v2Index+")),eq("+norm2+","+v1Index+"))";
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}
	
	public boolean buildCtrMaximum(String id, XVarInteger[] list, Condition condition) {
		return buildCtrMinOrMax(list, condition, true);
	}

	private boolean buildCtrMinOrMax(XVarInteger[] list, Condition condition, boolean isMax) {
		Predicate p = new Predicate();
		SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
		SortedSet<String> strVars = new TreeSet<>();
		for(int i=0; i<list.length; ++i) {
			vars.add(varmapping.get(list[i].id));
		}
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(isMax ? "max(" : "min(");
		String norm = CtrBuilderUtils.normalizeCspVarName(list[0].id);
		sbuf.append(norm);
		strVars.add(norm);
		for(int i=1; i<list.length; ++i) {
			norm = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			sbuf.append(',');
			sbuf.append(norm);
			strVars.add(norm);
		}
		sbuf.append(")");
		StringCondition strCond = StringCondition.buildStringCondition(condition);
		strCond.setPredicateExpression(p, sbuf.toString(), false);
		if(strCond.hasVariables()) {
			for(String var : strCond.getVarIds()) {
				vars.add(varmapping.get(var));
				strVars.add(CtrBuilderUtils.normalizeCspVarName(var));
			}
		}
		for(String var : strVars) p.addVariable(var);
		try {
			p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}

	public boolean buildCtrMinimum(String id, XVarInteger[] list, Condition condition) {
		return buildCtrMinOrMax(list, condition, false);
	}
	
	public boolean buildCtrElement(String id, XVarInteger[] list, int value) {
		return buildCtrElement(id, list, 0, null, TypeRank.ANY, value);
	}
	
	public boolean buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		if(condition != null) {
			if(buildCtrMaximum(id, list, condition)) return true;
		}
		if(buildCtrMinOrMaxAtIndex(list, index, startIndex, true)) return true;
		if(rank == TypeRank.FIRST) {
			if(buildCtrNotMinOrMaxBeforeIndex(list, index, startIndex, true)) return true;
		} else if(rank == TypeRank.LAST) {
			if(buildCtrNotMaxAfterIndex(list, index, startIndex, true)) return true;
		}
		return false;
	}
	
	public boolean buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		if(condition != null) {
			if(buildCtrMinimum(id, list, condition)) return true;
		}
		if(buildCtrMinOrMaxAtIndex(list, index, startIndex, false)) return true;
		if(rank == TypeRank.FIRST) {
			if(buildCtrNotMinOrMaxBeforeIndex(list, index, startIndex, false)) return true;
		} else if(rank == TypeRank.LAST) {
			if(buildCtrNotMaxAfterIndex(list, index, startIndex, false)) return true;
		}
		return false;
	}
	
	private boolean buildCtrMinOrMaxAtIndex(XVarInteger[] list, XVarInteger index, int startIndex, boolean isMax) {
		SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
		SortedSet<String> strVars = new TreeSet<>();
		vars.add(varmapping.get(index.id));
		String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
		strVars.add(normIndex);
		StringBuffer maxExprBuf = new StringBuffer();
		maxExprBuf.append(isMax ? "max(" : "min(");
		for(int i=0; i<list.length; ++i) {
			vars.add(varmapping.get(list[i].id));
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			strVars.add(normVar);
			if(i>0) maxExprBuf.append(',');
			maxExprBuf.append(normVar);
		}
		maxExprBuf.append(')');
		String maxExpr = maxExprBuf.toString();
		for(int i=startIndex; i<startIndex+list.length; ++i) {
			// index==i => max(x0,...,xn)==xi
			String expr = "or(ne("+normIndex+","+i+"),eq("+maxExpr+","+CtrBuilderUtils.normalizeCspVarName(list[i-startIndex].id)+"))";
			Predicate p = new Predicate();
			p.setExpression(expr);
			for(String var : strVars) p.addVariable(var);
			try {
				p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}

	private boolean buildCtrNotMinOrMaxBeforeIndex(XVarInteger[] list, XVarInteger index, int startIndex, boolean isMax) {
		for(int i=1+startIndex; i<startIndex+list.length; ++i) {
			// index==i => and(x0<xi,...,x(i-1)<xi)
			SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
			SortedSet<String> strVars = new TreeSet<>();
			vars.add(varmapping.get(index.id));
			String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
			strVars.add(normIndex);
			vars.add(varmapping.get(list[i-startIndex].id));
			String normMax = CtrBuilderUtils.normalizeCspVarName(list[i-startIndex].id);
			strVars.add(normMax);
			StringBuffer andExprBuf = new StringBuffer();
			andExprBuf.append("and(");
			for(int j=0; j<i-startIndex; ++j) {
				vars.add(varmapping.get(list[j].id));
				String normCurVar = CtrBuilderUtils.normalizeCspVarName(list[j].id);
				strVars.add(normCurVar);
				if(j>0) andExprBuf.append(',');
				andExprBuf.append(isMax ? "lt(" : "gt(").append(normCurVar).append(',').append(normMax).append(')');
			}
			andExprBuf.append(')');
			String expr = "or(ne("+normIndex+","+i+"),"+andExprBuf.toString()+")";
			Predicate p = new Predicate();
			p.setExpression(expr);
			for(String var : strVars) p.addVariable(var);
			try {
				p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}

	private boolean buildCtrNotMaxAfterIndex(XVarInteger[] list, XVarInteger index, int startIndex, boolean isMax) {
		for(int i=startIndex; i<startIndex+list.length-1; ++i) {
			// index==i => and(x(i+1)<xi,...,xn<xi)
			SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
			SortedSet<String> strVars = new TreeSet<>();
			vars.add(varmapping.get(index.id));
			String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
			strVars.add(normIndex);
			vars.add(varmapping.get(list[i-startIndex].id));
			String normMax = CtrBuilderUtils.normalizeCspVarName(list[i-startIndex].id);
			strVars.add(normMax);
			StringBuffer andExprBuf = new StringBuffer();
			andExprBuf.append("and(");
			for(int j=i-startIndex+1; j<list.length; ++j) {
				vars.add(varmapping.get(list[j].id));
				String normCurVar = CtrBuilderUtils.normalizeCspVarName(list[j].id);
				strVars.add(normCurVar);
				if(j>i-startIndex+1) andExprBuf.append(',');
				andExprBuf.append(isMax ? "lt(" : "gt(").append(normCurVar).append(',').append(normMax).append(')');
			}
			andExprBuf.append(')');
			String expr = "or(ne("+normIndex+","+i+"),"+andExprBuf.toString()+")";
			Predicate p = new Predicate();
			p.setExpression(expr);
			for(String var : strVars) p.addVariable(var);
			try {
				p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}

	public boolean buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value) {
		if(rank == TypeRank.ANY) {
			return buildCtrElementAnyIndex(list, value);
		} else if(rank == TypeRank.FIRST) {
			return buildCtrElementNotAnyIndex(list, startIndex, index, value, true);
		} else if(rank == TypeRank.LAST) {
			return buildCtrElementNotAnyIndex(list, startIndex, index, value, false);
		}
		throw new IllegalArgumentException();
	}

	private boolean buildCtrElementAnyIndex(XVarInteger[] list, int value) {
		Predicate p = new Predicate();
		SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
        SortedSet<String> strVars = new TreeSet<>();
		for(int i=0; i<list.length; ++i) {
			vars.add(varmapping.get(list[i].id));
		}
		int len = list.length;
		String[] subExprs = new String[len];
		String strValue = Integer.toString(value);
		for(int i=0; i<len; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			strVars.add(normVar);
			subExprs[i] = "eq("+normVar+","+strValue+")";
		}
		p.setExpression(CtrBuilderUtils.chainExpressions(subExprs, "or"));
		for(String var : strVars) p.addVariable(var);
		try {
			p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
		} catch (ContradictionException e) {
			return true;
		}
        return false;
	}

	private boolean buildCtrElementNotAnyIndex(XVarInteger[] list, int startIndex, XVarInteger index, int value, boolean isFirst) {
		Predicate p = new Predicate();
		Var[] varArray = new Var[list.length+1];
		for(int i=0; i<list.length; ++i) {
			varArray[i] = varmapping.get(list[i].id);
		}
		varArray[varArray.length-1] = varmapping.get(index);
		Vec<Var> scope = new Vec<Var>(varArray);
		Vec<Evaluable> vars = new Vec<Evaluable>(varArray);
		int len = list.length;
		String[] subExprs = new String[len];
		String strValue = Integer.toString(value);
		String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
		for(int i=0; i<len; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			String firstOp = isFirst ? "le" : "ge";
			String lowerIndexCase = "and("+firstOp+"("+Integer.toString(i+startIndex)+","+normIndex+"),ne("+normVar+","+strValue+"))";
			p.addVariable(normIndex);
			p.addVariable(normVar);
			String rightIndexCase = "and(eq("+Integer.toString(i+startIndex)+","+normIndex+"),eq("+normVar+","+strValue+"))";
			p.addVariable(normIndex);
			p.addVariable(normVar);
			String lastOp = isFirst ? "ge" : "le";
			String higherIndexCase = lastOp+"("+Integer.toString(i+startIndex)+","+normIndex+")";
			p.addVariable(normIndex);
			subExprs[i] = "or("+lowerIndexCase+","+rightIndexCase+","+higherIndexCase+")";
		}
		p.setExpression(CtrBuilderUtils.chainExpressions(subExprs, "and"));
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}

	public boolean buildCtrElement(String id, XVarInteger[] list, XVarInteger value) {
		return buildCtrElement(id, list, 0, null, TypeRank.ANY, value);
	}

	public boolean buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		if(rank == TypeRank.ANY) {
			return buildCtrElementAnyIndex(list, value);
		} else if(rank == TypeRank.FIRST) {
			return buildCtrElementFirstIndex(list, startIndex, index, value);
		} else if(rank == TypeRank.LAST) {
			return buildCtrElementLastIndex(list, startIndex, index, value);
		}
		throw new IllegalArgumentException();
	}
	
	private boolean buildCtrElementAnyIndex(XVarInteger[] list, XVarInteger value) {
		Predicate p = new Predicate();
		SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
		SortedSet<String> strVars = new TreeSet<>();
		for(int i=0; i<list.length; ++i) {
			vars.add(varmapping.get(list[i].id));
			strVars.add(CtrBuilderUtils.normalizeCspVarName(list[i].id));
		}
		vars.add(varmapping.get(value.id));
		strVars.add(CtrBuilderUtils.normalizeCspVarName(value.id));
		int len = list.length;
		String[] subExprs = new String[len];
		String normValue = CtrBuilderUtils.normalizeCspVarName(value.id);
		for(int i=0; i<len; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			subExprs[i] = "eq("+normVar+","+normValue+")";
		}
		p.setExpression(CtrBuilderUtils.chainExpressions(subExprs, "or"));
		for(String var : strVars) p.addVariable(var);
		try {
			p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}
	
	private boolean buildCtrElementFirstIndex(XVarInteger[] list, int startIndex, XVarInteger index, XVarInteger value) {
		for(int i=0; i<list.length; ++i) {
			// or(ne(i,index),and(eq(i,index),eq(xi,value),ne(x0,value),...,ne(xi-1,value)))
			Predicate p = new Predicate();
			SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
            SortedSet<String> strVars = new TreeSet<>();
			for(int j=0; j<=i; ++j) {
				vars.add(varmapping.get(list[j].id));
				strVars.add(CtrBuilderUtils.normalizeCspVarName(list[j].id));
			}
			vars.add(varmapping.get(index.id));
			vars.add(varmapping.get(value.id));
			String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
			strVars.add(normIndex);
			String normValue = CtrBuilderUtils.normalizeCspVarName(value.id);
			strVars.add(normValue);
			StringBuffer sbuf = new StringBuffer();
			sbuf.append("or(ne(");
			sbuf.append(i+startIndex);
			sbuf.append(',');
			sbuf.append(normIndex);
			sbuf.append(')'); // end NE operator
			sbuf.append(",and(eq(");
			sbuf.append(i+startIndex);
			sbuf.append(',');
			sbuf.append(normIndex);
			sbuf.append(')'); // end EQ1 operator
			sbuf.append(",eq(");
			sbuf.append(CtrBuilderUtils.normalizeCspVarName(list[i].id));
			sbuf.append(',');
			sbuf.append(normValue);
			sbuf.append(')'); // end EQ2 operator
			for(int j=0; j<i; ++j) {
				sbuf.append(',');
				sbuf.append("ne(");
				sbuf.append(CtrBuilderUtils.normalizeCspVarName(list[j].id));
				sbuf.append(',');
				sbuf.append(normValue);
				sbuf.append(')'); // end NE operator
			}
			sbuf.append("))");
			p.setExpression(sbuf.toString());
			for(String var : strVars) p.addVariable(var);
            try {
                    p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
            } catch (ContradictionException e) {
                    return true;
            }
		}
		return false;
	}
	
	private boolean buildCtrElementLastIndex(XVarInteger[] list, int startIndex, XVarInteger index, XVarInteger value) {
		for(int i=0; i<list.length; ++i) {
			// or(ne(i,index),and(eq(i,index),eq(xi,value),ne(xi+1,value),...,ne(xn,value)))
			Predicate p = new Predicate();
			SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
            SortedSet<String> strVars = new TreeSet<>();
			for(int j=i; j<list.length; ++j) {
				vars.add(varmapping.get(list[j].id));
				strVars.add(CtrBuilderUtils.normalizeCspVarName(list[j].id));
			}
			vars.add(varmapping.get(index.id));
			vars.add(varmapping.get(value.id));
			String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
			strVars.add(normIndex);
			String normValue = CtrBuilderUtils.normalizeCspVarName(value.id);
			strVars.add(normValue);
			StringBuffer sbuf = new StringBuffer();
			sbuf.append("or(ne(");
			sbuf.append(i+startIndex);
			sbuf.append(',');
			sbuf.append(normIndex);
			sbuf.append(')'); // end NE operator
			sbuf.append(",and(eq(");
			sbuf.append(i+startIndex);
			sbuf.append(',');
			sbuf.append(normIndex);
			sbuf.append(')'); // end EQ1 operator
			sbuf.append(",eq(");
			sbuf.append(CtrBuilderUtils.normalizeCspVarName(list[i].id));
			sbuf.append(',');
			sbuf.append(normValue);
			sbuf.append(')'); // end EQ2 operator
			for(int j=i+1; j<list.length; ++j) {
				sbuf.append(',');
				sbuf.append("ne(");
				sbuf.append(CtrBuilderUtils.normalizeCspVarName(list[j].id));
				sbuf.append(',');
				sbuf.append(normValue);
				sbuf.append(')'); // end NE operator
			}
			sbuf.append("))");
			p.setExpression(sbuf.toString());
			for(String var : strVars) p.addVariable(var);
            try {
                    p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
            } catch (ContradictionException e) {
                    return true;
            }
		}
		return false;
	}

}
