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
package org.sat4j.minisat.constraints.cnf;

import java.io.Serializable;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TernaryClauses implements Constr, Serializable {

    private static final long serialVersionUID = 1L;

    private final IVecInt stubs = new VecInt();

    private final ILits voc;

    private final int phead;

    public TernaryClauses(ILits voc, int p) {
        this.voc = voc;
        this.phead = p;
    }

    public void addTernaryClause(int a, int b) {
        stubs.push(a);
        stubs.push(b);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#remove()
     */
    public void remove() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#propagate(org.sat4j.minisat.UnitPropagationListener,
     *      int)
     */
    public boolean propagate(UnitPropagationListener s, int p) {
        assert voc.isSatisfied(p);
        assert voc.isFalsified(phead);
        voc.watch(p, this);
        for (int i = 0; i < stubs.size(); i += 2) {
            int a = stubs.get(i);
            int b = stubs.get(i + 1);
            if (voc.isSatisfied(a) || voc.isSatisfied(b)) {
                continue;
            }
            if (voc.isFalsified(a) && !s.enqueue(b, this)) {
                return false;
            } else if (voc.isFalsified(b) && !s.enqueue(a, this)) {
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#simplify()
     */
    public boolean simplify() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#undo(int)
     */
    public void undo(int p) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#calcReason(int, org.sat4j.datatype.VecInt)
     */
    public void calcReason(int p, IVecInt outReason) {
        assert voc.isFalsified(this.phead);
        if (p == ILits.UNDEFINED) {
            int i = 0;
            while (!voc.isFalsified(stubs.get(i))
                    || !voc.isFalsified(stubs.get(i + 1))) {
                i += 2;
            }
            outReason.push(this.phead ^ 1);
            outReason.push(stubs.get(i) ^ 1);
            outReason.push(stubs.get(i + 1) ^ 1);
        } else {
            outReason.push(this.phead ^ 1);
            int i = 0;
            while ((stubs.get(i) != p) || (!voc.isFalsified(stubs.get(i ^ 1)))) {
                i++;
            }
            assert !voc.isFalsified(stubs.get(i));
            outReason.push(stubs.get(i ^ 1) ^ 1);
            assert voc.isFalsified(stubs.get(i ^ 1));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#learnt()
     */
    public boolean learnt() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#incActivity(double)
     */
    public void incActivity(double claInc) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#getActivity()
     */
    public double getActivity() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#locked()
     */
    public boolean locked() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#setLearnt()
     */
    public void setLearnt() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#register()
     */
    public void register() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#rescaleBy(double)
     */
    public void rescaleBy(double d) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#size()
     */
    public int size() {
        return stubs.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#get(int)
     */
    public int get(int i) {
        throw new UnsupportedOperationException();
    }

    public void assertConstraint(UnitPropagationListener s) {
        throw new UnsupportedOperationException();
    }
}
