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
* Based on the original MiniSat specification from:
* 
* An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
* Sixth International Conference on Theory and Applications of Satisfiability
* Testing, LNCS 2919, pp 502-518, 2003.
*
* See www.minisat.se for the original solver in C++.
* 
*******************************************************************************/
package org.sat4j.minisat.constraints;

import org.sat4j.minisat.constraints.cnf.LearntWLClause;
import org.sat4j.minisat.constraints.cnf.Lits23;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits23;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MixedDataStructureWithBinaryAndTernary extends
        AbstractDataStructureFactory<ILits23> {

    private static final long serialVersionUID = 1L;


    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createClause(org.sat4j.datatype.VecInt)
     */
     public Constr createClause(IVecInt literals) throws ContradictionException {
        IVecInt v = WLClause.sanityCheck(literals, lits, solver);
        if (v == null)
            return null;
        if (v.size() == 2) {
            lits.binaryClauses(v.get(0), v.get(1));
            return null;
        }
        if (v.size() == 3) {
            lits.ternaryClauses(v.get(0), v.get(1), v.get(2));
            return null;
        }
        return WLClause.brandNewClause(solver, lits, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#learnContraint(org.sat4j.minisat.Constr)
     */
    @Override
    public void learnConstraint(Constr constr) {
        if (constr.size() == 2) {
            lits.binaryClauses(constr.get(0), constr.get(1));
            // solver.getStats().learnedbinaryclauses++;
        } else if (constr.size() == 3) {
            lits.ternaryClauses(constr.get(0), constr.get(1), constr.get(2));
            // solver.getStats().learnedternaryclauses++;
        } else {
            super.learnConstraint(constr);
        }
    }

    @Override
    protected ILits23 createLits() {
        return new Lits23();
    }

    public Constr createUnregisteredClause(IVecInt literals) {
        return new LearntWLClause(literals, getVocabulary());
    }

}
