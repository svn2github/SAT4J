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
import java.util.LinkedHashMap;
import java.util.Map;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.csp.Domain;
import org.sat4j.csp.Var;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.xcsp.parser.XDomains.XDomInteger;
import org.xcsp.parser.XEnums.TypeObjective;
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * An objective function builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class ObjBuilder {
	
	/** the solver in which the problem is encoded */
	private IPBSolver solver;
	
	/** a mapping from the CSP variable names to Sat4j CSP variables */
	private Map<String, Var> varmapping = new LinkedHashMap<String, Var>();
	
	/** a mapping from a Sat4j CSP variable to the first solver internal variable used to encode it */
	private Map<Var, Integer> firstInternalVarMapping;

	public ObjBuilder(IPBSolver solver, Map<String, Var> varmapping, Map<Var, Integer> firstInternalVarMapping) {
		this.solver = solver;
		this.varmapping = varmapping;
		this.firstInternalVarMapping = firstInternalVarMapping;
	}
	
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] xlist, int[] xcoeffs) {
		ObjectiveFunction globalObj = null;
		switch(type) {
		case SUM:
		case MAXIMUM:
			globalObj = buildObjForVarSum(xlist, xcoeffs);
			globalObj.negate();
			break;
		case MINIMUM:
			try {
				globalObj = buildObjForVarMin(xlist, xcoeffs);
			} catch (ContradictionException e) {
				throw new IllegalStateException("Contradiction must not occur while setting objective function !!");
			}
			break;
		default:
			throw new UnsupportedOperationException("This kind of objective function is not handled yet");
		}
		this.solver.setObjectiveFunction(globalObj);
	}

	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list) {
		int[] coeffs = new int[list.length];
		Arrays.fill(coeffs, 1);
		buildObjToMinimize(id, type, list, coeffs);
	}

	public void buildObjToMinimize(String id, XVarInteger x) {
		ObjectiveFunction obj = buildObjForVar(x);
		this.solver.setObjectiveFunction(obj);
	}

	private ObjectiveFunction buildObjForVar(XVarInteger x) {
		Var var = this.varmapping.get(x.id);
		Domain dom = var.domain();
		IVecInt literals = new VecInt(dom.size());
		IVec<BigInteger> coeffs = new Vec<BigInteger>(dom.size());
		Integer firstIndex = this.firstInternalVarMapping.get(var);
		for(int i=0; i<dom.size(); ++i) {
			literals.push(firstIndex+i);
			coeffs.push(BigInteger.valueOf(dom.get(i)));
		}
		ObjectiveFunction obj = new ObjectiveFunction(literals, coeffs);
		return obj;
	}
	
	public void buildObjToMaximize(String id, XVarInteger x) {
		buildObjToMinimize(id, x);
		this.solver.getObjectiveFunction().negate();
	}
	
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] xlist, int[] xcoeffs) {
		ObjectiveFunction globalObj = null;
		switch(type) {
		case SUM:
		case MINIMUM:
			globalObj = buildObjForVarSum(xlist, xcoeffs);
			break;
		case MAXIMUM:
			try {
				globalObj = buildObjForVarMax(xlist, xcoeffs);
			} catch (ContradictionException e) {
				throw new IllegalStateException("Contradiction must not occur while setting objective function !!");
			}
			break;
		default:
			throw new UnsupportedOperationException("This kind of objective function is not handled yet");
		}
		this.solver.setObjectiveFunction(globalObj);
	}
	
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		int[] coeffs = new int[list.length];
		Arrays.fill(coeffs, 1);
		buildObjToMaximize(id, type, list, coeffs);
	}
	
	private ObjectiveFunction buildObjForVarSum(XVarInteger[] xlist,
			int[] xcoeffs) {
		IVecInt lits = new VecInt();
		IVec<BigInteger> coeffs = new Vec<BigInteger>();
		for(int i=0; i<xlist.length; ++i) {
			ObjectiveFunction subObj = buildObjForVar(xlist[i]);
			for(int j=0; j<subObj.getVars().size(); ++j) {
				lits.push(subObj.getVars().get(j));
				coeffs.push(subObj.getCoeffs().get(j).multiply(BigInteger.valueOf(xcoeffs[i])));
			}
		}
		ObjectiveFunction globalObj = new ObjectiveFunction(lits, coeffs);
		return globalObj;
	}
	
	private ObjectiveFunction buildObjForVarMax(XVarInteger[] xlist,
			int[] xcoeffs) throws ContradictionException {
		ObjectiveFunction[] varObjs = new ObjectiveFunction[xlist.length];
		long max = Long.MIN_VALUE;
		for(int i=0; i<xlist.length; ++i) {
			max = Math.max(max, ((XDomInteger) xlist[i].dom).getLastValue());
			varObjs[i] = buildObjForVar(xlist[i]);
		}
		ObjectiveFunction finalObj = buildBoundObj(max);
		for(ObjectiveFunction obj : varObjs) {
			ObjectiveFunction boundCstrParams = buildBoundConstraintParams(obj,
					finalObj);
			this.solver.addAtLeast(boundCstrParams.getVars(), boundCstrParams.getCoeffs(), BigInteger.ZERO);
		}
		return finalObj;
	}
	
	private ObjectiveFunction buildObjForVarMin(XVarInteger[] xlist,
			int[] xcoeffs) throws ContradictionException {
		ObjectiveFunction[] varObjs = new ObjectiveFunction[xlist.length];
		long max = Long.MIN_VALUE;
		for(int i=0; i<xlist.length; ++i) {
			max = Math.max(max, ((XDomInteger) xlist[i].dom).getLastValue());
			varObjs[i] = buildObjForVar(xlist[i]);
		}
		ObjectiveFunction finalObj = buildBoundObj(max);
		for(ObjectiveFunction obj : varObjs) {
			ObjectiveFunction boundCstrParams = buildBoundConstraintParams(obj,
					finalObj);
			this.solver.addAtMost(boundCstrParams.getVars(), boundCstrParams.getCoeffs(), BigInteger.ZERO);
		}
		return finalObj;
	}
	
	private ObjectiveFunction buildBoundConstraintParams(
			ObjectiveFunction varObj, ObjectiveFunction finalObj) {
		IVec<BigInteger> objCoeffs = varObj.getCoeffs();
		IVecInt cstrLits = new VecInt(objCoeffs.size() + finalObj.getVars().size());
		IVec<BigInteger> cstrCoeffs = new Vec<>(objCoeffs.size() + finalObj.getVars().size());
		finalObj.getVars().copyTo(cstrLits);
		finalObj.getCoeffs().copyTo(cstrCoeffs);
		varObj.getVars().copyTo(cstrLits);
		for(int i=0; i<objCoeffs.size(); ++i) {
			cstrCoeffs.push(objCoeffs.get(i).negate());
		}
		ObjectiveFunction boundCstrParams = new ObjectiveFunction(cstrLits, cstrCoeffs);
		return boundCstrParams;
	}

	private ObjectiveFunction buildBoundObj(long max) {
		int nNewVars = 0;
		while(max > 0) {
			++nNewVars;
			max >>= 1;
		}
		IVecInt maxVarLits = new VecInt(nNewVars);
		IVec<BigInteger> maxVarCoeffs = new Vec<>(nNewVars);
		BigInteger fact = BigInteger.ONE;
		for(int i=0; i<nNewVars; ++i) {
			maxVarLits.push(solver.nextFreeVarId(true));
			maxVarCoeffs.push(fact);
			fact = fact.shiftLeft(1);
		}
		ObjectiveFunction finalObj = new ObjectiveFunction(maxVarLits, maxVarCoeffs);
		return finalObj;
	}

}
