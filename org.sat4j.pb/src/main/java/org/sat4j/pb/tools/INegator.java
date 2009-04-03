package org.sat4j.pb.tools;

public interface INegator<T> {

	boolean isNegated(T thing);

	T unNegate(T thing);
}
