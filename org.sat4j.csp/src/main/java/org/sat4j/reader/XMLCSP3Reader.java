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
package org.sat4j.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.csp.Domain;
import org.sat4j.csp.Domains;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.Var;
import org.sat4j.csp.constraints.AllDiffCard;
import org.sat4j.csp.constraints.GentSupports;
import org.sat4j.csp.constraints.Nogoods;
import org.sat4j.csp.constraints.Relation;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.w3c.dom.Document;
import org.xcsp.parser.XCallbacks2;
import org.xcsp.parser.XDomains.XDomInteger;
import org.xcsp.parser.XEnums.TypeArithmeticOperator;
import org.xcsp.parser.XEnums.TypeConditionOperatorRel;
import org.xcsp.parser.XEnums.TypeFlag;
import org.xcsp.parser.XEnums.TypeObjective;
import org.xcsp.parser.XEnums.TypeOperator;
import org.xcsp.parser.XNodeExpr;
import org.xcsp.parser.XNodeExpr.XNodeLeaf;
import org.xcsp.parser.XNodeExpr.XNodeParent;
import org.xcsp.parser.XParser.Condition;
import org.xcsp.parser.XParser.ConditionVal;
import org.xcsp.parser.XParser.ConditionVar;
import org.xcsp.parser.XParser;
import org.xcsp.parser.XValues.IntegerEntity;
import org.xcsp.parser.XVariables.VEntry;
import org.xcsp.parser.XVariables.XArray;
import org.xcsp.parser.XVariables.XVar;
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * A reader for XCSP3 instance format.
 * Reads an instance and encodes it as a SAT problem using an {@link IPBSolver}.
 * This class may lack some XCSP3 capabilities handling - work in progress.
 * 
 * @author Emmanuel Lonca <lonca@cril.fr>
 *
 */
public class XMLCSP3Reader extends Reader implements XCallbacks2 {

	/** String replacement for opening brackets in variable names */
	private static final String VAR_NAME_OP_BRACK_REPL = "_";
	
	/** String replacement for closing brackets in variable names */
	private static final String VAR_NAME_CL_BRACK_REPL = "";

	/** the solver in which the problem is encoded */
	private IPBSolver solver;
	
	/** the last solver internal variable used to encode a CSP variable */
	private int lastVarNumber;
	
	/** contains all the variables defined in the instance, including the ones not explicitly create by buildVarXXX methods */
	private List<XVar> allVars = new ArrayList<>();
	
	/** a mapping from the CSP variable names to Sat4j CSP variables */
	private final Map<String, Var> varmapping = new LinkedHashMap<String, Var>();
	
	/** a mapping from a Sat4j CSP variable to the first solver internal variable used to encode it */
	private final Map<Var, Integer> firstInternalVarMapping = new LinkedHashMap<Var, Integer>();
	
	/** the {@link Domains} class instance */
	private Domains domains = Domains.getInstance();

	/** a flag set iff a contradiction has been bound while parsing */
	private boolean contradictionFound = false;
	
	/**
	 * Builds a new parser.
	 * 
	 * @param aSolver the solver in which the problem will be encoded
	 */
	public XMLCSP3Reader(ISolver aSolver) {
		if(!(aSolver instanceof IPBSolver)) {
			throw new IllegalArgumentException("provided solver must have PB capabilities");
		}
		this.solver = new PseudoOptDecorator((IPBSolver) aSolver);
		this.solver.setVerbose(true);
	}
	
	/**
	 * @see Reader#decode(int[], PrintWriter)
	 */
	@Override
	public void decode(int[] model, PrintWriter out) {
		out.print(decode(model));
	}

	/**
	 * @see Reader#decode(int[])
	 */
	@Override
	public String decode(int[] model) {
		StringBuffer sbuf = new StringBuffer();
		for(XVar xvar : this.allVars) {
			Var var = varmapping.get(xvar.id);
			sbuf.append(var == null ? ((XDomInteger)xvar.dom).getFirstValue() : var.findValue(model));
			sbuf.append(' ');
		}
		return sbuf.toString();
	}

