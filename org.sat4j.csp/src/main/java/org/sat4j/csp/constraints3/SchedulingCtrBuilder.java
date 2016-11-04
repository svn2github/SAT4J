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
import java.util.List;

import org.sat4j.csp.intension.IntensionCtrEncoder;
import org.sat4j.reader.XMLCSP3Reader;
import org.xcsp.common.Condition;
import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to noOverlap constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class SchedulingCtrBuilder {

	private final IntensionCtrEncoder intensionEnc;

	public SchedulingCtrBuilder(IntensionCtrEncoder intensionEnc) {
		this.intensionEnc = intensionEnc;
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
		StringBuffer sb = new StringBuffer();
		sb.append("imp(");
		// COND part
		if(stretchBeginIndex > 0) {
			sb.append("and(");
		}
		sb.append("eq(").append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex].id)).append(',').append(value).append(')');
		if(stretchBeginIndex > 0) {
			sb.append(",ne(").append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex-1].id)).append(',').append(value).append("))");
		}
		sb.append(',');
		// THEN part
		sb.append("or(");
		for(int i=stretchBeginIndex+1; i<=stretchBeginIndex+widthMax; ++i) {
			if(i>stretchBeginIndex+1) sb.append(',');
			sb.append("ne(").append(CtrBuilderUtils.normalizeCspVarName(list[i].id)).append(',').append(value).append(')');
		}
		sb.append("))");
		return this.intensionEnc.encode(sb.toString());
	}
	
	private boolean preventUnderLength(int value, XVarInteger[] list, int stretchBeginIndex, int widthMin) {
		// trivial case
		if(widthMin == 1) return false;
		// not enough length case
		if(list.length-stretchBeginIndex < widthMin) {
			return preventUnderLengthDueToBeginIndex(value, list, stretchBeginIndex);
		}
		// (l[i] == value && l[i]-1 != value) => (l[i+1] == value && ... && l[i+min-1] == value)
		StringBuffer sb = new StringBuffer();
		sb.append("imp(");
		// COND part
		if(stretchBeginIndex > 0) {
			sb.append("and(");
		}
		sb.append("eq(").append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex].id)).append(',').append(value).append(')');
		if(stretchBeginIndex > 0) {
			sb.append(",ne(").append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex-1].id)).append(',').append(value).append("))");
		}
		sb.append(',');
		// THEN part
		sb.append("and(");
		for(int i=stretchBeginIndex+1; i<=stretchBeginIndex+widthMin-1; ++i) {
			if(i>stretchBeginIndex+1) sb.append(',');
			sb.append("eq(").append(CtrBuilderUtils.normalizeCspVarName(list[i].id)).append(',').append(value).append(')');
		}
		sb.append("))");
		return this.intensionEnc.encode(sb.toString());
	}

	private boolean preventUnderLengthDueToBeginIndex(int value, XVarInteger[] list, int stretchBeginIndex) {
		StringBuffer sb = new StringBuffer();
		if(stretchBeginIndex > 0) {
			sb.append("or(");
		}
		sb.append("ne(").append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex].id)).append(',').append(value).append(')');
		if(stretchBeginIndex > 0) {
			sb.append(",eq(").append(CtrBuilderUtils.normalizeCspVarName(list[stretchBeginIndex-1].id)).append(',').append(value).append("))");
		}
		return this.intensionEnc.encode(sb.toString());
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
			String normVar1 = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			String normVar2 = CtrBuilderUtils.normalizeCspVarName(list[i+1].id);
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
			if(this.intensionEnc.encode(predExpr)) return true;
		}
		return false;
	}
	
	public boolean buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition) {
		int maxT = computeMaxT(origins, lengths);
		for(int t=0; t<maxT; ++t) {
			StringBuffer exprBuff = new StringBuffer();
			exprBuff.append("add(");
			builtCtrCumulativeHeightComp(origins, 0, lengths, t, heights, exprBuff);
			for(int i=1; i<origins.length; ++i) {
				exprBuff.append(',');
				builtCtrCumulativeHeightComp(origins, i, lengths, t, heights, exprBuff);
			}
			exprBuff.append(')');
			StringCondition strCond = StringCondition.buildStringCondition(condition);
			if(this.intensionEnc.encode(strCond.asString(exprBuff.toString()))) return true;
		}
		return false;
	}

	private void builtCtrCumulativeHeightComp(XVarInteger[] origins, int originIndex, int[] lengths, int t,
			int[] heights, StringBuffer exprBuff) {
		exprBuff.append("ite(");
		buildCtrCumulativeHeightCompCondition(origins, originIndex, lengths, t, exprBuff);
		exprBuff.append(',').append(Integer.toString(heights[originIndex])).append(",0)");
	}
	
	private void builtCtrCumulativeHeightComp(XVarInteger[] origins, int originIndex, int[] lengths, int t,
			XVarInteger[] heights, StringBuffer exprBuff) {
		exprBuff.append("ite(");
		buildCtrCumulativeHeightCompCondition(origins, originIndex, lengths, t, exprBuff);
		exprBuff.append(',');
		String normVar = CtrBuilderUtils.normalizeCspVarName(heights[originIndex].id);
		exprBuff.append(normVar);
		exprBuff.append(",0)");
	}
	
	private void builtCtrCumulativeHeightComp(XVarInteger[] origins, int originIndex, XVarInteger[] lengths, int t,
			int[] heights, StringBuffer exprBuff) { // TODO: some refactoring needed here (pass all arguments as text to common method
		exprBuff.append("ite(");
		buildCtrCumulativeHeightCompCondition(origins, originIndex, lengths, t, exprBuff);
		exprBuff.append(',');
		exprBuff.append(Integer.toString(heights[originIndex]));
		exprBuff.append(",0)");
	}
	
	private void builtCtrCumulativeHeightComp(XVarInteger[] origins, int originIndex, XVarInteger[] lengths, int t,
			XVarInteger[] heights, StringBuffer exprBuff) {
		exprBuff.append("ite(");
		buildCtrCumulativeHeightCompCondition(origins, originIndex, lengths, t, exprBuff);
		exprBuff.append(',');
		String normVar = CtrBuilderUtils.normalizeCspVarName(heights[originIndex].id);
		exprBuff.append(normVar);
		exprBuff.append(",0)");
	}

	private void buildCtrCumulativeHeightCompCondition(XVarInteger[] origins, int originIndex, int[] lengths, int t,
			StringBuffer exprBuff) {
		// and(le(x,t),gt(add(x,l),t))
		exprBuff.append("and(le(");
		String normVar = CtrBuilderUtils.normalizeCspVarName(origins[originIndex].id);
		exprBuff.append(normVar).append(',').append(Integer.toString(t)).append("),gt(add(").append(normVar).append(',').append(Integer.toString(lengths[originIndex])).append("),").append(Integer.toString(t)).append("))"); // end GT operator
	}

	private void buildCtrCumulativeHeightCompCondition(XVarInteger[] origins, int originIndex, XVarInteger[] lengths,
			int t, StringBuffer exprBuff) {
		// and(le(x,t),gt(add(x,l),t))
		exprBuff.append("and(le(");
		String normVar = CtrBuilderUtils.normalizeCspVarName(origins[originIndex].id);
		exprBuff.append(normVar).append(',').append(Integer.toString(t)).append("),gt(add(").append(normVar).append(',');
		String normLength = CtrBuilderUtils.normalizeCspVarName(lengths[originIndex].id);
		exprBuff.append(normLength);
		exprBuff.append("),").append(Integer.toString(t)).append("))");
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
			StringBuffer exprBuff = new StringBuffer();
			exprBuff.append("add(");
			builtCtrCumulativeHeightComp(origins, 0, lengths, t, heights, exprBuff);
			for(int i=1; i<origins.length; ++i) {
				exprBuff.append(',');
				builtCtrCumulativeHeightComp(origins, i, lengths, t, heights, exprBuff);
			}
			exprBuff.append(')');
			StringCondition strCond = StringCondition.buildStringCondition(condition);
			if(this.intensionEnc.encode(strCond.asString(exprBuff.toString()))) return true;
		}
		return false;
	}

	public boolean buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition) {
		int maxT = computeMaxT(origins, lengths);
		for(int t=0; t<maxT; ++t) {
			StringBuffer exprBuff = new StringBuffer();
			exprBuff.append("add(");
			builtCtrCumulativeHeightComp(origins, 0, lengths, t, heights, exprBuff);
			for(int i=1; i<origins.length; ++i) {
				exprBuff.append(',');
				builtCtrCumulativeHeightComp(origins, i, lengths, t, heights, exprBuff);
			}
			exprBuff.append(')');
			StringCondition strCond = StringCondition.buildStringCondition(condition);
			if(this.intensionEnc.encode(strCond.asString(exprBuff.toString()))) return true;
		}
		return false;
	}

	public boolean buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition) {
		int maxT = computeMaxT(origins, lengths);
		for(int t=0; t<maxT; ++t) {
			StringBuffer exprBuff = new StringBuffer();
			exprBuff.append("add(");
			builtCtrCumulativeHeightComp(origins, 0, lengths, t, heights, exprBuff);
			for(int i=1; i<origins.length; ++i) {
				exprBuff.append(',');
				builtCtrCumulativeHeightComp(origins, i, lengths, t, heights, exprBuff);
			}
			exprBuff.append(')');
			StringCondition strCond = StringCondition.buildStringCondition(condition);
			if(this.intensionEnc.encode(strCond.asString(exprBuff.toString()))) return true;
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
			StringBuffer sbuff = new StringBuffer();
			sbuff.append("eq(add(");
			String normVar = CtrBuilderUtils.normalizeCspVarName(origins[i].id);
			sbuff.append(normVar).append(',').append(Integer.toString(lengths[i])).append("),");
			String normEnd = CtrBuilderUtils.normalizeCspVarName(ends[i].id);
			sbuff.append(normEnd).append(')');
			if(this.intensionEnc.encode(sbuff.toString())) return true;
		}
		return false;
	}
	
	
	private boolean buildCtrCumulativeEnds(XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends) {
		for(int i=0; i<origins.length; ++i) {
			StringBuffer sbuff = new StringBuffer();
			sbuff.append("eq(add(");
			String normVar = CtrBuilderUtils.normalizeCspVarName(origins[i].id);
			sbuff.append(normVar).append(',');
			String normLength = CtrBuilderUtils.normalizeCspVarName(lengths[i].id);
			sbuff.append(normLength).append("),");
			String normEnd = CtrBuilderUtils.normalizeCspVarName(ends[i].id);
			sbuff.append(normEnd).append(')');
			if(this.intensionEnc.encode(sbuff.toString())) return true;
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
			List<String> strOrigins1 = new ArrayList<>();
			for(XVarInteger var : origins1) {
				strOrigins1.add(CtrBuilderUtils.normalizeCspVarName(var.id));
			}
			List<String> strLengths1 = new ArrayList<>();
			for(Integer length : lengths1) strLengths1.add(Integer.toString(length));
			for(int oi2 = oi1+1; oi2 < origins.length; ++oi2) {
				XVarInteger[] origins2 = origins[oi2];
				int[] lengths2 = lengths[oi2];
				if(zeroIgnored && isZeroLengthBox(lengths2)) continue;
				List<String> strOrigins2 = new ArrayList<>();
				for(XVarInteger var : origins2) {
					strOrigins2.add(CtrBuilderUtils.normalizeCspVarName(var.id));
				}
				List<String> strLengths2 = new ArrayList<>();
				for(Integer length : lengths2) strLengths2.add(Integer.toString(length));
				String expr = buildCtrNoOverlapStr(strOrigins1, strLengths1, strOrigins2, strLengths2, false);
				if(this.intensionEnc.encode(expr)) return true;
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
		sbuf.append("and(eq(0,").append(strLengths.get(0)).append(')');
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
			List<String> strOrigins1 = new ArrayList<>();
			for(XVarInteger var : origins1) {
				strOrigins1.add(CtrBuilderUtils.normalizeCspVarName(var.id));
			}
			List<String> strLengths1 = new ArrayList<>();
			for(XVarInteger length : lengths1) {
				strLengths1.add(CtrBuilderUtils.normalizeCspVarName(length.id));
			}
			for(int oi2 = oi1+1; oi2 < origins.length; ++oi2) {
				XVarInteger[] origins2 = origins[oi2];
				XVarInteger[] lengths2 = lengths[oi2];
				List<String> strOrigins2 = new ArrayList<>();
				for(XVarInteger var : origins2) {
					strOrigins2.add(CtrBuilderUtils.normalizeCspVarName(var.id));
				}
				List<String> strLengths2 = new ArrayList<>();
				for(XVarInteger length : lengths2) {
					strLengths2.add(CtrBuilderUtils.normalizeCspVarName(length.id));
				}
				String expr = buildCtrNoOverlapStr(strOrigins1, strLengths1, strOrigins2, strLengths2, zeroIgnored);
				if(this.intensionEnc.encode(expr)) return true;
			}
		}
		return false;
	}

}
