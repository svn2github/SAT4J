package org.sat4j.pb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.pb.tools.LexicoDecoratorPB;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class LexicoDecoratorPBTest {

    private LexicoDecoratorPB lexico;

    @Before
    public void setUp() throws Exception {
        lexico = new LexicoDecoratorPB(SolverFactory.newDefault());
    }

    @Test
    public void test() throws ContradictionException, TimeoutException {

        IVecInt clause = new VecInt();
        clause.push(-1).push(-2);
        lexico.addClause(clause);
        clause.clear();
        clause.push(1);
        lexico.addClause(clause);
        clause.clear();
        clause.push(2);
        lexico.addClause(clause);
        clause.clear();
        clause.push(-1).push(-2);
        lexico.addCriterion(clause);
        IVecInt clause2 = new VecInt();
        clause2.push(1).push(2);
        lexico.addCriterion(clause2);
        assertFalse(lexico.admitABetterSolution());
    }

    @Test
    public void testSimple() throws ContradictionException, TimeoutException {
        IVecInt clause = new VecInt();
        clause.push(1).push(2).push(3);
        lexico.addClause(clause);
        clause.clear();
        clause.push(2).push(4).push(5);
        lexico.addClause(clause);
        clause.clear();
        clause.push(6).push(7).push(8);
        lexico.addClause(clause);
        clause.clear();
        clause.push(1).push(2).push(3).push(4).push(5).push(6).push(7).push(8);
        lexico.addCriterion(clause);

        try {
            if (lexico.admitABetterSolution()) {
                int[] expectedModel = new int[] { -1, 2, -3, -4, -5, 6, -7, -8 };
                int[] actualModel = lexico.model();
                for (int i = 0; i < expectedModel.length; ++i) {
                    assertEquals(expectedModel[i], actualModel[i]);
                }
                System.out.println(Arrays.toString(actualModel));
                System.out.println(lexico.getObjectiveValue());

            }
        } catch (TimeoutException e) {
            fail(e.getMessage());
        }
    }
}
