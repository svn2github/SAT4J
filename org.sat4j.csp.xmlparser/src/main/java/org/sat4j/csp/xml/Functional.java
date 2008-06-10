package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

class Functional extends Element {
	
	private StringBuilder expr;

	public Functional(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes attributes) {
		expr = new StringBuilder();
	}

	public void characters(String s) {
		expr.append(s);
	}

	public void endElement() {
		getCB().predicateExpression(expr.toString().trim());
	}


}