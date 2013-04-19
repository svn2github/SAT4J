package org.sat4j.br4cp;

import java.util.List;
import java.util.Set;

import org.sat4j.specs.TimeoutException;

public interface IBr4cpBackboneComputer {

	public void addAssumption(String var) throws TimeoutException;

	public List<String> getAssumptions();

	public void removeLastAssumption();

	public void clearAssumptions();

	public Set<String> getBackbones();

	public Set<String> asserted();

	public Set<String> assertedFalse();

	public Set<String> newlyAsserted();

	public Set<String> newlyAssertedFalse();
}
