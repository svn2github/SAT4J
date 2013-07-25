package org.sat4j.scala

import org.sat4j.specs.ISolver
import org.sat4j.minisat.SolverFactory
import org.sat4j.core.VecInt
import org.sat4j.specs.IVecInt
import org.sat4j.specs.IProblem
import org.sat4j.tools._;

abstract class Status {}
case object Satisfiable extends Status {}
case object Unsatisfiable extends Status {}
case object Unknown extends Status {}

class Problem {

  val problem = SolverFactory.newDefault

  /** Adds a clause in the problem. */	  
  def +=(clause: IVecInt) = {
    problem addClause clause
    clause.clear 
    this
  }
  
  /** Adds a cardinality constraint in the problem. */
  def addAtMost(literals: IVecInt, k: Int) = {
    problem addAtMost(literals, k)
  }
  
  /** Adds a cardinality constraint in the problem. */
  def addAtLeast(literals: IVecInt, k: Int) = {
    problem addAtLeast(literals, k)
  }

  /** Adds a cardinality constraint in the problem. */
  def addEq(literals: IVecInt, k: Int) = {
    problem addExactly(literals, k)
  }
  
  def solve = {
    try {
      if (problem.isSatisfiable) 
         Satisfiable
       else 
         Unsatisfiable
    } catch {
      case _ =>  Unknown
    }
  }

  def enumerate = {
    try {
      var sat = false
      var done = false
      var sols = List[Array[Int]]()
      val modelListener = new SolutionFoundListener() {
          def onSolutionFound(model:Array[Int]) = {
            sat = true
            sols = model::sols
        }
        def onSolutionFound(solution:IVecInt ) = {
            // do nothing
        }
        def onUnsatTermination() = {
            done = true
        }
      }
      problem setSearchListener new SearchEnumeratorListener(modelListener)
      problem.isSatisfiable
      if (sat) 
        if (done) 
         (Satisfiable,sols)
         else 
           (Unknown,sols)
       else 
         (Unsatisfiable,List())
    } catch {
      case _ =>  (Unknown,List())
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
