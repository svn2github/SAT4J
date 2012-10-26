package org.sat4j.scala



object testLogic {
	
	import Logic._;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(79); 
	
	val f = True;System.out.println("""f  : org.sat4j.scala.Logic.True.type = """ + $show(f ));$skip(20); 
	
	val f2 = 'y & 'x;System.out.println("""f2  : org.sat4j.scala.Logic.And = """ + $show(f2 ));$skip(20); 
	
	val f3 = f2 | 'y;System.out.println("""f3  : org.sat4j.scala.Logic.Or = """ + $show(f3 ));$skip(33); 
	
	val f4 = 'x & 'y | ('z -> 'd);System.out.println("""f4  : org.sat4j.scala.Logic.Or = """ + $show(f4 ));$skip(29); 
	
	val f5 = (f3 & True) | 'y;System.out.println("""f5  : org.sat4j.scala.Logic.Or = """ + $show(f5 ));$skip(39); 
	
	
	
	val cnf = (True & 'x) toCnfList;System.out.println("""cnf  : List[List[org.sat4j.scala.Logic.BoolExp]] = """ + $show(cnf ));$skip(24); val res$0 = 
	
	
	
	PrettyPrint(cnf);System.out.println("""res0: String = """ + $show(res$0));$skip(35); val res$1 = 
	
	
	PrettyPrint(simplifyCnf(cnf));System.out.println("""res1: String = """ + $show(res$1));$skip(62); val res$2 = 
                                 
	
	encode(simplifyCnf(cnf));System.out.println("""res2: (List[List[Int]], Map[String,Int]) = """ + $show(res$2));$skip(55); val res$3 = 
                                      
	
	
	encode(f5);System.out.println("""res3: (List[List[Int]], Map[String,Int]) = """ + $show(res$3));$skip(26); val res$4 = 
        
  
  encode (f4);System.out.println("""res4: (List[List[Int]], Map[String,Int]) = """ + $show(res$4));$skip(22); val res$5 = 
  
  encode ('a | 'b);System.out.println("""res5: (List[List[Int]], Map[String,Int]) = """ + $show(res$5));$skip(25); val res$6 = 
  
  
  isSat ('x & ~'x);System.out.println("""res6: (Boolean, Option[List[String]]) = """ + $show(res$6));$skip(18); val res$7 = 
  isSat ('a | 'b);System.out.println("""res7: (Boolean, Option[List[String]]) = """ + $show(res$7));$skip(26); val res$8 = 
  
  

	isValid ('a | 'b);System.out.println("""res8: (Boolean, Option[List[String]]) = """ + $show(res$8));$skip(22); val res$9 = 
	
	isValid( 'a | ~'a);System.out.println("""res9: (Boolean, Option[List[String]]) = """ + $show(res$9));$skip(14); 


	val x = 'x;System.out.println("""x  : Symbol = """ + $show(x ))}
}