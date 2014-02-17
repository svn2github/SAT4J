package org.sat4j.br4cp;

import java.util.List;
import java.util.Set;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

/**
 * This class is used to get implied variables from assumptions.
 * 
 * @author lonca
 * 
 */
public interface IBr4cpBackboneComputer {

	/**
	 * Adds a configuration variable as an assumption and compute the implied
	 * variables.
	 * 
	 * @param configVar
	 *            the variable to assume
	 * @throws TimeoutException
	 *             if the computation time exceed the solver given time
	 * @throws ContradictionException
	 *             if the new configuration variable causes a contradiction
	 */
	public void addAssumption(String configVar) throws TimeoutException,
			ContradictionException;

	/**
	 * Makes necessary assumptions for the optional configuration variables
	 * which have the same name as the parameter to be all set to not installed.
	 * 
	 * @param optConfigVar
	 *            the optional configuration variable
	 * @throws TimeoutException
	 *             if the computation time exceed the solver given time
	 */
	public void setOptionalConfigVarAsNotInstalled(String optConfigVar)
			throws TimeoutException;

	/**
	 * Adds an additional variable as an assumption and compute the implied
	 * variables.
	 * 
	 * @param addVar
	 *            the additional variable.
	 * @throws TimeoutException
	 *             if the computation time exceed the solver given time.
	 */
	public void addAdditionalVarAssumption(String addVar)
			throws TimeoutException;

	/**
	 * Removes all the assumptions.
	 */
	public void clearAssumptions();

	/**
	 * Returns all the propagated configurations variables, that is the ones for
	 * which only one version is available due to the assumptions.
	 * 
	 * @return all the propagated configurations variables, that is the ones for
	 *         which only one version is available due to the assumptions.
	 */
	public Set<String> propagatedConfigVars();

	/**
	 * Returns the variables which became unavailable due to the assumptions.
	 * Configuration variables vx=y are not returned if vx=z is propagated.
	 * 
	 * @return the variables which became unavailable due to the assumptions.
	 */
	public Set<String> domainReductions();

	/**
	 * Returns the propagated additional variables due to the assumptions, ended with "=1".
	 * Additional variables propagated to false are not returned, see
	 * {@link IBr4cpBackboneComputer#unavailableAdditionalVars()}
	 * 
	 * @return the propagated additional variables due to the assumptions.
	 */
	public Set<String> propagatedAdditionalVars();
	
	/**
	 * Returns the additional variables propagated to false, ended with "=1".
	 * 
	 * @return the additional variables propagated to false, ended with "=1".
	 */
	public Set<String> unavailableAdditionalVars();

	/**
	 * Allow to get the assumptions made by the computer. The solver variables
	 * are returned.
	 * 
	 * @return the solver variables in the assumption stack
	 */
	public List<Set<Integer>> getSolverAssumptions();
	
	/**
	 * Returns the number of SAT calls needed to compute the backbone.
	 * 
	 * @return the numbers of calls to the underlying SAT solver.
	 */
	public int getNumberOfSATCalls();

	/**
	 * Check if a particular value is still in the domain of a variable.
	 * 
	 * @param var a variable
	 * @param val a value
	 * @return true iff the variable is still in the domain of var
	 */
	public boolean isPresentInCurrentDomain(String var, String val);

}
