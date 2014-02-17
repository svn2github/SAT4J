package org.sat4j.br4cp;

import java.util.Map;
import java.util.Set;

import br4cp.Configurator;

public class Br4cpConfigurator implements Configurator {

	private Br4cpCLI br4cp;
	private boolean fault = false;
	
	@Override
	public void readProblem(String problemName) {
		String instance = problemName + ".txt";
		String prices = problemName + "_prices.txt";
		try {
			br4cp = new Br4cpCLI(instance, prices);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}

	}

	@Override
	public void assign(String var, String val) {
		fault = false;
		try {
			br4cp.assumeMe(var + "=" + val);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			fault = true;
		}
	}

	@Override
	public void unassign(String var) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void propagateChoices() {
		// TODO Auto-generated method stub

	}

	@Override
	public int mincost() {
		try {
			if (br4cp.minimize()) {
				return br4cp.getObjectiveValue().intValue();
			} else {
				throw new IllegalStateException(
						"There is no solution to minimize !!!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return -1;
		}
	}

	@Override
	public Map<String, String> mincostCertificate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int maxcost() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getSizeOfCurrentDomainOf(String var) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPresentInCurrentDomain(String var, String val) {
		return br4cp.isPresentInCurrentDomain(var,val);
	}

	@Override
	public Set<String> getCurrentDomainOf(String var) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Integer> mincosts(String var) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Integer> maxcosts(String var) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getFreeVariables() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNoChoice() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPossiblyConsistent() {
		return !fault;
	}

	@Override
	public Set<String> getAlternativeDomainOf(String var) {
		throw new UnsupportedOperationException();
	}

}
