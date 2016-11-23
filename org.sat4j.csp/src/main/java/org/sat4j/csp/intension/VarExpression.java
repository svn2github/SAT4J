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
package org.sat4j.csp.intension;

import java.util.HashSet;
import java.util.Set;

/**
 * An {@link IExpression} equal to a CSP variable.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class VarExpression implements IExpression {
	
	private final String var;
	
	private final Set<String> involvedVars = new HashSet<>();
	
	public VarExpression(final String value) {
		this.var = value;
		this.involvedVars.add(value);
	}
	
	public String getVar() {
		return this.var;
	}

	@Override
	public String toString() {
		return this.var;
	}
	
	@Override
	public int compareTo(final IExpression o) {
		return this.toString().compareTo(o.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((var == null) ? 0 : var.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VarExpression))
			return false;
		VarExpression other = (VarExpression) obj;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}

	@Override
	public Set<String> getInvolvedVars() {
		return this.involvedVars;
	}
	
	@Override
	public IExpression[] getOperands() {
		return null;
	}

	@Override
	public String typeAsString() {
		return "var";
	}

}
