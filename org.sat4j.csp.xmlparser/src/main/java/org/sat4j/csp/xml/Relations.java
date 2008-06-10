package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

import static org.sat4j.csp.xml.TagNames.*;

class Relations extends Element {


    public Relations(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att) {
		getCB().beginRelationsSection(
				Integer.parseInt(att.getValue(NB_RELATIONS)));
	}

	public void endElement() {
		getCB().endRelationsSection();
	}

}