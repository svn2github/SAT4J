package org.sat4j.scala

import org.sat4j.specs.ContradictionException

object Logic {

  /** A pretty printer for logic syntax trees. */
  object PrettyPrint {
    def apply(e: Exp): String = e match {
      case True => "True"
      case False => "False"
      case Not(True) => "~True"
      case Not(False) => "~False"
      case v: AnonymousVariable => v.toString
      case Not(v: AnonymousVariable) => "~" + v.toString
      case Ident(s) => s.toString
      case IndexedIdent(s, is) => s.toString + is.mkString("(", ",", ")")
      case Not(Ident(s)) => "~" + s
      case Not(IndexedIdent(s, is)) => "~" + s.toString + is.mkString("(", ",", ")")
      case Not(b) => "~(" + apply(b) + ")"
      case And(b1, b2) => "(" + apply(b1) + " & " + apply(b2) + ")"
      case Or(b1, b2) => "(" + apply(b1) + " | " + apply(b2) + ")"
      case Implies(b1, b2) => "(" + apply(b1) + " implies " + apply(b2) + ")"
      case Iff(b1, b2) => "(" + apply(b1) + " iff " + apply(b2) + ")"
      case CardEQ(bs, k) => "(" + bs.map(apply).mkString(" + ") + " === " + k + ")"
      case CardLE(bs, k) => "(" + bs.map(apply).mkString(" + ") + " <= " + k + ")"
      case CardLT(bs, k) => "(" + bs.map(apply).mkString(" + ") + " < " + k + ")"
      case CardGE(bs, k) => "(" + bs.map(apply).mkString(" + ") + " >= " + k + ")"
      case CardGT(bs, k) => "(" + bs.map(apply).mkString(" + ") + " > " + k + ")"
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

  /** Abstract base class of all DSL expressions. */
  abstract class Exp

  /** Abstract base class of all boolean valued expressions. */
  abstract class BoolExp extends Exp {
    def &(b: BoolExp) = And(this, b)
    def |(b: BoolExp) = Or(this, b)
    def implies(b: BoolExp) = Implies(this, b)
    def iff(b: BoolExp) = Iff(this, b)
    def unary_~() = Not(this)
    def +(b: BoolExp) = Card(List(b, this))
    def ===(k: Int) = CardEQ(List(this), k)
    def <=(k: Int) = CardLE(List(this), k)
    def <(k: Int) = CardLT(List(this), k)
    def >=(k: Int) = CardGE(List(this), k)
    def >(k: Int) = CardGT(List(this), k)
    def toCnfList = {
      isAlreadyInCnf(this) match {
        case (true, Some(x)) => x
        case _ => {
          val next = nextAnonymousVar
          val translated = tseitinListSimple(this, List())._2
          assert(!(_createdVars isEmpty))
          List(next) :: translated
        }
      }
    }
  }

  /** Base class for boolean constants True and False. */
  abstract class BoolValue extends BoolExp

  /** Truth. */
  case object True extends BoolValue

  /** Falsity. */
  case object False extends BoolValue

  /** Logical conjunction operator. */
  private[Logic] case class And(b1: BoolExp, b2: BoolExp) extends BoolExp

  /** Logical disjunction operator. */
  private[Logic] case class Or(b1: BoolExp, b2: BoolExp) extends BoolExp

  /** Logical implication operator. */
  private[Logic] case class Implies(b1: BoolExp, b2: BoolExp) extends BoolExp

  /** Logical equivalence operator. */
  private[Logic] case class Iff(b1: BoolExp, b2: BoolExp) extends BoolExp

  /** Base class for cardinality operators. */
  abstract class CardExp extends BoolExp

  /** Cardinality equals k operator. */
  private[Logic] case class CardEQ(bs: List[BoolExp], k: Int) extends CardExp

  /** Cardinality less than or equals k operator. */
  private[Logic] case class CardLE(bs: List[BoolExp], k: Int) extends CardExp

  /** Cardinality less than k operator. */
  private[Logic] case class CardLT(bs: List[BoolExp], k: Int) extends CardExp

  /** Cardinality greater than or equals k operator. */
  private[Logic] case class CardGE(bs: List[BoolExp], k: Int) extends CardExp

  /** Cardinality greater than k operator. */
  private[Logic] case class CardGT(bs: List[BoolExp], k: Int) extends CardExp

  /** Abstract base class of all integer valued expressions. */
  protected abstract class IntExp extends Exp

  /** Cardinality operator. */
  private[Logic] case class Card(bs: List[BoolExp]) extends IntExp {
    def +(b: BoolExp) = Card(b :: bs)
    def ===(k: Int) = CardEQ(bs.reverse, k)
    def <=(k: Int) = CardLE(bs.reverse, k)
    def <(k: Int) = CardLT(bs.reverse, k)
    def >=(k: Int) = CardGE(bs.reverse, k)
    def >(k: Int) = CardGT(bs.reverse, k)
  }

  /** Logical negation operator. */
  private[Logic] case class Not(b: BoolExp) extends BoolExp

  abstract class Identifier extends BoolExp
  /** Logical proposition identifier. */
  protected case class Ident[U](name: U) extends Identifier {
    def apply(indices: Int*) = IndexedIdent(name, indices.toList)
  }

  /** Logical proposition identifier. */
  private[Logic] case class IndexedIdent[U](name: U, indices: List[Int] = Nil) extends Identifier {

  }

  /** Anonymous logical proposition. */
  private[Logic] case class AnonymousVariable extends BoolExp {
    private val id = nextVarId
    override def toString = "_nv#" + id
    override def equals(o: Any) = o match {
      case x: AnonymousVariable => id == x.id
      case _ => false
    }
    override def hashCode() = id
  }

  private var _varId = 0
  private def nextVarId = { _varId += 1; _varId };
  private var _createdVars = List[AnonymousVariable]()

  /** create new propositional variables for translation into CNF */
  private def newVar = if (_cachedVar != null) { val tmp = _cachedVar; _cachedVar = null; tmp } else uncachedNewVar

  private def uncachedNewVar = {
    val v = new AnonymousVariable
    _createdVars = v :: _createdVars
    v
  }

  private def nextAnonymousVar = if (_cachedVar == null) { _cachedVar = uncachedNewVar; _cachedVar } else _cachedVar;

  private var _cachedVar = uncachedNewVar

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
  
  /** Implicit conversion from string to logical identifier */
  implicit def identFromString(s: String): Ident[String] = Ident(s)

  /** Implicit conversion from string to logical identifier */
  implicit def identFromSymbol(i: Symbol): Ident[Symbol] = Ident(i)

  /** Convert any Scala object into a propositional variable */
  def toProp[U](u: U): Ident[U] = Ident(u)

  /** Returns true iff the given expression is already in conjunctive normal form. */
  private def isAlreadyInCnf(f: BoolExp): (Boolean, Option[List[List[BoolExp]]]) = f match {
    case And(b1, b2) => {
      val (r1, l1) = isAlreadyInCnf(b1)
      if (r1) {
        val (r2, l2) = isAlreadyInCnf(b2)
        if (r2) (true, Some(l1.get ++ l2.get))
        else (false, None)
      } else (false, None)
    }
    case Or(b1, b2) => isDisjunction(f)
    case _ => isLiteral(f)
  }

  private def isDisjunction(f: BoolExp): (Boolean, Option[List[List[BoolExp]]]) = f match {
    case Or(b1, b2) => {
      val (r1, l1) = isDisjunction(b1)
      if (r1) {
        val (r2, l2) = isDisjunction(b2)
        if (r2) (true, Some(List(l1.get(0) ++ l2.get(0))))
        else (false, None)
      } else (false, None)
    }
    case _ => isLiteral(f)
  }

  private def isLiteral(f: BoolExp): (Boolean, Option[List[List[BoolExp]]]) = f match {
    case True => (true, Some(List(List(True))))
    case False => (true, Some(List(List(False))))
    case Ident(_) => (true, Some(List(List(f))))
    case Not(Ident(_)) => (true, Some(List(List(f))))
    case _ => (false, None)
  }

  private def tseitinListSimple(b: BoolExp, l: List[List[BoolExp]]): (BoolExp, List[List[BoolExp]]) = {
    b match {

      case True => (True, List())
      case Not(False) => (True, List())

      case False => (False, List())
      case Not(True) => (False, List())

      case Ident(s) => (Ident(s), List())

      case IndexedIdent(s, l) => (IndexedIdent(s, l), List())

      case Not(b1) => {
        val v = newVar
        val t1 = tseitinListSimple(b1, List())
        (v, List(~t1._1, ~v) :: List(t1._1, v) :: t1._2)
      }

      case And(b1, b2) => {
        val v = newVar
        val t1 = tseitinListSimple(b1, List())
        val t2 = tseitinListSimple(b2, List())
        (v, List(~t1._1, ~t2._1, v) :: List(t1._1, ~v) :: List(t2._1, ~v) :: t1._2 ++ t2._2)

      }

      case Or(b1, b2) => {
        val v = newVar
        val t1 = tseitinListSimple(b1, List())
        val t2 = tseitinListSimple(b2, List())
        (v, List(t1._1, t2._1, ~v) :: List(~t1._1, v) :: List(~t2._1, v) :: t1._2 ++ t2._2)
      }

      case Implies(b1, b2) => {
        val v = newVar
        val t1 = tseitinListSimple(b1, List())
        val t2 = tseitinListSimple(b2, List())
        (v, List(~t1._1, t2._1, ~v) :: List(t1._1, v) :: List(~t2._1, v) :: t1._2 ++ t2._2)
      }

      case Iff(b1, b2) => {
        val v = newVar
        val t1 = tseitinListSimple(b1, List())
        val t2 = tseitinListSimple(b2, List())
        (v, List(~t1._1, t2._1, ~v) :: List(t1._1, ~t2._1, ~v) :: List(t1._1, t2._1, v) :: List(~t1._1, ~t2._1, v) :: t1._2 ++ t2._2)
      }
    }
  }

  private def simplifyClause(c: List[BoolExp]): List[BoolExp] = c match {
    case Nil => List()
    case True :: t => List(True)
    case Not(False) :: t => List(True)
    case False :: t => simplifyClause(t)
    case Not(True) :: t => simplifyClause(t)
    case h :: t => h :: simplifyClause(t)
  }

  private def simplifyCnf(l: List[List[BoolExp]]): List[List[BoolExp]] = l match {
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

  def encode(cnf: BoolExp): (List[List[Int]], Map[BoolExp, Int]) = {
    _createdVars = List()
    _varId = 0
    encode(simplifyCnf(cnf.toCnfList))
  }

  def encode(cnf: List[List[BoolExp]]): (List[List[Int]], Map[BoolExp, Int]) = {
    encodeCnf0(cnf, Map[BoolExp, Int]())
  }

  private def encodeCnf0(cnf: List[List[BoolExp]], m: Map[BoolExp, Int]): (List[List[Int]], Map[BoolExp, Int]) = cnf match {
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

  private def inv(x: Int): Int = -x

  private def encodeClause0(c: List[BoolExp], m: Map[BoolExp, Int]): (List[Int], Map[BoolExp, Int]) = c match {
    case Nil => (List(), m)
    case (s: AnonymousVariable) :: q => encodeClause1(s, q, m, x => x)
    case (s: Ident[_]) :: q => encodeClause1(s, q, m, x => x)
    case (s: IndexedIdent[_]) :: q => encodeClause1(s, q, m, x => x)
    case (Not(s: AnonymousVariable)) :: q => encodeClause1(s, q, m, inv)
    case (Not(s: Ident[_])) :: q => encodeClause1(s, q, m, inv)
    case (Not(s: IndexedIdent[_])) :: q => encodeClause1(s, q, m, inv)

    case _ => throw new Exception("There is something that is not a litteral in the clause " + PrettyPrint(List(c)))
  }

  def encodeClause1(c: BoolExp, q: List[BoolExp], m: Map[BoolExp, Int], f: Int => Int): (List[Int], Map[BoolExp, Int]) = m.get(c) match {
    case Some(i) => {
      val p = encodeClause0(q, m)
      (f(i) :: p._1, p._2)
    }
    case None => {
      val n = m.size + 1
      val p = encodeClause0(q, m.updated(c, n))
      (f(n) :: p._1, p._2)
    }
  }

  def isSat[U](f: BoolExp): (Boolean, Option[Map[U, Boolean]]) = {

    val (cnf, m) = encode(f)
    val mapRev = m map {
      case (x, y) => (y, x)
    }
    val problem = new Problem
    try {
      cnf.foldLeft(problem) { (p, c) => p += Clause(c) }
      val res = problem.solve
      res match {
        case Satisfiable => {
          val listeBoolExp = problem.model.toList map { x => if (x > 0) (mapRev(x) -> true) else (mapRev(-x) -> false) }
          val mapIdentBool = listeBoolExp filter (x => x match {
            case (s: AnonymousVariable, _) => false
            case (Ident(s), _) => true
            case _ => false
          })
          val mapUBool = mapIdentBool map (z => z match {
            case (Ident(s: U), b) => (s, b)
            case _ => throw new IllegalStateException
          })
          (true, Some(mapUBool.toMap))
        }
        case Unsatisfiable => (false, None)
        case _ => throw new IllegalStateException("Got a time out")
      }
    } catch {
      case e: ContradictionException => (false, None)
    }
  }

  def isValid[U](f: BoolExp): (Boolean, Option[Map[U, Boolean]]) = {
    val (b, m) = isSat[U](~f)
    (!b, m)
  }

}