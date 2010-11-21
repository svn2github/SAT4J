package org.sat4j.pb.tools;

import java.util.Collection;

import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PBSolverDecorator;
import org.sat4j.specs.IVecInt;

public class LexicoHelper<T, C> extends DependencyHelper<T, C> {

	private final LexicoDecoratorPB lexico;

	public LexicoHelper(IPBSolver solver) {
		super(new OptToPBSATAdapter(new LexicoDecoratorPB(solver)));
		lexico = (LexicoDecoratorPB) ((PBSolverDecorator) getSolver())
				.decorated();
	}

	public LexicoHelper(IPBSolver solver, boolean explanationEnabled,
			boolean canonicalOptFunctionEnabled) {
		super(new OptToPBSATAdapter(new LexicoDecoratorPB(solver)),
				explanationEnabled, canonicalOptFunctionEnabled);
		lexico = (LexicoDecoratorPB) ((PBSolverDecorator) getSolver())
				.decorated();
	}

	public LexicoHelper(IPBSolver solver, boolean explanationEnabled) {
		super(new OptToPBSATAdapter(new LexicoDecoratorPB(solver)),
				explanationEnabled);
		lexico = (LexicoDecoratorPB) ((PBSolverDecorator) getSolver())
				.decorated();
	}

	public void addCriterion(Collection<T> things) {
		IVecInt literals = new VecInt(things.size());
		for (T thing : things) {
			literals.push(getIntValue(thing));
		}
		lexico.addCriterion(literals);
	}
}
