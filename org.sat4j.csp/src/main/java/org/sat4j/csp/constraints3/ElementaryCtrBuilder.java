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
package org.sat4j.csp.constraints3;

import org.sat4j.csp.intension.ICspToSatEncoder;
import org.sat4j.csp.intension.IIntensionCtrEncoder;
import org.sat4j.reader.XMLCSP3Reader;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * A constraint builder for XCSP3 instance format.
 * Used by {@link XMLCSP3Reader}.
 * This class is dedicated to elementary (clauses, terms) constraints.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class ElementaryCtrBuilder {

	private final IIntensionCtrEncoder intensionEncoder;

	public ElementaryCtrBuilder(IIntensionCtrEncoder intensionEnc) {
		this.intensionEncoder = intensionEnc;
	}
	
	public boolean buildCtrInstantiation(String id, XVarInteger[] list, int[] values) {
		for(int i=0; i<list.length; ++i) {
			final ICspToSatEncoder solver = this.intensionEncoder.getSolver();
			final int[] unitCl = new int[]{solver.getSolverVar(list[i].id, values[i])};
			if(solver.addClause(unitCl)) return true;
		}
		return false;
	}

	public boolean buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg) {
		int nPos = pos.length;
		StringBuffer expressionBuffer = new StringBuffer();
		boolean first = true;
		expressionBuffer.append("or(");
		for(int i=0; i<nPos; ++i) {
			if(!first) {
				expressionBuffer.append(',');
			} else {
				first = false;
			}
			String var = pos[i].id;
			String normVar = CtrBuilderUtils.normalizeCspVarName(var);
			expressionBuffer.append(normVar);
		}
		for(int i=0; i<neg.length; ++i) {
			if(!first) {
				expressionBuffer.append(',');
			} else {
				first = false;
			}
			String var = neg[i].id;
			String normVar = CtrBuilderUtils.normalizeCspVarName(var);
			expressionBuffer.append("not(").append(normVar).append(')');
		}
		expressionBuffer.append(')');
		return this.intensionEncoder.encode(expressionBuffer.toString());
	}

}
