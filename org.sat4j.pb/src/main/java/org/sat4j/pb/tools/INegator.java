package org.sat4j.pb.tools;

public interface INegator {

	boolean isNegated(Object thing);

	Object unNegate(Object thing);
}
