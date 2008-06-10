package org.sat4j.csp.xml;

class Instance extends Element {

	public Instance(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void endElement() {
		getCB().endInstance();
	}

}
