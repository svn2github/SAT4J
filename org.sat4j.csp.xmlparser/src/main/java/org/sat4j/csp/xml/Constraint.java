/**
 * Copyright (c) 2008 Olivier ROUSSEL (olivier.roussel <at> cril.univ-artois.fr)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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