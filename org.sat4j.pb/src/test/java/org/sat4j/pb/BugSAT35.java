package org.sat4j.pb;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.sat4j.pb.tools.LexicoHelper;

public class BugSAT35 {

	private LexicoHelper<String, String> helper;

	@Test
	public void buildHelperWithDefaultConstructor() {
		helper = new LexicoHelper<String, String>(SolverFactory.newDefault());
		assertNotNull(helper);
	}

	@Test
	public void buildHelperWithExplanationParameterToTrueConstructor() {
		helper = new LexicoHelper<String, String>(SolverFactory.newDefault(),
				true);
		assertNotNull(helper);
	}

	@Test
	public void buildHelperWithExplanationParameterToFalseConstructor() {
		helper = new LexicoHelper<String, String>(SolverFactory.newDefault(),
				false);
		assertNotNull(helper);
	}

}
