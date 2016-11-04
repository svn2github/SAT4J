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

import java.util.SortedSet;
import java.util.TreeSet;

import org.sat4j.csp.intension.IntensionCtrEncoder;
import org.sat4j.reader.XMLCSP3Reader;
import org.xcsp.common.Types.TypeOperator;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to allDifferent constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class ComparisonCtrBuilder {
	
	private final IntensionCtrEncoder intensionCtrEncoder;

	public ComparisonCtrBuilder(IntensionCtrEncoder intensionEnc) {
		this.intensionCtrEncoder = intensionEnc;
	}
	
	public boolean buildCtrAllDifferent(String id, XVarInteger[] list) { // TODO: remove dependence to varmapping
		for(int i=0; i<list.length-1; ++i) {
			final String norm1 = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			for(int j=i+1; j<list.length; ++j) {
				final String norm2 = CtrBuilderUtils.normalizeCspVarName(list[j].id);
				final String expr = "ne("+norm1+","+norm2+")";
				if(this.intensionCtrEncoder.encode(expr)) return true;
			}
		}
		return false;
	}
	
	public boolean buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		SortedSet<XVarInteger> varSet = new TreeSet<>((o1,o2) -> o1.id.compareTo(o2.id));
		for(int i=0; i<list.length; ++i) {
			varSet.add(list[i]);
		}
		XVarInteger[] cleanList = new XVarInteger[varSet.size()];
		cleanList = varSet.toArray(cleanList);
		if(except.length == 0) {
			return buildCtrAllDifferent(id, cleanList);
		}
		if(cleanList.length < 2) return false;
		String exceptBase = "eq(X,"+except[0]+")";
		for(int i=1; i<except.length; ++i) {
			exceptBase = "or("+exceptBase+",eq(X,"+except[i]+"))";
		}
		String[] exprs = new String[cleanList.length-1];
		for(int i=0; i<cleanList.length-1; ++i) {
			String normalizedCurVar = CtrBuilderUtils.normalizeCspVarName(cleanList[i].id);
			String exceptExpr = exceptBase.replaceAll("X", normalizedCurVar);
			String neExpr = "ne("+normalizedCurVar+","+CtrBuilderUtils.normalizeCspVarName(cleanList[i+1].id)+")";
			for(int j=i+2; j<cleanList.length; ++j) {
				neExpr = "and("+neExpr+",ne("+normalizedCurVar+","+CtrBuilderUtils.normalizeCspVarName(cleanList[j].id)+"))";
			}
			exprs[i] = "or("+exceptExpr+","+neExpr+")";
		}
		return this.intensionCtrEncoder.encode(CtrBuilderUtils.chainExpressionsForAssociativeOp(exprs, "and"));
	}
	
	public boolean buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		for(int i=0; i<lists.length-1; ++i) {
			for(int j=i+1; j<lists.length; ++j) {
				StringBuffer sbufExpr = new StringBuffer();
				sbufExpr.append("or(");
				buildCtrAllDifferentListAux(lists, i, j, sbufExpr, 0);
				for(int k=1; k<lists[i].length; ++k) {
					sbufExpr.append(',');
					buildCtrAllDifferentListAux(lists, i, j, sbufExpr, k);
				}
				sbufExpr.append(')');
				if(this.intensionCtrEncoder.encode(sbufExpr.toString())) return true;
			}
		}
		return false;
	}

	private void buildCtrAllDifferentListAux(XVarInteger[][] lists, int i, int j, StringBuffer sbufExpr, int k) {
		sbufExpr.append("ne(");
		String var1Id = lists[i][k].id;
		String normVar1 = CtrBuilderUtils.normalizeCspVarName(var1Id);
		sbufExpr.append(normVar1).append(',');
		String var2Id = lists[j][k].id;
		String normVar2 = CtrBuilderUtils.normalizeCspVarName(var2Id);
		sbufExpr.append(normVar2).append(')');
	}

	public boolean buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		boolean contradictionFound = false;
		XVarInteger[][] tMatrix = CtrBuilderUtils.transposeMatrix(matrix);
		for(int i=0; i<matrix.length; ++i) {
			contradictionFound |= buildCtrAllDifferent(id, matrix[i]);
		}
		for(int i=0; i<tMatrix.length; ++i) {
			contradictionFound |= buildCtrAllDifferent(id, tMatrix[i]);
		}
		return contradictionFound;
	}
	
	public boolean buildCtrAllEqual(String id, XVarInteger[] list) {
		for(int i=0; i<list.length-1; ++i) {
			String norm1 = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			String norm2 = CtrBuilderUtils.normalizeCspVarName(list[i+1].id);
			if(this.intensionCtrEncoder.encode("eq("+norm1+","+norm2+")")) return true;
		}
		return false;
	}
	
	public boolean buildCtrOrdered(String id, XVarInteger[] list, TypeOperator operator) {
		for(int i=0; i<list.length-1; ++i) {
			String normalized1 = CtrBuilderUtils.normalizeCspVarName(list[i].id);
			String normalized2 = CtrBuilderUtils.normalizeCspVarName(list[i+1].id);
			String expr = operator.name().toLowerCase()+"("+normalized1+","+normalized2+")";
			if(this.intensionCtrEncoder.encode(expr)) return true;
		}
		return false;
	}
	
	public boolean buildCtrLex(String id, XVarInteger[][] lists, TypeOperator operator) {
		boolean contradictionFound = false;
		for(int i=0; i<lists.length-1; ++i) {
			contradictionFound |= buildCtrLex(id, lists[i], lists[i+1], operator);
		}
		return contradictionFound;
	}
	
	public boolean buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperator operator) {
		boolean contradictionFound = false;
		contradictionFound |= buildCtrLex(id, matrix, operator);
		XVarInteger[][] tMatrix = CtrBuilderUtils.transposeMatrix(matrix);
		contradictionFound |= buildCtrLex(id, tMatrix, operator);
		return contradictionFound;
	}
	
	private boolean buildCtrLex(String id, XVarInteger[] list1,
			XVarInteger[] list2, TypeOperator operator) {
		TypeOperator strictOp = CtrBuilderUtils.strictTypeOperator(operator);
		String[] chains = new String[list1.length];
		String id01 = list1[0].id;
		String id02 = list2[0].id;
		chains[0] = list1.length == 1
				? operator.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(id01)+","+CtrBuilderUtils.normalizeCspVarName(id02)+")"
				: strictOp.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(id01)+","+CtrBuilderUtils.normalizeCspVarName(id02)+")";
		for(int i=1; i<list1.length; ++i) {
			String eqChain = "eq("+CtrBuilderUtils.normalizeCspVarName(id01)+","+CtrBuilderUtils.normalizeCspVarName(id02)+")";
			for(int j=1; j<i; ++j) {
				String idj1 = list1[j].id;
				String idj2 = list2[j].id;
				eqChain = "and("+eqChain+",eq("+CtrBuilderUtils.normalizeCspVarName(idj1)+","+CtrBuilderUtils.normalizeCspVarName(idj2)+"))";
			}
			String idi1 = list1[i].id;
			String idi2 = list2[i].id;
			String finalMember =  i == list1.length-1
					? operator.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(idi1)+","+CtrBuilderUtils.normalizeCspVarName(idi2)+")"
					: strictOp.name().toLowerCase()+"("+CtrBuilderUtils.normalizeCspVarName(idi1)+","+CtrBuilderUtils.normalizeCspVarName(idi2)+")";
			chains[i] = "and("+eqChain+","+finalMember+")";
		}
		return this.intensionCtrEncoder.encode(CtrBuilderUtils.chainExpressionsForAssociativeOp(chains, "or"));
	}

}
