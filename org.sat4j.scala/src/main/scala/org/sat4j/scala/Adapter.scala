package org.sat4j.scala

import org.sat4j.scala.Satisfiable;
import org.sat4j.scala.Status;
import org.sat4j.scala.Unknown;
import org.sat4j.scala.Unsatisfiable;
import org.sat4j.specs.ISolver
import org.sat4j.minisat.SolverFactory
import org.sat4j.core.VecInt
import org.sat4j.specs.IVecInt
import org.sat4j.specs.IProblem

abstract class Status {}
case object Satisfiable extends Status {}
case object Unsatisfiable extends Status {}
case object Unknown extends Status {}

object Problem {

  val problem = SolverFactory.newDefault();

  def +=(clause: IVecInt): IProblem = {
    problem addClause clause
    clause.clear
    problem
  }

  def solve(): Status = {
    try {
      if (problem.isSatisfiable()) {
        return Satisfiable
      } else {
        return Unsatisfiable
      }
    } catch {
      case _ => return Unknown
    }
  }

  def model() = {
    problem.model();
  }
}

object Clause {

  def apply(args: Int*) = {
    val clause = new VecInt()
    for (arg <- args)
      clause.push(arg)
    clause
  }
}
