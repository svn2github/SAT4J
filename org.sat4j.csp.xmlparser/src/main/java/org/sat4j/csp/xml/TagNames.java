package org.sat4j.csp.xml;

interface TagNames {
    // From Constraint
    String SCOPE = "scope";
    String REFERENCE = "reference";
    String ARITY = "arity";
    String NAME = "name";

    // From Constraints
    String NB_CONSTRAINTS = "nbConstraints";

    // From Domains
    String NB_DOMAINS = "nbDomains";

    // From relation (+ ARITY and NAME)
    String SUPPORT = "supports";
    String CONFLICT = "conflicts";
    String SEMANTICS = "semantics";
    String NB_TUPLES = "nbTuples";
    String TUPLE_SEPARATOR = "\\|";

    // From relations
    String NB_RELATIONS = "nbRelations";
    
    // From variable (+ NAME)
    String DOMAIN = "domain";
    
    // From variables
    String NB_VARIABLES = "nbVariables";
}
