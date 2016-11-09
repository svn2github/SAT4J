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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sat4j.core.Vec;
import org.sat4j.csp.Var;
import org.sat4j.csp.constraints.GentSupports;
import org.sat4j.csp.constraints.Nogoods;
import org.sat4j.csp.constraints.Relation;
import org.sat4j.csp.intension.IIntensionCtrEncoder;
import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.XMLCSP3Reader;
import org.sat4j.specs.ContradictionException;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeLeaf;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XValues.IntegerEntity;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to intension (including "primitive" special cases) constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class GenericCtrBuilder {
	
	/** the solver in which the problem is encoded */
	private IPBSolver solver;
	
	/** a mapping from the CSP variable names to Sat4j CSP variables */
	private Map<String, Var> varmapping = new LinkedHashMap<String, Var>();

	private final IIntensionCtrEncoder intensionCtrEnc;

	public GenericCtrBuilder(IPBSolver solver, Map<String, Var> varmapping, IIntensionCtrEncoder intensionEnc) {
		this.solver = solver;
		this.varmapping = varmapping;
		this.intensionCtrEnc = intensionEnc;
	}
	
	public boolean buildCtrIntension(String id, XVarInteger[] xscope, XNodeParent<XVarInteger> syntaxTreeRoot) {
		syntaxTreeRootToString(syntaxTreeRoot);
		String expr = syntaxTreeRootToString(syntaxTreeRoot);
		this.intensionCtrEnc.encode(expr);
		return false;
	}
	
	private String syntaxTreeRootToString(XNodeParent<XVarInteger> syntaxTreeRoot) {
		StringBuffer treeToString = new StringBuffer();
		fillSyntacticStrBuffer(syntaxTreeRoot, treeToString);
		return treeToString.toString();
	}

	private void fillSyntacticStrBuffer(XNode<XVarInteger> child,
			StringBuffer treeToString) {
		if(child instanceof XNodeLeaf<?>) {
			treeToString.append(CtrBuilderUtils.normalizeCspVarName(child.toString()));
			return;
		}
		treeToString.append(child.getType().toString().toLowerCase());
		XNode<XVarInteger>[] sons = ((XNodeParent<XVarInteger>) child).sons;
		treeToString.append('(');
		fillSyntacticStrBuffer(sons[0], treeToString);
		for(int i=1; i<sons.length; ++i) {
			treeToString.append(',');
			fillSyntacticStrBuffer(sons[i], treeToString);
		}
		treeToString.append(')');
	}
	
	public boolean buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags) {
		XVarInteger[] xArr = new XVarInteger[]{x};
		int nVals = values.length;
		int[][] tuples = new int[nVals][];
		for(int i=0; i<nVals; ++i) {
			tuples[i] = new int[]{values[i]};
		}
		return buildCtrExtension(id, xArr, tuples, positive, flags);
	}
	
	public boolean buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags) { // TODO: remove varmapping dependence
		if(flags.contains(TypeFlag.STARRED_TUPLES)) {
			tuples = unfoldStarredTuples(list, tuples);
		}
		SortedSet<Var> vars = new TreeSet<Var>((v1,v2) -> v1.toString().compareTo(v2.toString()));
		for(int i=0; i<list.length; ++i) {
			vars.add(this.varmapping.get(list[i].id));
		}
		if(vars.size() != list.length) {
			tuples = removeUnsatTuples(list, tuples);
			if(tuples.length == 0) return positive;
		}
		Relation cstr = positive ? new GentSupports(vars.size(), tuples.length) : new Nogoods(vars.size(), tuples.length);
		for(int i=0; i<tuples.length; ++i) {
			cstr.addTuple(i, tuples[i]);
		}
		try {
			cstr.toClause(this.solver, CtrBuilderUtils.toVarVec(vars), new Vec<>());
		} catch (ContradictionException e) {
			return true;
		}
		return false;
	}

	private int[][] unfoldStarredTuples(XVarInteger[] list, int[][] tuples) {
		List<int[]> newTuples = new ArrayList<>();
		for(int i=0; i<tuples.length; ++i) {
			List<XVarInteger> starredVars = new ArrayList<>();
			List<Integer> starredIndexes = new LinkedList<>();
			for(int j=0; j<tuples[i].length; ++j) {
				if(tuples[i][j] == Integer.MAX_VALUE-1) { // STARRED TUPLE 
					starredVars.add(list[j]);
					starredIndexes.add(j);
				}
			}
			if(starredVars.isEmpty()) {
				newTuples.add(tuples[i]);
			} else {
				newTuples.addAll(unfoldStarredTuple(list, tuples[i], starredVars, starredIndexes));
			}
		}
		int[][] newTuplesArray = new int[newTuples.size()][];
		for(int i=0; i<newTuples.size(); ++i) newTuplesArray[i] = newTuples.get(i);
		return newTuplesArray;
	}

	private Collection<int[]> unfoldStarredTuple(XVarInteger[] list, int[] tuple,
			List<XVarInteger> starredVars, List<Integer> starredIndexes) {
		List<int[]> result = new ArrayList<>();
		List<Integer> firstStarredVarDomValues = new ArrayList<>();
		XDomInteger domain = (XDomInteger) (starredVars.get(0).dom);
		for(Object objIntegerEntity : domain.values) {
			IntegerEntity intEntity = (IntegerEntity) objIntegerEntity;
			if(intEntity.isSingleton()) {
				firstStarredVarDomValues.add((int) intEntity.greatest());
				continue;
			}
			for(int i=(int) intEntity.smallest(); i<=(int) intEntity.greatest(); ++i) {
				firstStarredVarDomValues.add(i);
			}
		}
		int starredIndex = starredIndexes.remove(0);
		starredVars.remove(0);
		for(int i=0; i<firstStarredVarDomValues.size(); ++i) {
			int[] newTuple = new int[tuple.length];
			System.arraycopy(tuple, 0, newTuple, 0, tuple.length);
			newTuple[starredIndex] = firstStarredVarDomValues.get(i);
			if(starredVars.isEmpty()) {
				result.add(newTuple);
			} else {
				result.addAll(unfoldStarredTuple(list, newTuple, new ArrayList<>(starredVars), new ArrayList<>(starredIndexes)));
			}
		}
		return result;
	}

	private int[][] removeUnsatTuples(XVarInteger[] list, int[][] tuples) {
		for(int i=0; i<list.length-1; ++i) {
			for(int j=i+1; j<list.length; ++j) {
				if(!list[i].id.equals(list[j].id)) continue;
				tuples = removeUnsatTuplesForVar(tuples, i, j);
				if(tuples.length == 0) return tuples;
			}
		}
		return tuples;
	}

	private int[][] removeUnsatTuplesForVar(int[][] tuples, int varIndex1, int varIndex2) {
		List<int[]> tuplesList = new ArrayList<>();
		for(int i=0; i<tuples.length; ++i) {
			if(tuples[i][varIndex1] == tuples[i][varIndex2]) tuplesList.add(tuples[i]);
		}
		int[][] tuplesArray = new int[tuplesList.size()][];
		return tuplesList.toArray(tuplesArray);
	}

}
