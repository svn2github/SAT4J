package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

import static org.sat4j.csp.xml.TagNames.NB_DOMAINS;

class Domains extends Element {

    public Domains(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att){
		getCB().beginDomainsSection(Integer.parseInt(att.getValue(NB_DOMAINS)));
	}

	public void endElement() {
		getCB().endDomainsSection();
	}

}