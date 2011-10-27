package org.sat4j.maxsat;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;
import org.sat4j.tools.OptToSatAdapter;

public class BugFatih2 {

	@Test
	public void testBugReport() throws ContradictionException, TimeoutException {
		// ModelIterator solver = new ModelIterator(new OptToSatAdapter(
		// new MaxSatDecorator(SolverFactory.newDefault())));
		WeightedMaxSatDecorator maxSatSolver = new WeightedMaxSatDecorator(
				org.sat4j.maxsat.SolverFactory.newDefault());
		ModelIterator solver = new ModelIterator(new OptToSatAdapter(new PseudoOptDecorator(maxSatSolver)));
		System.out.println("Taille de voc : " + solver.nVars());
		solver.newVar(13);
		solver.setExpectedNumberOfClauses(24);
		maxSatSolver.addHardClause(new VecInt(new int[] { -1 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -2 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -3, 4 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -3, 5 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -3, 6 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -1, 7 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -2, 6 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -4, 3 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -5, 3 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -6, 3, 2 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -7, 1 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { 3, -1, 8 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -3, 1, 8 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -3, -1, 9 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -9 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { 1, -2, 10 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -1, 2, 10 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -1, -2, 11 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -10 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -11 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { 3, -1, 12 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -3, 1, 12 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -3, -1, 13 }));
		maxSatSolver.addHardClause(new VecInt(new int[] { -13 }));
		System.out.println("Taille de voc : " + solver.nVars());
		while (solver.isSatisfiable()) {
			System.out.println("Taille du modèle : " + solver.model().length);
			for (int i = 1; i <= solver.model().length; i++) {
				System.out.print(solver.model(i) + " ");
			}
			System.out.println();
		}
	}
}
