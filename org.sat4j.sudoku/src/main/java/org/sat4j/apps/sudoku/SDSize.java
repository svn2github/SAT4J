package org.sat4j.apps.sudoku;

class SDSize {
    private int largeSide, smallCols, smallRows;

    SDSize() {
        setSide(3);
    }

    public final void setSide(final int side) {
        setSide(side, side);
        largeSide = side * side;
    }

    public final void setSide(final int rows, final int cols) {
        smallRows = rows;
        smallCols = cols;
        largeSide = rows * cols;
    }

    public int getSmallRows() {
        return smallRows;
    }

    public int getSmallCols() {
        return smallCols;
    }

    public int getLargeSide() {
        return largeSide;
    }

    public int getBase() {
        if (largeSide < 10) {
            return 10;
        }
        return largeSide + 1;
    }

    public int getBase2() {
        return getBase() * getBase();
    }
}
