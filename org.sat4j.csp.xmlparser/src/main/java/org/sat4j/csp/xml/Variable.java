package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

import static org.sat4j.csp.xml.TagNames.*;

class Variable extends Element {

    public Variable(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att) {
		getCB().addVariable(att.getValue(NAME), att.getValue(DOMAIN));
	}

}