package org.sat4j.apps.sudoku;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public class SortedCoordinateSet {
    SortedCoordinateSet(int rows, int columns, int smallRows, int smallCols,
            int v[][]) {
        coordinates = new Vector<Coordinate>();

        this.rows = rows;
        this.columns = columns;
        this.smallRows = smallRows;
        this.smallCols = smallCols;

        score = new int[rows + 1][columns + 1];

        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= columns; c++) {
                coordinates.add(new Coordinate(r, c));
                score[rows][columns] = 0;
            }
        }

        values = v;

        Collections.sort(coordinates, new CompareCoords());
    }

    public void note(Coordinate c) {
        for (int c1 = 1; c1 <= columns; c1++) {
            score[c.getRow()][c1]++;
        }
        for (int r1 = 1; r1 <= columns; r1++) {
            score[r1][c.getColumn()]++;
        }

        // System.out.println ("\n\n" + c.getRow() + " " + c.getColumn());
        int a = (c.getRow() - 1) / smallRows + 1;
        a = (a - 1) * smallRows + 1;
        int b = (c.getColumn() - 1) / smallCols + 1;
        b = (b - 1) * smallCols + 1;
        for (int r1 = a; r1 < a + smallRows; r1++) {
            for (int c1 = b; c1 < b + smallCols; c1++) {
                // System.out.println (r1 + " " + c1);
                score[r1][c1]++;
            }
        }

        int val = values[c.getRow()][c.getColumn()];
        for (int r1 = 1; r1 <= rows; r1++) {
            for (int c1 = 1; c1 <= columns; c1++) {
                if (values[r1][c1] == val) {
                    score[r1][c1]++;
                }
            }
        }

        Collections.sort(coordinates, new CompareCoords());

    }

    public Coordinate[] getAll() {
        Coordinate[] result = new Coordinate[rows * columns];

        for (int i = 0; i < rows * columns; i++) {
            result[i] = coordinates.get(i);
        }

        return result;
    }

    class CompareCoords implements Comparator<Coordinate> {
        public int compare(Coordinate c1, Coordinate c2) {
            if (score[c1.getRow()][c1.getColumn()] < score[c2.getRow()][c2
                    .getColumn()]) {
                return -1;
            } else if (score[c1.getRow()][c1.getColumn()] == score[c2.getRow()][c2
                    .getColumn()]) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    private List<Coordinate> coordinates;

    private int rows, columns, smallRows, smallCols;

    private int score[][];

    private int values[][];
}
