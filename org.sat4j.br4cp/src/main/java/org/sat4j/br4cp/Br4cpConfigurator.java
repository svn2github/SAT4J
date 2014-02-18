package org.sat4j.br4cp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import br4cp.Configurator;

public class Br4cpConfigurator implements Configurator {

	private Br4cpCLI br4cp;
	private boolean fault = false;
	
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

	public void assign(String var, String val) {
		fault = false;
		try {
			br4cp.assumeMe(var + "=" + val);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			fault = true;
		}
	}

	public void unassign(String var) {
		br4cp.unassign(var);

	}

	public void propagateChoices() {
		// TODO Auto-generated method stub

	}

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

	public Map<String, String> mincostCertificate() {
		throw new UnsupportedOperationException();
	}

	public int maxcost() {
		throw new UnsupportedOperationException();
	}

	public int getSizeOfCurrentDomainOf(String var) {
		return br4cp.getSizeOfCurrentDomainOf(var);
	}

	public boolean isPresentInCurrentDomain(String var, String val) {
		return br4cp.isPresentInCurrentDomain(var,val);
	}

	public Set<String> getCurrentDomainOf(String var) {
		return br4cp.getCurrentDomainOf(var);
	}

	public Map<String, Integer> mincosts(String var) {		
		Map<String,Integer> certificate = new HashMap<String,Integer>();
		for (String value : getCurrentDomainOf(var)) {
			assign(var, value);
			certificate.put(value, mincost());
		}
		unassign(var);
		return certificate;
	}

	public Map<String, Integer> maxcosts(String var) {
		throw new UnsupportedOperationException();
	}

	public Set<String> getFreeVariables() {
		return br4cp.getFreeVariables();
	}

	public boolean hasNoChoice() {
		throw new UnsupportedOperationException();
	}

	public boolean isPossiblyConsistent() {
		return !fault;
	}

	public Set<String> getAlternativeDomainOf(String var) {
		throw new UnsupportedOperationException();
	}

}
