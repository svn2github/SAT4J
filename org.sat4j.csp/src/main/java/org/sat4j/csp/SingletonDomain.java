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

import java.util.NoSuchElementException;

import org.sat4j.specs.IteratorInt;

/**
 * Represents a domain with a single value.
 * 
 * @author leberre
 */
public class SingletonDomain implements Domain {

    private final int value;

    public SingletonDomain(int v) {
        value = v;
    }

    public int get(int i) {
        if (i != 0) {
            throw new IllegalArgumentException();
        }
        return value;
    }

    public int size() {
        return 1;
    }

    public IteratorInt iterator() {
        return new IteratorInt() {
            private int i = 0;

            public boolean hasNext() {
                return i < 1;
            }

            public int next() {
                if (i == 1)
                    throw new NoSuchElementException();
                return value;
            }
        };
    }

    public int pos(int theValue) {
        if (theValue != this.value) {
            throw new IllegalArgumentException();
        }
        return 0;
    }

}
