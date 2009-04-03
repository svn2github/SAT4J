package org.sat4j.pb.tools;


public class StringNegator implements INegator<String> {

	public static final INegator<String> instance = new StringNegator();

	private StringNegator() {
		// no access to constructor
	}

	public boolean isNegated(String thing) {
		return thing.startsWith("-");
	}

	public String unNegate(String thing) {
		if (isNegated(thing))
			return thing.substring(1);
		return thing;
	}
}
