package org.sat4j.scala

import org.sat4j.specs.ContradictionException

object Logic {

  /** A pretty printer for logic syntax trees. */
  object PrettyPrint {
    def apply(e: BoolExp): String = e match {
      case True => "True"
      case False => "False"
      case Not(True) => "~True"
      case Not(False) => "~False"
      case Ident(s) => s
      case Not(Ident(s)) => "~" + s
      case Not(b) => "~(" + apply(b) + ")"
      case And(b1, b2) => "(" + apply(b1) + " & " + apply(b2) + ")"
      case Or(b1, b2) => "(" + apply(b1) + " | " + apply(b2) + ")"
    }

    def apply(cnfList: List[List[BoolExp]]): String =
      cnfList match {
        case Nil => "";
        case c :: t => {
          val line =
            for (l <- c) yield apply(l)
          "\n" + (line mkString " ") + apply(t)
        }
      }

  }

  abstract class BoolExp {
    def &(b: BoolExp) = And(this, b)
    def |(b: BoolExp) = Or(this, b)
    def ->(b: BoolExp) = implies(this, b)
    def <->(b: BoolExp) = iff(this, b)
    def unary_~() = Not(this)

    def toCnfList = List(Ident("_nv0")) :: (tseitinListSimple(this, List(), List())._2)

  }

  abstract class BoolValue extends BoolExp

  case object True extends BoolValue

  case object False extends BoolValue

  case class And(b1: BoolExp, b2: BoolExp) extends BoolExp

  case class Or(b1: BoolExp, b2: BoolExp) extends BoolExp

  case class Not(b: BoolExp) extends BoolExp

  case class Ident(name: String) extends BoolExp

  /** n-ary conjunction. */
  def and(l: BoolExp*): BoolExp = and(l.toList)

  /** n-ary conjunction. */
  def and(l: List[BoolExp]): BoolExp = l match {
    case Nil => True
    case b :: Nil => b
    case b :: t => l.reduceLeft { (b1, b2) => And(b1, b2) }
  }

  /** n-ary disjunction. */
  def or(l: BoolExp*): BoolExp = or(l.toList)

  /** n-ary disjunction. */
  def or(l: List[BoolExp]): BoolExp = l match {
    case Nil => False
    case b :: Nil => b
    case b :: t => l.reduceLeft { (b1, b2) => Or(b1, b2) }
  }

  /** Logical implication. */
  def implies(b1: BoolExp, b2: BoolExp) = Or(Not(b1), b2)

  /** Logical equivalence. */
  def iff(b1: BoolExp, b2: BoolExp) = And(implies(b1, b2), implies(b2, b1))

  /** Implicit conversion from string to logical identifier */
  implicit def identFromString(s: String): Ident = Ident(s)

  /** Implicit conversion from string to logical identifier */
  implicit def identFromString(i: Symbol): Ident = Ident(i.toString.substring(1))

  def tseitinListSimple(b: BoolExp, l: List[List[BoolExp]], listVars: List[String]): (BoolExp, List[List[BoolExp]], List[String]) = {
    b match {

      case True => (True, List(), listVars)
      case Not(False) => (True, List(), listVars)

      case False => (False, List(), listVars)
      case Not(True) => (False, List(), listVars)

      case Ident(s) => (Ident(s), List(), if (listVars contains s) listVars else s :: listVars)

      case Not(b1) => {
        val name = "_nv" + listVars.size;
        val v = Ident(name)
        val t1 = tseitinListSimple(b1, List(), name :: listVars)
        (v, List(~t1._1, ~v) :: List(t1._1, v) :: t1._2, name :: listVars)
      }

      case And(b1, b2) => {
        val name = "_nv" + listVars.size;
        val v = Ident(name)
        val t1 = tseitinListSimple(b1, List(), name :: listVars)
        val t2 = tseitinListSimple(b2, List(), name :: listVars)
        (v, List(~t1._1, ~t2._1, v) :: List(t1._1, ~v) :: List(t2._1, ~v) :: t1._2 ++ t2._2, name :: listVars)

      }
      case Or(b1, b2) => {
        val name = "_nv" + listVars.size;
        val v = Ident(name)
        val t1 = tseitinListSimple(b1, List(), name :: listVars)
        val t2 = tseitinListSimple(b2, List(), name :: listVars)
        (v, List(t1._1, t2._1, ~v) :: List(~t1._1, v) :: List(~t2._1, v) :: t1._2 ++ t2._2, name :: listVars)

      }
    }
  }

