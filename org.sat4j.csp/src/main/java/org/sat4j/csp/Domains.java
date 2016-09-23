/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2016 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
*******************************************************************************/
package org.sat4j.csp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A class based on the singleton DP to globally store the domains defined in a CSP problem.
 * Mappings are used to prevent defining several equivalent domains.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 *
 */
public class Domains {
	
	/** the singleton instance */
	private static Domains instance;
	
	/** a mapping used to store "full domains", that is domains that contain all integer values between their bounds */
	private Map<Integer, Map<Integer, Domain>> fullDomains = new HashMap<Integer, Map<Integer, Domain>>();
	
	/** a mapping used to store "enum domains", that is domains built from set of values which are not "full domains" */
	private Map<Integer, Domain> enumDomains = new HashMap<Integer, Domain>();
	
	private Domains() {
		// singleton DP; prevents public instantiation
	}
	
	/**
	 * Gets the singleton instance.
	 * 
	 * @return the (only) {@link Domains} instance
	 */
	public static Domains getInstance() {
		if(instance == null) {
			instance = new Domains();
		}
		return instance;
	}

	/**
	 * Returns a new domain containing all the values from the range provided by the two bounds parameters.
	 * If an equivalent domain has already been created by this method, the old domain is returned instead of a new one.
	 * 
	 * @param minValue the "full domain" minimal bound 
	 * @param maxValue the "full domain" maximal bound
	 * @return a "full domain" containing all the values from the range bounded by the parameters
	 */
	public Domain getDomain(int minValue, int maxValue) {
		Map<Integer, Domain> fromMin = fullDomains.get(minValue);
		if(fromMin == null) {
			fromMin = new HashMap<Integer, Domain>();
			fullDomains.put(minValue, fromMin);
		}
		Domain dom = fromMin.get(maxValue);
		if(dom == null) {
			dom = new RangeDomain(minValue, maxValue);
			fromMin.put(maxValue, dom);
		}
		return dom;
	}

	/**
	 * Returns a new domain containing the values provided in the array parameter.
	 * If an equivalent domain has already been created by this method, the old domain is returned instead of a new one.
	 * 
	 * @param values the values to the domain
	 * @return an "enum domain" containing the values provided by the parameter
	 */
	public Domain getDomain(int[] values) {
		Arrays.sort(values);
		int hashCode = Arrays.hashCode(values);
		Domain dom = enumDomains.get(hashCode);
		if(dom == null) {
			dom = new EnumeratedDomain(values);
			enumDomains.put(hashCode, dom);
		}
		return dom;
	}

}
