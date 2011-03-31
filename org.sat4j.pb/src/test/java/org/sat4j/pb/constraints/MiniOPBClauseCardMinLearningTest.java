package org.sat4j.pb.constraints;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;

public class MiniOPBClauseCardMinLearningTest extends
		AbstractPseudoBooleanAndPigeonHoleTest {

	public MiniOPBClauseCardMinLearningTest(String arg) {
		super(arg);
	}

	@Override
	protected IPBSolver createSolver() {
		return SolverFactory.newMiniOPBClauseCardMin();
	}

}
