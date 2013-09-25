package org.sat4j.scala



object testLogic {
	
	import Logic._
	
	val f = True                              //> f  : org.sat4j.scala.Logic.True.type = True
	
	val f2 = 'y & 'x(1,2,3)                   //> f2  : org.sat4j.scala.Logic.And = And(Ident('y),IndexedIdent('x,List(1, 2, 3
                                                  //| )))
	 
	val f3 = f2 | 'y                          //> f3  : org.sat4j.scala.Logic.Or = Or(And(Ident('y),IndexedIdent('x,List(1, 2,
                                                  //|  3))),Ident('y))
	
	val f4 = 'x & 'y | ('z implies 'd)        //> f4  : org.sat4j.scala.Logic.Or = Or(And(Ident('x),Ident('y)),Implies(Ident('
                                                  //| z),Ident('d)))
	
	val f5 = (f3 & True) | 'y                 //> f5  : org.sat4j.scala.Logic.Or = Or(And(Or(And(Ident('y),IndexedIdent('x,Lis
                                                  //| t(1, 2, 3))),Ident('y)),True),Ident('y))
	
	val c1 = f + f2 + f3 === 4                //> c1  : org.sat4j.scala.Logic.CardEQ = CardEQ(List(True, And(Ident('y),Indexed
                                                  //| Ident('x,List(1, 2, 3))), Or(And(Ident('y),IndexedIdent('x,List(1, 2, 3))),I
                                                  //| dent('y))),4)
	val c2 = f + f2 + f3 < 10                 //> c2  : org.sat4j.scala.Logic.CardLT = CardLT(List(True, And(Ident('y),Indexed
                                                  //| Ident('x,List(1, 2, 3))), Or(And(Ident('y),IndexedIdent('x,List(1, 2, 3))),I
                                                  //| dent('y))),10)
	val c3 = f + f2 + f3 > 11                 //> c3  : org.sat4j.scala.Logic.CardGT = CardGT(List(True, And(Ident('y),Indexed
                                                  //| Ident('x,List(1, 2, 3))), Or(And(Ident('y),IndexedIdent('x,List(1, 2, 3))),I
                                                  //| dent('y))),11)
	val c4 = f + f2 + f3 <= 42                //> c4  : org.sat4j.scala.Logic.CardLE = CardLE(List(True, And(Ident('y),Indexed
                                                  //| Ident('x,List(1, 2, 3))), Or(And(Ident('y),IndexedIdent('x,List(1, 2, 3))),I
                                                  //| dent('y))),42)
	val c5 = f + f2 + f3 >= 666               //> c5  : org.sat4j.scala.Logic.CardGE = CardGE(List(True, And(Ident('y),Indexed
                                                  //| Ident('x,List(1, 2, 3))), Or(And(Ident('y),IndexedIdent('x,List(1, 2, 3))),I
                                                  //| dent('y))),666)
	
	val x32 = c5 implies c1                   //> x32  : org.sat4j.scala.Logic.Implies = Implies(CardGE(List(True, And(Ident('
                                                  //| y),IndexedIdent('x,List(1, 2, 3))), Or(And(Ident('y),IndexedIdent('x,List(1,
                                                  //|  2, 3))),Ident('y))),666),CardEQ(List(True, And(Ident('y),IndexedIdent('x,Li
                                                  //| st(1, 2, 3))), Or(And(Ident('y),IndexedIdent('x,List(1, 2, 3))),Ident('y))),
                                                  //| 4))
	
	// TODO : doit-on permettre cela?
	val x35 = 'x > 0                          //> x35  : org.sat4j.scala.Logic.CardGT = CardGT(List(Ident('x)),0)
	
	println(PrettyPrint(f4))                  //> (('x & 'y) | ('z implies 'd))
	println(PrettyPrint(f5))                  //> (((('y & 'x(1,2,3)) | 'y) & True) | 'y)
	println(PrettyPrint(c1))                  //> (True + ('y & 'x(1,2,3)) + (('y & 'x(1,2,3)) | 'y) === 4)
	println(PrettyPrint(c2))                  //> (True + ('y & 'x(1,2,3)) + (('y & 'x(1,2,3)) | 'y) < 10)
	println(PrettyPrint(c3))                  //> (True + ('y & 'x(1,2,3)) + (('y & 'x(1,2,3)) | 'y) > 11)
	println(PrettyPrint(c4))                  //> (True + ('y & 'x(1,2,3)) + (('y & 'x(1,2,3)) | 'y) <= 42)
	println(PrettyPrint(c5))                  //> (True + ('y & 'x(1,2,3)) + (('y & 'x(1,2,3)) | 'y) >= 666)
	println(PrettyPrint(x32))                 //> ((True + ('y & 'x(1,2,3)) + (('y & 'x(1,2,3)) | 'y) >= 666) implies (True + 
                                                  //| ('y & 'x(1,2,3)) + (('y & 'x(1,2,3)) | 'y) === 4))
	
	 
	isSat(f4)                                 //> (>>>>>>>>>>>>,Ident('z),Ident('d))(_nv#3,(Ident('z),List()),(Ident('d),List(
                                                  //| )),List(_nv#3, _nv#2, _nv#1))res0: (Boolean, Option[Map[Nothing,Boolean]]) =
                                                  //|  (true,Some(Map('x -> false, 'y -> false, 'z -> false, 'd -> false)))
	
	
	
	val cnf = (True & 'x) toCnfList new Context
                                                  //> cnf  : List[List[org.sat4j.scala.Logic.BoolExp]] = List(List(True), List(Ide
                                                  //| nt('x)))
	
	
	
	PrettyPrint(cnf)                          //> res1: String = "
                                                  //| True
                                                  //| 'x"
	
	
	PrettyPrint(simplifyCnf(cnf))             //> res2: String = "
                                                  //| 'x"
                                 
	
	encode(simplifyCnf(cnf))                  //> res3: (List[List[Int]], Map[org.sat4j.scala.Logic.BoolExp,Int]) = (List(List
                                                  //| (1)),Map(Ident('x) -> 1))
                                      
	
	
	
	encode(f5,new Context)                    //> res4: (List[List[Int]], Map[org.sat4j.scala.Logic.BoolExp,Int]) = (List(List
                                                  //| (1), List(2, 3, -1), List(-2, 1), List(-3, 1), List(-4, 2), List(4, -2), Lis
                                                  //| t(5, 3, -4), List(-5, 4), List(-3, 4), List(-3, -6, 5), List(3, -5), List(6,
                                                  //|  -5)),Map(Ident('y) -> 3, _nv#1 -> 1, _nv#2 -> 2, _nv#3 -> 4, IndexedIdent('
                                                  //| x,List(1, 2, 3)) -> 6, _nv#4 -> 5))
        
  
  encode (f4,new Context)                         //> (>>>>>>>>>>>>,Ident('z),Ident('d))(_nv#3,(Ident('z),List()),(Ident('d),List(
                                                  //| )),List(_nv#3, _nv#2, _nv#1))res5: (List[List[Int]], Map[org.sat4j.scala.Log
                                                  //| ic.BoolExp,Int]) = (List(List(1), List(2, 3, -1), List(-2, 1), List(-3, 1), 
                                                  //| List(-4, -5, 2), List(4, -2), List(5, -2), List(-6, 7, -3), List(6, 3), List
                                                  //| (-7, 3)),Map(Ident('x) -> 4, Ident('y) -> 5, _nv#1 -> 1, Ident('d) -> 7, _nv
                                                  //| #2 -> 2, _nv#3 -> 3, Ident('z) -> 6))
  
  encode ('a | 'b,new Context)                    //> res6: (List[List[Int]], Map[org.sat4j.scala.Logic.BoolExp,Int]) = (List(List
                                                  //| (1, 2)),Map(Ident('a) -> 1, Ident('b) -> 2))
  
  
  isSat ('x & ~'x)                                //> res7: (Boolean, Option[Map[Nothing,Boolean]]) = (false,None)
  
  isSat ('a | 'b)                                 //> res8: (Boolean, Option[Map[Nothing,Boolean]]) = (true,Some(Map('a -> false,
                                                  //|  'b -> true)))
  
  

	isValid ('a | 'b)                         //> res9: (Boolean, Option[Map[Nothing,Boolean]]) = (false,Some(Map('a -> false
                                                  //| , 'b -> false)))
	
	isValid( 'a | ~'a)                        //> res10: (Boolean, Option[Map[Nothing,Boolean]]) = (true,None)

	val l = 'a & 'b                           //> l  : org.sat4j.scala.Logic.And = And(Ident('a),Ident('b))
	
	PrettyPrint(l)                            //> res11: String = ('a & 'b)
	
	isSat (l & ~l)                            //> res12: (Boolean, Option[Map[Nothing,Boolean]]) = (false,None)

	isValid (l)                               //> res13: (Boolean, Option[Map[Nothing,Boolean]]) = (false,Some(Map('a -> fals
                                                  //| e, 'b -> false)))
	
	isSat(~'a | 'a)                           //> res14: (Boolean, Option[Map[Nothing,Boolean]]) = (true,Some(Map('a -> false
                                                  //| )))
	
	isSat ('a implies 'a)                     //> (>>>>>>>>>>>>,Ident('a),Ident('a))(_nv#1,(Ident('a),List()),(Ident('a),List
                                                  //| ()),List(_nv#1))res15: (Boolean, Option[Map[Nothing,Boolean]]) = (true,Some
                                                  //| (Map('a -> false)))
	
	val liste = List('a -> 'b, 'c iff 'd)     //> liste  : List[Product with Serializable] = List(('a,'b), Iff(Ident('c),Iden
                                                  //| t('d)))
  ('a & 'b iff 'a | 'b) toCnfList new Context     //> res16: List[List[org.sat4j.scala.Logic.BoolExp]] = List(List(_nv#1), List(N
                                                  //| ot(_nv#2), _nv#3, Not(_nv#1)), List(_nv#2, Not(_nv#3), Not(_nv#1)), List(_n
                                                  //| v#2, _nv#3, _nv#1), List(Not(_nv#2), Not(_nv#3), _nv#1), List(Not(Ident('a)
                                                  //| ), Not(Ident('b)), _nv#2), List(Ident('a), Not(_nv#2)), List(Ident('b), Not
                                                  //| (_nv#2)), List(Ident('a), Ident('b), Not(_nv#3)), List(Not(Ident('a)), _nv#
                                                  //| 3), List(Not(Ident('b)), _nv#3))
  
  
   
  PrettyPrint (~('a & 'b) toCnfList new Context)  //> res17: String = "
                                                  //| _nv#1
                                                  //| ~_nv#2 ~_nv#1
                                                  //| _nv#2 _nv#1
                                                  //| ~'a ~'b _nv#2
                                                  //| 'a ~_nv#2
                                                  //| 'b ~_nv#2"
                                  
 
}