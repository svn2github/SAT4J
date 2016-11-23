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
package org.sat4j.csp;

import org.sat4j.pb.IPBSolver;

/**
 * An extension of the {@link IPBSolver} interface allowing to know if a solver used for CSP should display the encoding used.
 * This is particularly useful since the encoding must be displayed for DIMACS output solvers but might not be for "real" solvers, as it may be huge.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public interface ICspPBSatSolver extends IPBSolver {
	
	/**
	 * Tells whether the CSP to SAT encoding should be displayed.
	 * 
	 * @return true iff the encoding must be displayed
	 */
	boolean shouldOnlyDisplayEncoding();
	
	/**
	 * Sets the flag indicating whether the CSP to SAT encoding should be displayed.
	 * 
	 * @param b the flag value (true for display)
	 */
	void setShouldOnlyDisplayEncoding(boolean b);

}
