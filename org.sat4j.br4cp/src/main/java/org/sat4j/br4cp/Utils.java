package org.sat4j.br4cp;

/**
 * Utility classes to easily reuse methods and constants across the code.
 * 
 * @author leberre
 * 
 */
public class Utils {

	public static Valeur extractValeur(String araliaName) {
		int index = araliaName.lastIndexOf('_');
		String version = araliaName.substring(index + 1);
		try {
			int val = Integer.valueOf(version);
			return new Valeur(araliaName.substring(0, index), val);
		} catch (NumberFormatException nfe) {
			return new Valeur(araliaName, -1);
		}
	}

	public static String extractName(String araliaName) {
		int index = araliaName.lastIndexOf('_');
		if (index == -1) {
			return araliaName;
		}
		String version = araliaName.substring(index + 1);
		try {
			Integer.valueOf(version);
			return araliaName.substring(0, index);
		} catch (NumberFormatException nfe) {
			return araliaName;
		}
	}

	public static final int JOKER = 99;
}
