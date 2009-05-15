package org.sat4j.pb;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.sat4j.pb.reader.OPBEclipseReader2007;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class TestEclipseBug275101 {
	private static final String PREFIX = System.getProperty("test.pbprefix");

	@Test
	public void testReserveVarsButUseLess() throws ContradictionException,
			TimeoutException, FileNotFoundException, ParseFormatException,
			IOException {
		IPBSolver solver = SolverFactory.newEclipseP2();
		Reader reader = new OPBEclipseReader2007(solver);
		reader.parseInstance(PREFIX + "bug275101.opb");
		assertTrue(solver.isSatisfiable());
	}
}
