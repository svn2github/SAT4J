package org.sat4j.scala

import Logic.PrettyPrint
import Logic.True
import Logic.identFromString


object Main extends App{
	import Logic._
	
	
	val f = True
	
	val f2 = 'y & 'x(1,2,3)
	
	val f3 = f2 | 'y 
	
	val f4 = 'x & 'y | ('z implies 'd)
	
	val f5 = (f3 & True) | 'y
	
	
	val c1 = f + f2 + f3 === 4
	val c2 = f + f2 + f3 < 10
	val c3 = f + f2 + f3 > 11
	val c4 = f + f2 + f3 <= 42
	val c5 = f + f2 + f3 >= 666
	// cardinalité peut seulement être de niveau zéro
	// tout ceci est maintenant non autorisé
	//val x32 = c5 implies c1 
	//val x33 = ('x | 'y + 'z >= 2) implies (~'z & 'x | 'z & 'y) 
	//val x34= (('x | 'y) + 'z >= 2) implies ('z & ('x | 'z) & 'y)	
	val x36 = 'a | 'b & 'c iff 'x & 'y | 'z implies 'w
	val x37 = 'x & 'y iff 'w implies'c & 'b
	
	// TODO : doit-on permettre cela?
	val x35 = ~'x > 0
	
	(0 to 10) foreach { i => println(('x(i) & 'x(i+1)) iff ('x(i-1) & 'x(i+1)))}
	
	println(PrettyPrint(f4))
	println(PrettyPrint(f5))
	println(PrettyPrint(c1))
	println(PrettyPrint(c2))
	println(PrettyPrint(c3))
	println(PrettyPrint(c4))
	println(PrettyPrint(c5))
	println(PrettyPrint(x35))
	println(PrettyPrint(x36))
	println(PrettyPrint(x37))
	// team modeling
	val n = 5                                 //> n  : Int = 5
	val teams = (1 to n).toList               //> teams  : List[Int] = List(1, 2, 3, 4, 5)
	val time = (1 until n).toList             //> time  : List[Int] = List(1, 2, 3, 4)
	val myformula = and(for (t <- time) yield
					and(for (i <- teams)
						yield Card((for (j <- teams ; if (j != i)) yield 'meet(i,j,t))) === 1))
	
	println(PrettyPrint(myformula))
	
}