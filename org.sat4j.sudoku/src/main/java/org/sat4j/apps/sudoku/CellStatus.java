package org.sat4j.apps.sudoku;

/**
 * Provides integer fields to describe the possible status of each cell.
 */

public enum CellStatus {
    /**
     * The cell is protected, so it is not possible to type a value into it.
     */
    PROTECTED,

    /**
     * The cell's value was entered by the user.
     */
    USER_ENTERED,

    /**
     * The cell's value was entered by the automatic solver.
     */
    SOLVER_ENTERED

};
