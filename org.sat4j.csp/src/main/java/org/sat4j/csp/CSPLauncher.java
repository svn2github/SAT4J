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
 *******************************************************************************/
package org.sat4j.csp;

import org.sat4j.AbstractLauncher;
import org.sat4j.reader.CSPExtSupportReader;
import org.sat4j.reader.Reader;
import org.sat4j.reader.XMLCSPReader;
import org.sat4j.specs.ISolver;

public class CSPLauncher extends AbstractLauncher {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.Lanceur#configureSolver(java.lang.String[])
	 */
	@Override
	protected ISolver configureSolver(String[] args) {
		ISolver asolver = SolverFactory.newDefault();
		log(asolver.toString(COMMENT_PREFIX));
		return asolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.Lanceur#createReader(org.sat4j.specs.ISolver)
	 */
	@Override
	protected Reader createReader(final ISolver aSolver, final String problemname) {
		Reader aReader;
		if (problemname.endsWith(".txt")) {
			aReader = new CSPExtSupportReader(aSolver);
		} else {
			assert problemname.endsWith(".xml");
			aReader = new XMLCSPReader(aSolver);
		}
		if (System.getProperty("verbose") != null) {
			log("verbose mode on");
			aReader.setVerbosity(true);
		}
		return aReader;
	}

	public static void main(String[] args) {
		AbstractLauncher lanceur = new CSPLauncher();
		if (args.length != 1) {
			lanceur.usage();
			return;
		}
		try {
			lanceur.run(args);
		} catch (IllegalArgumentException e) {
			lanceur.log(">>>> " + e.getMessage() + " <<<<");
		}
	}

	@Override
	public void displayLicense() {
		super.displayLicense();
		log("That software uses the Rhino library from the Mozilla project.");
	}

	@Override
	public void usage() {
		System.out.println("Please provide a CSP instance file!"); //$NON-NLS-1$
	}

	@Override
	protected String getInstanceName(String[] args) {
		assert args.length == 1;
		return args[0];
	}

}
