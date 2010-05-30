package org.sat4j.multicore;

import org.sat4j.core.ASolverFactory;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.ISolver;

public class SolverFactory extends ASolverFactory<IPBSolver> {

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
		return new ManyCore<ISolver>(
				org.sat4j.minisat.SolverFactory.instance(), "Default",
				"MiniSATNoRestarts", "MiniSATHeapExpSimp", "MiniSAT2Heap");
	}

	public static IPBSolver newManyCorePB() {
		return new ManyCorePB(org.sat4j.pb.SolverFactory.instance(),
				"Resolution", "CuttingPlanes");
	}

	@Override
	public IPBSolver defaultSolver() {
		return newManyCorePB();
	}

	@Override
	public IPBSolver lightSolver() {
		return newManyCorePB();
	}

}
