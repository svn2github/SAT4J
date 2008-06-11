package org.sat4j.csp.xml;
public interface ICSPCallback {

	/**
	 * signal the beginning of parsing
	 * 
	 * @param name
	 *            name of the instance
	 */
	void beginInstance(String name);

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the domains declarations
	 * 
	 * @param nbDomains
	 *            number of domains that will be declared
	 */
	void beginDomainsSection(int nbDomains);

	/**
	 * callback called at the beginning of the declaration of one domain
	 * 
	 * @param name
	 *            identifier of the domain
	 * @param nbValue
	 *            number of values in the domain
	 */
	void beginDomain(String name, int nbValue);

	/**
	 * add a single value to the current domain
	 * 
	 * @param v
	 *            value to add to the domain
	 */
	void addDomainValue(int v);

	/**
	 * add the range of values [first..last] to the current domain
	 * 
	 * @param first
	 *            first value to add to the domain
	 * @param last
	 *            last value to add to the domain
	 */
	void addDomainValue(int first, int last);

	/**
	 * ends the definition of the current domain
	 */
	void endDomain();

	/**
	 * end the definition of all domains
	 */
	void endDomainsSection();

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the variables declarations
	 * 
	 * @param nbVariables
	 *            number of variables that will be declared
	 */
	void beginVariablesSection(int nbVariables);

	/**
	 * callback called to define a new variable
	 * 
	 * @param name
	 *            identifier of the variable
	 * @param domain
	 *            identifier of the variable domain
	 */
	void addVariable(String name, String domain);

	/**
	 * end the definition of all variables
	 */
	void endVariablesSection();

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the relations declarations
	 * 
	 * @param nbRelations
	 *            number of relations that will be declared
	 */
	void beginRelationsSection(int nbRelations);

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
	void beginRelation(String name, int arity, int nbTuples, boolean isSupport);

	/**
	 * add a single tuple to the current relation
	 * 
	 * @param tuple
	 *            tuple to add to the relation (contains arity elements)
	 */
	void addRelationTuple(int tuple[]);

	/**
	 * ends the definition of the current relation
	 */
	void endRelation();

	/**
	 * end the definition of all relations
	 */
	void endRelationsSection();

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the predicates declarations
	 * 
	 * @param nbPredicates
	 *            number of predicates that will be declared
	 */
	void beginPredicatesSection(int nbPredicates);

	/**
	 * callback called at the beginning of the declaration of one predicate
	 * 
	 * @param name
	 *            identifier of the predicate
	 */
	void beginPredicate(String name);

	/**
	 * add a formal parameter to the current predicate
	 * 
	 * @param name
	 *            name of the parameter
	 * @param type
	 *            type of the parameter
	 */
	void addFormalParameter(String name, String type);

	/**
	 * provide the expression of the current predicate
	 * 
	 * @param expr
	 *            the abstract syntax tree representing the expression
	 */
	void predicateExpression(String expr);

	/**
	 * ends the definition of the current predicate
	 */
	void endPredicate();

	/**
	 * end the definition of all predicates
	 */
	void endPredicatesSection();

	/** ***************************************************************** */

	/**
	 * callback called at the beginning of the constraints declarations
	 * 
	 * @param nbConstraints
	 *            number of constraints that will be declared
	 */
	void beginConstraintsSection(int nbConstraints);

	/**
	 * callback called at the beginning of the declaration of one constraint
	 * 
	 * @param name
	 *            identifier of the constraint
	 * @param arity
	 *            arity of the constraint
	 */
	void beginConstraint(String name, int arity);

	/**
	 * provide the definition of the current constraint
	 * 
	 * @param name
	 *            the refererence to the definition of this constraint. May be a
	 *            relation, a predicate or the name of a global constraint
	 */
	void constraintReference(String name);

	/**
	 * declares that a variable is in the constraint scope
	 * 
	 * @param name
	 *            name of the variable
	 */
	void addVariableToConstraint(String name);

	/**
	 * add an effective parameter which is a simple variable to the current
	 * constraint
	 * 
	 * @param name
	 *            name of the variable passed as parameter
	 */
	void addEffectiveParameter(String name);

	/**
	 * add an effective parameter which is a simple integer
	 * 
	 * @param value
	 *            value of the parameter
	 */
	void addEffectiveParameter(int value);
	
	/**
	 * begins the list tag for parameters of a constraint
	 */
	void beginParameterList();
	
	/**
	 * provides an integer value in a parameter list of a constraint
	 * @param value
	 * 			value of current list item
	 */
	void addIntegerItem(int value);
	
	/**
	 * provides the name of a variable in a parameter list of a constraint
	 * @param name
	 * 			name of the current list item
	 */
	void addVariableItem(String name);
	
	/**
	 * ends the list tag for parameters of a constraint
	 */
	void endParamaterList();
	
	/**
	 * provides a constant value
	 */
	void addConstantParameter(String name, int value);

	 
	/**
	 * provide the expression of the current constraint as an expression in a
	 * syntac chosen by the solver
	 * 
	 * @param expr
	 *            the expression
	 */
	void constraintExpression(String expr);

	/**
	 * ends the definition of the current constraint
	 */
	void endConstraint();

	/**
	 * end the definition of all constraints
	 */
	void endConstraintsSection();

	/** ***************************************************************** */

	/**
	 * signal the end of parsing
	 */
	void endInstance();
};
