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

import org.sat4j.csp.intension.IIntensionCtrEncoder;
import org.sat4j.reader.XMLCSP3Reader;
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

	private final IIntensionCtrEncoder intensionCtrEncoder;

	public ConnectionCtrBuilder(IIntensionCtrEncoder intensionEnc) {
		this.intensionCtrEncoder = intensionEnc;
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
		String norm1 = CtrBuilderUtils.normalizeCspVarName(v1.id);
		String norm2 = CtrBuilderUtils.normalizeCspVarName(v2.id);
		String expr = "or(not(eq("+norm1+","+v2Index+")),eq("+norm2+","+v1Index+"))";
		this.intensionCtrEncoder.encode(expr);
		return false;
	}
	
	public boolean buildCtrMaximum(String id, XVarInteger[] list, Condition condition) {
		return buildCtrMinOrMax(list, condition, true);
	}
	
	private boolean buildCtrMinOrMax(XVarInteger[] list, Condition condition, boolean isMax) {
		final StringBuffer leftOpBuf = new StringBuffer();
		leftOpBuf.append(isMax ? "max(" : "min(");
		String norm = CtrBuilderUtils.normalizeCspVarName(list[0].id);
		leftOpBuf.append(norm);
		for(int i=1; i<list.length; ++i) {
			norm = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			leftOpBuf.append(',').append(norm);
		}
		leftOpBuf.append(")");
		final StringCondition strCond = StringCondition.buildStringCondition(condition);
		final String expr = strCond.asString(leftOpBuf.toString());
		return this.intensionCtrEncoder.encode(expr);
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
		String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
		StringBuffer maxExprBuf = new StringBuffer();
		maxExprBuf.append(isMax ? "max(" : "min(");
		for(int i=0; i<list.length; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			if(i>0) maxExprBuf.append(',');
			maxExprBuf.append(normVar);
		}
		maxExprBuf.append(')');
		String maxExpr = maxExprBuf.toString();
		for(int i=startIndex; i<startIndex+list.length; ++i) {
			// index==i => max(x0,...,xn)==xi
			String expr = "or(ne("+normIndex+","+i+"),eq("+maxExpr+","+CtrBuilderUtils.normalizeCspVarName(list[i-startIndex].id)+"))";
			this.intensionCtrEncoder.encode(expr);
		}
		return false;
	}

	private boolean buildCtrNotMinOrMaxBeforeIndex(XVarInteger[] list, XVarInteger index, int startIndex, boolean isMax) {
		for(int i=1+startIndex; i<startIndex+list.length; ++i) {
			// index==i => and(x0<xi,...,x(i-1)<xi)
			String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
			String normMax = CtrBuilderUtils.normalizeCspVarName(list[i-startIndex].id);
			StringBuffer andExprBuf = new StringBuffer();
			andExprBuf.append("and(");
			for(int j=0; j<i-startIndex; ++j) {
				String normCurVar = CtrBuilderUtils.normalizeCspVarName(list[j].id);
				if(j>0) andExprBuf.append(',');
				andExprBuf.append(isMax ? "lt(" : "gt(").append(normCurVar).append(',').append(normMax).append(')');
			}
			andExprBuf.append(')');
			String expr = "or(ne("+normIndex+","+i+"),"+andExprBuf.toString()+")";
			this.intensionCtrEncoder.encode(expr);
		}
		return false;
	}

	private boolean buildCtrNotMaxAfterIndex(XVarInteger[] list, XVarInteger index, int startIndex, boolean isMax) {
		for(int i=startIndex; i<startIndex+list.length-1; ++i) {
			// index==i => and(x(i+1)<xi,...,xn<xi)
			String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
			String normMax = CtrBuilderUtils.normalizeCspVarName(list[i-startIndex].id);
			StringBuffer andExprBuf = new StringBuffer();
			andExprBuf.append("and(");
			for(int j=i-startIndex+1; j<list.length; ++j) {
				String normCurVar = CtrBuilderUtils.normalizeCspVarName(list[j].id);
				if(j>i-startIndex+1) andExprBuf.append(',');
				andExprBuf.append(isMax ? "lt(" : "gt(").append(normCurVar).append(',').append(normMax).append(')');
			}
			andExprBuf.append(')');
			String expr = "or(ne("+normIndex+","+i+"),"+andExprBuf.toString()+")";
			this.intensionCtrEncoder.encode(expr);
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
		int len = list.length;
		String[] subExprs = new String[len];
		String strValue = Integer.toString(value);
		for(int i=0; i<len; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			subExprs[i] = "eq("+normVar+","+strValue+")";
		}
		this.intensionCtrEncoder.encode(CtrBuilderUtils.chainExpressionsForAssociativeOp(subExprs, "or"));
        return false;
	}

	private boolean buildCtrElementNotAnyIndex(XVarInteger[] list, int startIndex, XVarInteger index, int value, boolean isFirst) {
		int len = list.length;
		String[] subExprs = new String[len];
		String strValue = Integer.toString(value);
		String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
		for(int i=0; i<len; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			String firstOp = isFirst ? "le" : "ge";
			String lowerIndexCase = "and("+firstOp+"("+Integer.toString(i+startIndex)+","+normIndex+"),ne("+normVar+","+strValue+"))";
			String rightIndexCase = "and(eq("+Integer.toString(i+startIndex)+","+normIndex+"),eq("+normVar+","+strValue+"))";
			String lastOp = isFirst ? "ge" : "le";
			String higherIndexCase = lastOp+"("+Integer.toString(i+startIndex)+","+normIndex+")";
			subExprs[i] = "or("+lowerIndexCase+","+rightIndexCase+","+higherIndexCase+")";
		}
		this.intensionCtrEncoder.encode(CtrBuilderUtils.chainExpressionsForAssociativeOp(subExprs, "and"));
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
		int len = list.length;
		String[] subExprs = new String[len];
		String normValue = CtrBuilderUtils.normalizeCspVarName(value.id);
		for(int i=0; i<len; ++i) {
			String normVar = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			subExprs[i] = "eq("+normVar+","+normValue+")";
		}
		this.intensionCtrEncoder.encode(CtrBuilderUtils.chainExpressionsForAssociativeOp(subExprs, "or"));
		return false;
	}
	
	private boolean buildCtrElementFirstIndex(XVarInteger[] list, int startIndex, XVarInteger index, XVarInteger value) {
		for(int i=0; i<list.length; ++i) {
			// or(ne(i,index),and(eq(i,index),eq(xi,value),ne(x0,value),...,ne(xi-1,value)))
			String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
			String normValue = CtrBuilderUtils.normalizeCspVarName(value.id);
			StringBuffer sbuf = new StringBuffer();
			sbuf.append("or(ne(").append(i+startIndex).append(',').append(normIndex).append("),and(eq(").append(i+startIndex).append(',').append(normIndex).append("),eq(").append(CtrBuilderUtils.normalizeCspVarName(list[i].id)).append(',').append(normValue).append(')');
			for(int j=0; j<i; ++j) {
				sbuf.append(",ne(").append(CtrBuilderUtils.normalizeCspVarName(list[j].id)).append(',').append(normValue).append(')'); // end NE operator
			}
			sbuf.append("))");
			this.intensionCtrEncoder.encode(sbuf.toString());
		}
		return false;
	}
	
	private boolean buildCtrElementLastIndex(XVarInteger[] list, int startIndex, XVarInteger index, XVarInteger value) {
		for(int i=0; i<list.length; ++i) {
			// or(ne(i,index),and(eq(i,index),eq(xi,value),ne(xi+1,value),...,ne(xn,value)))
			String normIndex = CtrBuilderUtils.normalizeCspVarName(index.id);
			String normValue = CtrBuilderUtils.normalizeCspVarName(value.id);
			StringBuffer sbuf = new StringBuffer();
			sbuf.append("or(ne(").append(i+startIndex).append(',').append(normIndex).append("),and(eq(").append(i+startIndex).append(',').append(normIndex).append("),eq(").append(CtrBuilderUtils.normalizeCspVarName(list[i].id)).append(',').append(normValue).append(')'); // end EQ2 operator
			for(int j=i+1; j<list.length; ++j) {
				sbuf.append(",ne(").append(CtrBuilderUtils.normalizeCspVarName(list[j].id)).append(',').append(normValue).append(')');
			}
			sbuf.append("))");
			this.intensionCtrEncoder.encode(sbuf.toString());
		}
		return false;
	}

}
