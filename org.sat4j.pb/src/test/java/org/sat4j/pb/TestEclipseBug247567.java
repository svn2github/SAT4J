/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
* 
* Based on the pseudo boolean algorithms described in:
* A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
* Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
* Volume 24, Issue 3, March 2005 Page(s): 305 - 317
* 
* and 
* Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
* Framework. Ph.D. Dissertation, University of Oregon.
*******************************************************************************/
package org.sat4j.pb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.sat4j.pb.reader.OPBEclipseReader2007;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

/**
 * Test case to prevent a bug occurring with some Eclipse test cases:
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=247567
 * 
 * @author daniel
 *
 */
public class TestEclipseBug247567 {
	private static final String PREFIX = System.getProperty("test.pbprefix");

	@Test
	public void testReserveVarsButUseLess() throws ContradictionException, TimeoutException, FileNotFoundException, ParseFormatException, IOException {
		IPBSolver solver = SolverFactory.newEclipseP2();
		Reader reader = new OPBEclipseReader2007(solver);
		reader.parseInstance(PREFIX+"bug247567.opb");
		assertTrue(solver.isSatisfiable());
		assertTrue(solver.model(1));
		assertTrue(solver.model(2));
		assertTrue(solver.model(3));
		assertFalse(solver.model(4));
		assertFalse(solver.model(5));
		assertFalse(solver.model(6));
	}
}
