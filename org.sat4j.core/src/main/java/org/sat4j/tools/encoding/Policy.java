/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/

package org.sat4j.tools.encoding;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * This class allows the use of different encodings for different cardinality
 * constraints.
 * 
 * @author stephanieroussel
 * @since 2.3.1
 */
public class Policy extends EncodingStrategyAdapter {

    private final Sequential seq = new Sequential();
    private final Binary binary = new Binary();
    private final Product product = new Product();
    private final Commander commander = new Commander();
    private final Binomial binomial = new Binomial();
    private final Ladder ladder = new Ladder();

    private EncodingStrategyAdapter atMostOneEncoding;
    private EncodingStrategyAdapter atMostKEncoding;
    private EncodingStrategyAdapter exactlyOneEncoding;
    private EncodingStrategyAdapter exactlyKEncoding;
    private EncodingStrategyAdapter atLeastOneEncoding;
    private EncodingStrategyAdapter atLeastKEncoding;

    private EncodingStrategyAdapter getAdapterFromEncodingName(
            EncodingStrategy encodingName) {
        switch (encodingName) {
        case BINARY:
            return this.binary;
        case BINOMIAL:
            return this.binomial;
        case COMMANDER:
            return this.commander;
        case LADDER:
            return this.ladder;
        case PRODUCT:
            return this.product;
        case SEQUENTIAL:
            return this.seq;
        default:
            return null;
        }
    }

    public EncodingStrategyAdapter getAtMostOneEncoding() {
        return this.atMostOneEncoding;
    }

    public void setAtMostOneEncoding(EncodingStrategyAdapter atMostOneEncoding) {
        this.atMostOneEncoding = atMostOneEncoding;
    }

    public void setAtMostOneEncoding(EncodingStrategy atMostOneEncoding) {
        this.atMostOneEncoding = getAdapterFromEncodingName(atMostOneEncoding);
    }

    public EncodingStrategyAdapter getAtMostKEncoding() {
        return this.atMostKEncoding;
    }

    public void setAtMostKEncoding(EncodingStrategyAdapter atMostKEncoding) {
        this.atMostKEncoding = atMostKEncoding;
    }

    public void setAtMostKEncoding(EncodingStrategy atMostKEncoding) {
        this.atMostKEncoding = getAdapterFromEncodingName(atMostKEncoding);
    }

    public EncodingStrategyAdapter getExactlyOneEncoding() {
        return this.exactlyOneEncoding;
    }

    public void setExactlyOneEncoding(EncodingStrategyAdapter exactlyOneEncoding) {
        this.exactlyOneEncoding = exactlyOneEncoding;
    }

    public void setExactlyOneEncoding(EncodingStrategy exactlyOneEncoding) {
        this.exactlyOneEncoding = getAdapterFromEncodingName(exactlyOneEncoding);
    }

    public EncodingStrategyAdapter getExactlyKEncoding() {
        return this.exactlyKEncoding;
    }

    public void setExactlyKEncoding(EncodingStrategyAdapter exactlyKEncoding) {
        this.exactlyKEncoding = exactlyKEncoding;
    }

    public void setExactlyKEncoding(EncodingStrategy exactlyKEncoding) {
        this.exactlyKEncoding = getAdapterFromEncodingName(exactlyKEncoding);
    }

    public EncodingStrategyAdapter getAtLeastOneEncoding() {
        return this.atLeastOneEncoding;
    }

    public void setAtLeastOneEncoding(EncodingStrategyAdapter atLeastOneEncoding) {
        this.atLeastOneEncoding = atLeastOneEncoding;
    }

    public void setAtLeastOneEncoding(EncodingStrategy atLeastOneEncoding) {
        this.atLeastOneEncoding = getAdapterFromEncodingName(atLeastOneEncoding);
    }

    public EncodingStrategyAdapter getAtLeastKEncoding() {
        return this.atLeastKEncoding;
    }

    public void setAtLeastKEncoding(EncodingStrategyAdapter atLeastKEncoding) {
        this.atLeastKEncoding = atLeastKEncoding;
    }

    public void setAtLeastKEncoding(EncodingStrategy atLeastKEncoding) {
        this.atLeastKEncoding = getAdapterFromEncodingName(atLeastKEncoding);
    }

    @Override
    public IConstr addAtMost(ISolver solver, IVecInt literals, int k)
            throws ContradictionException {

        if (k == 0 || literals.size() == 1) {
            // will propagate unit literals
            return super.addAtMost(solver, literals, k);
        }
        if (literals.size() <= 1) {
            throw new UnsupportedOperationException(
                    "requires at least 2 literals");
        }
        if (k == 1 && this.atMostOneEncoding != null) {
            return this.atMostOneEncoding.addAtMostOne(solver, literals);
        }
        if (this.atMostKEncoding != null) {
            if (k == 1) {
                return this.atMostKEncoding.addAtMostOne(solver, literals);
            } else {
                return this.atMostKEncoding.addAtMost(solver, literals, k);
            }
        }
        return super.addAtMost(solver, literals, k);
    }

    @Override
    public IConstr addExactly(ISolver solver, IVecInt literals, int n)
            throws ContradictionException {
        if (n == 1 && this.exactlyOneEncoding != null) {
            return this.exactlyOneEncoding.addExactlyOne(solver, literals);
        } else if (this.exactlyKEncoding != null) {
            if (n == 1) {
                return this.exactlyKEncoding.addExactlyOne(solver, literals);
            } else {
                return this.exactlyKEncoding.addExactly(solver, literals, n);
            }
        }

        return super.addExactly(solver, literals, n);
    }

    @Override
    public IConstr addAtLeast(ISolver solver, IVecInt literals, int n)
            throws ContradictionException {
        if (n == 1) {
            if (this.atLeastOneEncoding != null) {
                return this.atLeastOneEncoding.addAtLeastOne(solver, literals);
            }
        } else if (this.atLeastKEncoding != null) {
            return this.atLeastKEncoding.addAtLeast(solver, literals, n);
        }

        return super.addAtLeast(solver, literals, n);

    }

}
