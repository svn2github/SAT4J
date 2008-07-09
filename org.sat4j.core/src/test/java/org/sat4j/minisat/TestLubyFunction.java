package org.sat4j.minisat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sat4j.minisat.restarts.LubyRestarts;

public class TestLubyFunction {

    @Test
    public void testSomeKnownValues() {
        int[] knowvalues = { 1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8 };
        for (int i = 0; i < knowvalues.length; i++) {
            assertEquals("Wrong value for "+i,knowvalues[i], LubyRestarts.luby(i + 1));
        }
    }


}
