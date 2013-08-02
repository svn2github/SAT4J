package org.sat4j.minisat.core;

/**
 * Callback method called when a mandatory literal is found in a constraint.
 * 
 * @author leberre
 * 
 */
public interface MandatoryLiteralListener {

    /**
     * 
     * @param p
     *            a literal in internal representation.
     */
    void isMandatory(int p);
}