  def simplifyClause(c: List[BoolExp]): List[BoolExp] = c match {
    case Nil => List()
    case True :: t => List(True)
    case Not(False) :: t => List(True)
    case False :: t => simplifyClause(t)
    case Not(True) :: t => simplifyClause(t)
    case h :: t => h :: simplifyClause(t)
  }

  def simplifyCnf(l: List[List[BoolExp]]): List[List[BoolExp]] = l match {
    case Nil => List()
    case h :: t => {
      val s = simplifyClause(h)
      s match {
        case List() => List(List())
        case List(True) => simplifyCnf(t)
        case _ => s :: simplifyCnf(t)
      }
    }
  }

  def encode(cnf: BoolExp): (List[List[Int]], Map[String, Int]) = encode(simplifyCnf(cnf.toCnfList))

  def encode(cnf: List[List[BoolExp]]): (List[List[Int]], Map[String, Int]) = encodeCnf0(cnf, Map[String, Int]())

  def encodeCnf0(cnf: List[List[BoolExp]], m: Map[String, Int]): (List[List[Int]], Map[String, Int]) = cnf match {
    case Nil => (List(), m)
    case h :: t => {
      val p = encodeClause0(h, m)
      p match {
        case (Nil, _) => (List(List()), m)
        case (l, mUpdated) => {
          val cnfT = encodeCnf0(t, mUpdated)
          (l :: cnfT._1, cnfT._2)
        }
      }
    }
  }

  def encodeClause0(c: List[BoolExp], m: Map[String, Int]): (List[Int], Map[String, Int]) = c match {
    case Nil => (List(), m)
    case Ident(s) :: q => m.get(s) match {
      case Some(i) => {
        val p = encodeClause0(q, m)
        (i :: p._1, p._2)
      }
      case None => {
        val n = m.size + 1
        val p = encodeClause0(q, m.updated(s, n))
        (n :: p._1, p._2)
      } 
    }
    case Not(Ident(s)) :: q => m.get(s) match {
      case Some(i) => {
        val p = encodeClause0(q, m)
        (-i :: p._1, p._2)
      }
      case None => {
        val n = m.size + 1
        val p = encodeClause0(q, m.updated(s, n))
        (-n :: p._1, p._2)
      }
    }
    case _ => throw new Exception("There is something that is not a litteral in the clause " + PrettyPrint(List(c)))
  }

  def isSat(f: BoolExp): (Boolean, Option[List[String]]) = {
    val (cnf, m) = encode(f)
    val mapRev = m map {
      case (x, y) => (y, x)
    }
    val problem = new Problem
    try {
      cnf.foldLeft(problem) { (p, c) => p += Clause(c) }
      val res = problem.solve
      res match {
        case Satisfiable => (true, Some(problem.model.toList map {x => if(x>0) mapRev(x) else "~" + mapRev(-x)} filter{ x => !(x startsWith "_nv" ) && !(x startsWith "~_nv" )}))
        case Unsatisfiable => (false, None)
        case _ => throw new IllegalStateException("Got a time out")
      }
    } catch {
      case e: ContradictionException => (false, None)
    }
  }
  
  def isValid(f: BoolExp) : (Boolean, Option[List[String]]) = {
    val (b,m) = isSat (~f) 
    (!b,m) }
  
  

}