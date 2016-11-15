package org.sat4j.csp.intension;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.sat4j.core.VecInt;
import org.sat4j.csp.Domain;
import org.sat4j.csp.Domains;
import org.sat4j.csp.Var;
import org.sat4j.csp.constraints3.CtrBuilderUtils;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.ContradictionException;
import org.xcsp.parser.entries.XVariables.XVarInteger;

public class CspToPBSolverDecorator implements ICspToSatEncoder {
	
	private final IPBSolver solver;
	
	/** a mapping from the CSP variable names to Sat4j CSP variables */
	private final Map<String, Var> varmapping = new LinkedHashMap<String, Var>();

	/** a mapping from a Sat4j CSP variable to the first solver internal variable used to encode it */
	private final Map<Var, Integer> firstInternalVarMapping = new LinkedHashMap<Var, Integer>();

	public CspToPBSolverDecorator(IPBSolver solver) {
		this.solver = solver;
	}

	@Override
	public int[] getCspVarDomain(String strVar) {
		final Var var = this.varmapping.get(strVar);
		if(var == null) return null;
		final Domain domain = var.domain();
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

	@Override
	public Integer newSatSolverVar() {
		return this.solver.nextFreeVarId(true);
	}
	
	@Override
	public void newCspVar(XVarInteger var, int minDom, int maxDom) {
		Domain dom = Domains.getInstance().getDomain(minDom, maxDom);
		newCspVar(var, dom);
	}
	
	@Override
	public void newCspVar(XVarInteger var, int[] domain) {
		Arrays.sort(domain);
		boolean isRange = true;
		for(int i=0; i<domain.length-1; ++i) {
			if(domain[i] != domain[i+1]-1) {
				isRange = false;
				break;
			}
		}
		Domain dom = isRange ? Domains.getInstance().getDomain(domain[0], domain[domain.length-1]) : Domains.getInstance().getDomain(domain);
		newCspVar(var, dom);
	}

	private void newCspVar(XVarInteger var, Domain dom) {
		Var cspVar = new Var(CtrBuilderUtils.normalizeCspVarName(var.id), dom, this.solver.nextFreeVarId(false)-1);
		this.firstInternalVarMapping.put(cspVar, this.solver.nextFreeVarId(false));
		for(int i=0; i<dom.size(); ++i) this.solver.nextFreeVarId(true);
		try {
			cspVar.toClause(solver);
		} catch (ContradictionException e) {
			throw new IllegalStateException("cannot occur");
		}
		this.varmapping.put(var.id, cspVar);
	}
	
	@Override
	public Map<Integer, String> getMapping() {
		final SortedMap<Integer, String> mapping = new TreeMap<>(); 
		for(String var : this.varmapping.keySet()) {
			int[] domain = getCspVarDomain(var);
			for(int i=0; i<domain.length; ++i) {
				final int solverVar = getSolverVar(var, domain[i]);
				mapping.put(solverVar, var+"="+domain[i]);
			}
		}
		return mapping;
	}

}
