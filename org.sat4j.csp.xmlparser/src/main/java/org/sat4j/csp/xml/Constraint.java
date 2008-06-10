package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

import static org.sat4j.csp.xml.TagNames.*;

class Constraint extends Element {

    public Constraint(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att){
		getCB().beginConstraint(att.getValue(NAME), Integer.parseInt(att.getValue(ARITY)));
		String ref = att.getValue(REFERENCE);
		if (ref != null)
			getCB().constraintReference(ref);
		String scope = att.getValue(SCOPE);
		if (scope != null){
			// traitement des variables dans le scope
			String[] tokens = scope.trim().split("\\s+");
			for(String tok : tokens)
				if (!tok.equals(""))
					getCB().addVariableToConstraint(tok);	
		}
		context = "Constraint";
	}

	public void endElement() {
		getCB().endConstraint();
		context = "";
	}

	private String context;
	
	public String getContexte(){
		return context;
	}

	
}