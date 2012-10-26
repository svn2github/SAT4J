package samples

import org.junit._
import Assert._
import org.sat4j.scala._

@Test
class AppTest {

    @Test
    def testSatisfiable() = {
       val problem = new Problem
       problem += Clause(2, -3, 4)
       problem += Clause(-2, 3, -4)
       assertEquals(Satisfiable,(problem solve))
    }

}


