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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.sat4j.core.Vec;
import org.sat4j.csp.Domain;
import org.sat4j.csp.Domains;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.Var;
import org.sat4j.csp.constraints.GentSupports;
import org.sat4j.csp.constraints.Nogoods;
import org.sat4j.csp.constraints.Relation;
import org.sat4j.csp.constraints3.AllDifferentCtrBuilder;
import org.sat4j.csp.constraints3.ChannelCtrBuilder;
import org.sat4j.csp.constraints3.IntensionCtrBuilder;
import org.sat4j.csp.constraints3.LexCtrBuilder;
import org.sat4j.csp.constraints3.NoOverlapCtrBuilder;
import org.sat4j.csp.constraints3.ObjBuilder;
import org.sat4j.csp.constraints3.SumCtrBuilder;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.w3c.dom.Document;
import org.xcsp.parser.XCallbacks2;
import org.xcsp.parser.XDomains.XDomInteger;
import org.xcsp.parser.XEnums.TypeArithmeticOperator;
import org.xcsp.parser.XEnums.TypeConditionOperatorRel;
import org.xcsp.parser.XEnums.TypeFlag;
import org.xcsp.parser.XEnums.TypeObjective;
import org.xcsp.parser.XEnums.TypeOperator;
import org.xcsp.parser.XNodeExpr.XNodeParent;
import org.xcsp.parser.XParser;
import org.xcsp.parser.XParser.Condition;
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
	
	/** object dedicated to allDifferent constraints building */
	private AllDifferentCtrBuilder allDifferentBuilder;
	
	/** object dedicated to channel constraints building */
	private ChannelCtrBuilder channelBuilder;
	
	/** object dedicated to lex constraints building */
	private LexCtrBuilder lexBuilder;
	
	/** object dedicated to noOverlap constraints building */
	private NoOverlapCtrBuilder noOverlapBuilder;
	
	/** object dedicated to intension (and associated "primitive") constraints building */
	private IntensionCtrBuilder intensionBuilder;
	
	/** object dedicated to sum constraints building */
	private SumCtrBuilder sumBuilder;
	
	/** object dedicated to objective function building */
	private ObjBuilder objBuilder;
	
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
		this.allDifferentBuilder = new AllDifferentCtrBuilder(solver, varmapping);
		this.channelBuilder = new ChannelCtrBuilder(solver, varmapping);
		this.lexBuilder = new LexCtrBuilder(solver, varmapping);
		this.noOverlapBuilder = new NoOverlapCtrBuilder(solver, varmapping);
		this.intensionBuilder = new IntensionCtrBuilder(solver, varmapping);
		this.sumBuilder = new SumCtrBuilder(solver, varmapping);
		this.objBuilder = new ObjBuilder(solver, varmapping, firstInternalVarMapping);
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
		Var cspVar = new Var(normalizeCspVarName(var.id), dom, this.solver.nextFreeVarId(false)-1);
		this.firstInternalVarMapping.put(cspVar, this.solver.nextFreeVarId(false));
		try {
			cspVar.toClause(solver);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
		this.varmapping.put(var.id, cspVar);
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
		this.contradictionFound |= this.intensionBuilder.buildCtrPrimitive(id, x, op, k);
	}
	
	/**
	 * @see XCallbacks2#buildCtrPrimitive(String, XVarInteger, TypeArithmeticOperator, XVarInteger, TypeConditionOperatorRel, int)
	 */
	@Override
	public void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, int k) {
		this.contradictionFound |= this.intensionBuilder.buildCtrPrimitive(id, x, opa, y, op, k);
	}

	/**
	 * @see XCallbacks2#buildCtrIntension(String, XVarInteger[], XNodeParent)
	 */
	@Override
	public void buildCtrIntension(String id, XVarInteger[] xscope, XNodeParent syntaxTreeRoot) {
		this.contradictionFound |= this.intensionBuilder.buildCtrIntension(id, xscope, syntaxTreeRoot);
	}
	
	/**
	 * @see XCallbacks2#buildCtrAllDifferent(String, XVarInteger[])
	 */
	@Override
	public void buildCtrAllDifferent(String id, XVarInteger[] list) {
		this.contradictionFound |= this.allDifferentBuilder.buildCtrAllDifferent(id, list);
	}
	
	/**
	 * @see XCallbacks2#buildCtrAllDifferentList(String, XVarInteger[][])
	 */
	@Override
	public void buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		this.contradictionFound |= this.allDifferentBuilder.buildCtrAllDifferentList(id, lists);
	}

	/**
	 * @see XCallbacks2#buildCtrAllDifferentExcept(String, XVarInteger[], int[])
	 */
	@Override
	public void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		this.contradictionFound |= this.allDifferentBuilder.buildCtrAllDifferentExcept(id, list, except);
	}

	/**
	 * @see XCallbacks2#buildCtrAllDifferentMatrix(String, XVarInteger[][])
	 */
	@Override
	public void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		this.contradictionFound |= this.allDifferentBuilder.buildCtrAllDifferentMatrix(id, matrix);		
	}
	
	private String normalizeCspVarName(String name) {
		return name.replaceAll("\\[", VAR_NAME_OP_BRACK_REPL).replaceAll("\\]", VAR_NAME_CL_BRACK_REPL);
	}
	
	/**
	 * @see XCallbacks2#buildObjToMinimize(String, XVarInteger)
	 */
	@Override
	public void buildObjToMinimize(String id, XVarInteger x) {
		this.objBuilder.buildObjToMaximize(id, x);
	}
	
	/**
	 * @see XCallbacks2#buildObjToMaximize(String, XVarInteger)
	 */
	@Override
	public void buildObjToMaximize(String id, XVarInteger x) {
		this.objBuilder.buildObjToMaximize(id, x);
	}
	
	/**
	 * @see XCallbacks2#buildObjToMinimize(String, TypeObjective, XVarInteger[], int[])
	 */
	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] xlist, int[] xcoeffs) {
		this.objBuilder.buildObjToMinimize(id, type, xlist, xcoeffs);
	}
	
	/**
	 * @see XCallbacks2#buildObjToMaximize(String, TypeObjective, XVarInteger[], int[])
	 */
	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] xlist, int[] xcoeffs) {
		this.objBuilder.buildObjToMaximize(id, type, xlist, xcoeffs);
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
		this.objBuilder.buildObjToMinimize(id, type, list);
	}

	/**
	 * @see XCallbacks2#buildObjToMaximize(String, TypeObjective, XVarInteger[])
	 */
	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		this.objBuilder.buildObjToMaximize(id, type, list);
	}
	
	/**
	 * @see XCallbacks2#buildCtrNoOverlap(String, XVarInteger[], int[], boolean)
	 */
	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		this.contradictionFound |= this.noOverlapBuilder.buildCtrNoOverlap(id, origins, lengths, zeroIgnored);
	}
	
	/**
	 * @see XCallbacks2#buildCtrNoOverlap(String, XVarInteger[][], int[][], boolean)
	 */
	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		this.contradictionFound |= this.noOverlapBuilder.buildCtrNoOverlap(id, origins, lengths, zeroIgnored);
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
		this.contradictionFound |= this.sumBuilder.buildCtrSum(id, list, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrSum(String, XVarInteger[], int[], Condition)
	 */
	public void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition) {
		this.contradictionFound |= this.sumBuilder.buildCtrSum(id, list, coeffs, condition);
	}
	
	/**
	 * @see XCallbacks2#buildCtrLex(String, XVarInteger[][], TypeOperator)
	 */
	@Override
	public void buildCtrLex(String id, XVarInteger[][] lists, TypeOperator operator) {
		this.contradictionFound |= this.lexBuilder.buildCtrLex(id, lists, operator);
	}

	/**
	 * @see XCallbacks2#buildCtrLexMatrix(String, XVarInteger[][], TypeOperator)
	 */
	@Override
	public void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperator operator) {
		this.contradictionFound |= this.lexBuilder.buildCtrLexMatrix(id, matrix, operator);
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
	
	/**
	 * @see XCallbacks2#buildCtrChannel(String, XVarInteger[], int)
	 */
	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex) {
		this.contradictionFound |= this.channelBuilder.buildCtrChannel(id, list, startIndex);
	}
	
	/**
	 * @see XCallbacks2#buildCtrChannel(String, XVarInteger[], int, XVarInteger)
	 */
	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value) {
		this.contradictionFound |= this.channelBuilder.buildCtrChannel(id, list, startIndex, value);
	}

	/**
	 * @see XCallbacks2#buildCtrChannel(String, XVarInteger[], int, XVarInteger[], int)
	 */
	@Override
	public void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2) {
		this.contradictionFound |= this.channelBuilder.buildCtrChannel(id, list1, startIndex1, list2, startIndex2);
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
