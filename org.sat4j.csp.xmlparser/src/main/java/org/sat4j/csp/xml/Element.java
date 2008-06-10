package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

abstract class Element {

	private final ICSPCallback out;
	
	private final String tagName;

	public Element(ICSPCallback out, String tagName) {
		this.out = out;
		this.tagName = tagName;
	}

	public ICSPCallback getCB() {
		return out;
	}

	public void startElement(Attributes attributes) {
	}

	public void characters(String s) {
	}

	public void endElement() {
	}
	
	public String getContext(){
		return "";
	}

	public String tagName(){
		return tagName;
	}
}