	/**
	 * @see Reader#parseInstance(InputStream)
	 */
	@Override
	public IProblem parseInstance(InputStream in) throws ParseFormatException,
			ContradictionException, IOException {
		callbacksParameters.remove(XCallbacksParameters.RECOGNIZE_SPECIAL_TERNARY_INTENSION_CASES);
		try {
			loadInstance(in);
			if(System.getProperty("justRead") != null) {
				System.out.println("c the solver has been set to exit after reading. Exiting now with \"SUCCESS\" status code.");
				System.exit(0);
			}
		} catch(ParseFormatException | ContradictionException | IOException e) {
			throw e;
		} catch (Exception e) {
			throw new ParseFormatException(e);
		}
		if(this.contradictionFound) {
			throw new ContradictionException();
		}
		return this.solver;
	}
	
	/**
	 * Loads a XCSP3 instance.
	 * 
	 * @param in an input stream in which instance is read
	 * @throws Exception if any kind of error makes the parsing fail
	 */
	public void loadInstance(InputStream in) throws Exception {
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		XParser parser = new XParser(document);
		beginInstance(parser.typeFramework);
		beginVariables(parser.vEntries);
		loadVariables(parser);
		endVariables();
		beginConstraints(parser.cEntries);
		loadConstraints(parser);
		endConstraints();
		beginObjectives(parser.oEntries, parser.typeCombination);
		loadObjectives(parser.oEntries);
		endObjectives();
		endInstance();
	}

	/**
	 * @see XCallbacks2#buildVarInteger(XVarInteger, int, int)
	 */
	@Override
	public void buildVarInteger(XVarInteger var, int minValue, int maxValue) {
		Domain dom = domains.getDomain(minValue, maxValue);
		createNewCspVar(var, dom);
	}
	
	/**
	 * @see XCallbacks2#buildVarInteger(XVarInteger, int[])
	 */
	@Override
	public void buildVarInteger(XVarInteger var, int[] values) {
		Domain dom = domains.getDomain(values);
		createNewCspVar(var, dom);
	}

	private void createNewCspVar(XVarInteger var, Domain dom) {
		Var cspVar = new Var(normalizeCspVarName(var.id), dom, this.lastVarNumber);
		this.firstInternalVarMapping.put(cspVar, this.solver.nextFreeVarId(false));
		try {
			cspVar.toClause(solver);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
		this.varmapping.put(var.id, cspVar);
		this.lastVarNumber += dom.size();
	}
	
	/**
	 * @see XCallbacks2#buildCtrExtension(String, XVarInteger[], int[][], boolean, Set)
	 */
	@Override
	public void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags) {
		if(flags.contains(TypeFlag.STARRED_TUPLES)) {
			throw new UnsupportedOperationException();
		}
		IVec<Var> scope = new Vec<Var>(list.length);
		IVec<Evaluable> vars = new Vec<Evaluable>(list.length); 
		for(int i=0; i<list.length; ++i) {
			scope.push(this.varmapping.get(list[i].id));
			vars.push(this.varmapping.get(list[i].id));
		}
		Relation cstr = positive ? new GentSupports(list.length, tuples.length) : new Nogoods(list.length, tuples.length);
		for(int i=0; i<tuples.length; ++i) {
			cstr.addTuple(i, tuples[i]);
		}
		try {
			cstr.toClause(this.solver, scope, vars); // TODO : check scope == vars
		} catch (ContradictionException e) {
			this.contradictionFound  = true;
		}
	}
	
