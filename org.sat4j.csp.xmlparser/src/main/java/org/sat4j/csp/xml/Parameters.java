package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

class Parameters extends Element {

	private StringBuilder allParameters;

	private InstanceParser ip;

	public Parameters(ICSPCallback out, String tagName, InstanceParser ip) {
		super(out,tagName);
		this.ip = ip;
	}

	public void startElement(Attributes att) {
		allParameters = new StringBuilder();
	}

	public void characters(String s) {
		allParameters.append(s);
	}

	public void endElement() {
		if (ip.getParentElement().tagName().equals("constraint"))
			effectiveParameters();
		else
			formalParameters();
	}

	private void formalParameters() {
		String[] tokens = allParameters.toString().trim().split("\\s+");
		int i = 0;
		String type;
		String name;
		while (i < tokens.length && !tokens[i].equals("")) {
			type = tokens[i];
			// is the following OK?
			if (!type.equals("int"))
				throw new CSPFormatException(type
						+ " type for parameters not supported");
			i++;
			if (i == tokens.length || tokens[i].equals(""))
				throw new CSPFormatException("a parameter name is missing.");
			name = tokens[i];
			getCB().addFormalParameter(name, type);
			i++;
		}
	}

	private void effectiveParameters() {
		String[] tokens = allParameters.toString().trim().split("\\s+");
		for (String tok : tokens) {
			if (!tok.equals("")) {
				try {
					getCB().addEffectiveParameter(Integer.parseInt(tok));
				} catch (NumberFormatException e) {
					getCB().addEffectiveParameter(tok);
				}
			}
		}
	}
}