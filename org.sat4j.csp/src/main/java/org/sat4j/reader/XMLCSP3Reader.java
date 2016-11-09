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

import org.sat4j.AbstractLauncher;
import org.sat4j.core.VecInt;
import org.sat4j.csp.Domain;
import org.sat4j.csp.Domains;
import org.sat4j.csp.Var;
import org.sat4j.csp.constraints3.ComparisonCtrBuilder;
import org.sat4j.csp.constraints3.ConnectionCtrBuilder;
import org.sat4j.csp.constraints3.CtrBuilderUtils;
import org.sat4j.csp.constraints3.ElementaryCtrBuilder;
import org.sat4j.csp.constraints3.GenericCtrBuilder;
import org.sat4j.csp.constraints3.LanguageCtrBuilder;
import org.sat4j.csp.constraints3.SchedulingCtrBuilder;
import org.sat4j.csp.intension.ICspToSatEncoder;
import org.sat4j.csp.intension.IIntensionCtrEncoder;
import org.sat4j.csp.intension.IntensionCtrEncoderFactory;
import org.sat4j.csp.constraints3.ObjBuilder;
import org.sat4j.csp.constraints3.CountingCtrBuilder;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.w3c.dom.Document;
import org.xcsp.common.Condition;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperator;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.XCallbacks2;
import org.xcsp.parser.XParser;
import org.xcsp.parser.entries.AnyEntry.VEntry;
import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XVariables.XArray;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;
import org.xcsp.parser.entries.XVariables.XVarSymbolic;

