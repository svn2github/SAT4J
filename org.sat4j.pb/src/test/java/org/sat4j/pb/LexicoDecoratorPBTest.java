package org.sat4j.pb;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.tools.LexicoDecoratorPB;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class LexicoDecoratorPBTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() throws ContradictionException, TimeoutException {
        LexicoDecoratorPB lexico = new LexicoDecoratorPB(
                SolverFactory.newDefault());
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
}
