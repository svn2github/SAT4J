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

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public class Var implements Evaluable, Comparable<Var> {

    private final Domain domain;

    private final String id;

    private final int startid;

    public Var(String idvar, Domain domain, int lastvarnumber) {
        this.domain = domain;
        this.id = idvar;
        this.startid = lastvarnumber + 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.csp.Evaluable#domain()
     */
    public Domain domain() {
        return domain;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.csp.Evaluable#translate(int)
     */
    public int translate(int key) {
        return domain.pos(key) + startid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.csp.Evaluable#toClause(org.sat4j.specs.ISolver)
     */
    public void toClause(ISolver solver) throws ContradictionException {
        IVecInt clause = new VecInt(domain.size());
        for (int i = 0; i < domain.size(); i++)
            clause.push(i + startid);
        solver.addExactly(clause, 1);
    }

    public int findValue(int[] model) {
        for (int i = 0; i < domain.size(); i++) {
            int varnum = i + startid;
            if (model[varnum - 1] == varnum)
                return domain.get(i);
        }
        throw new IllegalStateException("BIG PROBLEM: no value for a var!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return id;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Var other = (Var) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(Var o) {
		return id.compareTo(o.id);
	}
    
}
