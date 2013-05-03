package org.sat4j.br4cp;

import java.util.Set;

import org.sat4j.specs.TimeoutException;

/**
 * This class is used to get implied variables from assumptions.
 * 
 * @author lonca
 *
 */
public interface IBr4cpBackboneComputer {

	/**
	 * Adds a configuration variable as an assumption and compute the implied variables.
	 * 
	 * @param configVar
	 *            the variable to assume
	 * @throws TimeoutException
	 *             if the computation time exceed the solver given time
	 */
	public void addAssumption(String configVar) throws TimeoutException;

	/**
	 * Makes necessary assumptions for the optional configuration variables which
	 * have the same name as the parameter to be all set to not installed.
	 * 
	 * @param optConfigVar
	 *            the optional configuration variable
	 * @throws TimeoutException
	 *             if the computation time exceed the solver given time
	 */
	public void setOptionalConfigVarAsNotInstalled(String optConfigVar)
			throws TimeoutException;
	
	/**
	 * Adds an additional variable as an assumption and compute the implied variables.
	 * @param addVar the additional variable.
	 * @throws TimeoutException if the computation time exceed the solver given time.
	 */
	public void addAdditionalVarAssumption(String addVar) throws TimeoutException;

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
	 * Returns the propagated configuration variables appeared after the last
	 * assumption.
	 * 
	 * @return the propagated configuration variables appeared after the last
	 *         assumption.
	 */
	public Set<String> newPropagatedConfigVars();

	/**
	 * Returns the variables which became unavailable due to the assumptions.
	 * 
	 * @return the variables which became unavailable due to the assumptions.
	 */
	public Set<String> domainReductions();

	/**
	 * Returns the variables which became unavailable after the last assumption.
	 * 
	 * @return the variables which became unavailable after the last assumption.
	 */
	public Set<String> newDomainReductions();

	/**
	 * Returns the propagated additional variables due to the assumptions.
	 * 
	 * @return the propagated additional variables due to the assumptions.
	 */
	public Set<String> propagatedAdditionalVars();

	/**
	 * Returns the propagated additional variables appeared after the last
	 * assumption.
	 * 
	 * @return the propagated additional variables appeared after the last
	 *         assumption.
	 */
	public Set<String> newPropagatedAdditionalVars();

	/**
	 * Returns the new domain reductions as a CSP solver would do, that is
	 * adding a X=99 value if optional variable X is not installed.
	 * 
	 * @return the new domain reductions as a CSP solver would do.
	 */
	public Set<String> newCspDomainReductions();

}
