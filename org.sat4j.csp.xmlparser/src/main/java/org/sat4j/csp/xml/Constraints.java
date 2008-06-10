package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

import static org.sat4j.csp.xml.TagNames.NB_CONSTRAINTS;

class Constraints extends Element {


    public Constraints(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att){
		getCB().beginConstraintsSection(
				Integer.parseInt(att.getValue(NB_CONSTRAINTS)));
	}

	public void endElement() {
		getCB().endConstraintsSection();
	}

}