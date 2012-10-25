package org.sat4j.scala



object testLogic {
	
	import Logic._
	
	val f = True                              //> f  : org.sat4j.scala.Logic.True.type = True
	
	val f2 = 'y & 'x                          //> f2  : org.sat4j.scala.Logic.And = And(Ident(y),Ident(x))
	
	val f3 = f2 | 'y                          //> f3  : org.sat4j.scala.Logic.Or = Or(And(Ident(y),Ident(x)),Ident(y))
	
	val f4 = 'x & 'y | ('z -> 'd)             //> f4  : org.sat4j.scala.Logic.Or = Or(And(Ident(x),Ident(y)),Or(Not(Ident(z)),
                                                  //| Ident(d)))
	
	val f5 = (f3 & True) | 'y                 //> f5  : org.sat4j.scala.Logic.Or = Or(And(Or(And(Ident(y),Ident(x)),Ident(y)),
                                                  //| True),Ident(y))
	
	
	
	val cnf = (True & 'x) toCnfList           //> cnf  : List[List[org.sat4j.scala.Logic.BoolExp]] = List(List(Ident(_nv0)), L
                                                  //| ist(Not(True), Not(Ident(x)), Ident(_nv0)), List(True, Not(Ident(_nv0))), Li
                                                  //| st(Ident(x), Not(Ident(_nv0))))
	
	
	
	PrettyPrint(cnf)                          //> res0: String = "
                                                  //| _nv0
                                                  //| ~True ~x _nv0
                                                  //| True ~_nv0
                                                  //| x ~_nv0"
	
	
	PrettyPrint(simplifyCnf(cnf))             //> res1: String = "
                                                  //| _nv0
                                                  //| ~x _nv0
                                                  //| x ~_nv0"
                                 
	
	encode(simplifyCnf(cnf))                  //> res2: (List[List[Int]], Map[String,Int]) = (List(List(1), List(-2, 1), List(
                                                  //| 2, -1)),Map(_nv0 -> 1, x -> 2))
                                      
	
	
	encode(f5)                                //> res3: (List[List[Int]], Map[String,Int]) = (List(List(1), List(2, 3, -1), Li
                                                  //| st(-2, 1), List(-3, 1), List(-4, 2), List(4, -2), List(5, 3, -4), List(-5, 4
                                                  //| ), List(-3, 4), List(-3, -6, 5), List(3, -5), List(6, -5)),Map(x -> 6, _nv1 
                                                  //| -> 2, y -> 3, _nv2 -> 4, _nv3 -> 5, _nv0 -> 1))
        
  
  encode (f4)                                     //> res4: (List[List[Int]], Map[String,Int]) = (List(List(1), List(2, 2, -1), Li
                                                  //| st(-2, 1), List(-2, 1), List(-3, -4, 2), List(3, -2), List(4, -2), List(5, 6
                                                  //| , -2), List(-5, 2), List(-6, 2), List(-7, -5), List(7, 5)),Map(x -> 3, _nv1 
                                                  //| -> 2, y -> 4, _nv2 -> 5, _nv0 -> 1, z -> 7, d -> 6))
  
  encode ('a | 'b)                                //> res5: (List[List[Int]], Map[String,Int]) = (List(List(1), List(2, 3, -1), Li
                                                  //| st(-2, 1), List(-3, 1)),Map(_nv0 -> 1, a -> 2, b -> 3))
  
  isSat ('a | 'b)                                 //> res6: (Boolean, Option[List[String]]) = (true,Some(List(a, ~b)))




	val x = 'x                                //> x  : Symbol = 'x
}