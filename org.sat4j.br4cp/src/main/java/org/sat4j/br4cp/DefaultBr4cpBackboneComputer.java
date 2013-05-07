package org.sat4j.br4cp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
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
 * assumptions. This class uses assumptions to keep informations. Although it is
 * efficient to chain assumptions ("scenarios"), this class should get
 * efficiency issues while canceling first assumptions.
 * 
 * @author lonca
 */
public class DefaultBr4cpBackboneComputer implements
		IBr4cpBackboneComputer {

	private ISolver solver;

	private ConfigVarMap idMap;

	protected List<Set<Integer>> solverAssumptions = new ArrayList<Set<Integer>>();

	private Set<String> assumedVars = new HashSet<String>();
	private Set<String> propagatedVars = new HashSet<String>();

	protected Stack<IVecInt> backbonesStack = new Stack<IVecInt>();

	private Set<String> newPropagatedConfigVars = null;
	private Set<String> newDomainReductions = null;
	private Set<String> newPropagatedAdditionalVars = null;
	private Set<String> newReducedAdditionalVars = null;
	

	public DefaultBr4cpBackboneComputer(ISolver solver,
			ConfigVarMap idMap) throws TimeoutException {
		this.solver = solver;
		this.idMap = idMap;
		this.backbonesStack.push(computeBackbone(solver));
		computePropagations();
	}

	protected IVecInt computeBackbone(ISolver solver) throws TimeoutException {
		IVecInt assumps = new VecInt();
		for (Iterator<Set<Integer>> it = solverAssumptions.iterator(); it
				.hasNext();) {
			for (Iterator<Integer> it2 = it.next().iterator(); it2.hasNext();)
				assumps.push(it2.next());
		}
		return Backbone.compute(solver, assumps);
	}

	public void addAssumption(String var) throws TimeoutException,
			ContradictionException {
		if (this.idMap.configVarExists(var)) {
			Integer id = this.idMap.getSolverVar(var);
			try {
				addAssumption(id);
				int lastDotIndex = var.lastIndexOf('.');
				this.assumedVars.add(var.substring(0, lastDotIndex));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("\"" + var
						+ "\" implies a contradiction");
			}
		} else {
			throw new IllegalArgumentException(var + " is not defined");
		}
		computePropagations();
	}

	private void addAssumption(Integer id) throws TimeoutException {
		Set<Integer> newSet = new HashSet<Integer>();
		newSet.add(id);
		this.solverAssumptions.add(newSet);
		if (this.backbonesStack.peek().contains(id)) {
			this.backbonesStack.push(this.backbonesStack.peek());
		} else {
			IVecInt backbone = computeBackbone(this.solver);
			if (backbone.isEmpty()) {
				throw new IllegalArgumentException(Integer.toString(id)
						+ " implies a contradiction");
			}
			this.backbonesStack.push(backbone);
		}
	}

	public void addAdditionalVarAssumption(String assump)
			throws TimeoutException {
		int lastDotIndex = assump.lastIndexOf('.');
		String name = assump.substring(0, lastDotIndex);
		int state;
		try {
			state = Integer.valueOf(assump.substring(lastDotIndex + 1));
		} catch (NumberFormatException e) {
			throw new NumberFormatException(assump+" has no version or state");
		}
		if (state == 1) {
			addAssumption(this.idMap.getSolverVar(name));
		} else {
			addAssumption(-this.idMap.getSolverVar(name));
		}
		this.assumedVars.add(name);
		computePropagations();
	}

	public void setOptionalConfigVarAsNotInstalled(String var)
			throws TimeoutException {
		Set<String> domain = this.idMap.getConfigVarDomain(var);
		Set<Integer> newAssumps = new HashSet<Integer>();
		for (String s : domain) {
			newAssumps.add(-this.idMap.getSolverVar(s));
		}
		this.solverAssumptions.add(newAssumps);
		this.backbonesStack.push(computeBackbone(this.solver));
		int lastDotIndex = var.lastIndexOf('.');
		this.assumedVars.add(var.substring(0, lastDotIndex));
		computePropagations();
	}

	public void clearAssumptions() {
		while (this.backbonesStack.size() > 1) {
			this.backbonesStack.pop();
		}
		this.solverAssumptions = new ArrayList<Set<Integer>>();
		this.assumedVars = new HashSet<String>();
		this.propagatedVars = new HashSet<String>();
		computePropagations();
	}

	public Set<String> propagatedConfigVars() {
		if (this.backbonesStack.isEmpty()) {
			return new HashSet<String>();
		}
		Set<String> propagated = new HashSet<String>();
		for (IteratorInt it = this.backbonesStack.peek().iterator(); it
				.hasNext();) {
			int next = it.next();
			if ((next > 0) && this.idMap.isConfigVar(next)) {
				String name = this.idMap.getConfigVar(next);
				int lastDotIndex = name.lastIndexOf('.');
				name = name.substring(0, lastDotIndex) + "="
						+ name.substring(lastDotIndex + 1);
				propagated.add(name);
			}
		}
		return propagated;
	}

	public Set<String> propagatedAdditionalVars() {
		if (this.backbonesStack.isEmpty()) {
			return new HashSet<String>();
		}
		Set<String> assertions = new HashSet<String>();
		for (IteratorInt it = this.backbonesStack.peek().iterator(); it
				.hasNext();) {
			int next = it.next();
			if (this.idMap.isAdditionalVar(next)) {
				assertions
						.add((next > 0) ? (this.idMap.getConfigVar(next) + "=1")
								: (this.idMap.getConfigVar(-next) + "=99"));
			}
		}
		return assertions;
	}

	private void computePropagations() {
		computeNewPropagatedConfigVars();
		computeNewDomainReductions();
		computeNewPropagatedAdditionalVars();
	}

	private void computeNewPropagatedConfigVars() {
		if (this.backbonesStack.isEmpty())
			return;
		if (this.backbonesStack.size() == 1) {
			this.newPropagatedConfigVars = propagatedConfigVars();
			return;
		}
		Set<String> currentlyAsserted = propagatedConfigVars();
		IVecInt stackTop = this.backbonesStack.pop();
		Set<String> lastStepAsserted = propagatedConfigVars();
		this.backbonesStack.push(stackTop);
		this.newPropagatedConfigVars = new TreeSet<String>(VAR_COMP);
		Set<String> lastAssumpsNames = new HashSet<String>();
		for (Integer i : this.solverAssumptions.get(this.solverAssumptions
				.size() - 1)) {
			lastAssumpsNames.add(this.idMap.getConfigVar(i));
		}
		for (String s : currentlyAsserted) {
			if (!lastStepAsserted.contains(s) && !lastAssumpsNames.contains(s)) {
				int lastDotIndex = s.lastIndexOf('=');
				String name = s.substring(0, lastDotIndex);
				if (!this.assumedVars.contains(name)
						&& !this.propagatedVars.contains(name)) {
					this.propagatedVars.add(name);
					this.newPropagatedConfigVars.add(s);
				}
			}
		}
	}

	private void computeNewDomainReductions() {
		if (this.backbonesStack.isEmpty())
			return;
		if (this.backbonesStack.size() == 1) {
			this.newDomainReductions = domainReductions();
			return;
		}
		Set<String> currentlyAssertedFalse = domainReductions();
		IVecInt stackTop = this.backbonesStack.pop();
		Set<String> lastStepAssertedFalse = domainReductions();
		this.backbonesStack.push(stackTop);
		this.newDomainReductions = new TreeSet<String>(VAR_COMP);
		for (String s : currentlyAssertedFalse) {
			if (!lastStepAssertedFalse.contains(s)) {
				int lastDotIndex = s.lastIndexOf('=');
				String name = s.substring(0, lastDotIndex);
				if (!this.assumedVars.contains(name)
						&& !this.propagatedVars.contains(name)) {
					this.newDomainReductions.add(s);
				}
			}
		}
	}

	public Set<String> domainReductions() {
		if (this.backbonesStack.isEmpty()) {
			return new HashSet<String>();
		}
		Set<String> reductions = new HashSet<String>();
		for (IteratorInt it = this.backbonesStack.peek().iterator(); it
				.hasNext();) {
			int next = it.next();
			if ((next < 0) && this.idMap.isConfigVar(next)) {
				String name = this.idMap.getConfigVar(-next);
				int lastDotIndex = name.lastIndexOf('.');
				int version = Integer.valueOf(name.substring(lastDotIndex + 1));
				name = name.substring(0, lastDotIndex);
				reductions.add(name + "=" + version);
			}
		}
		return reductions;
	}
	
	private void computeNewPropagatedAdditionalVars() {
		if (this.backbonesStack.isEmpty())
			return;
		Set<String> currentBooleanAssertion = propagatedAdditionalVars();
		Set<String> lastStepBooleanAssertion = new HashSet<String>();
		if (this.backbonesStack.size() > 1) {
			IVecInt stackTop = this.backbonesStack.pop();
			lastStepBooleanAssertion = propagatedAdditionalVars();
			this.backbonesStack.push(stackTop);
		}
		this.newPropagatedAdditionalVars = new TreeSet<String>(VAR_COMP);
		this.newReducedAdditionalVars = new TreeSet<String>(VAR_COMP);
		for (String s : currentBooleanAssertion) {
			if (!lastStepBooleanAssertion.contains(s)) {
				int lastDotIndex = s.lastIndexOf('=');
				String name = s.substring(0, lastDotIndex);
				int version = Integer.valueOf(s.substring(lastDotIndex+1));
				if (!this.assumedVars.contains(name)) {
					if(version == 1)
						this.newPropagatedAdditionalVars.add(s);
					else
						this.newReducedAdditionalVars.add(name+"=1");
				}
			}
		}
	}

	public Set<String> newCspDomainReductions() {
		if(this.backbonesStack.isEmpty())
			return null;
		Set<String> lastAssumpNames = new HashSet<String>();
		Set<Integer> newAssumptions = new HashSet<Integer>();
		for(IteratorInt it = this.backbonesStack.peek().iterator(); it.hasNext(); )
			newAssumptions.add(it.next());
		if(this.backbonesStack.size() > 1) {
			IVecInt stackTop = this.backbonesStack.pop();
			for(IteratorInt it = this.backbonesStack.peek().iterator(); it.hasNext(); ) {
				newAssumptions.remove(it.next());
			}
			this.backbonesStack.push(stackTop);
			for (Integer i : this.solverAssumptions.get(this.solverAssumptions.size()-1)){
				String var = this.idMap.getConfigVar(i>0?i:-i);
				if(this.idMap.isAdditionalVar(i)) {
					lastAssumpNames.add(var);
				}else{
					lastAssumpNames.add(var.substring(0, Math.max(var.lastIndexOf('.'),var.lastIndexOf('='))));
				}
			}
		}
		Set<String> res = new HashSet<String>();
		for(Integer i : newAssumptions) {
			String var = null;
			if((i > 0) && this.idMap.isConfigVar(i)){
				var = this.idMap.getConfigVar(i);
				if(this.idMap.isOptionalDomainVar(var)) {
					res.add(var.substring(0, var.lastIndexOf('.'))+"=99");
				}
			}
			if((i < 0) && this.idMap.isConfigVar(i)) {
				var = this.idMap.getConfigVar(-i);
				if(!lastAssumpNames.contains(var.substring(0, var.lastIndexOf('.'))))
					res.add(var);
			} else if(this.idMap.isAdditionalVar(i)) {
				var = (i>0)?this.idMap.getConfigVar(i):this.idMap.getConfigVar(-i);
				if(!lastAssumpNames.contains(var))
					res.add(i>0?var+"=99":var+"=1");
			}
		}
		return res;
	}

	Comparator<String> VAR_COMP = new Comparator<String>() {

		@Override
		public int compare(String arg0, String arg1) {
			String[] t1 = arg0.split("[.=]");
			String[] t2 = arg1.split("[.=]");
			for (int i = 0; i < Math.min(t1.length, t2.length); ++i) {
				boolean firstIsInteger = false, secondIsInteger = false;
				Integer n1 = null, n2 = null;
				try {
					n1 = Integer.valueOf(t1[i]);
					firstIsInteger = true;
				} catch (NumberFormatException e) {
				}
				try {
					n2 = Integer.valueOf(t2[i]);
					secondIsInteger = true;
				} catch (NumberFormatException e) {
				}
				if (firstIsInteger != secondIsInteger) {
					return (firstIsInteger) ? (-1) : (1);
				}
				if (firstIsInteger && secondIsInteger) {
					if (n1.equals(n2)) {
						continue;
					} else {
						return n1.compareTo(n2);
					}
				}
				firstIsInteger = false;
				secondIsInteger = false;
				try {
					n1 = Integer.valueOf(t1[i].replaceAll("[a-zA-Z]", ""));
					firstIsInteger = true;
				} catch (NumberFormatException e) {
				}
				try {
					n2 = Integer.valueOf(t2[i].replaceAll("[a-zA-Z]", ""));
					secondIsInteger = true;
				} catch (NumberFormatException e) {
				}
				if (firstIsInteger != secondIsInteger) {
					return (firstIsInteger) ? (-1) : (1);
				}
				if (firstIsInteger && secondIsInteger) {
					if (n1.equals(n2)) {
						continue;
					} else {
						return n1.compareTo(n2);
					}
				}
				return t1[i].compareTo(t2[i]);
			}
			return t1.length - t2.length;
		}

	};

	@Override
	public Set<String> newPropagatedConfigVars() {
		return this.newPropagatedConfigVars;
	}

	@Override
	public Set<String> newDomainReductions() {
		return this.newDomainReductions;
	}

	@Override
	public Set<String> newPropagatedAdditionalVars() {
		return this.newPropagatedAdditionalVars;
	}

	@Override
	public Set<String> newReducedAdditionalVars() {
		return this.newReducedAdditionalVars;
	}

}
