package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

import static org.sat4j.csp.xml.TagNames.*;

class Variables extends Element {

    public Variables(ICSPCallback out, String tagName) {
		super(out, tagName);
	}

	public void startElement(Attributes att) {
		getCB().beginVariablesSection(
				Integer.parseInt(att.getValue(NB_VARIABLES)));
	}

	public void endElement() {
		getCB().endVariablesSection();
	}

}