	/**
	 * @see XCallbacks2#buildCtrPrimitive(String, XVarInteger, TypeConditionOperatorRel, int)
	 */
	@Override
	public void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorRel op, int k) {
		String expr = op.name().toLowerCase()+"("+normalizeCspVarName(x.id)+","+k+")";
		IVec<Var> scope = new Vec<Var>(1);
		scope.push(this.varmapping.get(x.id));
		IVec<Evaluable> vars = new Vec<Evaluable>(1);
		vars.push(this.varmapping.get(x.id));
		Predicate p = new Predicate();
		p.addVariable(normalizeCspVarName(x.id));
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}
	
	/**
	 * @see XCallbacks2#buildCtrPrimitive(String, XVarInteger, TypeArithmeticOperator, XVarInteger, TypeConditionOperatorRel, int)
	 */
	@Override
	public void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, int k) {
		String expr = op.name().toLowerCase()+"("+opa.name().toLowerCase()+"("+normalizeCspVarName(x.id)+","+normalizeCspVarName(y.id)+"),"+k+")";
		Vec<Var> scope = new Vec<Var>(new Var[]{this.varmapping.get(x.id), this.varmapping.get(y.id)});
		Vec<Evaluable> vars = new Vec<Evaluable>(new Evaluable[]{this.varmapping.get(x.id), this.varmapping.get(y.id)});
		Predicate p = new Predicate();
		p.addVariable(normalizeCspVarName(x.id));
		p.addVariable(normalizeCspVarName(y.id));
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}

	/**
	 * @see XCallbacks2#buildCtrIntension(String, XVarInteger[], XNodeParent)
	 */
	@Override
	public void buildCtrIntension(String id, XVarInteger[] xscope, XNodeParent syntaxTreeRoot) {
		syntaxTreeRootToString(syntaxTreeRoot);
		Vec<Var> scope = new Vec<Var>(xscope.length);
		Vec<Evaluable> vars = new Vec<Evaluable>(xscope.length);
		Predicate p = new Predicate();
		for(XVarInteger vxscope : xscope) {
			String strVar = vxscope.toString();
			p.addVariable(normalizeCspVarName(strVar));
			scope.push(varmapping.get(strVar));
			vars.push(varmapping.get(strVar));
		}
		String expr = syntaxTreeRootToString(syntaxTreeRoot);
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}

	private String syntaxTreeRootToString(XNodeParent syntaxTreeRoot) {
		StringBuffer treeToString = new StringBuffer();
		fillSyntacticStrBuffer(syntaxTreeRoot, treeToString);
		return treeToString.toString();
	}

	private void fillSyntacticStrBuffer(XNodeExpr root,
			StringBuffer treeToString) {
		if(root instanceof XNodeLeaf) {
			treeToString.append(normalizeCspVarName(root.toString()));
			return;
		}
		treeToString.append(root.getType().toString().toLowerCase());
		XNodeExpr[] sons = ((XNodeParent) root).sons;
		treeToString.append('(');
		fillSyntacticStrBuffer(sons[0], treeToString);
		for(int i=1; i<sons.length; ++i) {
			treeToString.append(',');
			fillSyntacticStrBuffer(sons[i], treeToString);
		}
		treeToString.append(')');
	}
	
	/**
	 * @see XCallbacks2#buildCtrAllDifferent(String, XVarInteger[])
	 */
	@Override
	public void buildCtrAllDifferent(String id, XVarInteger[] list) {
		Vec<Var> scope = new Vec<Var>(list.length);
		Vec<Evaluable> vars = new Vec<Evaluable>(list.length);
		for(XVarInteger vxscope : list) {
			String strVar = vxscope.toString();
			scope.push(varmapping.get(strVar));
			vars.push(varmapping.get(strVar));
		}
		AllDiffCard card = new AllDiffCard();
		try {
			card.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}
	
	/**
	 * @see XCallbacks2#buildCtrAllDifferentList(String, XVarInteger[][])
	 */
	@Override
	public void buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		for(XVarInteger[] list : lists) {
			buildCtrAllDifferent(id, list);
		}
	}

	/**
	 * @see XCallbacks2#buildCtrAllDifferentExcept(String, XVarInteger[], int[])
	 */
	@Override
	public void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		if(except.length == 0) {
			buildCtrAllDifferent(id, list);
			return;
		}
		Vec<Var> scope = new Vec<Var>(list.length);
		Vec<Evaluable> vars = new Vec<Evaluable>(list.length);
		Predicate p = new Predicate();
		String exceptBase = "eq(X,"+except[0]+")";
		for(int i=1; i<except.length; ++i) {
			exceptBase = "or("+exceptBase+",eq(X,"+except[i]+"))";
		}
		String[] exprs = new String[list.length-1];
		for(int i=0; i<list.length-1; ++i) {
			scope.push(varmapping.get(list[i].id));
			vars.push(varmapping.get(list[i].id));
			String normalizedCurVar = normalizeCspVarName(list[i].id);
			p.addVariable(normalizedCurVar);
			String exceptExpr = exceptBase.replaceAll("X", normalizedCurVar);
			String neExpr = "ne("+normalizedCurVar+","+normalizeCspVarName(list[i+1].id)+")";
			for(int j=i+2; j<list.length; ++j) {
				neExpr = "and("+neExpr+",ne("+normalizedCurVar+","+normalizeCspVarName(list[j].id)+"))";
			}
			exprs[i] = "or("+exceptExpr+","+neExpr+")";
		}
		XVarInteger lastVar = list[list.length-1];
		scope.push(varmapping.get(lastVar.id));
		vars.push(varmapping.get(lastVar.id));
		String normalizedCurVar = normalizeCspVarName(lastVar.id);
		p.addVariable(normalizedCurVar);
		p.setExpression(chainExpressions(exprs, "and"));
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}

	/**
	 * @see XCallbacks2#buildCtrAllDifferentMatrix(String, XVarInteger[][])
	 */
	@Override
	public void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		XVarInteger[][] tMatrix = transposeMatrix(matrix);
		for(int i=0; i<matrix.length; ++i) {
			buildCtrAllDifferent(id, matrix[i]);
			buildCtrAllDifferent(id, tMatrix[i]);
		}		
	}
	
	private String normalizeCspVarName(String name) {
		return name.replaceAll("\\[", VAR_NAME_OP_BRACK_REPL).replaceAll("\\]", VAR_NAME_CL_BRACK_REPL);
	}
	
	/**
	 * @see XCallbacks2#buildObjToMinimize(String, XVarInteger)
	 */
	@Override
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
	
	/**
	 * @see XCallbacks2#buildObjToMaximize(String, XVarInteger)
	 */
	@Override
	public void buildObjToMaximize(String id, XVarInteger x) {
		buildObjToMinimize(id, x);
		this.solver.getObjectiveFunction().negate();
	}
	
	/**
	 * @see XCallbacks2#buildObjToMinimize(String, TypeObjective, XVarInteger[], int[])
	 */
	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] xlist, int[] xcoeffs) {
		ObjectiveFunction globalObj = null;
		switch(type) {
		case SUM:
		case MINIMUM:
			globalObj = buildObjForVarSum(xlist, xcoeffs);
			break;
		case MAXIMUM:
			globalObj = buildObjForVarMax(xlist, xcoeffs);
			break;
		default:
			throw new UnsupportedOperationException("This kind of objective function is not handled yet");
		}
		this.solver.setObjectiveFunction(globalObj);
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
			int[] xcoeffs) {
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
			try {
				this.solver.addAtLeast(boundCstrParams.getVars(), boundCstrParams.getCoeffs(), BigInteger.ZERO);
			} catch (ContradictionException e) {
				this.contradictionFound = true;
			}
		}
		return finalObj;
	}
	
	private ObjectiveFunction buildObjForVarMin(XVarInteger[] xlist,
			int[] xcoeffs) {
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
			try {
				this.solver.addAtMost(boundCstrParams.getVars(), boundCstrParams.getCoeffs(), BigInteger.ZERO);
			} catch (ContradictionException e) {
				this.contradictionFound = true;
			}
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
		lastVarNumber += nNewVars;
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
	
	/**
	 * @see XCallbacks2#buildObjToMaximize(String, TypeObjective, XVarInteger[], int[])
	 */
	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] xlist, int[] xcoeffs) {
		ObjectiveFunction globalObj = null;
		switch(type) {
		case SUM:
		case MAXIMUM:
			globalObj = buildObjForVarSum(xlist, xcoeffs);
			globalObj.negate();
			break;
		case MINIMUM:
			globalObj = buildObjForVarMin(xlist, xcoeffs);
			break;
		default:
			throw new UnsupportedOperationException("This kind of objective function is not handled yet");
		}
		this.solver.setObjectiveFunction(globalObj);
	}
	
	/**
	 * @see XCallbacks2#buildObjToMinimize(String, XNodeParent)
	 */
	@Override
	public void buildObjToMinimize(String id, XNodeParent syntaxTreeRoot) {
		unimplementedCase(id);
		// TODO
	}

	/**
	 * @see XCallbacks2#buildObjToMaximize(String, XNodeParent)
	 */
	@Override
	public void buildObjToMaximize(String id, XNodeParent syntaxTreeRoot) {
		unimplementedCase(id);
		// TODO
	}

	/**
	 * @see XCallbacks2#buildObjToMinimize(String, TypeObjective, XVarInteger[])
	 */
	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list) {
		int[] coeffs = new int[list.length];
		Arrays.fill(coeffs, 1);
		buildObjToMinimize(id, type, list, coeffs);
	}

	/**
	 * @see XCallbacks2#buildObjToMaximize(String, TypeObjective, XVarInteger[])
	 */
	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		int[] coeffs = new int[list.length];
		Arrays.fill(coeffs, 1);
		buildObjToMaximize(id, type, list, coeffs);
	}
	
	/**
	 * @see XCallbacks2#buildCtrNoOverlap(String, XVarInteger[], int[], boolean)
	 */
	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		if(!zeroIgnored) {
			throw new UnsupportedOperationException("not implemented yet: zeroIgnored=false in buildCtrNoOverlap");
		}
		for(int i=0; i<origins.length-1; ++i) {
			for(int j=i+1; j<origins.length; ++j) {
				XVarInteger var1 = origins[i];
				XVarInteger var2 = origins[j];
				int length1 = lengths[i];
				int length2 = lengths[j];
				Vec<Var> scope = new Vec<Var>(new Var[]{this.varmapping.get(var1.id), this.varmapping.get(var2.id)});
				Vec<Evaluable> vars = new Vec<Evaluable>(new Evaluable[]{this.varmapping.get(var1.id), this.varmapping.get(var2.id)});
				buildDirectionalNoOverlapCstr(var1, var2, length1, scope, vars);
				buildDirectionalNoOverlapCstr(var2, var1, length2, scope, vars);
			}
		}
	}

	private void buildDirectionalNoOverlapCstr(XVarInteger var1,
			XVarInteger var2, int length1, Vec<Var> scope, Vec<Evaluable> vars) {
		Predicate p = new Predicate();
		String normalize2 = normalizeCspVarName(var2.id);
		p.addVariable(normalize2);
		String normalized1 = normalizeCspVarName(var1.id);
		p.addVariable(normalized1);
		String expr = "ge(sub("+normalize2+","+normalized1+"),"+length1+")";
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}
	
	/**
	 * @see XCallbacks2#buildCtrNoOverlap(String, XVarInteger[][], int[][], boolean)
	 */
	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		for(int i=0; i<origins.length; ++i) {
			buildCtrNoOverlap(id, origins[i], lengths[i], zeroIgnored);
		}
	}
	
	/**
	 * @see XCallbacks2#buildCtrOrdered(String, XVarInteger[], TypeOperator)
	 */
	@Override
	public void buildCtrOrdered(String id, XVarInteger[] list, TypeOperator operator) {
		for(int i=0; i<list.length-1; ++i) {
			Predicate p = new Predicate();
			String normalized1 = normalizeCspVarName(list[i].id);
			p.addVariable(normalized1);
			String normalized2 = normalizeCspVarName(list[i+1].id);
			p.addVariable(normalized2);
			String expr = operator.name().toLowerCase()+"("+normalized1+","+normalized2+")";
			p.setExpression(expr);
			Vec<Var> scope = new Vec<Var>(new Var[]{this.varmapping.get(list[i].id), this.varmapping.get(list[i+1].id)});
			Vec<Evaluable> vars = new Vec<Evaluable>(new Evaluable[]{this.varmapping.get(list[i].id), this.varmapping.get(list[i+1].id)});
			try {
				p.toClause(this.solver, scope, vars);
			} catch (ContradictionException e) {
				this.contradictionFound = true;
			}
		}
	}
	
	/**
	 * @see XCallbacks2#buildCtrSum(String, XVarInteger[], Condition)
	 */
	public void buildCtrSum(String id, XVarInteger[] list, Condition condition) {
		int[] coeffs = new int[list.length];
		Arrays.fill(coeffs, 1);
		buildCtrSum(id, list, coeffs, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrSum(String, XVarInteger[], int[], Condition)
	 */
	public void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition) {
		Predicate p = new Predicate();
		Vec<Var> scope = new Vec<Var>();
		Vec<Evaluable> vars = new Vec<Evaluable>();
		String varId;
		StringBuffer exprBuf = new StringBuffer();
		exprBuf.append(condition.operator.toString().toLowerCase());
		exprBuf.append('(');
		for(int i=0; i<list.length-1; ++i) {
			exprBuf.append("add(");
			if(coeffs[i] != 1) {
				exprBuf.append("mul(");
				exprBuf.append(coeffs[i]);
				exprBuf.append(',');
			}
			varId = list[i].id;
			addVarToPredExprBuffer(varId, p, scope, vars, exprBuf);
			if(coeffs[i] != 1) {
				exprBuf.append(')');
			}
			exprBuf.append(',');
		}
		varId = list[list.length-1].id;
		addVarToPredExprBuffer(varId, p, scope, vars, exprBuf);
		for(int i=0; i<list.length-1; ++i) {
			exprBuf.append(')');
		}
		exprBuf.append(',');
		if(condition instanceof ConditionVar) {
			varId = ((ConditionVar) condition).x.id;
			addVarToPredExprBuffer(varId, p, scope, vars, exprBuf);
		} else if(condition instanceof ConditionVal) {
			exprBuf.append(((ConditionVal) condition).k);
		} else {
			throw new UnsupportedOperationException("this kind of condition is not supported yet.");
		}
		exprBuf.append(')');
		String expr = exprBuf.toString();
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}

	private void addVarToPredExprBuffer(String varId, Predicate p, Vec<Var> scope,
			Vec<Evaluable> vars, StringBuffer exprBuf) {
		scope.push(this.varmapping.get(varId));
		vars.push(this.varmapping.get(varId));
		String normalizeName = normalizeCspVarName(varId);
		p.addVariable(normalizeName);
		exprBuf.append(normalizeName);
	}
	
	/**
	 * @see XCallbacks2#buildCtrLex(String, XVarInteger[][], TypeOperator)
	 */
	public void buildCtrLex(String id, XVarInteger[][] lists, TypeOperator operator) {
		for(int i=0; i<lists.length-1; ++i) {
			buildCtrLex(id, lists[0], lists[1], operator);
		}
	}

	private void buildCtrLex(String id, XVarInteger[] list1,
			XVarInteger[] list2, TypeOperator operator) {
		TypeOperator strictOp = strictTypeOperator(operator);
		Predicate p = new Predicate();
		Vec<Var> scope = new Vec<Var>();
		Vec<Evaluable> vars = new Vec<Evaluable>();
		for(int i=0; i<list1.length; ++i) {
			String id01 = list1[i].id;
			String id02 = list2[i].id;
			scope.push(this.varmapping.get(id01));
			scope.push(this.varmapping.get(id02));
			vars.push(this.varmapping.get(id01));
			vars.push(this.varmapping.get(id02));
			p.addVariable(normalizeCspVarName(id01));
			p.addVariable(normalizeCspVarName(id02));
		}
		String[] chains = new String[list1.length];
		String id01 = list1[0].id;
		String id02 = list2[0].id;
		chains[0] = list1.length == 1
				? operator.name().toLowerCase()+"("+normalizeCspVarName(id01)+","+normalizeCspVarName(id02)+")"
				: strictOp.name().toLowerCase()+"("+normalizeCspVarName(id01)+","+normalizeCspVarName(id02)+")";
		for(int i=1; i<list1.length; ++i) {
			String eqChain = "eq("+normalizeCspVarName(id01)+","+normalizeCspVarName(id02)+")";
			for(int j=1; j<i; ++j) {
				String idj1 = list1[j].id;
				String idj2 = list2[j].id;
				eqChain = "and("+eqChain+",eq("+normalizeCspVarName(idj1)+","+normalizeCspVarName(idj2)+"))";
			}
			String idi1 = list1[i].id;
			String idi2 = list2[i].id;
			String finalMember =  i == list1.length-1
					? operator.name().toLowerCase()+"("+normalizeCspVarName(idi1)+","+normalizeCspVarName(idi2)+")"
					: strictOp.name().toLowerCase()+"("+normalizeCspVarName(idi1)+","+normalizeCspVarName(idi2)+")";
			chains[i] = "and("+eqChain+","+finalMember+")";
		}
		p.setExpression(chainExpressions(chains, "or"));
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}

	private String chainExpressions(String[] exprs, String op) {
		StringBuffer exprBuff = new StringBuffer();
		for(int i=0; i<exprs.length-1; ++i) {
			exprBuff.append(op);
			exprBuff.append("(");
		}
		exprBuff.append(exprs[0]);
		for(int i=1; i<exprs.length; ++i) {
			exprBuff.append(',');
			exprBuff.append(exprs[i]);
			exprBuff.append(')');
		}
		return exprBuff.toString();
	}
	
	private TypeOperator strictTypeOperator(TypeOperator op) {
		switch(op) {
		case GE: return TypeOperator.GT;
		case LE: return TypeOperator.LT;
		case SUBSEQ: return TypeOperator.SUBSET;
		case SUPSEQ: return TypeOperator.SUPSET;
		default: return op;
		}
	}

	/**
	 * @see XCallbacks2#buildCtrLexMatrix(String, XVarInteger[][], TypeOperator)
	 */
	public void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperator operator) {
		buildCtrLex(id, matrix, operator);
		XVarInteger[][] tMatrix = transposeMatrix(matrix);
		buildCtrLex(id, tMatrix, operator);
	}

	private XVarInteger[][] transposeMatrix(XVarInteger[][] matrix) {
		XVarInteger[][] tMatrix = new XVarInteger[matrix[0].length][matrix.length];
		for(int i=0; i<matrix[0].length; ++i) {
			for(int j=0; j<matrix.length; ++j) {
				tMatrix[i][j] = matrix[j][i];
			}
		}
		return tMatrix;
	}
	
	/**
	 * @see XCallbacks2#buildCtrAllEqual(String, XVarInteger[])
	 */
	@Override
	public void buildCtrAllEqual(String id, XVarInteger[] list) {
		for(int i=0; i<list.length-1; ++i) {
			Predicate p = new Predicate();
			Var[] varArray = new Var[]{varmapping.get(list[i].id), varmapping.get(list[i+1].id)};
			Vec<Var> scope = new Vec<Var>(varArray);
			Vec<Evaluable> vars = new Vec<Evaluable>(varArray);
			String norm1 = normalizeCspVarName(list[i].id);
			p.addVariable(norm1);
			String norm2 = normalizeCspVarName(list[i+1].id);
			p.addVariable(norm2);
			p.setExpression("eq("+norm1+","+norm2+")");
			try {
				p.toClause(this.solver, scope, vars);
			} catch (ContradictionException e) {
				this.contradictionFound = true;
			}
		}
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
	
	private void buildChannelImplCstr(XVarInteger v1, int v1Index, XVarInteger v2, int v2Index) {
		Predicate p = new Predicate();
		Var[] varArray = new Var[]{varmapping.get(v1.id), varmapping.get(v2.id)};
		Vec<Var> scope = new Vec<Var>(varArray);
		Vec<Evaluable> vars = new Vec<Evaluable>(varArray);
		String norm1 = normalizeCspVarName(v1.id);
		p.addVariable(norm1);
		String norm2 = normalizeCspVarName(v2.id);
		p.addVariable(norm2);
		String expr = "or(not(eq("+norm1+","+v2Index+")),eq("+norm2+","+v1Index+"))";
		p.setExpression(expr);
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}
	
	private void buildListCtrChannel(XVarInteger[] list1, int startIndex1,
			XVarInteger[] list2, int startIndex2) {
		for(int i=0; i<list2.length; ++i) {
			XVarInteger var = list2[i];
			XDomInteger domain = (XDomInteger)(var.dom);
			for(int j=0; j<domain.getNbValues(); ++j) {
				int value = (int) ((IntegerEntity) domain.values[j]).smallest();
				buildChannelImplCstr(var, i+startIndex2, list1[value-startIndex1], value);
			}
		}
	}
	
	/**
	 * @see XCallbacks2#buildCtrChannel(String, XVarInteger[], int)
	 */
	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex) {
		checkChannelPrerequisites(list, startIndex);
		buildListCtrChannel(list, startIndex, list, startIndex);
	}
	
	/**
	 * @see XCallbacks2#buildCtrChannel(String, XVarInteger[], int, XVarInteger)
	 */
	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value) {
		buildSumEqOneCstr(list);
		for(int i=0; i<list.length; ++i) {
			XVarInteger var = list[i];
			Predicate p = new Predicate();
			Var[] varArray = new Var[]{varmapping.get(var.id)};
			Vec<Var> scope = new Vec<Var>(varArray);
			Vec<Evaluable> vars = new Vec<Evaluable>(varArray);
			String norm = normalizeCspVarName(var.id);
			p.addVariable(norm);
			String expr = "iff(eq("+norm+",1),eq("+value+","+(i+startIndex)+"))";
			p.setExpression(expr);
			try {
				p.toClause(this.solver, scope, vars);
			} catch (ContradictionException e) {
				this.contradictionFound = true;
			}
		}
	}

	private void buildSumEqOneCstr(XVarInteger[] list) {
		Predicate p = new Predicate();
		Vec<Var> scope = new Vec<Var>(list.length);
		Vec<Evaluable> vars = new Vec<Evaluable>(list.length);
		String[] toChain = new String[list.length];
		for(int i=0; i<list.length; ++i) {
			XVarInteger var = list[i];
			scope.push(varmapping.get(var.id));
			vars.push(varmapping.get(var.id));
			String norm = normalizeCspVarName(var.id);
			p.addVariable(norm);
			toChain[i] = norm;
		}
		p.setExpression("eq("+chainExpressions(toChain, "add")+",1)");
		try {
			p.toClause(this.solver, scope, vars);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
	}

	/**
	 * @see XCallbacks2#buildCtrChannel(String, XVarInteger[], int, XVarInteger[], int)
	 */
	@Override
	public void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2) {
		if(list1.length != list2.length) {
			throw new IllegalArgumentException("lists of different sizes provided as arguments of channel constraint");
		}
		checkChannelPrerequisites(list1, startIndex2);
		checkChannelPrerequisites(list2, startIndex1);
		buildListCtrChannel(list2, startIndex2, list1, startIndex1);
		buildListCtrChannel(list1, startIndex1, list2, startIndex2);
	}
	
	/**
	 * @see XCallbacks2#beginVariables(List)
	 */
	@Override
	public void beginVariables(List<VEntry> vEntries) {
		for(VEntry entry : vEntries) {
			manageEntry(entry);
		}
	}

	private void manageEntry(VEntry entry) {
		if(entry instanceof XArray) {
			XArray array = (XArray) entry;
			for(VEntry subEntry : array.vars) {
				manageEntry(subEntry);
			}
		} else {
			this.allVars.add((XVar) entry);
		}
	}
}
