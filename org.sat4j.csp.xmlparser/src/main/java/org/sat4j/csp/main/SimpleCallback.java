/**
 * Copyright (c) 2008 Olivier ROUSSEL (olivier.roussel <at> cril.univ-artois.fr)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sat4j.csp.main;
import org.sat4j.csp.xml.ICSPCallback;

public class SimpleCallback implements ICSPCallback {

	/**
	 * signal the beginning of parsing
	 * 
	 * @param name
	 *            name of the instance
	 */
	public void beginInstance(String name) {
		System.out.println("begin instance : " + name);
	}

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the domains declarations
	 * 
	 * @param nbDomains
	 *            number of domains that will be declared
	 */
	public void beginDomainsSection(int nbDomains) {
		System.out.println("begin domain section - number of domains : "
				+ nbDomains);
	}

	/**
	 * callback called at the beginning of the declaration of one domain
	 * 
	 * @param name
	 *            identifier of the domain
	 * @param nbValue
	 *            number of values in the domain
	 */
	public void beginDomain(String name, int nbValue) {
		System.out.println("begin domain : " + name + " - number of values : "
				+ nbValue);
	}

	/**
	 * add a single value to the current domain
	 * 
	 * @param v
	 *            value to add to the domain
	 */
	public void addDomainValue(int v) {
		//System.out.println("value : " + v);
	}

	/**
	 * add the range of values [first..last] to the current domain
	 * 
	 * @param first
	 *            first value to add to the domain
	 * @param last
	 *            last value to add to the domain
	 */
	public void addDomainValue(int first, int last) {
		//System.out.println("values between " + first + " and " + last);
	}

	/**
	 * ends the definition of the current domain
	 */
	public void endDomain() {
		System.out.println("end domain");
	}

	/**
	 * end the definition of all domains
	 */
	public void endDomainsSection() {
		System.out.println("end domain section");
	}

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the variables declarations
	 * 
	 * @param nbVariables
	 *            number of variables that will be declared
	 */
	public void beginVariablesSection(int nbVariables) {
		System.out.println("begin variables section - number of variables : "
				+ nbVariables);
	}

	/**
	 * callback called to define a new variable
	 * 
	 * @param name
	 *            identifier of the variable
	 * @param domain
	 *            identifier of the variable domain
	 */
	public void addVariable(String name, String domain) {
		//System.out.println("variable - name : " + name + " - domain :" + domain);
	}

	/**
	 * end the definition of all variables
	 */
	public void endVariablesSection() {
		System.out.println("end variables section");
	}

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the relations declarations
	 * 
	 * @param nbRelations
	 *            number of relations that will be declared
	 */
	public void beginRelationsSection(int nbRelations) {
		System.out.println("begin relations section - number of relations : "
				+ nbRelations);
	}

	/**
	 * callback called at the beginning of the declaration of one relation
	 * 
	 * @param name
	 *            identifier of the relation
	 * @param arity
	 *            arity of the relation
	 * @param nbTuples
	 *            number of tuples in the relation
	 * @param isSupport
	 *            true if tuples represent support, false if tuples represent
	 *            conflicts
	 */
	public void beginRelation(String name, int arity, int nbTuples,
			boolean isSupport) {
//		System.out.println("relation - name : " + name + " - arity : " + arity
//				+ " - number of tuples : " + nbTuples
//				+ (isSupport ? " - support" : " - conflict"));
	}

	/**
	 * add a single tuple to the current relation
	 * 
	 * @param tuple
	 *            tuple to add to the relation (contains arity elements)
	 */
	public void addRelationTuple(int tuple[]) {
//		System.out.print("tuple : ");
//		for (int i : tuple)
//			System.out.print(i+" ");
//		System.out.println();
	}

	/**
	 * ends the definition of the current relation
	 */
	public void endRelation() {
//		System.out.println("end relation");
	}

	/**
	 * end the definition of all relations
	 */
	public void endRelationsSection() {
		System.out.println("end relations section");
	}

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the predicates declarations
	 * 
	 * @param nbPredicates
	 *            number of predicates that will be declared
	 */
	public void beginPredicatesSection(int nbPredicates) {
		System.out.println("begin predicates section - number of predicates : "
				+ nbPredicates);
	}

	/**
	 * callback called at the beginning of the declaration of one predicate
	 * 
	 * @param name
	 *            identifier of the predicate
	 */
	public void beginPredicate(String name) {
		System.out.println("predicate - name : " + name);
	}

	/**
	 * add a formal parameter to the current predicate
	 * 
	 * @param name
	 *            name of the parameter
	 * @param type
	 *            type of the parameter
	 */
	public void addFormalParameter(String name, String type) {
		System.out.println("parameter - name : " + name + " - type : " + type);
	}

	/**
	 * provide the expression of the current predicate
	 * 
	 * @param expression
	 *            the abstract syntax tree representing the expression
	 */
	public void predicateExpression(String expression) {
		System.out.println("predicate expression : " + expression);
	}

	/**
	 * ends the definition of the current predicate
	 */
	public void endPredicate() {
		System.out.println("end predicate");
	}

	/**
	 * end the definition of all predicates
	 */
	public void endPredicatesSection() {
		System.out.println("end predicates section");
	}

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the constraints declarations
	 * 
	 * @param nbConstraints
	 *            number of constraints that will be declared
	 */
	public void beginConstraintsSection(int nbConstraints) {
		System.out
				.println("begin constraints section - number of constraints : "
						+ nbConstraints);
	}

	/**
	 * callback called at the beginning of the declaration of one constraint
	 * 
	 * @param name
	 *            identifier of the constraint
	 * @param arity
	 *            arity of the constraint
	 */
	public void beginConstraint(String name, int arity) {
		System.out
				.println("constraint - name : " + name + " - arity : " + arity);
	}

	/**
	 * provide the definition of the current constraint
	 * 
	 * @param name
	 *            the refererence to the definition of this constraint. May be a
	 *            relation, a predicate or the name of a global constraint
	 */
	public void constraintReference(String name) {
		System.out.println("reference :" + name);
	}

	/**
	 * declares that a variable is in the constraint scope
	 * 
	 * @param name
	 *            name of the variable
	 */
	public void addVariableToConstraint(String name) {
		System.out.println("var : " + name);
	}

	/**
	 * add an effective parameter which is a simple variable to the current
	 * constraint
	 * 
	 * @param name
	 *            name of the variable passed as parameter
	 */
	public void addEffectiveParameter(String name) {
		System.out.println("param : " + name);
	}

	/**
	 * add an effective parameter which is a simple variable to the current
	 * constraint
	 * 
	 * @param value
	 *            name of the variable passed as parameter
	 */
	public void addEffectiveParameter(int value) {
		System.out.println("param : " + value);
	}

	/**
	 * provide the expression of the current constraint as an expression in a
	 * syntac chosen by the solver
	 * 
	 * @param expr
	 *            the expression
	 */
	public void constraintExpression(String expr) {
		System.out.println("contraint expression : " + expr);
	}

	/**
	 * ends the definition of the current constraint
	 */
	public void endConstraint() {
		System.out.println("end constraint");
	}

	/**
	 * end the definition of all constraints
	 */
	public void endConstraintsSection() {
		System.out.println("end constraints section");
	}
	
	/**
	 * begins the list tag for parameters of a constraint
	 */
	public void beginParameterList(){}
	
	/**
	 * provides an integer value in a parameter list of a constraint
	 * @param value
	 * 			value of current list item
	 */
	public void addIntegerItem(int value){}
	
	/**
	 * provides the name of a variable in a parameter list of a constraint
	 * @param name
	 * 			name of the current list item
	 */
	public void addVariableItem(String name){}
	
	/**
	 * ends the list tag for parameters of a constraint
	 */
	public void endParamaterList(){}
	
	/**
	 * provides a constant value
	 */
	public void addConstantParameter(String name, int value){}



	/** ***************************************************************** */

	/**
	 * signal the end of parsing
	 */
	public void endInstance() {
		System.out.println("end instance");
	}

}