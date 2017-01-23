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

import java.math.BigInteger;
import java.util.Arrays;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.csp.intension.ICspToSatEncoder;
import org.sat4j.csp.intension.IIntensionCtrEncoder;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * An objective function builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class ObjBuilder {
	

	private final ICspToSatEncoder cspToSatEncoder;

	private IIntensionCtrEncoder intensionEnc;
	
	public ObjBuilder(final IPBSolver solver, final IIntensionCtrEncoder intensionEnc) {
		this.intensionEnc = intensionEnc;
		this.cspToSatEncoder = intensionEnc.getSolver();
	}
	
	public void buildObjToMinimize(final String id, final XVarInteger x) {
		this.cspToSatEncoder.setObjectiveFunction(buildObjForVar(x));
	}
	
	private ObjectiveFunction buildObjForVar(final XVarInteger x) {
		final String varId = x.id;
		int[] domain = this.cspToSatEncoder.getCspVarDomain(varId);
		final IVecInt literals = new VecInt(domain.length);
		final IVec<BigInteger> coeffs = new Vec<BigInteger>(domain.length);
		for(Integer val : domain) {
			literals.push(this.cspToSatEncoder.getSolverVar(varId, val));
			coeffs.push(BigInteger.valueOf(val));
		}
		return new ObjectiveFunction(literals, coeffs);
	}

	public void buildObjToMaximize(final String id, final XVarInteger x) {
		this.cspToSatEncoder.setObjectiveFunction(buildObjForVar(x).negate());
	}
	
	private String opExpr(final String op, final XVarInteger[] xlist, final int[] xcoeffs) {
		final StringBuffer sb = new StringBuffer();
		sb.append(op);
		sb.append('(');
		sb.append(chainObjVars(xlist, xcoeffs));
		sb.append(')');
		return sb.toString();
	}
	
	private String chainObjVars(final XVarInteger[] xlist, final int[] xcoeffs) {
		final StringBuffer sb = new StringBuffer();
		for(int i=0; i<xlist.length; ++i) {
			if(i>0) sb.append(',');
			if(xcoeffs[i] == 1) {
				sb.append(CtrBuilderUtils.normalizeCspVarName(xlist[i].id));
			} else {
				sb.append("mul(");
				sb.append(CtrBuilderUtils.normalizeCspVarName(xlist[i].id));
				sb.append(',');
				sb.append(xcoeffs[i]);
				sb.append(")");
			}
		}
		sb.append(')');
		return sb.toString();
	}

	public void buildObjToMinimize(final String id, final TypeObjective type, final XVarInteger[] xlist, final int[] xcoeffs) {
		ObjectiveFunction obj = null;
		switch(type) {
		case SUM:
			obj = buildSumObjToMinimize(xlist, xcoeffs);
			break;
		case PRODUCT:
			obj = buildExprObjToMinimize(opExpr("mul", xlist, xcoeffs));
			break;
		case MAXIMUM:
			obj = buildExprObjToMinimize(opExpr("max", xlist, xcoeffs));
			break;
		case MINIMUM:
			obj = buildExprObjToMinimize(opExpr("min", xlist, xcoeffs));
			break;
		case NVALUES:
			obj = buildExprObjToMinimize(nValuesExpr(xlist, xcoeffs));
			break;
		case LEX:
			obj = buildLexObjToMinimize(xlist, xcoeffs);
			break;
		case EXPRESSION:
			throw new UnsupportedOperationException();
		}
		this.cspToSatEncoder.setObjectiveFunction(obj);
	}
	
	private String nValuesExpr(final XVarInteger[] list, final int[] coeffs) {
		final StringBuffer sbuf = new StringBuffer();
		boolean firstAddMember = true;
		sbuf.append("add(");
		for(int i=0; i<list.length; ++i) {
			if(!firstAddMember) {
				sbuf.append(',');
			}
			if(i == 0) {
				sbuf.append('1');
				firstAddMember = false;
				continue;
			}
			final String normVar = coeffs[i] == 1 ? CtrBuilderUtils.normalizeCspVarName(list[i].id) : "mul("+CtrBuilderUtils.normalizeCspVarName(list[i].id)+","+coeffs[i]+")";
			sbuf.append("if(and(");
			boolean firstAndMember = true;
			for(int j=0; j<i; ++j) {
				if(!firstAndMember) {
					sbuf.append(',');
					firstAndMember = false;
				}
				final String normOtherVar = coeffs[j] == 1 ? CtrBuilderUtils.normalizeCspVarName(list[j].id) : "mul("+CtrBuilderUtils.normalizeCspVarName(list[j].id)+","+coeffs[j]+")";
				sbuf.append("ne(").append(normVar).append(',').append(normOtherVar).append(')');
				firstAndMember = false;
			}
			sbuf.append("),1,0)");
			firstAddMember = false;
		}
		sbuf.append(')');
		return sbuf.toString();
	}

	public void buildObjToMaximize(final String id, final TypeObjective type, final XVarInteger[] xlist, final int[] xcoeffs) {
		buildObjToMinimize(id, type, xlist, xcoeffs);
		this.cspToSatEncoder.getObjectiveFunction().negate();
	}

	private ObjectiveFunction buildLexObjToMinimize(final XVarInteger[] xlist, final int[] xcoeffs) {
		long max = Long.MIN_VALUE;
		long min = Long.MAX_VALUE;
		for(int i=0; i<xlist.length; ++i) {
			max = Math.max(max, ((XDomInteger) xlist[i].dom).getLastValue());
			min = Math.min(max, ((XDomInteger) xlist[i].dom).getLastValue());
		}
		if(min < 0) throw new UnsupportedOperationException("negative coeff");
		final ObjectiveFunction obj = new ObjectiveFunction();
		final BigInteger step = BigInteger.valueOf(max+1);
		BigInteger fact = BigInteger.ONE;
		for(int i=xlist.length; i>=0; --i) {
			obj.add(buildObjForVar(xlist[i]).multiply(fact));
			fact = fact.multiply(step);
		}
		return obj;
	}

	private ObjectiveFunction buildExprObjToMinimize(String expr) {
		return this.intensionEnc.encodeObj(expr);
	}

	private ObjectiveFunction buildSumObjToMinimize(final XVarInteger[] xlist, final int[] xcoeffs) {
		final ObjectiveFunction obj = new ObjectiveFunction();
		final int size = xlist.length;
		for(int i=0; i<size; ++i) {
			obj.add(buildObjForVar(xlist[i]).multiply(BigInteger.valueOf(xcoeffs[i])));
		}
		return obj;
	}

	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list) {
		final int[] coeffs = new int[list.length];
		Arrays.fill(coeffs, 1);
		buildObjToMinimize(id, type, list, coeffs);
	}
	
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		final int[] coeffs = new int[list.length];
		Arrays.fill(coeffs, 1);
		buildObjToMaximize(id, type, list, coeffs);
	}

	public void buildObjToMinimize(String id, XNodeParent<XVarInteger> syntaxTreeRoot) {
		buildExprObjToMinimize(CtrBuilderUtils.syntaxTreeRootToString(syntaxTreeRoot));
	}
	
	public void buildObjToMaximize(String id, XNodeParent<XVarInteger> syntaxTreeRoot) {
		buildExprObjToMinimize(CtrBuilderUtils.syntaxTreeRootToString(syntaxTreeRoot));
		this.cspToSatEncoder.getObjectiveFunction().negate();
	}

}
