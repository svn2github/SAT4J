package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

class Presentation extends Element {

	public Presentation(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att) {
		getCB().beginInstance(att.getValue("name"));
	}

}