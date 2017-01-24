package org.sat4j.csp.constraints3;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ComparisonCtrBuilderTest.class,
	ConnectionCtrBuilderTest.class,
	CountingCtrBuilderTest.class,
	ElementaryCtrBuilderTest.class,
	GenericCtrBuilderTest.class,
	LanguageCtrBuilderTest.class,
	SchedulingCtrBuilderTest.class,
	ObjBuilderTest.class
	})

public class XCSP3TestSuite {
	
	@BeforeClass
	public static void setUp() {
		TestUtils.setSolverClass(XCSP3Sat4jSolver.class);
	}

}
