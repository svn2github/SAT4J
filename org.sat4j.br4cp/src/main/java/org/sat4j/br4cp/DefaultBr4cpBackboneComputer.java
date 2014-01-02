package org.sat4j.br4cp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.Backbone;

/**
 * A class used to compile propagation in configuration problem due to
 * assumptions.
 * 
 * @author lonca
 */
public class DefaultBr4cpBackboneComputer implements IBr4cpBackboneComputer {
	
	private ISolver solver;
	private ConfigVarMap varMap;
	private List<Set<Integer>> solverAssumptions = new ArrayList<Set<Integer>>();
	private Set<String> fixedVars = new HashSet<String>();
	
	private Set<String> propagatedConfigVars;
	private Set<String> domainReductions;
	private Set<String> propagatedAdditionalVars;
	private Set<String> unavailableAdditionalVars;

	public DefaultBr4cpBackboneComputer(ISolver solver, ConfigVarMap varMap) throws TimeoutException{
		this.solver = solver;
		this.varMap = varMap;
		computeBackbone(solver);
	}
	
	protected void computeBackbone(ISolver solver) throws TimeoutException {
		IVecInt assumps = new VecInt();
		for (Iterator<Set<Integer>> it = solverAssumptions.iterator(); it
				.hasNext();) {
			for (Iterator<Integer> it2 = it.next().iterator(); it2.hasNext();)
				assumps.push(it2.next());
		}
		IVecInt backbone = Backbone.instance().compute(solver, assumps);
		if (backbone.isEmpty()) {
			if (this.solverAssumptions.isEmpty()) {
				throw new IllegalStateException("The formula is unsat!");
			} 
			Set<Integer> removed = this.solverAssumptions.remove(this.solverAssumptions.size()-1);
			for(Iterator<Integer> it = removed.iterator(); it.hasNext(); ) {
				String nextVar = this.varMap.getConfigVar(Math.abs(it.next()));
				this.fixedVars.remove(nextVar.substring(0, nextVar.lastIndexOf('.')));
			}
			throw new IllegalArgumentException("last assumption implies a contradiction");
		}
		computePropagationsAndReductions(backbone);
	}

	private void computePropagationsAndReductions(IVecInt backbone) {
		this.propagatedConfigVars = new TreeSet<String>(new ConfigVarComparator());
		this.domainReductions = new TreeSet<String>(new ConfigVarComparator());
		this.propagatedAdditionalVars = new TreeSet<String>(new ConfigVarComparator());
		this.unavailableAdditionalVars = new TreeSet<String>(new ConfigVarComparator());
		for(IteratorInt it = backbone.iterator(); it.hasNext(); ) {
			int next = it.next();
			if ((next > 0) && this.varMap.isConfigVar(next)) {
				String name = this.varMap.getConfigVar(next);
				int lastDotIndex = name.lastIndexOf('.');
				name = name.substring(0, lastDotIndex) + "="
						+ name.substring(lastDotIndex + 1);
				this.fixedVars.add(name.substring(0, lastDotIndex));
				this.propagatedConfigVars.add(name);
			}else if ((next < 0) && this.varMap.isConfigVar(next)) {
				String name = this.varMap.getConfigVar(-next);
				int lastDotIndex = name.lastIndexOf('.');
				name = name.substring(0, lastDotIndex) + "="
						+ name.substring(lastDotIndex + 1);
				this.domainReductions.add(name);
			}else if(this.varMap.isAdditionalVar(next)) {
				String name = this.varMap.getConfigVar(Math.abs(next))+"=1";
				if(next < 0) {
					this.unavailableAdditionalVars.add(name);
				}else{
					this.propagatedAdditionalVars.add(name);
				}
			}
		}
		for(Iterator<String> it = this.domainReductions.iterator(); it.hasNext(); ) {
			String next = it.next();
			if(this.fixedVars.contains(next.substring(0, next.lastIndexOf('=')))){
				it.remove();
			}
		}
	}

	public void addAssumption(String configVar) throws TimeoutException,
			ContradictionException {
		if (this.varMap.configVarExists(configVar)) {
			int lastDotIndex = configVar.lastIndexOf('.');
			String configVarName = configVar.substring(0, lastDotIndex);
			if (this.fixedVars.contains(configVarName)
					&& Options.getInstance().areReplacementAllowed()) {
				removeAssumedConfigVar(configVarName);
			}
			Set<Integer> newAssumpSet = new HashSet<Integer>();
			newAssumpSet.add(this.varMap.getSolverVar(configVar));
			this.solverAssumptions.add(newAssumpSet);
			this.fixedVars.add(configVarName);
			computeBackbone(solver);
		} else {
			throw new IllegalArgumentException(configVar + " is not defined");
		}		
	}

	private void removeAssumedConfigVar(String configVarName) {
		this.fixedVars = new HashSet<String>();
		boolean found = false;
		for(Iterator<Set<Integer>> it = this.solverAssumptions.iterator(); !found && it.hasNext(); ) {
			Set<Integer> nextSet = it.next();
			for(Iterator<Integer> it2 = nextSet.iterator(); it2.hasNext(); ) {
				String nextVar = this.varMap.getConfigVar(Math.abs(it2.next()));
				String nextVarName = nextVar.substring(0, nextVar.lastIndexOf('.'));
				if(configVarName.equals(nextVarName)) {
					it.remove();
					break;
				}else{
					this.fixedVars.add(nextVarName);
				}
			}
		}
	}

	public void setOptionalConfigVarAsNotInstalled(String optConfigVar)
			throws TimeoutException {
		Set<String> domain = this.varMap.getConfigVarDomain(optConfigVar);
		Set<Integer> newAssumps = new HashSet<Integer>();
		for (String s : domain) {
			newAssumps.add(-this.varMap.getSolverVar(s));
		}
		this.solverAssumptions.add(newAssumps);
		this.fixedVars.add(optConfigVar.substring(0, optConfigVar.lastIndexOf('.')));
		computeBackbone(solver);
	}

	public void addAdditionalVarAssumption(String addVar)
			throws TimeoutException {
		int lastDotIndex = addVar.lastIndexOf('.');
		String name = addVar.substring(0, lastDotIndex);
		int state;
		try {
			state = Integer.valueOf(addVar.substring(lastDotIndex + 1));
		} catch (NumberFormatException e) {
			throw new NumberFormatException(addVar + " has no version or state");
		}
		Set<Integer> newAssumpSet = new HashSet<Integer>();
		if (state == 1) {
			newAssumpSet.add(this.varMap.getSolverVar(name));
		} else {
			newAssumpSet.add(-this.varMap.getSolverVar(name));
		}
		this.solverAssumptions.add(newAssumpSet);
		this.fixedVars.add(name);
		computeBackbone(solver);
	}

	public void clearAssumptions() {
		this.solverAssumptions.clear();
		this.fixedVars.clear();
		try {
			computeBackbone(solver);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> propagatedConfigVars() {
		return this.propagatedConfigVars;
	}

	public Set<String> domainReductions() {
		return this.domainReductions;
	}

	public Set<String> propagatedAdditionalVars() {
		return this.propagatedAdditionalVars;
	}
	
	public Set<String> unavailableAdditionalVars() {
		return this.unavailableAdditionalVars;
	}

	public List<Set<Integer>> getSolverAssumptions() {
		return this.solverAssumptions;
	}

}
