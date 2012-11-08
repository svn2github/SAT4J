package org.sat4j.scala

import Logic.PrettyPrint
import Logic.True
import Logic.identFromString


object Main extends App{
	import Logic._
	
	
	val f = True
	
	val f2 = 'y & 'x(1,2,3)
	
	val f3 = f2 | 'y 
	
	val f4 = 'x & 'y | ('z -> 'd)
	
	val f5 = (f3 & True) | 'y
	
	val c1 = f + f2 + f3 === 4
	val c2 = f + f2 + f3 < 10
	val c3 = f + f2 + f3 > 11
	val c4 = f + f2 + f3 <= 42
	val c5 = f + f2 + f3 >= 666
	
	val x32 = c5 -> c1
	val x33 = ('x | 'y + 'z >= 2) -> ('z & 'x | 'z & 'y) 
	val x34= (('x | 'y) + 'z >= 2) -> ('z & ('x | 'z) & 'y)	

	// TODO : doit-on permettre cela?
	val x35 = 'x > 0
	
	println(PrettyPrint(f4))
	println(PrettyPrint(f5))
	println(PrettyPrint(c1))
	println(PrettyPrint(c2))
	println(PrettyPrint(c3))
	println(PrettyPrint(c4))
	println(PrettyPrint(c5))
	println(PrettyPrint(x32))
	println(PrettyPrint(x33))
	println(PrettyPrint(x34))
	println(PrettyPrint(x35))
	
	
//	println(PrettyPrint(f4 toCnf ))
	
//	println(PrettyPrint (f5 preprocess))
	
}