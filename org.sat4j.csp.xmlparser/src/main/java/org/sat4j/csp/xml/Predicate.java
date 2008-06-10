package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

class Predicate extends Element {

	public Predicate(ICSPCallback out, String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att) {
		getCB().beginPredicate(att.getValue("name"));
		context = "Predicate";
	}

	public void endElement() {
		context = "";
		getCB().endPredicate();
	}

	private String context;
	
	public String getContext(){
		return context;
	}
	
}