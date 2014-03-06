package org.sat4j.br4cp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sat4j.specs.ISolver;

/**
 * A class that provides a mapping between configuration/additional variables
 * and solver variables. It also provides some methods to check if a variable is
 * a configuration or an additional one, if it is optional, ...
 * 
 * @author lonca
 * 
 */
public class ConfigVarMap {

	private Map<String, Integer> configVarToSolverVar = new HashMap<String, Integer>();
	private Map<Integer, String> solverVarToConfigVar = new HashMap<Integer, String>();
	private Map<String, Set<Integer>> configVarDomains = new HashMap<String, Set<Integer>>();
	private Set<String> additionalVars = new HashSet<String>();
	private Set<String> optionalConfigVars = new HashSet<String>();
	private ISolver solver;

	public ConfigVarMap(ISolver solver) {
		this.solver = solver;
	}

	/**
	 * Returns the solver variable mapped with a configuration variable. If a
	 * solver variable is not already mapped with the configuration variable, a
	 * new solver variable is reserved and returned.
	 * 
	 * @param configVar
	 *            the configuration variable
	 * @return the solver variable
	 */
	public int getSolverVar(String configVar) {
		Integer solverVar = configVarToSolverVar.get(configVar);
		if (solverVar == null) {
			solverVar = solver.nextFreeVarId(true);
			this.solverVarToConfigVar.put(solverVar, configVar);
			this.configVarToSolverVar.put(configVar, solverVar);
			try {
				int version = extractVarVersion(configVar);
				String name = extractVarName(configVar);
				Set<Integer> versions = this.configVarDomains.get(name);
				if (versions == null) {
					versions = new HashSet<Integer>();
					this.configVarDomains.put(name, versions);
				}
				versions.add(version);
			} catch (NumberFormatException e) {
				this.additionalVars.add(configVar);
			}
		}
		return solverVar;
	}

	/**
	 * Checks if a configuration variable is already mapped with a solver
	 * variable.
	 * 
	 * @param configVar
	 *            the configuration variable
	 * @return true iff a configuration variable is already mapped with a solver
	 *         variable.
	 */
	public boolean configVarExists(String configVar) {
		return configVarToSolverVar.get(configVar) != null;
	}

	/**
	 * Returns the configuration variable mapped with a solver variable
	 * 
	 * @param solverVar
	 *            the solver variable
	 * @return the configuration variable
	 */
	public String getConfigVar(Integer solverVar) {
		return this.solverVarToConfigVar.get(solverVar);
	}

	/**
	 * Checks if a solver variable or its negation is mapped with an additional
	 * variable
	 * 
	 * @param solverVar
	 *            the solver variable
	 * @return true iff a solver variable is mapped with an additional variable
	 */
	public boolean isAdditionalVar(Integer solverVar) {
		solverVar = Math.max(solverVar, -solverVar);
		String name = this.solverVarToConfigVar.get(solverVar);
		return (name != null) && this.additionalVars.contains(name);
	}
	
	/**
	 * Checks if a solver variable or its negation is mapped with an additional
	 * variable
	 * 
	 * @param solverVar
	 *            the solver variable
	 * @return true iff a solver variable is mapped with an additional variable
	 */
	public boolean isAdditionalVar(String var) {
		return this.additionalVars.contains(var) || this.additionalVars.contains(var.substring(0, Math.max(var.lastIndexOf('.'), var.lastIndexOf('='))));
	}

	/**
	 * Checks if a solver variable or its negation is mapped with a
	 * configuration variable
	 * 
	 * @param solverVar
	 *            the solver variable
	 * @return true iff a solver variable is mapped with a configuration
	 *         variable
	 */
	public boolean isConfigVar(Integer solverVar) {
		solverVar = Math.max(solverVar, -solverVar);
		String name = this.solverVarToConfigVar.get(solverVar);
		return this.configVarToSolverVar.containsKey(name)
				&& !isAdditionalVar(solverVar);
	}

	/**
	 * Checks if a variable is a configuration variable
	 * 
	 * @param var
	 * @return true iff a variable is a configuration variable
	 */
	public boolean isConfigVar(String var) {
		return this.configVarToSolverVar.containsKey(var) && !isBooleanVar(var);
	}

	/**
	 * Checks if a variable is a configuration variable with a valid version
	 * number, i.e. already defined.
	 * 
	 * @param var
	 *            the variable
	 * @return true iff a variable is a configuration variable with a valid
	 *         version number.
	 */
	public boolean isOutOfDomainConfigVar(String var) {
		String name = extractVarName(var);
		int version = extractVarVersion(var);
		return this.configVarDomains.containsKey(name)
				&& !this.configVarDomains.get(name).contains(version);
	}

	/**
	 * Returns all the configuration variables which have the same name as the
	 * one passed as a parameter (included) as a set of String.
	 * 
	 * @param configVar
	 *            the configuration variable with a value (SAT encoding)
	 * @return a set of String which contains which contains all the
	 *         configuration variables that have the same name.
	 */
	public Set<String> getConfigVarDomain(String configVar) {
		String name = extractVarName(configVar);
		Set<Integer> versions = this.configVarDomains.get(name);
		Set<String> res = new HashSet<String>();
		for (Integer v : versions) {
			res.add(name + "." + v.toString());
		}
		return res;
	}

	/**
	 * Returns all the configuration variables which have the same name as the
	 * one passed as a parameter (included) as a set of String.
	 * 
	 * @param name
	 *            the configuration variable with no value (original encoding)
	 * @return a set of String which contains which contains all the
	 *         configuration variables that have the same name.
	 */
	public Set<String> getDomain(String name) {
		Set<Integer> versions = this.configVarDomains.get(name);
		if (versions==null) {
			return null;
		}
		Set<String> res = new HashSet<String>();
		for (Integer v : versions) {
			res.add(name + "." + v.toString());
		}
		return res;
	}
	
	/**
	 * Sets a configuration variable (and all the ones which have the same name)
	 * as optional. This method must be called for some other, included
	 * implications counting one, but maybe not restricted to, to work properly.
	 * 
	 * @param configVar
	 *            the configuration variable
	 */
	public void setAsOptionalConfigVar(String configVar) {
		this.optionalConfigVars.add(extractVarName(configVar));
	}

	/**
	 * Checks if a configuration variable is optional.
	 * 
	 * @param configVar
	 *            the configuration variable
	 * @return true iff a configuration variable is optional.
	 */
	public boolean isOptionalDomainVar(String configVar) {
		return this.optionalConfigVars.contains(extractVarName(configVar));
	}

	private String extractVarName(String var) {
		int lastDotIndex = Math.max(var.lastIndexOf('.'), var.lastIndexOf('='));
		if(lastDotIndex == -1){
			throw new IllegalArgumentException(var + " is not defined");
		}
		return var.substring(0, lastDotIndex);
	}

	private Integer extractVarVersion(String var) {
		int lastDotIndex = Math.max(var.lastIndexOf('.'), var.lastIndexOf('='));
		if(lastDotIndex == -1){
			throw new IllegalArgumentException(var + " is not defined");
		}
		try {
			return Integer.valueOf(var.substring(lastDotIndex + 1));
		} catch (NumberFormatException e) {
			throw new NumberFormatException(var+" has no version or state");
		}
	}

	private boolean isBooleanVar(String s) {
		return this.additionalVars.contains(s);
	}

	public Set<String> getVars() {
		Set<String> vars =  new HashSet<String>();
		vars.addAll(this.configVarDomains.keySet());
		vars.addAll(this.additionalVars);
		return vars;
	}
}
