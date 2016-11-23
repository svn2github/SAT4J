/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2016 Daniel Le Berre
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
package org.sat4j.reader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sat4j.AbstractLauncher;
import org.sat4j.specs.ISolver;

/**
 * An enumeration whose values correspond to handle input formats.
 * Given an instance, it aims at determining the format, build a reader, and determine whether or not optimization mode must be set.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public enum ECSPFormat {
	
	/**
	 * Text format.
	 */
	TXT(false) {
		/**
		 * @see ECSPFormat#getReader(ISolver)
		 */
		public Reader getReader(AbstractLauncher launcher, ISolver solver) {
			boolean allDiffCards = System.getProperty("allDiffCards") != null;
			return new CSPExtSupportReader(solver, allDiffCards);
		}
		
		/**
		 * @see ECSPFormat#decoratePrintWriter(PrintWriter)
		 */
		public PrintWriter decoratePrintWriter(boolean shouldOnlyDisplayEncoding, PrintWriter pw) {
			return pw;
		}
	},
	
	/**
	 * XCSP2 format.
	 */
	XCSP2(false) {
		/**
		 * @see ECSPFormat#getReader(ISolver)
		 */
		public Reader getReader(AbstractLauncher launcher, ISolver solver) {
			boolean allDiffCards = System.getProperty("allDiffCards") != null;
			return new XMLCSPReader(solver, allDiffCards);
		}
		
		/**
		 * @see ECSPFormat#decoratePrintWriter(PrintWriter)
		 */
		public PrintWriter decoratePrintWriter(boolean shouldOnlyDisplayEncoding, PrintWriter pw) {
			return pw;
		}
	},
	
	/**
	 * XCSP3 format.
	 */
	XCSP3(true) {
		/**
		 * @see ECSPFormat#getReader(ISolver)
		 */
		public Reader getReader(AbstractLauncher launcher, ISolver solver) {
			return new XMLCSP3Reader(solver, launcher);
		}
		
		/**
		 * @see ECSPFormat#decoratePrintWriter(PrintWriter)
		 */
		public PrintWriter decoratePrintWriter(boolean shouldOnlyDisplayEncoding, PrintWriter pw) {
			if(shouldOnlyDisplayEncoding) {
				return pw;
			}
			XMLCommentPrintWriter commentPrintWriter = new XMLCommentPrintWriter(pw);
			commentPrintWriter.addDncPrefix("v ");
			return commentPrintWriter;
		}
	},
	
	/**
	 * Unknown format, or error occurred while attempting to determine the instance type. 
	 */
	UNKNOWN(false) {
		/**
		 * @see ECSPFormat#getReader(ISolver)
		 */
		public Reader getReader(AbstractLauncher launcher, ISolver solver) {
			throw new IllegalArgumentException("unable to determine instance type");
		}
		
		/**
		 * @see ECSPFormat#decoratePrintWriter(PrintWriter)
		 */
		public PrintWriter decoratePrintWriter(boolean shouldOnlyDisplayEncoding, PrintWriter pw) {
			return pw;
		}
	};
	
	/** flag set iff optimization mode must be set */
	private final boolean optimizationModeRequired;
	
	private ECSPFormat(boolean optimizationModeRequired) {
		this.optimizationModeRequired = optimizationModeRequired;
	}
	
	/**
	 * Tells whether optimization mode must be set.
	 * 
	 * @return <code>true</code> iff optimization mode must be set
	 */
	public boolean isOptimizationModeRequired() {
		return this.optimizationModeRequired;
	}
	
	/**
	 * Returns a reader corresponding to the instance format.
	 * 
	 * @param launcher the CSP launcher
	 * @param solver the solver used by the reader
	 * @return a reader corresponding to the problem
	 */
	public Reader getReader(AbstractLauncher launcher, ISolver solver) {
		throw new IllegalStateException("This code should never be called");
	}
	
	/**
	 * Decorates a {@link PrintWriter} dedicated to solver output in order to provide the correct output given the CSP format.
	 * @param shouldOnlyDisplayEncoding 
	 * 
	 * @param pw the default writer
	 * @return the decorator
	 */
	public PrintWriter decoratePrintWriter(boolean shouldOnlyDisplayEncoding, PrintWriter pw) {
		throw new IllegalStateException("This code should never be called");
	}
	
	/**
	 * Tries to determine the instance format
	 * 
	 * @param filename the name of the instance file
	 * @return the instance format
	 */
	public static ECSPFormat inferInstanceType(String filename) {
		if(filename.endsWith(".txt")) {
			return TXT;
		}
		ECSPFormat xmlType = tryToInferXmlType(filename);
		if(xmlType != UNKNOWN) {
			return xmlType;
		}
		return UNKNOWN;
	}
	
	private static ECSPFormat tryToInferXmlType(String filename) {
		try(InputStream is = Reader.getInputStreamFromFilename(filename)) {
			return tryToInferXmlType(is);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private static ECSPFormat tryToInferXmlType(InputStream is) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("<instance")) {
					return tryToInferXmlTypeFromRootMarkup(line);
				}
			}
		} catch (FileNotFoundException e) {
			return UNKNOWN;
		} catch (IOException e) {
			return UNKNOWN;
		}
		return UNKNOWN;
	}

	private static ECSPFormat tryToInferXmlTypeFromRootMarkup(String line) {
		Pattern pattern = Pattern.compile("<instance .*format=\"([^\"]*)\".*>");
		Matcher matcher = pattern.matcher(line);
		if(!matcher.matches()) {
			return XCSP2;
		}
		String format = matcher.group(1);
		switch (format) {
		case "XCSP2" :
			return XCSP2;
		case "XCSP3" :
			return XCSP3;
		default:
			return UNKNOWN;
		}
	}

}
