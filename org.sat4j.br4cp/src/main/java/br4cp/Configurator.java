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
	void assign(String var, String val);

	/**
	 * Unassign a specific variable
	 * 
	 * @param var
	 */
	void unassign(String var);

	/**
	 * Propagate user choices in the configurator.
	 */
	void propagateChoices();

	/**
	 * Get the minimal price of the configurations compatible with the current
	 * choices.
	 * 
	 * @return the cost of the configuration
	 */
	int mincost();

	Map<String, String> mincostCertificate();

	/**
	 * Get the maximal price of the configurations compatible with the current
	 * choices.
	 * 
	 * @return the cost of the configuration
	 */
	int maxcost();

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

	Set<String> getCurrentDomainOf(String var); // les valeurs autorisés dans le
												// domaine de la variable ?? que
												// rendre ? un set de string ?

	/**
	 * Retrieve for each valid value of the variable the minimal cost of the
	 * configuration.
	 * 
	 * @param var
	 *            a variable id
	 * @return a map value->mincost
	 */
	Map<String, Integer> mincosts(String var); // cout min pour les valeurs de
												// var, null si non autorisée.
												// ?? que rendre ? un hasmap
												// string int ?

	Map<String, Integer> maxcosts(String var); // cout max pour les valeurs de
												// var, null si non autorisée ??
												// que rendre ? un hasmap string
												// int ?

	Set<String> getFreeVariables(); // nombre de variables auxquelles il reste
									// plus
									// d'une valeur

	/**
	 * Check that there is no more choice for the user.
	 * 
	 * @return true iff there is at most one value left per variable.
	 */
	boolean hasNoChoice(); // vrai ssi toute variable a au plus une valeur

	/**
	 * Check there there is at least one value in each domain. Note that
	 * depending of the level of consistency used, the configuration may of may
	 * not be finally consistent.
	 * 
	 * @return true iff there is at least one value left per variable.
	 */
	boolean isPossiblyConsistent(); // vrai ssi toute variable a au moins une
									// valeur

	Set<String> getAlternativeDomainOf(String var);
}
