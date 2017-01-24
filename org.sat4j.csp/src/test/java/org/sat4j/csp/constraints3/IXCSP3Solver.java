package org.sat4j.csp.constraints3;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Emmanuel Lonca - lonca@cril.fr
 * 
 * An interface to be implemented in order to run XCSP3 test cases on a solver.
 * The methods are all about returning models of XCSP3 instances.
 * 
 * Be careful at respecting the expected model format.
 * 
 * Models are given as string composed of values split by space characters.
 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
 * All declared variables must be involved in the models. Models must be returned in the alphabetical order.
 */
public interface IXCSP3Solver {
	
	static IXCSP3Solver newSolver(Class<? extends IXCSP3Solver> cl) {
		Constructor<? extends IXCSP3Solver> constructor;
		IXCSP3Solver instance = null;
		try {
			constructor = cl.getConstructor();
			instance = constructor.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return instance;
	}
	
	/**
	 * Computes the whole set of models a decision instance admits.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models. Models must be returned in the alphabetical order.
	 * 
	 * @param instance the instance
	 * @return the sorted set of models the instance admits
	 */
	List<String> computeModels(String instance);
	
	/**
	 * Computes the whole set of models a decision instance admits.
	 * The instance is given by its variable and constraint sections.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models. Models must be returned in the alphabetical order.
	 * 
	 * @param varSection the instance variable section
	 * @param ctrSection the instance constraint section
	 * @return the sorted set of models the instance admits
	 */
	default List<String> computeModels(String varSection, String ctrSection) {
		return computeModels(TestUtils.buildInstance(varSection, ctrSection));
	}
	
	/**
	 * Computes the whole set of models an optimization instance admits.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models. Models must be returned in the alphabetical order.
	 * 
	 * @param instance the instance
	 * @return the sorted set of models the instance admits
	 */
	List<String> computeOptimalModels(String instance);
	
	/**
	 * Computes the whole set of models an optimization instance admits.
	 * The instance is given by its variable, constraint and objective sections.
	 * Models are given as string composed of values split by space characters.
	 * Each value corresponds to a variable ; the values must be given in the order the variables have been declared in the instance.
	 * All declared variables must be involved in the models. Models must be returned in the alphabetical order.
	 * 
	 * @param varSection the instance variable section
	 * @param ctrSection the instance constraint section
	 * @return the sorted set of models the instance admits
	 */
	default List<String> computeOptimalModels(String varSection, String ctrSection, String objSection) {
		return computeOptimalModels(TestUtils.buildInstance(varSection, ctrSection, objSection));
	}

}
