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

import org.sat4j.csp.intension.IntensionCtrEncoder;
import org.sat4j.reader.XMLCSP3Reader;
import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Condition.ConditionVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to sum constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class CountingCtrBuilder {

	private final IntensionCtrEncoder intensionCtrEnc;

	public CountingCtrBuilder(IntensionCtrEncoder intensionEnc) {
		this.intensionCtrEnc = intensionEnc;
	}

	public boolean buildCtrSum(String id, XVarInteger[] list, Condition condition) {
		int[] coeffs = new int[list.length];
		Arrays.fill(coeffs, 1);
		return buildCtrSum(id, list, coeffs, condition);
	}

	public boolean buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition) {
		String varId;
		StringBuffer exprBuf = new StringBuffer();
		exprBuf.append(condition.operator.toString().toLowerCase());
		exprBuf.append('(');
		for(int i=0; i<list.length-1; ++i) {
			exprBuf.append("add(");
			if(coeffs[i] != 1) {
				exprBuf.append("mul(").append(coeffs[i]).append(',');
			}
			varId = list[i].id;
			exprBuf.append(CtrBuilderUtils.normalizeCspVarName(varId));
			if(coeffs[i] != 1) {
				exprBuf.append(')');
			}
			exprBuf.append(',');
		}
		varId = list[list.length-1].id;
		exprBuf.append(CtrBuilderUtils.normalizeCspVarName(varId));
		for(int i=0; i<list.length-1; ++i) {
			exprBuf.append(')');
		}
		exprBuf.append(',');
		if(condition instanceof ConditionVar) {
			varId = ((ConditionVar) condition).x.id();
			exprBuf.append(CtrBuilderUtils.normalizeCspVarName(varId));
		} else if(condition instanceof ConditionVal) {
			exprBuf.append(((ConditionVal) condition).k);
		} else {
			throw new UnsupportedOperationException("this kind of condition is not supported yet.");
		}
		exprBuf.append(')');
		String expr = exprBuf.toString();
		return this.intensionCtrEnc.encode(expr);
	}

	public boolean buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition) {
		return buildCtrCount(id, list, values, StringCondition.buildStringCondition(condition));
	}

	private boolean buildCtrCount(String id, XVarInteger[] list, int[] values, StringCondition condition) {
		StringBuffer inExprBuf = new StringBuffer();
		inExprBuf.append("set(").append(Integer.toString(values[0]));
		for(int i=1; i<values.length; ++i) {
			inExprBuf.append(',');
			inExprBuf.append(Integer.toString(values[i]));
		}
		inExprBuf.append(')');
		String inExpr = inExprBuf.toString();
		String sumExprs[] = new String[list.length];
		for(int i=0; i<list.length; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			sumExprs[i] = "if(in("+normVar+","+inExpr+"),1,0)";
		}
		String sumExpr = CtrBuilderUtils.chainExpressionsForAssociativeOp(sumExprs, "add");
		return this.intensionCtrEnc.encode(condition.asString(sumExpr));
	}

	public boolean buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition) {
		return buildCtrCount(id, list, values, StringCondition.buildStringCondition(condition));
	}

	private boolean buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, StringCondition condition) {
		int listLength = list.length;
		StringBuffer inExprBuf = new StringBuffer();
		inExprBuf.append("set(").append(CtrBuilderUtils.normalizeCspVarName(values[0].id));
		for(int i=1; i<values.length; ++i) {
			inExprBuf.append(',');
			inExprBuf.append(CtrBuilderUtils.normalizeCspVarName(values[i].id));
		}
		inExprBuf.append(')');
		String inExpr = inExprBuf.toString();
		String sumExprs[] = new String[listLength];
		for(int i=0; i<listLength; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			sumExprs[i] = "if(in("+normVar+","+inExpr+"),1,0)";
		}
		String sumExpr = CtrBuilderUtils.chainExpressionsForAssociativeOp(sumExprs, "add");
		return this.intensionCtrEnc.encode(condition.asString(sumExpr));
	}

	public boolean buildCtrAtLeast(String id, XVarInteger[] list, int value, int k) {
		return buildCtrCount(id, list, new int[]{value}, new StringCondition("ge(",","+Integer.toString(k)+")"));
	}

	public boolean buildCtrAtMost(String id, XVarInteger[] list, int value, int k) {
		return buildCtrCount(id, list, new int[]{value}, new StringCondition("le(",","+Integer.toString(k)+")"));
	}

	public boolean buildCtrExactly(String id, XVarInteger[] list, int value, int k) {
		return buildCtrCount(id, list, new int[]{value}, new StringCondition("eq(",","+Integer.toString(k)+")"));
	}

	public boolean buildCtrExactly(String id, XVarInteger[] list, int value, XVarInteger k) {
		String normVar = CtrBuilderUtils.normalizeCspVarName(k.id);
		StringCondition strCond = new StringCondition("eq(",","+normVar+")");
		strCond.addVariable(normVar);
		return buildCtrCount(id, list, new int[]{value}, strCond);
	}

	public boolean buildCtrAmong(String id, XVarInteger[] list, int[] values, int k) {
		return buildCtrCount(id, list, values, new StringCondition("eq(",","+Integer.toString(k)+")"));
	}

	public boolean buildCtrAmong(String id, XVarInteger[] list, int[] values, XVarInteger k) {
		String normVar = CtrBuilderUtils.normalizeCspVarName(k.id);
		StringCondition strCond = new StringCondition("eq(",","+normVar+")");
		strCond.addVariable(normVar);
		return buildCtrCount(id, list, values, strCond);
	}

	public boolean buildCtrNValues(String id, XVarInteger[] list, Condition condition) {
		return buildCtrNValuesExcept(id, list, new int[]{}, condition);
	}

	public boolean buildCtrNValues(String id, XVarInteger[] list, StringCondition condition) {
		return buildCtrNValuesExcept(id, list, new int[]{}, condition);
	}

	public boolean buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition) {
		return buildCtrNValuesExcept(id, list, except, StringCondition.buildStringCondition(condition));
	}

	private boolean buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, StringCondition strCond) {
		StringBuffer sbuf = new StringBuffer();
		String normalized;
		sbuf.append("distinct(set(");
		normalized = CtrBuilderUtils.normalizeCspVarName(list[0].id);
		sbuf.append(normalized);
		for(int i=1; i<list.length; ++i) {
			normalized = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			sbuf.append(',');
			sbuf.append(normalized);
		}
		sbuf.append("),set(");
		if(except.length > 0) {
			sbuf.append(except[0]);
			for(int i=1; i<except.length; ++i) {
				sbuf.append(',');
				sbuf.append(except[i]);
			}
		}
		sbuf.append("))");
		return this.intensionCtrEnc.encode(strCond.asString(sbuf.toString()));
	}

	public boolean buildCtrNotAllEqual(String id, XVarInteger[] list) {
		return buildCtrNValues(id, list, new StringCondition("ne(", ","+Integer.toString(list.length)+""));
	}

	public boolean buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs) {
		boolean contradictionFound = manageClosedCardinality(id, list, closed, values);
		for(int i=0; i<values.length && !contradictionFound; ++i) {
			buildCtrExactly(id, list, values[i], occurs[i]);
		}
		return contradictionFound;
	}

	private boolean manageClosedCardinality(String id, XVarInteger[] list, boolean closed, int[] values) {
		if(!closed) return false;
		for(int i=0; i<list.length; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			StringBuffer exprBuff = new StringBuffer();
			exprBuff.append("or(eq(").append(normVar).append(',').append(values[0]).append(')');
			for(int j=1; j<values.length; ++j) {
				exprBuff.append(",eq(").append(normVar).append(',').append(values[j]).append(')');
			}
			exprBuff.append(')');
			if(this.intensionCtrEnc.encode(exprBuff.toString())) return true;
		}
		return false;
	}

	public boolean buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs) {
		return buildCtrCardinality(id, list, closed, values, occurs, occurs);
	}

	public boolean buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax) {
		boolean contradictionFound = manageClosedCardinality(id, list, closed, values);
		for(int i=0; i<values.length; ++i) {
			if(occursMin[i] == occursMax[i]) {
				contradictionFound |= buildCtrExactly(id, list, values[i], occursMin[i]);
			} else {
				contradictionFound |= buildCtrAtLeast(id, list, values[i], occursMin[i]);
				contradictionFound |= buildCtrAtMost(id, list, values[i], occursMax[i]);
			}
		}
		return contradictionFound;
	}

	public boolean buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, XVarInteger[] occurs) {
		boolean contradictionFound = manageClosedCardinality(id, list, closed, values);
		for(int i=0; i<values.length && !contradictionFound; ++i) {
			String norm = CtrBuilderUtils.normalizeCspVarName(occurs[i].id);
			StringCondition stringCond = new StringCondition("eq(", ","+norm+")");
			stringCond.addVariable(norm);
			contradictionFound |= buildCtrCount(id, new XVarInteger[]{values[i]}, list, stringCond);
		}
		return contradictionFound;
	}

	private boolean manageClosedCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values) {
		boolean contradictionFound = false;
		if(closed) {
			for(int i=0; i<list.length && !contradictionFound; ++i) {
				contradictionFound |= buildCtrCount(id, new XVarInteger[]{list[i]}, values, new StringCondition("eq(", ",1"));
			}
		}
		return contradictionFound;
	}

	public boolean buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occurs) {
		return buildCtrCardinality(id, list, closed, values, occurs, occurs);
	}

	public boolean buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occursMin, int[] occursMax) {
		boolean contradictionFound = false;
		for(int i=0; i<values.length && !contradictionFound; ++i) {
			if(occursMin[i] == occursMax[i]) {
				contradictionFound |= buildCtrCount(id, new XVarInteger[]{values[i]}, list, new StringCondition("eq(", ","+Integer.toString(occursMin[i])+")"));
			} else {
				contradictionFound |= buildCtrCount(id, new XVarInteger[]{values[i]}, list, new StringCondition("ge(", ","+Integer.toString(occursMin[i])+")"));
				contradictionFound |= buildCtrCount(id, new XVarInteger[]{values[i]}, list, new StringCondition("le(", ","+Integer.toString(occursMax[i])+")"));
			}
		}
		return contradictionFound;
	}

}
