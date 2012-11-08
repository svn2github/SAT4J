package org.sat4j.scala

import org.sat4j.specs.ISolver
import org.sat4j.minisat.SolverFactory
import org.sat4j.core.VecInt
import org.sat4j.specs.IVecInt
import org.sat4j.specs.IProblem

abstract class Status {}
case object Satisfiable extends Status {}
case object Unsatisfiable extends Status {}
case object Unknown extends Status {}

class Problem {

  val problem = SolverFactory.newDefault

  def +=(clause: IVecInt) = {
    problem addClause clause
    clause.clear 
    this
  }

  def solve: Status = {
    try {
      if (problem.isSatisfiable) 
         Satisfiable
       else 
         Unsatisfiable
    } catch {
      case _ =>  Unknown
    }
  }

  def model = {
    problem.model
  }
}

object Clause {
  def apply(args: Int*) = {
    val clause = new VecInt()
    args foreach { arg => clause.push(arg) }
    clause
  }
  
  def apply(l: List[Int]) = {
    val clause = new VecInt()
    l foreach { arg => clause.push(arg) }
    clause
  }
}
