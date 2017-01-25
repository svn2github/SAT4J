package org.sat4j.csp.constraints3;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.xcsp.constraints3.test.ComparisonCtrBuilderTest;
import org.xcsp.constraints3.test.ConnectionCtrBuilderTest;
import org.xcsp.constraints3.test.CountingCtrBuilderTest;
import org.xcsp.constraints3.test.ElementaryCtrBuilderTest;
import org.xcsp.constraints3.test.GenericCtrBuilderTest;
import org.xcsp.constraints3.test.LanguageCtrBuilderTest;
import org.xcsp.constraints3.test.ObjBuilderTest;
import org.xcsp.constraints3.test.SchedulingCtrBuilderTest;
import org.xcsp.constraints3.test.TestUtils;

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
