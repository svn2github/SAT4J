package org.sat4j.apps.sudoku;

public class RandomPermutation {
    RandomPermutation(int max) {
        IndexSet s = new IndexSet(max);
        nested = false;
        p = new int[max];

        for (int i = 1; i <= max; i++) {
            p[i - 1] = s.getIndex();
        }
    }

    RandomPermutation(int max1, int max2) {
        this(max1);
        this.max1 = max1;
        this.max2 = max2;
        rp = new RandomPermutation[max1];
        for (int i = 0; i < max1; i++) {
            rp[i] = new RandomPermutation(max2);
        }
        nested = true;
    }

    public int permute(int i) {
        int x, y;
        if (nested) {
            x = (i - 1) / max1;
            y = 1 + (i - 1) % max2;
            return x * max2 + rp[x].permute(y);
        }
        return p[i - 1];
    }

    int[] p;

    int max1, max2;

    RandomPermutation rp[];

    boolean nested;
}
