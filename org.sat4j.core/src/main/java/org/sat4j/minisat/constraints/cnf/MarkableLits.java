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

import java.util.HashSet;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.IMarkableLits;
import org.sat4j.specs.IVecInt;

public class MarkableLits extends Lits implements IMarkableLits {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int[] marks;

    private static final int DEFAULTMARK = 1;

    @Override
    public void init(int nvar) {
        super.init(nvar);
        marks = new int[(nvar << 1) + 2];
    }

    public void setMark(int p, int mark) {
        assert marks != null;
        assert p > 1;
        assert p < marks.length;
        marks[p] = mark;
    }

    public void setMark(int p) {
        setMark(p, DEFAULTMARK);
    }

    public int getMark(int p) {
        return marks[p];
    }

    public boolean isMarked(int p) {
        return marks[p] != MARKLESS;
    }

    public void resetMark(int p) {
        marks[p] = MARKLESS;
    }

    public void resetAllMarks() {
        for (int i = 2; i < marks.length; i++)
            resetMark(i);
    }

    public IVecInt getMarkedLiterals() {
        IVecInt marked = new VecInt();
        for (int i = 2; i < marks.length; i++) {
            if (isMarked(i))
                marked.push(i);
        }
        return marked;
    }

    public IVecInt getMarkedLiterals(int mark) {
        IVecInt marked = new VecInt();
        for (int i = 2; i < marks.length; i++) {
            if (getMark(i) == mark)
                marked.push(i);
        }
        return marked;
    }

    public IVecInt getMarkedVariables() {
        IVecInt marked = new VecInt();
        for (int i = 2; i < marks.length; i += 2) {
            if (isMarked(i) || isMarked(i + 1))
                marked.push(i >> 1);
        }
        return marked;
    }

    public IVecInt getMarkedVariables(int mark) {
        IVecInt marked = new VecInt();
        for (int i = 2; i < marks.length; i += 2) {
            if (getMark(i) == mark || getMark(i + 1) == mark)
                marked.push(i >> 1);
        }
        return marked;
    }

    public Set<Integer> getMarks() {
        Set<Integer> markers = new HashSet<Integer>();
        for (int m : marks)
            if (m != MARKLESS)
                markers.add(m);
        return markers;
    }
}
