package org.sat4j.apps.sudoku;

import java.awt.print.Paper;

public class A4Paper extends Paper {
    private static final int DOTS_PER_INCH = 72;

    public A4Paper() {
        super();
        setSize(8.3 * DOTS_PER_INCH, 11.8 * DOTS_PER_INCH);
        setImageableArea(0.5 * DOTS_PER_INCH, 0.5 * DOTS_PER_INCH,
                7.3 * DOTS_PER_INCH, 10.8 * DOTS_PER_INCH);
    }
}
