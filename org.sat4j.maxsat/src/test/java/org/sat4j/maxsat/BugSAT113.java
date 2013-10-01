package org.sat4j.maxsat;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.maxsat.reader.WDimacsReader;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.tools.OptToSatAdapter;

public class BugSAT113 {

    private WeightedMaxSatDecorator maxsat;
    private WDimacsReader reader;

    @Before
    public void init() {
        this.maxsat = new WeightedMaxSatDecorator(SolverFactory.newLight());
        this.reader = new WDimacsReader(this.maxsat);
    }
    
    @Test
    public void testIssueReportedByYakoub() {
        testProblemWithExpectedAnswer("encodingWCNF.wcnf", 400);
    }

    private void testProblemWithExpectedAnswer(String filename,
             int expectedValue) {
        try {
            IProblem problem = this.reader.parseInstance(System.getProperty("test.prefix")+filename);
            assertNotNull(problem);
            IOptimizationProblem optproblem = new PseudoOptDecorator(
                    this.maxsat);
            IProblem satproblem = new OptToSatAdapter(optproblem);
            assertTrue(satproblem.isSatisfiable());
            int[] model = satproblem.model();
            assertNotNull(model);
            assertEquals(expectedValue, optproblem.getObjectiveValue()
                    .intValue());

        } catch (Exception e) {
            fail(" Problem when reading instance : " + e);
        }
    }

}