/**
 * A reader for XCSP3 instance format.
 * Reads an instance and encodes it as a SAT problem using an {@link IPBSolver}.
 * This class may lack some XCSP3 capabilities handling - work in progress.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class XMLCSP3Reader extends Reader implements XCallbacks2, ICspToSatEncoder {

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
	
	/** object dedicated to elementary constraints building */
	private ElementaryCtrBuilder elementaryCtrBuilder;

	/** object dedicated to allDifferent constraints building */
	private ComparisonCtrBuilder comparisonCtrBuilder;

	/** object dedicated to channel constraints building */
	private ConnectionCtrBuilder connectionCtrBuilder;

	/** object dedicated to noOverlap constraints building */
	private SchedulingCtrBuilder schedulingCtrBuilder;

	/** object dedicated to intension (and associated "primitive") constraints building */
	private GenericCtrBuilder genericCtrBuilder;

	/** object dedicated to sum constraints building */
	private CountingCtrBuilder countingCtrBuilder;
	
	private LanguageCtrBuilder languageCtrBuilder;

	/** object dedicated to objective function building */
	private ObjBuilder objBuilder;

	/** the CSP launchers that made this parser ; needed for model decoding */
	private final AbstractLauncher launcher;

	/**
	 * Builds a new parser.
	 * 
	 * @param aSolver the solver in which the problem will be encoded
	 * @param optimizationProblem tells whether the problem is a decision one or an optimization one
	 */
	public XMLCSP3Reader(ISolver aSolver, AbstractLauncher launcher) {
		if(!(aSolver instanceof IPBSolver)) {
			throw new IllegalArgumentException("provided solver must have PB capabilities");
		}
		this.launcher = launcher;
		this.solver = new PseudoOptDecorator((IPBSolver) aSolver);
		this.solver.setVerbose(true);
		IIntensionCtrEncoder intensionEnc = IntensionCtrEncoderFactory.getInstance().newDefault(this);
		this.elementaryCtrBuilder = new ElementaryCtrBuilder(intensionEnc);
		this.comparisonCtrBuilder = new ComparisonCtrBuilder(intensionEnc);
		this.connectionCtrBuilder = new ConnectionCtrBuilder(intensionEnc);
		this.schedulingCtrBuilder = new SchedulingCtrBuilder(intensionEnc);
		this.genericCtrBuilder = new GenericCtrBuilder(solver, varmapping, intensionEnc); // TODO: refactor to keep only intension encoder as parameter
		this.countingCtrBuilder = new CountingCtrBuilder(intensionEnc);
		this.languageCtrBuilder = new LanguageCtrBuilder(solver, varmapping, firstInternalVarMapping);
		this.objBuilder = new ObjBuilder(solver, varmapping, firstInternalVarMapping);
	}
	
	/**
	 * Builds a new parser for decision problems.
	 * 
	 * @param aSolver the solver in which the problem will be encoded
	 */
	public XMLCSP3Reader(ISolver aSolver) {
		this(aSolver, null);
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
		if(this.launcher == null) {
			throw new IllegalStateException("decoding a model needs to know the solver state");
		}
		if(model.length == 0) return "";
		StringBuffer strModelBuffer = new StringBuffer();
		switch(this.launcher.getExitCode()) {
		case OPTIMUM_FOUND:
			strModelBuffer.append("<instantiation type=\"optimum\" cost=\"")
				.append(this.solver.getObjectiveFunction().calculateDegree(this.solver).toString())
				.append("\">\n");
			appendModel(strModelBuffer, model);
			break;
		case UPPER_BOUND:
		case SATISFIABLE:
			strModelBuffer.append("<instantiation type=\"solution\">\n");
			appendModel(strModelBuffer, model);
			break;
		case UNSATISFIABLE:
			strModelBuffer.append(AbstractLauncher.COMMENT_PREFIX).append(" no model");
			break;
		case UNKNOWN:
		default:
			strModelBuffer.append(AbstractLauncher.COMMENT_PREFIX).append(" unknown state");
			break;
		}
		strModelBuffer.append("v </instantiation>");
		return strModelBuffer.toString();
	}

	private void appendModel(StringBuffer strModelBuffer, int[] model) {
		StringBuffer sbufList = new StringBuffer();
		sbufList.append("v \t<list> ");
		StringBuffer sbufValues = new StringBuffer();
		sbufValues.append("v \t<values> ");
		decodeModel(model, sbufList, sbufValues);
		sbufValues.append(" </values>\n");
		sbufList.append(" </list>\n");
		strModelBuffer.append(sbufList);
		strModelBuffer.append(sbufValues);
	}
	
	public String decodeModelAsValueSequence(int model[]) {
		XVar xvar = this.allVars.get(0);
		Var var = varmapping.get(xvar.id);
		StringBuffer sbufValues = new StringBuffer();
		sbufValues.append(var == null ? ((XDomInteger)xvar.dom).getFirstValue() : var.findValue(model));
		for(int i=1; i<this.allVars.size(); ++i) {
			xvar = this.allVars.get(i);
			var = varmapping.get(xvar.id);
			sbufValues.append(' ').append(var == null ? ((XDomInteger)xvar.dom).getFirstValue() : var.findValue(model));
		}
		return sbufValues.toString();
	}
	
	public boolean discardModel(int model[]) {
		IVecInt cl = new VecInt();
		for(XVar xvar : this.allVars) {
			Var var = this.varmapping.get(xvar.id);
			Integer firstSolverVar = this.firstInternalVarMapping.get(var);
			for(int i=0;;++i) {
				if(model[firstSolverVar+i-1] > 0) {
					cl.push(-firstSolverVar-i);
					break;
				}
			}
		}
		try {
			this.solver.addClause(cl);
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}

	private void decodeModel(int[] model, StringBuffer sbufList, StringBuffer sbufValues) {
		XVar xvar = this.allVars.get(0);
		Var var = varmapping.get(xvar.id);
		sbufList.append(xvar.id());
		sbufValues.append(var == null ? ((XDomInteger)xvar.dom).getFirstValue() : var.findValue(model));
		for(int i=1; i<this.allVars.size(); ++i) {
			xvar = this.allVars.get(i);
			var = varmapping.get(xvar.id);
			sbufList.append(' ').append(xvar.id());
			sbufValues.append(' ').append(var == null ? ((XDomInteger)xvar.dom).getFirstValue() : var.findValue(model));
		}
	}

	/**
	 * @see Reader#parseInstance(InputStream)
	 */
	@Override
	public IProblem parseInstance(InputStream in) throws ParseFormatException,
	ContradictionException, IOException {
		implem().currParameters.remove(XCallbacksParameters.RECOGNIZE_UNARY_PRIMITIVES);
		implem().currParameters.remove(XCallbacksParameters.RECOGNIZE_BINARY_PRIMITIVES);
		implem().currParameters.remove(XCallbacksParameters.RECOGNIZE_TERNARY_PRIMITIVES);
		try {
			loadInstance(in);
			if(System.getProperty("justRead") != null) {
				System.out.println("c the solver has been set to exit after reading. Exiting now with \"SUCCESS\" status code.");
				System.exit(0);
			}
		} catch(ParseFormatException | ContradictionException | IOException e) {
			throw e;
		} catch (Exception e) {
//			throw new ParseFormatException(e);
			e.printStackTrace();
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
		loadObjectives(parser);
		endObjectives();
		endInstance();
	}
	
	@Override
	public int[] getCspVarDomain(String var) {
		final Domain domain = this.varmapping.get(var).domain();
		final int domSize = domain.size();
		int[] domArray = new int[domSize];
		for(int i=0; i<domSize; ++i) {
			domArray[i] = domain.get(i);
		}
		return domArray;
	}

	@Override
	public int getSolverVar(String strVar, Integer value) {
		final Var var = this.varmapping.get(strVar);
		int solverVar = this.firstInternalVarMapping.get(var);
		final Domain domain = var.domain();
		final int domSize = domain.size();
		for(int i=0; i<domSize; ++i) {
			if(domain.get(i) == value) return solverVar;
			++solverVar;
		}
		throw new IllegalArgumentException();
	}

	@Override
	public boolean addClause(int[] clause) {
		try {
			this.solver.addClause(new VecInt(clause));
		} catch (ContradictionException e) {
			return true;
		}
		return false;
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
		Var cspVar = new Var(CtrBuilderUtils.normalizeCspVarName(var.id), dom, this.solver.nextFreeVarId(false)-1);
		this.firstInternalVarMapping.put(cspVar, this.solver.nextFreeVarId(false));
		for(int i=0; i<dom.size(); ++i) this.solver.nextFreeVarId(true);
		try {
			cspVar.toClause(solver);
		} catch (ContradictionException e) {
			this.contradictionFound = true;
		}
		this.varmapping.put(var.id, cspVar);
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


	@Override
	public void buildCtrFalse(String id, XVar[] list) {
		this.contradictionFound = true;
	}

	/**
	 * @see XCallbacks2#buildCtrExtension(String, XVarInteger, int[], boolean, Set)
	 */
	@Override
	public void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags) {
		this.contradictionFound |= this.genericCtrBuilder.buildCtrExtension(id, x, values, positive, flags);
	}

	/**
	 * @see XCallbacks2#buildCtrExtension(String, XVarInteger[], int[][], boolean, Set)
	 */
	@Override
	public void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags) {
		this.contradictionFound |= this.genericCtrBuilder.buildCtrExtension(id, list, tuples, positive, flags);
	}

	/**
	 * @see XCallbacks2#buildCtrPrimitive(String, XVarInteger, TypeConditionOperatorRel, int)
	 */
	@Override
	public void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorRel op, int k) {
		// not implemented ; not necessary due to removal of XCallbacksParameters.RECOGNIZE_SPECIAL_X_INTENSION_CASES
	}

	/**
	 * @see XCallbacks2#buildCtrPrimitive(String, XVarInteger, TypeArithmeticOperator, XVarInteger, TypeConditionOperatorRel, int)
	 */
	@Override
	public void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, int k) {
		// not implemented ; not necessary due to removal of XCallbacksParameters.RECOGNIZE_SPECIAL_X_INTENSION_CASES
	}

	/**
	 * @see XCallbacks2#buildCtrIntension(String, XVarInteger[], XNodeParent)
	 */
	@Override
	public void buildCtrIntension(String id, XVarInteger[] xscope, XNodeParent<XVarInteger> syntaxTreeRoot) {
		this.contradictionFound |= this.genericCtrBuilder.buildCtrIntension(id, xscope, syntaxTreeRoot);
	}

	/**
	 * @see XCallbacks2#buildCtrAllDifferent(String, XVarInteger[])
	 */
	@Override
	public void buildCtrAllDifferent(String id, XVarInteger[] list) {
		this.contradictionFound |= this.comparisonCtrBuilder.buildCtrAllDifferent(id, list);
	}

	/**
	 * @see XCallbacks2#buildCtrAllDifferentList(String, XVarInteger[][])
	 */
	@Override
	public void buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		this.contradictionFound |= this.comparisonCtrBuilder.buildCtrAllDifferentList(id, lists);
	}

	/**
	 * @see XCallbacks2#buildCtrAllDifferentExcept(String, XVarInteger[], int[])
	 */
	@Override
	public void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		this.contradictionFound |= this.comparisonCtrBuilder.buildCtrAllDifferentExcept(id, list, except);
	}

	/**
	 * @see XCallbacks2#buildCtrAllDifferentMatrix(String, XVarInteger[][])
	 */
	@Override
	public void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		this.contradictionFound |= this.comparisonCtrBuilder.buildCtrAllDifferentMatrix(id, matrix);		
	}

	/**
	 * @see XCallbacks2#buildObjToMinimize(String, XVarInteger)
	 */
	@Override
	public void buildObjToMinimize(String id, XVarInteger x) {
		this.objBuilder.buildObjToMaximize(id, x); // TODO not under test yet
	}

	/**
	 * @see XCallbacks2#buildObjToMaximize(String, XVarInteger)
	 */
	@Override
	public void buildObjToMaximize(String id, XVarInteger x) {
		this.objBuilder.buildObjToMaximize(id, x); // TODO not under test yet
	}

	/**
	 * @see XCallbacks2#buildObjToMinimize(String, TypeObjective, XVarInteger[], int[])
	 */
	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] xlist, int[] xcoeffs) {
		this.objBuilder.buildObjToMinimize(id, type, xlist, xcoeffs); // TODO not under test yet
	}

	/**
	 * @see XCallbacks2#buildObjToMaximize(String, TypeObjective, XVarInteger[], int[])
	 */
	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] xlist, int[] xcoeffs) {
		this.objBuilder.buildObjToMaximize(id, type, xlist, xcoeffs); // TODO not under test yet
	}

	/**
	 * @see XCallbacks2#buildObjToMinimize(String, XNodeParent)
	 */
	@Override
	public void buildObjToMinimize(String id, XNodeParent<XVarInteger> syntaxTreeRoot) {
		unimplementedCase(id); // TODO
	}

	/**
	 * @see XCallbacks2#buildObjToMaximize(String, XNodeParent)
	 */
	@Override
	public void buildObjToMaximize(String id, XNodeParent<XVarInteger> syntaxTreeRoot) {
		unimplementedCase(id); // TODO
	}

	/**
	 * @see XCallbacks2#buildObjToMinimize(String, TypeObjective, XVarInteger[])
	 */
	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list) {
		this.objBuilder.buildObjToMinimize(id, type, list); // TODO not under test yet
	}

	/**
	 * @see XCallbacks2#buildObjToMaximize(String, TypeObjective, XVarInteger[])
	 */
	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		this.objBuilder.buildObjToMaximize(id, type, list); // TODO not under test yet
	}

	/**
	 * @see XCallbacks2#buildCtrNoOverlap(String, XVarInteger[], int[], boolean)
	 */
	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrNoOverlap(id, origins, lengths, zeroIgnored);
	}

	/**
	 * @see XCallbacks2#buildCtrNoOverlap(String, XVarInteger[][], int[][], boolean)
	 */
	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrNoOverlap(id, origins, lengths, zeroIgnored);
	}

	/**
	 * @see XCallbacks2#buildCtrNoOverlap(String, XVarInteger[], XVarInteger[], boolean)
	 */
	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrNoOverlap(id, origins, lengths, zeroIgnored);
	}

	/**
	 * @see XCallbacks2#buildCtrNoOverlap(String, XVarInteger[][], XVarInteger[][], boolean)
	 */
	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrNoOverlap(id, origins, lengths, zeroIgnored);
	}

	/**
	 * @see XCallbacks2#buildCtrOrdered(String, XVarInteger[], TypeOperator)
	 */
	@Override
	public void buildCtrOrdered(String id, XVarInteger[] list, TypeOperator operator) {
		this.contradictionFound |= this.comparisonCtrBuilder.buildCtrOrdered(id, list, operator);
	}

	/**
	 * @see XCallbacks2#buildCtrSum(String, XVarInteger[], Condition)
	 */
	public void buildCtrSum(String id, XVarInteger[] list, Condition condition) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrSum(id, list, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrSum(String, XVarInteger[], int[], Condition)
	 */
	public void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrSum(id, list, coeffs, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrLex(String, XVarInteger[][], TypeOperator)
	 */
	@Override
	public void buildCtrLex(String id, XVarInteger[][] lists, TypeOperator operator) {
		this.contradictionFound |= this.comparisonCtrBuilder.buildCtrLex(id, lists, operator);
	}

	/**
	 * @see XCallbacks2#buildCtrLexMatrix(String, XVarInteger[][], TypeOperator)
	 */
	@Override
	public void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperator operator) {
		this.contradictionFound |= this.comparisonCtrBuilder.buildCtrLexMatrix(id, matrix, operator);
	}

	/**
	 * @see XCallbacks2#buildCtrAllEqual(String, XVarInteger[])
	 */
	@Override
	public void buildCtrAllEqual(String id, XVarInteger[] list) {
		this.contradictionFound |= this.comparisonCtrBuilder.buildCtrAllEqual(id, list);
	}

	/**
	 * @see XCallbacks2#buildCtrChannel(String, XVarInteger[], int)
	 */
	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrChannel(id, list, startIndex);
	}

	/**
	 * @see XCallbacks2#buildCtrChannel(String, XVarInteger[], int, XVarInteger)
	 */
	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value) {
		unimplementedCase(id); // TODO: check (again) set variables are not supported yet 
	}

	/**
	 * @see XCallbacks2#buildCtrChannel(String, XVarInteger[], int, XVarInteger[], int)
	 */
	@Override
	public void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrChannel(id, list1, startIndex1, list2, startIndex2);
	}

	/**
	 * @see XCallbacks2#buildCtrRegular(String, XVarInteger[], Object[][], String, String[])
	 */
	@Override
	public void buildCtrRegular(String id, XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates) {
		this.contradictionFound |= this.languageCtrBuilder.buildCtrRegular(id, list, transitions, startState, finalStates);
	}

	/**
	 * @see XCallbacks2#buildCtrMDD(String, XVarInteger[], Object[][])
	 */
	@Override
	public void buildCtrMDD(String id, XVarInteger[] list, Object[][] transitions) {
		this.contradictionFound |= this.languageCtrBuilder.buildCtrMDD(id, list, transitions);
	}

	/**
	 * @see XCallbacks2#buildCtrCount(String, XVarInteger[], int[], Condition)
	 */
	@Override
	public void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrCount(id, list, values, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrCount(String, XVarInteger[], XVarInteger[], Condition)
	 */
	@Override
	public void buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrCount(id, list, values, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrAtLeast(String, XVarInteger[], int, int)
	 */
	@Override
	public void buildCtrAtLeast(String id, XVarInteger[] list, int value, int k) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrAtLeast(id, list, value, k);
	}

	/**
	 * @see XCallbacks2#buildCtrAtMost(String, XVarInteger[], int, int)
	 */
	@Override
	public void buildCtrAtMost(String id, XVarInteger[] list, int value, int k) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrAtMost(id, list, value, k);
	}

	/**
	 * @see XCallbacks2#buildCtrExactly(String, XVarInteger[], int, int)
	 */
	@Override
	public void buildCtrExactly(String id, XVarInteger[] list, int value, int k) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrExactly(id, list, value, k);
	}

	/**
	 * @see XCallbacks2#buildCtrExactly(String, XVarInteger[], int, XVarInteger)
	 */
	@Override
	public void buildCtrExactly(String id, XVarInteger[] list, int value, XVarInteger k) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrExactly(id, list, value, k);
	}

	/**
	 * @see XCallbacks2#buildCtrAmong(String, XVarInteger[], int[], int)
	 */
	@Override
	public void buildCtrAmong(String id, XVarInteger[] list, int[] values, int k) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrAmong(id, list, values, k);
	}

	/**
	 * @see XCallbacks2#buildCtrAmong(String, XVarInteger[], int[], XVarInteger)
	 */
	@Override
	public void buildCtrAmong(String id, XVarInteger[] list, int[] values, XVarInteger k) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrAmong(id, list, values, k);
	}

	/**
	 * @see XCallbacks2#buildCtrNValues(String, XVarInteger[], Condition)
	 */
	@Override
	public void buildCtrNValues(String id, XVarInteger[] list, Condition condition) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrNValues(id, list, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrNValuesExcept(String, XVarInteger[], int[], Condition)
	 */
	@Override
	public void buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrNValuesExcept(id, list, except, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrNotAllEqual(String, XVarInteger[])
	 */
	@Override
	public void buildCtrNotAllEqual(String id, XVarInteger[] list) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrNotAllEqual(id, list);
	}

	/**
	 * @see XCallbacks2#buildCtrCardinality(String, XVarInteger[], boolean, int[], XVarInteger[])
	 */
	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrCardinality(id, list, closed, values, occurs);
	}

	/**
	 * @see XCallbacks2#buildCtrCardinality(String, XVarInteger[], boolean, int[], int[])
	 */
	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrCardinality(id, list, closed, values, occurs);
	}

	/**
	 * @see {@link XCallbacks2#buildCtrCardinality(String, XVarInteger[], boolean, int[], int[], int[])}
	 */
	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrCardinality(id, list, closed, values, occursMin, occursMax);
	}

	/**
	 * @see XCallbacks2#buildCtrCardinality(String, XVarInteger[], boolean, XVarInteger[], XVarInteger[])
	 */
	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, XVarInteger[] occurs) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrCardinality(id, list, closed, values, occurs);
	}

	/**
	 * @see XCallbacks2#buildCtrCardinality(String, XVarInteger[], boolean, XVarInteger[], int[])
	 */
	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occurs) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrCardinality(id, list, closed, values, occurs);
	}

	/**
	 * @see XCallbacks2#buildCtrCardinality(String, XVarInteger[], boolean, XVarInteger[], int[], int[])
	 */
	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occursMin, int[] occursMax) {
		this.contradictionFound |= this.countingCtrBuilder.buildCtrCardinality(id, list, closed, values, occursMin, occursMax);
	}

	/**
	 * @see XCallbacks2#buildCtrMaximum(String, XVarInteger[], Condition)
	 */
	@Override
	public void buildCtrMaximum(String id, XVarInteger[] list, Condition condition) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrMaximum(id, list, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrMaximum(String, XVarInteger[], int, XVarInteger, TypeRank, Condition)
	 */
	@Override
	public void buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrMaximum(id, list, startIndex, index, rank, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrMinimum(String, XVarInteger[], Condition)
	 */
	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, Condition condition) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrMinimum(id, list, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrMinimum(String, XVarInteger[], int, XVarInteger, TypeRank, Condition)
	 */
	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrMinimum(id, list, startIndex, index, rank, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrElement(String, XVarInteger[], int)
	 */
	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int value) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrElement(id, list, 0, null, TypeRank.ANY, value);
	}

	/**
	 * @see XCallbacks2#buildCtrElement(String, XVarInteger[], int, XVarInteger, TypeRank, int)
	 */
	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrElement(id, list, startIndex, index, rank, value);
	}

	/**
	 * @see XCallbacks2#buildCtrElement(String, XVarInteger[], XVarInteger)
	 */
	@Override
	public void buildCtrElement(String id, XVarInteger[] list, XVarInteger value) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrElement(id, list, 0, null, TypeRank.ANY, value);
	}

	/**
	 * @see XCallbacks2#buildCtrElement(String, XVarInteger[], int, XVarInteger, TypeRank, XVarInteger)
	 */
	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		this.contradictionFound |= this.connectionCtrBuilder.buildCtrElement(id, list, startIndex, index, rank, value);
	}

	/**
	 * @see {@link XCallbacks2#buildCtrStretch(String, XVarInteger[], int[], int[], int[])}
	 */
	@Override
	public void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrStretch(id, list, values, widthsMin, widthsMax);
	}

	/**
	 * @see XCallbacks2#buildCtrStretch(String, XVarInteger[], int[], int[], int[], int[][])
	 */
	@Override
	public void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrStretch(id, list, values, widthsMin, widthsMax, patterns);
	}

	/**
	 * @see XCallbacks2#buildCtrCumulative(String, XVarInteger[], int[], int[], Condition)
	 */
	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrCumulative(id, origins, lengths, heights, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrCumulative(String, XVarInteger[], int[], XVarInteger[], Condition)
	 */
	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrCumulative(id, origins, lengths, heights, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrCumulative(String, XVarInteger[], XVarInteger[], int[], Condition)
	 */
	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrCumulative(id, origins, lengths, heights, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrCumulative(String, XVarInteger[], XVarInteger[], XVarInteger[], Condition)
	 */
	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrCumulative(id, origins, lengths, heights, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrCumulative(String, XVarInteger[], int[], XVarInteger[], int[], Condition)
	 */
	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrCumulative(id, origins, lengths, ends, heights, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrCumulative(String, XVarInteger[], int[], XVarInteger[], XVarInteger[], Condition)
	 */
	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrCumulative(id, origins, lengths, ends, heights, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrCumulative(String, XVarInteger[], XVarInteger[], XVarInteger[], int[], Condition)
	 */
	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrCumulative(id, origins, lengths, ends, heights, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrCumulative(String, XVarInteger[], XVarInteger[], XVarInteger[], XVarInteger[], Condition)
	 */
	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		this.contradictionFound |= this.schedulingCtrBuilder.buildCtrCumulative(id, origins, lengths, ends, heights, condition);
	}

	/**
	 * @see XCallbacks2#buildCtrInstantiation(String, XVarInteger[], int[])
	 */
	@Override
	public void buildCtrInstantiation(String id, XVarInteger[] list, int[] values) {
		this.contradictionFound |= this.elementaryCtrBuilder.buildCtrInstantiation(id, list, values);
	}

	/**
	 * @see XCallbacks2#buildCtrClause(String, XVarInteger[], XVarInteger[])
	 */
	@Override
	public void buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg) { // TODO: externalize
		this.contradictionFound |= this.elementaryCtrBuilder.buildCtrClause(id, pos, neg);
	}

	public void buildVarSymbolic(XVarSymbolic x, String[] values) {
		unimplementedCase(x.id); // TODO
	}

	public void buildCtrIntension(String id, XVarSymbolic[] scope, XNodeParent<XVarSymbolic> syntaxTreeRoot) {
		unimplementedCase(id); // TODO
	}

	public void buildCtrExtension(String id, XVarSymbolic x, String[] values, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id); // TODO
	}

	public void buildCtrExtension(String id, XVarSymbolic[] list, String[][] tuples, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id); // TODO
	}

	public void buildCtrAllDifferent(String id, XVarSymbolic[] list) {
		unimplementedCase(id); // TODO
	}
	
	private Implem dataStructureImplementor = new Implem (this) ;

	@Override
	public Implem implem() {
	  return dataStructureImplementor ;
	}

	@Override
	public Integer newVar() {
		return this.solver.nextFreeVarId(true);
	}

}
