package org.sat4j.pb;

import org.sat4j.pb.tools.XplainPB;
import org.sat4j.tools.xplain.InsertionStrategy;
import org.sat4j.tools.xplain.Xplain;

public class TestInsertionXplain extends AbstractPBXplainTest {

	@Override
	protected Xplain<IPBSolver> getXplain() {
		Xplain<IPBSolver> solver = new XplainPB(SolverFactory.newDefault());
		solver.setMinimizationStrategy(new InsertionStrategy());
		return solver;
	}

}
