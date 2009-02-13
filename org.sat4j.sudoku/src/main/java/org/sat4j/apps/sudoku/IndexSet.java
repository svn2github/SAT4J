package org.sat4j.apps.sudoku;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Store a set of values in a particular range, and yield them in random order.
 */

public class IndexSet {
    /**
     * Store a set of values in a particular range, and yield them in random
     * order.
     * 
     * @param max
     *            The set created contains the range <code>1..max</code>.
     */
    protected IndexSet(int max) {

        for (int r = 1; r <= max; r++) {
            indices.add(Integer.valueOf(r));
        }
    }

    /**
     * Return a random remaining value and remove it from the set.
     */

    public int getIndex() {
        Integer result;
        int index;

        index = random.nextInt(indices.size());
        result = indices.get(index);
        indices.remove(index);

        return result.intValue();
    }

    private List<Integer> indices = new ArrayList<Integer>();

    private Random random = new Random();
}
