package org.sat4j;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.constraints.MixedDataStructureDanielWL;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.orders.RSATPhaseSelectionStrategy;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.restarts.ArminRestarts;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.minisat.uip.FirstUIP;
import org.sat4j.specs.ISolver;

public class LightFactory extends ASolverFactory<ISolver> {
	private static final long serialVersionUID = 1460304168178023681L;
	private static LightFactory instance;

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new LightFactory();
		}
	}

	/**
	 * Access to the single instance of the factory.
	 * 
	 * @return the singleton of that class.
	 */
	public static LightFactory instance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	@Override
	public ISolver defaultSolver() {
		return newMiniLearningHeapRsatExpSimpBiere();
	}

	@Override
	public ISolver lightSolver() {
		return newMiniLearningHeapRsatExpSimpBiere();
	}

	public static Solver<DataStructureFactory> newMiniLearningHeapRsatExpSimpBiere() {
		Solver<DataStructureFactory> solver = newMiniLearningHeapRsatExpSimp();
		solver.setRestartStrategy(new ArminRestarts());
		solver.setSearchParams(new SearchParams(1.1, 100));
		return solver;
	}

	public static Solver<DataStructureFactory> newMiniLearningHeapRsatExpSimp() {
		Solver<DataStructureFactory> solver = newMiniLearningHeapExpSimp();
		solver.setOrder(new VarOrderHeap(new RSATPhaseSelectionStrategy()));
		return solver;
	}

	public static Solver<DataStructureFactory> newMiniLearningHeapExpSimp() {
		Solver<DataStructureFactory> solver = newMiniLearningHeap();
		solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
		return solver;
	}

	public static Solver<DataStructureFactory> newMiniLearningHeap() {
		return newMiniLearningHeap(new MixedDataStructureDanielWL());
	}

	public static Solver<DataStructureFactory> newMiniLearningHeap(
			DataStructureFactory dsf) {
		return newMiniLearning(dsf, new VarOrderHeap());
	}

	public static Solver<DataStructureFactory> newMiniLearning(
			DataStructureFactory dsf, IOrder order) {
		// LimitedLearning<DataStructureFactory> learning = new
		// PercentLengthLearning<DataStructureFactory>(10);
		MiniSATLearning<DataStructureFactory> learning = new MiniSATLearning<DataStructureFactory>();
		Solver<DataStructureFactory> solver = new Solver<DataStructureFactory>(
				new FirstUIP(), learning, dsf, order, new MiniSATRestarts());
		learning.setSolver(solver);
		return solver;
	}

	public static void main(final String[] args) {
		BasicLauncher<ISolver> lanceur = new BasicLauncher<ISolver>(
				LightFactory.instance());
		if (args.length != 1) {
			lanceur.usage();
			return;
		}
		lanceur.run(args);
		System.exit(lanceur.getExitCode().value());
	}

}
