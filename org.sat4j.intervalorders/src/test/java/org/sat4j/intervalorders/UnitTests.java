package org.sat4j.intervalorders;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnitTests {

	@Test
	public void testEquality() {
		Indifferent i1 = new Indifferent("Toto", "Titi");
		Indifferent i2 = new Indifferent("Toto", "Titi");
		assertEquals(i1.hashCode(), i2.hashCode());
		assertEquals(i1, i2);
	}
}
