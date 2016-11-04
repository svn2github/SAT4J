package org.sat4j.csp.intension;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class IntensionCtrEncoder implements ICspToSatEncoder {
	
	private final  ICspToSatEncoder solver;
	
	private final Map<String, int[]> domains = new HashMap<>();

	public IntensionCtrEncoder(ICspToSatEncoder solver) {
		this.solver = solver;
	}
	
	public boolean encode(String strExpression) {
		Parser parser = new Parser(strExpression);
		parser.parse();
		final IExpression expression = parser.getExpression();
		return encodeExpression(expression);
	}

	private boolean encodeExpression(IExpression expression) {
		if(expression.isAndOperator()) {
			return encodeAndExpression(expression);
		}
		final Set<String> involved = expression.involvedVars();
		extractDomains(involved);
		InstantiationIterator instantiationIterator = new InstantiationIterator(involved, this.domains);
		for(Map<String, Integer> fullInstantiation : instantiationIterator) {
			if(expression.updateEvaluation(instantiationIterator.getInstantiationUpdate()) == 0) {
				if(encodeNogood(fullInstantiation)) return true;
			}
		}
		return false;
	}

	private boolean encodeAndExpression(IExpression expression) {
		boolean contradictionFound = false;
		for(int i=0; (!contradictionFound) && (i<expression.operands().length); ++i) {
			contradictionFound |= encodeExpression(expression.operands()[i]);
		}
		return contradictionFound;
	}

	private void extractDomains(Set<String> involved) {
		for(String var : involved) {
			if(domains.get(var) == null) {
				this.domains.put(var, this.solver.getCspVarDomain(var));
			}
		}
	}
	
	private boolean encodeNogood(Map<String, Integer> fullInstantiation) {
		int[] clause = new int[fullInstantiation.size()];
		int i=0;
		for(Map.Entry<String, Integer> assignment : fullInstantiation.entrySet()) {
			clause[i++] = -this.solver.getSolverVar(assignment.getKey(), assignment.getValue());
		}
		return this.solver.addClause(clause);
	}
	
	private class InstantiationIterator implements Iterable<Map<String, Integer>>, Iterator<Map<String, Integer>> {
		
		private final Map<String, int[]> domainMap;
		
		private final String[] vars;
		private final int[][] domains;
		private final int[] domainSizes;
		private final int[] domainIndexes;
		
		private Map<String, Integer> fullInstantiation;
		private Map<String, Integer> lastFullInstantiation;
		private Map<String, Integer> lastInstantiationUpdate;
		private Map<String, Integer> instantiationUpdate;
		
		public InstantiationIterator(final Set<String> involved, final Map<String, int[]> domainMap) {
			this.domainMap = domainMap;
			final int nVars = involved.size();
			if(nVars == 0) throw new IllegalArgumentException("number of involved vars must be higher than zero");
			this.vars = new String[nVars];
			this.domains = new int[nVars][];
			this.domainSizes = new int[nVars];
			this.domainIndexes = new int[nVars];
			initDataStructures(involved);
			computeFirst();
		}

		private void initDataStructures(final Set<String> involved) {
			int i=0;
			for(String var : involved) {
				this.vars[i] = var;
				final int[] domain = this.domainMap.get(var);
				this.domains[i] = domain;
				this.domainSizes[i] = domain.length;
				this.domainIndexes[i] = 0;
				++i;
			}
		}

		@Override
		public Iterator<Map<String, Integer>> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return instantiationUpdate != null;
		}

		@Override
		public Map<String, Integer> next() {
			this.lastFullInstantiation = this.fullInstantiation;
			this.lastInstantiationUpdate = this.instantiationUpdate;
			computeNext();
			return this.lastFullInstantiation;
		}
		
		public Map<String, Integer> getInstantiationUpdate() {
			return this.lastInstantiationUpdate;
		}
		
		private void computeFirst() {
			this.fullInstantiation = new HashMap<>();
			this.instantiationUpdate = new HashMap<>();
			for(int i=0; i<this.vars.length; ++i) {
				this.fullInstantiation.put(this.vars[i], this.domains[i][0]);
				this.instantiationUpdate.put(this.vars[i], this.domains[i][0]);
			}
		}

		private void computeNext() {
			this.fullInstantiation = new HashMap<>(this.lastFullInstantiation);
			this.instantiationUpdate = new HashMap<>();
			int i;
			for(i=this.vars.length-1; i>=0; --i) {
				if(this.domainIndexes[i] < this.domainSizes[i]-1) {
					++this.domainIndexes[i];
					this.fullInstantiation.put(this.vars[i], this.domains[i][this.domainIndexes[i]]);
					this.instantiationUpdate.put(this.vars[i], this.domains[i][this.domainIndexes[i]]);
					break;
				}
				this.domainIndexes[i] = 0;
				this.fullInstantiation.put(this.vars[i], this.domains[i][0]);
				this.instantiationUpdate.put(this.vars[i], this.domains[i][0]);
			}
			if(i == -1) {
				this.fullInstantiation = null;
				this.instantiationUpdate = null;
			}
		}
	}

	@Override
	public int[] getCspVarDomain(String var) {
		return this.solver.getCspVarDomain(var);
	}

	@Override
	public int getSolverVar(String var, Integer value) {
		return this.solver.getSolverVar(var, value);
	}

	@Override
	public boolean addClause(int[] clause) {
		return this.solver.addClause(clause);
	}

}
