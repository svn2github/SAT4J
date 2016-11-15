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

import java.io.IOException;
import java.util.Map;

import org.sat4j.AbstractLauncher;
import org.sat4j.ILauncherMode;
import org.sat4j.reader.ECSPFormat;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

public class CSPLauncher extends AbstractLauncher {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;
	
	public CSPLauncher() {
		bufferizeLog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.Lanceur#configureSolver(java.lang.String[])
	 */
	@Override
	protected ISolver configureSolver(String[] args) {
		ISolver asolver;
		if (args.length == 2) {
			asolver = SolverFactory.instance().createSolverByName(args[0]);
		} else {
			asolver = SolverFactory.newDefault();
		}
		log(asolver.toString(COMMENT_PREFIX));
		return asolver;
	}

	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sat4j.Lanceur#createReader(org.sat4j.specs.ISolver)
	 */
	@Override
	protected Reader createReader(final ISolver aSolver,
			final String problemname) {
		ECSPFormat cspFormat = ECSPFormat.inferInstanceType(problemname);
		this.out = cspFormat.decoratePrintWriter(this.out);
		flushLog();
		Reader aReader = cspFormat.getReader(this, aSolver);
		setLauncherMode(cspFormat.isOptimizationModeRequired() ? ILauncherMode.OPTIMIZATION : ILauncherMode.DECISION);
		if (System.getProperty("verbose") != null) {
			log("verbose mode on");
			aReader.setVerbosity(true);
			aSolver.setVerbose(true);
		} else {
			aSolver.setVerbose(false);
		}
		return aReader;
	}

	@Override
	protected IProblem readProblem(String problemname)
			throws ParseFormatException, IOException, ContradictionException {
		this.silent = true;
		IProblem problem = super.readProblem(problemname);
		if(this.reader.hasAMapping()) {
			this.out.write("c CSP to SAT var mapping:");
			Map<Integer, String> mapping = this.reader.getMapping();
			String lastVar="";
			for(Map.Entry<Integer, String> entry : mapping.entrySet()) {
				final String curVarAssignment = entry.getValue();
				final String curVar = curVarAssignment.substring(0, curVarAssignment.indexOf("="));
				if(!curVar.equals(lastVar)) {
					this.out.write("\nc ");
				}
				this.out.write(curVarAssignment+":"+entry.getKey()+" ");
				lastVar = curVar;
			}
			this.out.write("\n");
		}
		return problem;
	}

	public static void main(String[] args) {
		AbstractLauncher lanceur = new CSPLauncher();
		if (args.length > 2 || args.length == 0) {
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
		if (args.length == 1)
			return args[0];
		return args[1];
	}
}
