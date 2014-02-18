package br4cp;

import java.util.Map;
import java.util.Set;

public interface Configurator {

	/**
	 * Read a configuration file. Both the xml format and the textual format
	 * will be provided. It is up to the solver to choose which format to use.
	 * Note that the prices are expected to be found in a file with a "_prices"
	 * postfix.
	 * 
	 * 
	 * @param problemName
	 *            the path to the problem, without the extension (.xml or .txt)
	 */
	void readProblem(String problemName);

	/**
	 * Assign a specific value to a variable.
	 * 
	 * @param var
	 * @param val
	 * @return true iff the assignment can be done
	 * @pre getCurrentDomainOf(var).contains(val)
	 */
	void assignAndPropagate(String var, String val);

	/**
	 * Unassign a specific variable
	 * 
	 * @param var
	 */
	void unassignAndRestore(String var);

	/**
	 * Get the minimal price of the configurations compatible with the current
	 * choices.
	 * 
	 * @return the cost of the configuration
	 */
	int minCost();

	/**
	 * Provide a full configuration of minimal cost.
	 * 
	 * @return a full assignment var->value of minimal cost (given by {@link #minCost()}
	 */
	Map<String, String> minCostConfiguration();

	/**
	 * Get the maximal price of the configurations compatible with the current
	 * choices.
	 * 
	 * @return the cost of the configuration
	 */
	int maxCost();
	
	/**
	 * Provide a full configuration of maximal cost.
	 * 
	 * @return a full assignment var->value of maximal cost (given by {@link #maxCost()}
	 */
	Map<String, String> maxCostConfiguration();

	/**
	 * @inv getSizeOfCurrentDomain(var) == getCurrentDomainOf(var).size()
	 */
	int getSizeOfCurrentDomainOf(String var);

	/**
	 * 
	 * @param var
	 * @param val
	 * @return
	 * @inv isCurrentInCurrentDomain(var,val)==
	 *      getCurrentDomainOf(var).contains(val)
	 */
	boolean isPresentInCurrentDomain(String var, String val);

	Set<String> getCurrentDomainOf(String var);

	/**
	 * Retrieve for each valid value of the variable the minimal cost of the
	 * configuration.
	 * 
	 * @param var
	 *            a variable id
	 * @return a map value->mincost
	 */
	Map<String, Integer> minCosts(String var); 

	/**
	 * Retrieve for each valid value of the variable the maximal cost of the
	 * configuration.
	 * 
	 * @param var
	 *            a variable id
	 * @return a map value->maxcost
	 */
	Map<String, Integer> maxCosts(String var); 

	/**
	 * Get all unassigned variables.
	 * 
	 * @return a set of non assigned variables.
	 */
	Set<String> getFreeVariables(); 

	/**
	 * Check that there is no more choice for the user.
	 * 
	 * @return true iff there is exactly one value left per variable.
	 */
	boolean isConfigurationComplete(); 

	/**
	 * Check there there is at least one value in each domain. Note that
	 * depending of the level of consistency used, the configuration may of may
	 * not be finally consistent.
	 * 
	 * @return true iff there is at least one value left per variable.
	 */
	boolean isPossiblyConsistent(); 

	Set<String> getAlternativeDomainOf(String var);
}
