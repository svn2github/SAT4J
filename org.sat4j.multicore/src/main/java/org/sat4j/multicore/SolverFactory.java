package org.sat4j.multicore;

import org.sat4j.core.ASolverFactory;
import org.sat4j.specs.ISolver;

public class SolverFactory extends ASolverFactory<ISolver> {

	private static final SolverFactory instance = new SolverFactory();
	
	private SolverFactory() {
		
	}
	
	public static SolverFactory instance() {
		return instance;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static ISolver newManyCore() {
		return new ManyCore(org.sat4j.minisat.SolverFactory.instance(),"Default", "MiniSATNoRestarts","MiniSATHeapExpSimp","MiniSAT2Heap");
	}
	
	@Override
	public ISolver defaultSolver() {
		return newManyCore();
	}

	@Override
	public ISolver lightSolver() {
		return newManyCore();
	}

}
