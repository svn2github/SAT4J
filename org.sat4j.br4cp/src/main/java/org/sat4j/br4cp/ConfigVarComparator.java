package org.sat4j.br4cp;

import java.util.Comparator;

public class ConfigVarComparator implements Comparator<String> {

	public int compare(String arg0, String arg1) {
		String[] t1 = arg0.split("[_=]");
		String[] t2 = arg1.split("[_=]");
		for (int i = 0; i < Math.min(t1.length, t2.length); ++i) {
			boolean firstIsInteger = false, secondIsInteger = false;
			Integer n1 = null, n2 = null;
			try {
				n1 = Integer.valueOf(t1[i]);
				firstIsInteger = true;
			} catch (NumberFormatException e) {
			}
			try {
				n2 = Integer.valueOf(t2[i]);
				secondIsInteger = true;
			} catch (NumberFormatException e) {
			}
			if (firstIsInteger != secondIsInteger) {
				return (firstIsInteger) ? (-1) : (1);
			}
			if (firstIsInteger && secondIsInteger) {
				if (n1.equals(n2)) {
					continue;
				} else {
					return n1.compareTo(n2);
				}
			}
			firstIsInteger = false;
			secondIsInteger = false;
			try {
				n1 = Integer.valueOf(t1[i].replaceAll("[a-zA-Z]", ""));
				firstIsInteger = true;
			} catch (NumberFormatException e) {
			}
			try {
				n2 = Integer.valueOf(t2[i].replaceAll("[a-zA-Z]", ""));
				secondIsInteger = true;
			} catch (NumberFormatException e) {
			}
			if (firstIsInteger != secondIsInteger) {
				return (firstIsInteger) ? (-1) : (1);
			}
			if (firstIsInteger && secondIsInteger) {
				if (n1.equals(n2)) {
					continue;
				} else {
					return n1.compareTo(n2);
				}
			}
			if (t1[i].equals(t2[i])) {
				continue;
			}
			return t1[i].compareTo(t2[i]);
		}
		return t1.length - t2.length;
	}

}
