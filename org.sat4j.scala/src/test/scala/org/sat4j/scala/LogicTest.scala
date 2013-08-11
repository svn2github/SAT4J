package org.sat4j.scala

import org.specs2.ScalaCheck
import org.specs2.matcher.{Expectable, Matcher}

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner


@RunWith(classOf[JUnitRunner])
class LogicTest extends Specification with ScalaCheck {

  import Logic._

  type Model = Map[_, Boolean]
  type SatResult = (Boolean, Option[Model])

  object beSat extends Matcher[SatResult] {
    def apply[S <: SatResult](a: Expectable[S]) = {
      val sat = a.value._1
      result(sat,
        "Formula is SAT",
        "Formula is UNSAT",
        a)
    }
  }

  object beUnsat extends Matcher[SatResult] {
    def apply[S <: SatResult](a: Expectable[S]) = {
      val unsat = !a.value._1
      result(unsat,
        "Formula is UNSAT",
        "Formula is SAT",
        a)
    }
  }


  sequential // API not thread-safe yet
  "from testLogic" should {
    "'x & 'y | ('z implies 'd)" in {
      isSat('x & 'y | ('z implies 'd)) must beSat
    }

    "'x & ~'x" in {
      isSat('x & ~'x) must beUnsat
    }

    "'a | 'b" in {
      isSat('a | 'b) must beSat
      isValid('a | 'b) must beUnsat
      isValid('a | ~'a) must beSat
    }


    "'a & 'b" in {
      val l = 'a & 'b
      isSat(l & ~l) must beUnsat

      isValid(l) must beUnsat
    }

    "'a implies 'a" in {
      isSat('a implies 'a) must beSat
    }
  }

}
