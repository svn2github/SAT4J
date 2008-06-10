package org.sat4j.csp.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

class InstanceParser extends DefaultHandler {

	/** stacks of elements visited */
	private Vector<Element> parents = new Vector<Element>();

	/** map which associates tag name to the Element concerns with it */
	private Map<String, Element> theElts;

	public InstanceParser(ICSPCallback cb) {
		theElts = new HashMap<String, Element>();
		theElts.put("instance", new Instance(cb, "instance"));
		theElts.put("presentation", new Presentation(cb, "presentation"));
		theElts.put("domains", new Domains(cb, "domains"));
		theElts.put("domain", new Domain(cb, "domain"));
		theElts.put("variables", new Variables(cb, "variables"));
		theElts.put("variable", new Variable(cb, "variable"));
		theElts.put("relations", new Relations(cb, "relations"));
		theElts.put("relation", new Relation(cb, "relation"));
		theElts.put("predicates", new Predicates(cb, "predicates"));
		theElts.put("predicate", new Predicate(cb, "predicate"));
		theElts.put("parameters", new Parameters(cb, "parameters", this));
		theElts.put("expression", new Expression(cb, "expression"));
		theElts.put("functional", new Functional(cb, "functional"));
		theElts.put("constraints", new Constraints(cb, "constraints"));
		theElts.put("constraint", new Constraint(cb, "constraint"));
		theElts.put("list", new ListOfParameters(cb, "list"));
		theElts.put("cst", new ConstantParameter(cb, "cst"));
	}

	/** Receive notification of character data. $ */
	public void characters(char[] ch, int start, int length) {
		parents.lastElement().characters(new String(ch, start, length));
	}

	/** Receive notification of the end of an element. */
	public void endElement(String uri, String localName, String qName) {
		Element current = theElts.get(qName);
		if (current != null) {
			current.endElement();
			assert current.tagName().equals(parents.lastElement().tagName());
			parents.remove(parents.size() - 1);
		} else
			throw new CSPFormatException(qName + " : undefined tag");
	}

	/** Receive notification of the beginning of an element. */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) {
		Element current = theElts.get(qName);
		if (current != null) {
			parents.add(current);
			current.startElement(atts);
		} else
			throw new CSPFormatException(qName + " : undefined tag");
	}

	public Element getParentElement() {
		if (parents.size() >= 2)
			return parents.get(parents.size() - 2);
		else
			return null;
	}

	/**
	 * Receive notification of a recoverable error.
	 */
	public void error(SAXParseException exception) {
		System.out.println("error");
		System.out.println(exception.getMessage());
		System.out.println(exception);
		System.out.println("Colonne : " + exception.getColumnNumber()
				+ " Ligne : " + exception.getLineNumber());
		System.exit(1);
	}

	/**
	 * Receive notification of a non-recoverable error.
	 */
	public void fatalError(SAXParseException exception) {
		System.out.println("fatalError");
		System.out.println(exception.getMessage());
		System.out.println(exception);
		System.out.println("Colonne : " + exception.getColumnNumber()
				+ " Ligne : " + exception.getLineNumber());
		System.exit(1);
	}

	/**
	 * Receive notification of a warning.
	 */
	public void warning(SAXParseException exception) {
		System.out.println("warning");
		System.out.println(exception.getMessage());
		System.out.println(exception);
		System.out.println("Colonne : " + exception.getColumnNumber()
				+ " Ligne : " + exception.getLineNumber());
	}

}