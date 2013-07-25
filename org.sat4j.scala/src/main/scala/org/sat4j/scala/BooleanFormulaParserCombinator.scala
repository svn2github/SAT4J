package parser

import scala.util.parsing.combinator.PackratParsers
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import org.sat4j.scala.Logic._
import scala.annotation.migration

object BooleanFormulaParserCombinator extends StandardTokenParsers with PackratParsers {

  /**
   * listFormula = formula ; | formula ; listFormula
   * formula = term & formula | term '|' formula | term -> formula | term <-> formula | term
   * term = (formula) | ~(formula) | lit
   * lit = ~ ident | ident
   */
  
  
  lexical.delimiters += ("(", ")", "&", "|", "~", "->", "<->", ";")
    
  def scala2JavaList(sl: List[String]): java.util.List[String] = {
    var jl = new java.util.ArrayList[String]()
    sl.foreach(jl.add(_))
    jl
  }
  
  val listFormula: PackratParser[List[BoolExp]] =  
    (formula <~ ";") ~ listFormula ^^ {
      case f ~ l => f :: l
    } | formula <~ ";" ^^ {
    case f => f :: List()
    } 

  val formula: PackratParser[BoolExp] =  term ~ ("&" ~> formula) ^^ {
    case f1 ~ f2 => f1 & f2
  } | term ~ ("|" ~> formula) ^^ {
    case f1 ~ f2 => f1 | f2
  } | term ~ ("->" ~> formula) ^^ {
    case f1 ~ f2 => f1 implies f2
  } | term ~ ("<->" ~> formula) ^^ {
    case f1 ~ f2 => f1 iff f2
  } | term
  
  val term =  "~" ~> "(" ~> formula <~ ")" ^^ {
    case f  => f.unary_~
  } | "(" ~> formula <~ ")" | lit

  val lit: PackratParser[BoolExp] =  "~" ~> ident ^^ {
    case s => (s.toString()).unary_~
  } | ident ^^ {
    case s => (s.toString():BoolExp)
  } 
    
  /**
   * Parses a string into a couple (s,l) where s is a string representing the BoolExp with a PrettyPrint 
   * and l the list of Cnf composing the BoolExp
   */
  def parseListBoolExp(dsl : String) = 
    listFormula(new PackratReader(new lexical.Scanner(dsl))) match {
    case Success(ord, _) => ord map(f => (PrettyPrint(f), f toCnfList))
    case Failure(msg, _) => msg :: List()
    case Error(msg, _) => msg :: List()
    case p => p.toString() :: List() 
  }
  
  def javaParseListBool(dsl: String) = println(parseListBoolExp(dsl))

  def main(args: Array[String]) = {
    println(parseListBoolExp("(x & y) <-> z; a | b; ~a & b | c; ~(a & b);"))
    javaParseListBool("~(a & b);")
  }
}
