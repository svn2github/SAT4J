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
public class AssumptionsBasedBr4cpBackboneComputer implements
		IBr4cpBackboneComputer {

	private ISolver solver;

	private ConfigVarMap idMap;

	private List<Set<Integer>> assumptions = new ArrayList<Set<Integer>>();

	private Set<String> assumptionsNames = new HashSet<String>();

	private Stack<IVecInt> backbonesStack = new Stack<IVecInt>();

	public AssumptionsBasedBr4cpBackboneComputer(ISolver solver,
			ConfigVarMap idMap) throws TimeoutException {
		this.solver = solver;
		this.idMap = idMap;
		this.backbonesStack.push(computeBackbone(solver));
	}

	private IVecInt computeBackbone(ISolver solver) throws TimeoutException {
		IVecInt assumps = new VecInt();
		for (Iterator<Set<Integer>> it = assumptions.iterator(); it.hasNext();) {
			for (Iterator<Integer> it2 = it.next().iterator(); it2.hasNext();)
				assumps.push(it2.next());
		}
		if (!this.backbonesStack.isEmpty()) {
			for (IteratorInt it = this.backbonesStack.peek().iterator(); it
					.hasNext();) {
				int next = it.next();
				assumps.push(next);
			}
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
				this.assumptionsNames.add(var.substring(0, lastDotIndex));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("\"" + var
						+ "\" implies a contradiction");
			}
		} else {
			throw new IllegalArgumentException(var + " is not defined");
		}
	}

	private void addAssumption(Integer id) throws TimeoutException {
		Set<Integer> newSet = new HashSet<Integer>();
		newSet.add(id);
		this.assumptions.add(newSet);
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
		int state = Integer.valueOf(assump.substring(lastDotIndex + 1));
		if (state == 1) {
			addAssumption(this.idMap.getSolverVar(name));
		} else {
			addAssumption(-this.idMap.getSolverVar(name));
		}
		this.assumptionsNames.add(name);
	}

	public void setOptionalConfigVarAsNotInstalled(String var)
			throws TimeoutException {
		Set<String> domain = this.idMap.getConfigVarDomain(var);
		Set<Integer> newAssumps = new HashSet<Integer>();
		for (String s : domain) {
			newAssumps.add(-this.idMap.getSolverVar(s));
		}
		this.assumptions.add(newAssumps);
		this.backbonesStack.push(computeBackbone(this.solver));
	}

	public void clearAssumptions() {
		while (this.backbonesStack.size() > 1) {
			this.backbonesStack.pop();
		}
		this.assumptions = new ArrayList<Set<Integer>>();
		this.assumptionsNames = new HashSet<String>();
	}

	public Set<String> propagatedConfigVars() {
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

	public Set<String> newPropagatedConfigVars() {
		if (this.backbonesStack.isEmpty())
			return null;
		if (this.backbonesStack.size() == 1)
			return propagatedConfigVars();
		Set<String> currentlyAsserted = propagatedConfigVars();
		IVecInt stackTop = this.backbonesStack.pop();
		Set<String> lastStepAsserted = propagatedConfigVars();
		this.backbonesStack.push(stackTop);
		Set<String> newlyAsserted = new TreeSet<String>(VAR_COMP);
		Set<String> lastAssumpsNames = new HashSet<String>();
		for (Integer i : this.assumptions.get(this.assumptions.size() - 1)) {
			lastAssumpsNames.add(this.idMap.getConfigVar(i));
		}
		for (String s : currentlyAsserted) {
			if (!lastStepAsserted.contains(s) && !lastAssumpsNames.contains(s)) {
				int lastDotIndex = s.lastIndexOf('=');
				String name = s.substring(0, lastDotIndex);
				if (!this.assumptionsNames.contains(name)) {
					newlyAsserted.add(s);
				}
			}
		}
		return newlyAsserted;
	}

	public Set<String> newDomainReductions() {
		if (this.backbonesStack.isEmpty())
			return null;
		if (this.backbonesStack.size() == 1)
			return domainReductions();
		Set<String> currentlyAssertedFalse = domainReductions();
		IVecInt stackTop = this.backbonesStack.pop();
		Set<String> lastStepAssertedFalse = domainReductions();
		this.backbonesStack.push(stackTop);
		Set<String> newlyAssertedFalse = new TreeSet<String>(VAR_COMP);
		for (String s : currentlyAssertedFalse) {
			if (!lastStepAssertedFalse.contains(s)) {
				int lastDotIndex = s.lastIndexOf('=');
				String name = s.substring(0, lastDotIndex);
				if (!this.assumptionsNames.contains(name)) {
					newlyAssertedFalse.add(s);
				}
			}
		}
		return newlyAssertedFalse;
	}

	public Set<String> domainReductions() {
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

	public Set<String> newPropagatedAdditionalVars() {
		if (this.backbonesStack.isEmpty())
			return null;
		if (this.backbonesStack.size() == 1)
			return propagatedAdditionalVars();
		Set<String> currentBooleanAssertion = propagatedAdditionalVars();
		IVecInt stackTop = this.backbonesStack.pop();
		Set<String> lastStepBooleanAssertion = propagatedAdditionalVars();
		this.backbonesStack.push(stackTop);
		Set<String> newBooleanAssertion = new TreeSet<String>(VAR_COMP);
		for (String s : currentBooleanAssertion) {
			if (!lastStepBooleanAssertion.contains(s)) {
				int lastDotIndex = s.lastIndexOf('=');
				String name = s.substring(0, lastDotIndex);
				if (!this.assumptionsNames.contains(name)) {
					newBooleanAssertion.add(s);
				}
			}
		}
		return newBooleanAssertion;
	}

	public Set<String> newCspDomainReductions() {
		Set<String> res = new HashSet<String>();
		int lastDotIndex;
		Set<String> lastAssumpsNames = new HashSet<String>();
		if (!this.assumptions.isEmpty()) {
			for (Integer i : this.assumptions.get(this.assumptions.size() - 1)) {
				String name;
				if (i > 0) {
					name = this.idMap.getConfigVar(i);
				} else {
					name = "-" + this.idMap.getConfigVar(-i);
				}
				if (this.idMap.isConfigVar(i)) {
					name = name.substring(0, name.lastIndexOf('.'));
				}
				lastAssumpsNames.add(name);
			}
		}
		for (String s : newPropagatedAdditionalVars()) {
			int lastEqualIndex = s.lastIndexOf('=');
			String name = s.substring(0, lastEqualIndex);
			if (!lastAssumpsNames.contains(name)
					&& !lastAssumpsNames.contains("-" + name)) {
				if (s.endsWith("=1")) {
					res.add(s.replaceFirst("=1", "=99"));
				} else {
					res.add(s.replaceFirst("=99", "=1"));
				}
			}
		}
		for (String s : newDomainReductions()) {
			lastDotIndex = s.lastIndexOf('=');
			String domainName = s.substring(0, lastDotIndex);
			if (!lastAssumpsNames.contains(domainName)
					&& !lastAssumpsNames.contains("-" + domainName)) {
				res.add(s);
			}
		}
		for (String s : newPropagatedConfigVars()) {
			lastDotIndex = s.lastIndexOf('=');
			String domainName = s.substring(0, lastDotIndex);
			if (!lastAssumpsNames.contains(domainName)
					&& this.idMap.isOptionalDomainVar(s)) {
				res.add(domainName + "=99");
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
				boolean firstIsInteger = true;
				Integer n1 = null, n2 = null;
				try {
					n1 = Integer.valueOf(t1[i]);
				} catch (NumberFormatException e) {
					firstIsInteger = false;
					try {
						n1 = Integer.valueOf(t1[i].replaceAll("[a-zA-Z]", ""));
					} catch (NumberFormatException nfe) {
						n1 = -1;
					}
				}
				try {
					n2 = Integer.valueOf(t2[i]);
					if (!firstIsInteger) {
						return -1;
					}
				} catch (NumberFormatException e) {
					if (firstIsInteger) {
						return 1;
					}
					try {
						n2 = Integer.valueOf(t2[i].replaceAll("[a-zA-Z]", ""));
					} catch (NumberFormatException nfe) {
						n2 = -1;
					}
				}
				int v = n1.compareTo(n2);
				if (v != 0) {
					return v;
				}
			}
			return t1.length - t2.length;
		}

	};

}
