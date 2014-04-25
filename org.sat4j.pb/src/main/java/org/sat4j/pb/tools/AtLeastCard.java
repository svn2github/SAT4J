package org.sat4j.pb.tools;

import java.util.BitSet;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

public class AtLeastCard {

    private final IVecInt lits;
    private final int degree;

    public AtLeastCard(IVecInt atLeastLits, int degree) {
        this.lits = atLeastLits;
        this.degree = degree;
    }

    public AtLeastCard(BitSet atLeastLits, int degree, int offset) {
        this.lits = new VecInt(atLeastLits.cardinality());
        int from = 0;
        int cur;
        while ((cur = atLeastLits.nextSetBit(from)) != -1) {
            this.lits.push(cur + offset);
            from = cur + 1;
        }
        this.degree = degree;
    }

    public IVecInt getLits() {
        return lits;
    }

    public int getDegree() {
        return degree;
    }

    public AtMostCard toAtMost() {
        IVecInt atMostLits = new VecInt(this.lits.size());
        for (IteratorInt it = this.lits.iterator(); it.hasNext();)
            atMostLits.push(-it.next());
        int atMostDegree = this.lits.size() - this.degree;
        return new AtMostCard(atMostLits, atMostDegree);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (IteratorInt it = this.lits.iterator(); it.hasNext();) {
            sb.append(it.next());
            sb.append(" + ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(">= ");
        sb.append(this.degree);
        return sb.toString();
    }

}
