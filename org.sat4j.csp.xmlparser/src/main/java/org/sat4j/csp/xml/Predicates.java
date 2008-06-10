package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

class Predicates extends Element {

	public Predicates(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att) {
		getCB().beginPredicatesSection(
				Integer.parseInt(att.getValue("nbPredicates")));
	}

	public void endElement() {
		getCB().endPredicatesSection();
	}

}