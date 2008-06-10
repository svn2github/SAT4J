package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

class Domain extends Element {

	private static final String INTERVAL_SEPARATOR = "..";
	
	private StringBuilder allValues;


	public Domain(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att) {
		int nbValues = -1;
		String tmpValues = att.getValue("nbValues");
		if (tmpValues != null)
			nbValues = Integer.parseInt(tmpValues);
		getCB().beginDomain(att.getValue("name"), nbValues);
		allValues = new StringBuilder();
	}

	public void endElement() {
		if (allValues != null) {
			String[] tokens = allValues.toString().trim().split("\\s+");
			int index;
			for (String token : tokens) {
				if (!token.equals("")) {
					index = token.indexOf(INTERVAL_SEPARATOR);
					if (index > -1)
						getCB().addDomainValue(Integer.parseInt(token.substring(0,index)),
								Integer.parseInt(token.substring(index+2)));
					else
						getCB().addDomainValue(Integer.parseInt(token));
				}
			}
		}
		getCB().endDomain();
	}

	public void characters(String allValues) {
		this.allValues.append(allValues);
	}

}