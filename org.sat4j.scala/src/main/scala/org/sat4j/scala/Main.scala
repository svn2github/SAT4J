package org.sat4j.scala

import Logic.PrettyPrint
import Logic.True
import Logic.identFromString


object Main extends App{
	import Logic._
	
	
	val f = True
	
	val f2 = 'y & 'x
	
	val f3 = f2 | 'y 
	
	val f4 = 'x & 'y | ('z -> 'd)
	
	val f5 = (f3 & True) | 'y
	
	println(PrettyPrint(f4))
	
	println(PrettyPrint(f4))
	
//	println(PrettyPrint(f4 toCnf ))
	
//	println(PrettyPrint (f5 preprocess))
	
}