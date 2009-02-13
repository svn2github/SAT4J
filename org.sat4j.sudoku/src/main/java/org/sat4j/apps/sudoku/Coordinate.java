package org.sat4j.apps.sudoku;

public class Coordinate {
    int r, c;

    public Coordinate(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public int getRow() {
        return r;
    }

    public int getColumn() {
        return c;
    }
}
