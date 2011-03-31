package org.sat4j.pb.constraints;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;

public class PBCPMaxClauseCardConstrLearningTest extends
		AbstractPseudoBooleanAndPigeonHoleTest {

	public PBCPMaxClauseCardConstrLearningTest(String arg) {
		super(arg);
	}

	@Override
	protected IPBSolver createSolver() {
		return SolverFactory.newCompetPBCPMixedConstraintsObjective();
	}

}
