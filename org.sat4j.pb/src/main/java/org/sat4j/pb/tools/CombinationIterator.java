package org.sat4j.pb.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class CombinationIterator implements Iterable<Set<Integer>> {

    private final int combSize;
    private final Set<Integer> items;
    private final List<Integer> itemsList;

    private Set<Set<Integer>> combinations;

    CombinationIterator(int combSize, Set<Integer> items) {
        this.combSize = combSize;
        this.items = items;
        this.itemsList = new ArrayList<Integer>(items);
    }

    public CombinationIterator init() {
        this.combinations = new HashSet<Set<Integer>>();
        addCombinations(new HashSet<Integer>(), 0);
        return this;
    }

    private void addCombinations(Set<Integer> itemsIn, int fromIndex) {
        for (int i = fromIndex; i < items.size() - this.combSize
                + itemsIn.size() + 1; ++i) {
            Set<Integer> newItemsIn = new HashSet<Integer>(itemsIn);
            newItemsIn.add(this.itemsList.get(i));
            if (newItemsIn.size() == this.combSize) {
                this.combinations.add(newItemsIn);
            } else {
                addCombinations(newItemsIn, i + 1);
            }
        }
    }

    public Iterator<Set<Integer>> iterator() {
        return this.combinations.iterator();
    }
}