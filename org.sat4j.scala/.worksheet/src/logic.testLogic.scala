package logic



object testLogic {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(51); 
	
	val f = True;System.out.println("""f  : <error> = """ + $show(f ));$skip(20); 
	
	val f2 = 'y & 'x;System.out.println("""f2  : <error> = """ + $show(f2 ));$skip(20); 
	
	val f3 = f2 | 'y;System.out.println("""f3  : <error> = """ + $show(f3 ));$skip(33); 
	
	val f4 = 'x & 'y | ('z -> 'd);System.out.println("""f4  : <error> = """ + $show(f4 ));$skip(29); 
	
	val f5 = (f3 & True) | 'y;System.out.println("""f5  : <error> = """ + $show(f5 ));$skip(39); 
	
	
	
	val cnf = (True & 'x) toCnfList;System.out.println("""cnf  : <error> = """ + $show(cnf ));$skip(24); val res$0 = 
	
	
	
	PrettyPrint(cnf);System.out.println("""res0: <error> = """ + $show(res$0));$skip(35); val res$1 = 
	
	
	PrettyPrint(simplifyCnf(cnf));System.out.println("""res1: <error> = """ + $show(res$1));$skip(62); val res$2 = 
                                 
	
	encode(simplifyCnf(cnf));System.out.println("""res2: <error> = """ + $show(res$2));$skip(16); val res$3 = 
	
	
	encode(f5);System.out.println("""res3: <error> = """ + $show(res$3))}
              
              

}