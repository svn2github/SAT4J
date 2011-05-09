package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.sat4j.pb.tools.DependencyHelper;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class BugSAT22 {
	@Test
	public void testSimpleResolverUnitFirst() throws ContradictionException,
			TimeoutException {
		IPBSolver solver = SolverFactory.newEclipseP2();
		DependencyHelper<Named, String> helper = new DependencyHelper<Named, String>(
				solver, false);
		Set<Named> slice = new HashSet<Named>();
		Named A1 = new Named("A1");
		slice.add(A1);
		Named A2 = new Named("A2");
		slice.add(A2);
		Named B = new Named("B");
		slice.add(B);
		Named X = new Named("X");
		// base
		helper.setTrue(X, "Build");
		// objective function
		helper.addToObjectiveFunction(A2, 1);
		helper.addToObjectiveFunction(A1, 2);
		helper.addToObjectiveFunction(B, 1);
		// depends
		helper.or("a", X, new Named[] { A1, A2 });
		helper.or("b", X, new Named[] { B });
		// solve
		assertTrue(helper.hasASolution());
		IVec<Named> solution = helper.getSolution();
		assertEquals(3, solution.size());
		assertTrue(solution.contains(B));
		assertTrue(solution.contains(X));
		assertTrue(solution.contains(A2));
	}

	@Test
	public void testSimpleResolverUnitLast() throws ContradictionException,
			TimeoutException {
		IPBSolver solver = SolverFactory.newEclipseP2();
		DependencyHelper<Named, String> helper = new DependencyHelper<Named, String>(
				solver, false);
		Set<Named> slice = new HashSet<Named>();
		Named A1 = new Named("A1");
		slice.add(A1);
		Named A2 = new Named("A2");
		slice.add(A2);
		Named B = new Named("B");
		slice.add(B);
		Named X = new Named("X");
		// objective function
		helper.addToObjectiveFunction(A2, 1);
		helper.addToObjectiveFunction(A1, 2);
		helper.addToObjectiveFunction(B, 1);
		// depends
		helper.or("a", X, new Named[] { A1, A2 });
		helper.or("b", X, new Named[] { B });
		// base
		helper.setTrue(X, "Build");
		// solve
		assertTrue(helper.hasASolution());
		IVec<Named> solution = helper.getSolution();
		assertEquals(3, solution.size());
		assertTrue(solution.contains(B));
		assertTrue(solution.contains(X));
		assertTrue(solution.contains(A2));
	}

	public class Named {
		public String name;

		public Named(String name) {
			this.name = name;
		}
	}
}
