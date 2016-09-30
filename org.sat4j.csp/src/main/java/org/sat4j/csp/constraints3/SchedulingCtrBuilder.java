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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.xcsp.parser.XDomains.XDomInteger;
import org.xcsp.parser.XParser.Condition;
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to noOverlap constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class SchedulingCtrBuilder {

	/** the solver in which the problem is encoded */
	private IPBSolver solver;

	/** a mapping from the CSP variable names to Sat4j CSP variables */
	private Map<String, Var> varmapping = new LinkedHashMap<String, Var>();

	public SchedulingCtrBuilder(IPBSolver solver, Map<String, Var> varmapping) {
		this.solver = solver;
		this.varmapping = varmapping;		
	}
	
	public boolean buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax) {
		preventVarMultipleOccurrences(list);
		for(int valueIndex=0; valueIndex<values.length; ++valueIndex) {
			int widthMin = widthsMin[valueIndex];
			for(int stretchBeginIndex=0; stretchBeginIndex<list.length; ++stretchBeginIndex) {
				if(preventUnderLength(values[valueIndex], list, stretchBeginIndex, widthMin)) {
					return true;
				}
			}
			int widthMax = widthsMax[valueIndex];
			for(int stretchBeginIndex=0; stretchBeginIndex<list.length; ++stretchBeginIndex) {
				if(preventOverLength(values[valueIndex], list, stretchBeginIndex, widthMax)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void preventVarMultipleOccurrences(XVarInteger[] list) {
		for(int i=0; i<list.length-1; ++i) {
			for(int j=i+1; j<list.length; ++j) {
				if(list[i].id.equals(list[j].id)) {
					throw new IllegalArgumentException("Variables in //stretch/list may occur only once");
				}
			}
		}
	}

	private boolean preventOverLength(int value, XVarInteger[] list, int stretchBeginIndex, int widthMax) {
		// trivial case
		if(stretchBeginIndex+widthMax >= list.length) return false;
		// (l[i] == value && l[i]-1 != value) => (l[i+1] != value || ... || l[i+max] != value)
		Predicate p = new Predicate();
		SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
		SortedSet<String> strVars = new TreeSet<>();
		for(int i=Math.max(0, stretchBeginIndex-1); i<=stretchBeginIndex+widthMax; ++i) {
			strVars.add(CtrBuilderUtils.normalizeCspVarName(list[i].id));
			vars.add(varmapping.get(list[i].id));
		}
		StringBuffer sb = new StringBuffer();
		sb.append("ifThen(");
		// COND part
		if(stretchBeginIndex > 0) {
			sb.append("and(");
		}
		sb.append("eq(");
		sb.append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex].id));
		sb.append(',');
		sb.append(value);
		sb.append(')'); // end EQ
		if(stretchBeginIndex > 0) {
			sb.append(','); // comma EQ
			sb.append("ne(");
			sb.append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex-1].id));
			sb.append(',');
			sb.append(value);
			sb.append(')'); // end NE
			sb.append(')'); // end AND
		}
		sb.append(','); // comma of IFF
		// THEN part
		sb.append("or(");
		for(int i=stretchBeginIndex+1; i<=stretchBeginIndex+widthMax; ++i) {
			if(i>stretchBeginIndex+1) sb.append(',');
			sb.append("ne(");
			sb.append(CtrBuilderUtils.normalizeCspVarName(list[i].id));
			sb.append(',');
			sb.append(value);
			sb.append(')');
		}
		sb.append(')'); // end OR
		sb.append(')'); // end IFTHEN
		p.setExpression(sb.toString());
		for(String var : strVars) p.addVariable(var);
		try {
			p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
		} catch(ContradictionException e) {
			return true;
		}
		return false;
	}
	
	private boolean preventUnderLength(int value, XVarInteger[] list, int stretchBeginIndex, int widthMin) {
		// trivial case
		if(widthMin == 1) return false;
		// not enough length case
		if(list.length-stretchBeginIndex < widthMin) {
			return preventUnderLengthDueToBeginIndex(value, list, stretchBeginIndex);
		}
		// (l[i] == value && l[i]-1 != value) => (l[i+1] == value && ... && l[i+min-1] == value)
		Predicate p = new Predicate();
		SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
		SortedSet<String> strVars = new TreeSet<>();
		StringBuffer sb = new StringBuffer();
		sb.append("ifThen(");
		// COND part
		if(stretchBeginIndex > 0) {
			sb.append("and(");
		}
		sb.append("eq(");
		sb.append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex].id));
		vars.add(varmapping.get(list[stretchBeginIndex].id));
		strVars.add(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex].id));
		sb.append(',');
		sb.append(value);
		sb.append(')'); // end EQ
		if(stretchBeginIndex > 0) {
			sb.append(',');
			sb.append("ne(");
			sb.append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex-1].id));
			vars.add(varmapping.get(list[stretchBeginIndex-1].id));
			strVars.add(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex-1].id));
			sb.append(',');
			sb.append(value);
			sb.append(')'); // end NE
			sb.append(')'); // end AND
		}
		sb.append(','); // comma of IFF
		// THEN part
		sb.append("and(");
		for(int i=stretchBeginIndex+1; i<=stretchBeginIndex+widthMin-1; ++i) {
			if(i>stretchBeginIndex+1) sb.append(',');
			sb.append("eq(");
			sb.append(CtrBuilderUtils.normalizeCspVarName(list[i].id));
			vars.add(varmapping.get(list[i].id));
			strVars.add(CtrBuilderUtils.normalizeCspVarName(list[i].id));
			sb.append(',');
			sb.append(value);
			sb.append(')');
		}
		sb.append(')'); // end AND
		sb.append(')'); // end IFTHEN
		p.setExpression(sb.toString());
		for(String var : strVars) p.addVariable(var);
		try {
			p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
		} catch(ContradictionException e) {
			return true;
		}
		return false;
	}

	private boolean preventUnderLengthDueToBeginIndex(int value, XVarInteger[] list, int stretchBeginIndex) {
		Predicate p = new Predicate();
		SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
		SortedSet<String> strVars = new TreeSet<>();
		StringBuffer sb = new StringBuffer();
		for(int i=Math.max(0, stretchBeginIndex-1); i<=stretchBeginIndex; ++i) {
			strVars.add(CtrBuilderUtils.normalizeCspVarName(list[i].id));
			vars.add(varmapping.get(list[i].id));
		}
		if(stretchBeginIndex > 0) {
			sb.append("or(");
		}
		sb.append("ne(");
		sb.append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex].id));
		sb.append(',');
		sb.append(value);
		sb.append(')'); // end NE
		if(stretchBeginIndex > 0) {
			sb.append(',');
			sb.append("eq(");
			sb.append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex-1].id));
			sb.append(',');
			sb.append(value);
			sb.append(')'); // end EQ
			sb.append(')'); // end OR
		}
		p.setExpression(sb.toString());
		for(String var : strVars) p.addVariable(var);
		try {
			p.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), CtrBuilderUtils.toEvaluableVec(vars));
		} catch(ContradictionException e) {
			return true;
		}
		return false;
	}

	public boolean buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns) {
		if(buildCtrStretch(id, list, values, widthsMin, widthsMax)) {
			return true;
		}
		if(buildCtrStretchPatterns(list, patterns)) {
			return true;
		}
		return false;
	}

	private boolean buildCtrStretchPatterns(XVarInteger[] list, int[][] patterns) {
		for(int i=0; i<list.length-1; ++i) {
			Var[] varArray = new Var[]{varmapping.get(list[i].id), varmapping.get(list[i+1].id)};
			Vec<Var> scope = new Vec<Var>(varArray);
			Vec<Evaluable> vars = new Vec<Evaluable>(varArray);
			Predicate p = new Predicate();
			String normVar1 = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			p.addVariable(normVar1);
			String normVar2 = CtrBuilderUtils.normalizeCspVarName(list[i+1].id);
			p.addVariable(normVar2);
			String predExprCond = "ne("+normVar1+","+normVar2+")";
			StringBuffer predExprImplBuf = new StringBuffer();
			predExprImplBuf.append("or(");
			predExprImplBuf.append("and(eq("+normVar1+","+Integer.toString(patterns[0][0])+"),eq("+normVar2+","+Integer.toString(patterns[0][1])+"))");
			for(int j=1; j<patterns.length; ++j) {
				predExprImplBuf.append(',');
				predExprImplBuf.append("and(eq("+normVar1+","+Integer.toString(patterns[j][0])+"),eq("+normVar2+","+Integer.toString(patterns[j][1])+"))");
			}
			predExprImplBuf.append(')');
			String predExpr = "iff("+predExprCond+","+predExprImplBuf.toString()+")";
			p.setExpression(predExpr);
			try {
				p.toClause(this.solver, scope, vars);
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}
	
	public boolean buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition) {
		int maxT = computeMaxT(origins, lengths);
		for(int t=0; t<maxT; ++t) {
			Predicate p = new Predicate();
			StringBuffer exprBuff = new StringBuffer();
			exprBuff.append("add(");
			builtCtrCumulativeHeightComp(origins, 0, lengths, t, heights, p, exprBuff);
			for(int i=1; i<origins.length; ++i) {
				exprBuff.append(',');
				builtCtrCumulativeHeightComp(origins, i, lengths, t, heights, p, exprBuff);
			}
			exprBuff.append(')');
			StringCondition strCond = StringCondition.buildStringCondition(condition);
			strCond.setPredicateExpression(p, exprBuff.toString());
			Var[] varArray = new Var[origins.length];
			for(int i=0; i<origins.length; ++i) {
				varArray[i] = this.varmapping.get(origins[i].id);
			}
			try {
				p.toClause(solver, new Vec<Var>(varArray), new Vec<Evaluable>(varArray));
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}

	private void builtCtrCumulativeHeightComp(XVarInteger[] origins, int originIndex, int[] lengths, int t,
			int[] heights, Predicate p, StringBuffer exprBuff) {
		exprBuff.append("ite(");
		buildCtrCumulativeHeightCompCondition(origins, originIndex, lengths, t, p, exprBuff);
		exprBuff.append(',');
		exprBuff.append(Integer.toString(heights[originIndex]));
		exprBuff.append(",0)");
	}
	
	private void builtCtrCumulativeHeightComp(XVarInteger[] origins, int originIndex, int[] lengths, int t,
			XVarInteger[] heights, Predicate p, StringBuffer exprBuff) {
		exprBuff.append("ite(");
		buildCtrCumulativeHeightCompCondition(origins, originIndex, lengths, t, p, exprBuff);
		exprBuff.append(',');
		String normVar = CtrBuilderUtils.normalizeCspVarName(heights[originIndex].id);
		exprBuff.append(normVar);
		p.addVariable(normVar);
		exprBuff.append(",0)");
	}
	
	private void builtCtrCumulativeHeightComp(XVarInteger[] origins, int originIndex, XVarInteger[] lengths, int t,
			int[] heights, Predicate p, StringBuffer exprBuff) {
		exprBuff.append("ite(");
		buildCtrCumulativeHeightCompCondition(origins, originIndex, lengths, t, p, exprBuff);
		exprBuff.append(',');
		exprBuff.append(Integer.toString(heights[originIndex]));
		exprBuff.append(",0)");
	}
	
	private void builtCtrCumulativeHeightComp(XVarInteger[] origins, int originIndex, XVarInteger[] lengths, int t,
			XVarInteger[] heights, Predicate p, StringBuffer exprBuff) {
		exprBuff.append("ite(");
		buildCtrCumulativeHeightCompCondition(origins, originIndex, lengths, t, p, exprBuff);
		exprBuff.append(',');
		String normVar = CtrBuilderUtils.normalizeCspVarName(heights[originIndex].id);
		exprBuff.append(normVar);
		p.addVariable(normVar);
		exprBuff.append(",0)");
	}

	private void buildCtrCumulativeHeightCompCondition(XVarInteger[] origins, int originIndex, int[] lengths, int t,
			Predicate p, StringBuffer exprBuff) {
		// and(le(x,t),gt(add(x,l),t))
		exprBuff.append("and(");
		exprBuff.append("le(");
		String normVar = CtrBuilderUtils.normalizeCspVarName(origins[originIndex].id);
		exprBuff.append(normVar);
		p.addVariable(normVar);
		exprBuff.append(',');
		exprBuff.append(Integer.toString(t));
		exprBuff.append(')'); // end LE operator
		exprBuff.append(',');
		exprBuff.append("gt(");
		exprBuff.append("add(");
		exprBuff.append(normVar);
		p.addVariable(normVar);
		exprBuff.append(',');
		exprBuff.append(Integer.toString(lengths[originIndex]));
		exprBuff.append(')'); // end ADD operator
		exprBuff.append(',');
		exprBuff.append(Integer.toString(t));
		exprBuff.append(')'); // end GT operator
		exprBuff.append(')'); // end AND operator
	}

	private void buildCtrCumulativeHeightCompCondition(XVarInteger[] origins, int originIndex, XVarInteger[] lengths,
			int t, Predicate p, StringBuffer exprBuff) {
		// and(le(x,t),gt(add(x,l),t))
		exprBuff.append("and(");
		exprBuff.append("le(");
		String normVar = CtrBuilderUtils.normalizeCspVarName(origins[originIndex].id);
		exprBuff.append(normVar);
		p.addVariable(normVar);
		exprBuff.append(',');
		exprBuff.append(Integer.toString(t));
		exprBuff.append(')'); // end LE operator
		exprBuff.append(',');
		exprBuff.append("gt(");
		exprBuff.append("add(");
		exprBuff.append(normVar);
		p.addVariable(normVar);
		exprBuff.append(',');
		String normLength = CtrBuilderUtils.normalizeCspVarName(lengths[originIndex].id);
		exprBuff.append(normLength);
		p.addVariable(normLength);
		exprBuff.append(')'); // end ADD operator
		exprBuff.append(',');
		exprBuff.append(Integer.toString(t));
		exprBuff.append(')'); // end GT operator
		exprBuff.append(')'); // end AND operator
	}

	private int computeMaxT(XVarInteger[] origins, int[] heights) {
		int maxT = Integer.MIN_VALUE;
		for(int i=0; i<origins.length; ++i) {
			maxT = (int) Math.max(maxT, ((XDomInteger) origins[i].dom).getLastValue() + heights[i]);
		}
		return maxT;
	}
	
	private int computeMaxT(XVarInteger[] origins, XVarInteger[] lengths) {
		int maxT = Integer.MIN_VALUE;
		for(int i=0; i<origins.length; ++i) {
			maxT = (int) Math.max(maxT, ((XDomInteger) origins[i].dom).getLastValue() + ((XDomInteger) lengths[i].dom).getLastValue());
		}
		return maxT;
	}

	public boolean buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition) {
		int maxT = computeMaxT(origins, lengths);
		for(int t=0; t<maxT; ++t) {
			Predicate p = new Predicate();
			StringBuffer exprBuff = new StringBuffer();
			exprBuff.append("add(");
			builtCtrCumulativeHeightComp(origins, 0, lengths, t, heights, p, exprBuff);
			for(int i=1; i<origins.length; ++i) {
				exprBuff.append(',');
				builtCtrCumulativeHeightComp(origins, i, lengths, t, heights, p, exprBuff);
			}
			exprBuff.append(')');
			StringCondition strCond = StringCondition.buildStringCondition(condition);
			strCond.setPredicateExpression(p, exprBuff.toString());
			Var[] varArray = new Var[origins.length*2];
			for(int i=0; i<origins.length; ++i) {
				varArray[2*i] = this.varmapping.get(origins[i].id);
				varArray[(2*i)+1] = this.varmapping.get(heights[i].id);
			}
			try {
				p.toClause(solver, new Vec<Var>(varArray), new Vec<Evaluable>(varArray));
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}

	public boolean buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition) {
		int maxT = computeMaxT(origins, lengths);
		for(int t=0; t<maxT; ++t) {
			Predicate p = new Predicate();
			StringBuffer exprBuff = new StringBuffer();
			exprBuff.append("add(");
			builtCtrCumulativeHeightComp(origins, 0, lengths, t, heights, p, exprBuff);
			for(int i=1; i<origins.length; ++i) {
				exprBuff.append(',');
				builtCtrCumulativeHeightComp(origins, i, lengths, t, heights, p, exprBuff);
			}
			exprBuff.append(')');
			StringCondition strCond = StringCondition.buildStringCondition(condition);
			strCond.setPredicateExpression(p, exprBuff.toString());
			Var[] varArray = new Var[origins.length*2];
			for(int i=0; i<origins.length; ++i) {
				varArray[2*i] = this.varmapping.get(origins[i].id);
				varArray[(2*i)+1] = this.varmapping.get(lengths[i].id);
			}
			try {
				p.toClause(solver, new Vec<Var>(varArray), new Vec<Evaluable>(varArray));
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}

	public boolean buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition) {
		int maxT = computeMaxT(origins, lengths);
		for(int t=0; t<maxT; ++t) {
			Predicate p = new Predicate();
			StringBuffer exprBuff = new StringBuffer();
			exprBuff.append("add(");
			builtCtrCumulativeHeightComp(origins, 0, lengths, t, heights, p, exprBuff);
			for(int i=1; i<origins.length; ++i) {
				exprBuff.append(',');
				builtCtrCumulativeHeightComp(origins, i, lengths, t, heights, p, exprBuff);
			}
			exprBuff.append(')');
			StringCondition strCond = StringCondition.buildStringCondition(condition);
			strCond.setPredicateExpression(p, exprBuff.toString());
			Var[] varArray = new Var[origins.length*3];
			for(int i=0; i<origins.length; ++i) {
				varArray[3*i] = this.varmapping.get(origins[i].id);
				varArray[(3*i)+1] = this.varmapping.get(lengths[i].id);
				varArray[(3*i)+2] = this.varmapping.get(heights[i].id);
			}
			try {
				p.toClause(solver, new Vec<Var>(varArray), new Vec<Evaluable>(varArray));
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}

	public boolean buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		if(buildCtrCumulative(id, origins, lengths, heights, condition)) return true;
		if(buildCtrCumulativeEnds(origins, lengths, ends)) return true;
		return false;
	}

	private boolean buildCtrCumulativeEnds(XVarInteger[] origins, int[] lengths, XVarInteger[] ends) {
		for(int i=0; i<origins.length; ++i) {
			Predicate p = new Predicate();
			StringBuffer sbuff = new StringBuffer();
			sbuff.append("eq(add(");
			String normVar = CtrBuilderUtils.normalizeCspVarName(origins[i].id);
			sbuff.append(normVar);
			p.addVariable(normVar);
			sbuff.append(',');
			sbuff.append(Integer.toString(lengths[i]));
			sbuff.append(')');
			sbuff.append(',');
			String normEnd = CtrBuilderUtils.normalizeCspVarName(ends[i].id);
			sbuff.append(normEnd);
			p.addVariable(normEnd);
			sbuff.append(')');
			p.setExpression(sbuff.toString());
			Var[] varArray = new Var[]{this.varmapping.get(origins[i].id), this.varmapping.get(ends[i].id)};
			try {
				p.toClause(solver, new Vec<Var>(varArray), new Vec<Evaluable>(varArray));
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}
	
	
	private boolean buildCtrCumulativeEnds(XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends) {
		for(int i=0; i<origins.length; ++i) {
			Predicate p = new Predicate();
			StringBuffer sbuff = new StringBuffer();
			sbuff.append("eq(add(");
			String normVar = CtrBuilderUtils.normalizeCspVarName(origins[i].id);
			sbuff.append(normVar);
			p.addVariable(normVar);
			sbuff.append(',');
			String normLength = CtrBuilderUtils.normalizeCspVarName(lengths[i].id);
			sbuff.append(normLength);
			p.addVariable(normLength);
			sbuff.append(')');
			sbuff.append(',');
			String normEnd = CtrBuilderUtils.normalizeCspVarName(ends[i].id);
			sbuff.append(normEnd);
			p.addVariable(normEnd);
			sbuff.append(')');
			p.setExpression(sbuff.toString());
			Var[] varArray = new Var[]{this.varmapping.get(origins[i].id), this.varmapping.get(ends[i].id)};
			try {
				p.toClause(solver, new Vec<Var>(varArray), new Vec<Evaluable>(varArray));
			} catch (ContradictionException e) {
				return true;
			}
		}
		return false;
	}

	public boolean buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		if(buildCtrCumulative(id, origins, lengths, heights, condition)) return true;
		if(buildCtrCumulativeEnds(origins, lengths, ends)) return true;
		return false;
	}

	public boolean buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		if(buildCtrCumulative(id, origins, lengths, heights, condition)) return true;
		if(buildCtrCumulativeEnds(origins, lengths, ends)) return true;
		return false;
	}

	public boolean buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		if(buildCtrCumulative(id, origins, lengths, heights, condition)) return true;
		if(buildCtrCumulativeEnds(origins, lengths, ends)) return true;
		return false;
	}

	public boolean buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		int arrLen = origins.length;
		XVarInteger[][] originsArr = new XVarInteger[arrLen][];
		int[][] lengthsArr = new int[arrLen][];
		for(int i=0; i<arrLen; ++i) {
			originsArr[i] = new XVarInteger[]{origins[i]};
			lengthsArr[i] = new int[]{lengths[i]};
		}
		return buildCtrNoOverlap(id, originsArr, lengthsArr, zeroIgnored);
	}
	
	private boolean isZeroLengthBox(int[] lengths) {
		for(int i=0; i<lengths.length; ++i) {
			if(lengths[i] > 0) return false; 
		}
		return true;
	}

	public boolean buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		for(int oi1 = 0; oi1 < origins.length-1; ++oi1) {
			XVarInteger[] origins1 = origins[oi1];
			int[] lengths1 = lengths[oi1];
			if(zeroIgnored && isZeroLengthBox(lengths1)) continue;
			SortedSet<Var> varMappings1 = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
			List<String> strOrigins1 = new ArrayList<>();
			for(XVarInteger var : origins1) {
				strOrigins1.add(CtrBuilderUtils.normalizeCspVarName(var.id));
				varMappings1.add(varmapping.get(var.id));
			}
			List<String> strLengths1 = new ArrayList<>();
			for(Integer length : lengths1) strLengths1.add(Integer.toString(length));
			for(int oi2 = oi1+1; oi2 < origins.length; ++oi2) {
				XVarInteger[] origins2 = origins[oi2];
				int[] lengths2 = lengths[oi2];
				if(zeroIgnored && isZeroLengthBox(lengths2)) continue;
				SortedSet<Var> varMappings = new TreeSet<>(varMappings1);
				List<String> strOrigins2 = new ArrayList<>();
				for(XVarInteger var : origins2) {
					strOrigins2.add(CtrBuilderUtils.normalizeCspVarName(var.id));
					varMappings.add(varmapping.get(var.id));
				}
				List<String> strLengths2 = new ArrayList<>();
				for(Integer length : lengths2) strLengths2.add(Integer.toString(length));
				String expr = buildCtrNoOverlapStr(strOrigins1, strLengths1, strOrigins2, strLengths2, false);
				Predicate p = new Predicate();
				p.setExpression(expr);
				SortedSet<String> allVars = new TreeSet<>();
				allVars.addAll(strOrigins1);
				allVars.addAll(strOrigins2);
				for(String strVar : allVars) p.addVariable(strVar);
				try {
					p.toClause(solver, CtrBuilderUtils.toVarVec(varMappings), CtrBuilderUtils.toEvaluableVec(varMappings));
				} catch (ContradictionException e) {
					return true;
				}
			}
		}
		return false;
	}

	private String buildCtrNoOverlapStr(List<String> strOrigins1, List<String> strLengths1, List<String> strOrigins2,
			List<String> strLengths2, boolean zeroIgn) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("or(");
		sbuf.append("le(add(").append(strOrigins1.get(0)).append(',').append(strLengths1.get(0)).append(')').append(',').append(strOrigins2.get(0)).append(')');
		sbuf.append(",le(add(").append(strOrigins2.get(0)).append(',').append(strLengths2.get(0)).append(')').append(',').append(strOrigins1.get(0)).append(')');
		for(int i=1; i<strOrigins1.size(); ++i) {
			sbuf.append(",le(add(").append(strOrigins1.get(i)).append(',').append(strLengths1.get(i)).append(')').append(',').append(strOrigins2.get(i)).append(')');
			sbuf.append(",le(add(").append(strOrigins2.get(i)).append(',').append(strLengths2.get(i)).append(')').append(',').append(strOrigins1.get(i)).append(')');
		}
		if(zeroIgn) {
			sbuf.append(',').append(zeroLengthCtr(strLengths1));
			sbuf.append(',').append(zeroLengthCtr(strLengths2));
		}
		sbuf.append(')');
		return sbuf.toString();
	}

	private String zeroLengthCtr(List<String> strLengths) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("and(");
		sbuf.append("eq(0,").append(strLengths.get(0)).append(')');
		for(int i=1; i<strLengths.size(); ++i) sbuf.append(',').append("eq(0,").append(strLengths.get(0)).append(')');
		sbuf.append(')');
		return sbuf.toString();
	}

	public boolean buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored) {
		int arrLen = origins.length;
		XVarInteger[][] originsArr = new XVarInteger[arrLen][];
		XVarInteger[][] lengthsArr = new XVarInteger[arrLen][];
		for(int i=0; i<arrLen; ++i) {
			originsArr[i] = new XVarInteger[]{origins[i]};
			lengthsArr[i] = new XVarInteger[]{lengths[i]};
		}
		return buildCtrNoOverlap(id, originsArr, lengthsArr, zeroIgnored);
	}

	public boolean buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored) {
		for(int oi1 = 0; oi1 < origins.length-1; ++oi1) {
			XVarInteger[] origins1 = origins[oi1];
			XVarInteger[] lengths1 = lengths[oi1];
			SortedSet<Var> varMappings1 = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
			List<String> strOrigins1 = new ArrayList<>();
			for(XVarInteger var : origins1) {
				strOrigins1.add(CtrBuilderUtils.normalizeCspVarName(var.id));
				varMappings1.add(varmapping.get(var.id));
			}
			List<String> strLengths1 = new ArrayList<>();
			for(XVarInteger length : lengths1) {
				strLengths1.add(CtrBuilderUtils.normalizeCspVarName(length.id));
				varMappings1.add(varmapping.get(length.id));
			}
			for(int oi2 = oi1+1; oi2 < origins.length; ++oi2) {
				XVarInteger[] origins2 = origins[oi2];
				XVarInteger[] lengths2 = lengths[oi2];
				SortedSet<Var> varMappings = new TreeSet<>(varMappings1);
				List<String> strOrigins2 = new ArrayList<>();
				for(XVarInteger var : origins2) {
					strOrigins2.add(CtrBuilderUtils.normalizeCspVarName(var.id));
					varMappings.add(varmapping.get(var.id));
				}
				List<String> strLengths2 = new ArrayList<>();
				for(XVarInteger length : lengths2) {
					strLengths2.add(CtrBuilderUtils.normalizeCspVarName(length.id));
					varMappings.add(varmapping.get(length.id));
				}
				String expr = buildCtrNoOverlapStr(strOrigins1, strLengths1, strOrigins2, strLengths2, zeroIgnored);
				Predicate p = new Predicate();
				p.setExpression(expr);
				SortedSet<String> allVars = new TreeSet<>();
				allVars.addAll(strOrigins1);
				allVars.addAll(strOrigins2);
				allVars.addAll(strLengths1);
				allVars.addAll(strLengths2);
				for(String strVar : allVars) p.addVariable(strVar);
				try {
					p.toClause(solver, CtrBuilderUtils.toVarVec(varMappings), CtrBuilderUtils.toEvaluableVec(varMappings));
				} catch (ContradictionException e) {
					return true;
				}
			}
		}
		return false;
	}

}
