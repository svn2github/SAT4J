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
import java.util.LinkedHashMap;
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
import org.xcsp.parser.XEnums.TypeArithmeticOperator;
import org.xcsp.parser.XEnums.TypeConditionOperatorRel;
import org.xcsp.parser.XEnums.TypeFlag;
import org.xcsp.parser.XEnums.TypeObjective;
import org.xcsp.parser.XNodeExpr;
import org.xcsp.parser.XNodeExpr.XNodeLeaf;
import org.xcsp.parser.XNodeExpr.XNodeParent;
import org.xcsp.parser.XParser;
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
		StringBuffer sbuf = new StringBuffer();
		for (Var v : varmapping.values()) {
			out.print(v.toString()+" ");
			sbuf.append(v.findValue(model));
			sbuf.append(" ");
		}
		out.println("");
		out.print(sbuf.toString());
	}

	/**
	 * @see Reader#decode(int[])
	 */
	@Override
	public String decode(int[] model) {
		StringBuilder stb = new StringBuilder();
		for (Var v : varmapping.values()) {
			stb.append(v.findValue(model));
			stb.append(" ");
		}
		return stb.toString();
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
		if(flags.contains(TypeFlag.UNCLEAN_TUPLES) || flags.contains(TypeFlag.STARRED_TUPLES)) {
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
		String expr = op.name().toLowerCase()+"("+x.id+","+k+")";
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
		String expr = op.name().toLowerCase()+"("+opa.name().toLowerCase()+"("+x.id+","+y.id+"),"+k+")";
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
		if(type != TypeObjective.SUM) {
			throw new UnsupportedOperationException("This kind of objective function is not handled yet");
		}
		ObjectiveFunction globalObj = buildObjForVarSum(xlist, xcoeffs);
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
	
	/**
	 * @see XCallbacks2#buildObjToMaximize(String, TypeObjective, XVarInteger[], int[])
	 */
	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] xlist, int[] xcoeffs) {
		buildObjToMinimize(id, type, xlist, xcoeffs);
		this.solver.getObjectiveFunction().negate();
	}
	
}
