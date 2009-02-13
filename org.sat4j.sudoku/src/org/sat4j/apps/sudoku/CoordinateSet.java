package org.sat4j.apps.sudoku;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CoordinateSet {
    CoordinateSet(int rows, int columns) {
        coordinates = new ArrayList<Coordinate>();
        random = new Random();

        this.rows = rows;
        this.columns = columns;

        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= columns; c++) {
                coordinates.add(new Coordinate(r, c));
            }
        }
    }

    public Coordinate[] getGroup() {
        Coordinate centre = null;

        if (rows % 2 == 1) {
            centre = new Coordinate((rows + 1) / 2, (columns + 1) / 2);
            remove(centre);
        }
        Coordinate[] result = new Coordinate[4];

        result[0] = getCoordinate();
        if (result[0].getRow() * 2 == rows + 1) {
            result[1] = new Coordinate(result[0].getRow(), columns + 1
                    - result[0].getColumn());
            result[2] = new Coordinate(result[0].getColumn(), result[0]
                    .getRow());
            result[3] = new Coordinate(columns + 1 - result[0].getColumn(),
                    result[0].getRow());
        } else if (result[0].getColumn() * 2 == columns + 1) {
            result[1] = new Coordinate(result[0].getColumn(), result[0]
                    .getRow());
            result[2] = new Coordinate(rows + 1 - result[0].getRow(), result[0]
                    .getColumn());
            result[3] = new Coordinate(result[0].getColumn(), rows + 1
                    - result[0].getRow());
        } else {
            result[1] = new Coordinate(result[0].getRow(), columns + 1
                    - result[0].getColumn());
            result[2] = new Coordinate(rows + 1 - result[0].getRow(), result[0]
                    .getColumn());
            result[3] = new Coordinate(rows + 1 - result[0].getRow(), columns
                    + 1 - result[0].getColumn());
        }

        for (int i = 1; i <= 3; i++) {
            remove(result[i]);
        }

        if (rows % 2 == 1) {
            coordinates.add(centre);
        }

        return result;
    }

    void remove(Coordinate coord) {
        int row = coord.getRow();
        int column = coord.getColumn();
        boolean finished;
        finished = false;

        for (int index = 0; !finished && (index < coordinates.size()); index++) {
            Coordinate c = coordinates.get(index);
            if ((c.getRow() == row) && (c.getColumn() == column)) {
                coordinates.remove(index);
                finished = true;
            }
        }
    }

    public Coordinate[] getRandomArray() {
        Coordinate[] result = new Coordinate[coordinates.size()];
        Coordinate[] group;
        int pos = 0;

        while (coordinates.size() > 0) {

            if (coordinates.size() >= 400) {
                group = getGroup();
                for (int i = 0; i < group.length; i++) {
                    result[pos++] = group[i];
                }
            } else {
                result[pos++] = getCoordinate();
            }

        }

        return result;
    }

    public Coordinate[] getSpreadArray() {
        RandomPermutation rpr, rpc;
        rpr = new RandomPermutation(rows);
        rpc = new RandomPermutation(columns);
        Coordinate[] result = new Coordinate[rows];

        for (int i = 1; i <= rows; i++) {
            result[i - 1] = new Coordinate(rpr.permute(i), rpc.permute(i));
        }

        return result;
    }

    public Coordinate getCoordinate() {
        Coordinate result;
        int index;

        index = random.nextInt(coordinates.size());
        result = coordinates.get(index);
        coordinates.remove(index);

        return result;
    }

    List<Coordinate> coordinates;

    Random random;

    int rows, columns;
}
