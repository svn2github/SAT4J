package org.sat4j.pb.tools;

public class StringNegator implements INegator {

	public static final INegator instance = new StringNegator();

	private StringNegator() {
		// no access to constructor
	}

	public boolean isNegated(Object thing) {
		if (thing instanceof String)
			return ((String) thing).startsWith("-");
		return false;
	}

	public Object unNegate(Object thing) {
		if (isNegated(thing))
			return ((String) thing).substring(1);
		return thing;
	}
}
