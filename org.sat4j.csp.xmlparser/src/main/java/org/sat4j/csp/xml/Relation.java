package org.sat4j.csp.xml;
import org.xml.sax.Attributes;

import static org.sat4j.csp.xml.TagNames.*;

class Relation extends Element {


	private StringBuilder allTuples;

	private int arity;

	public Relation(ICSPCallback out,String tagName) {
		super(out,tagName);
	}

	public void startElement(Attributes att) {
		int nbTuples = -1;
		String tmpTuples = att.getValue(NB_TUPLES);
		if (tmpTuples != null)
			nbTuples = Integer.parseInt(tmpTuples);
		String semantics = att.getValue(SEMANTICS);
		boolean isSupport = (semantics != null && semantics.equals(SUPPORT));
		arity = Integer.parseInt(att.getValue(ARITY));
		getCB().beginRelation(att.getValue(NAME), arity, nbTuples, isSupport);
		// reinitialization of the string for the tuples
		allTuples = new StringBuilder();
	}

	public void characters(String allTuples) {
		this.allTuples.append(allTuples);
	}

	public void endElement() {
		String[] tuples = allTuples.toString().trim().split(TUPLE_SEPARATOR);
		int[] oneTuple;
		for (String tuple : tuples) {
			if (!tuple.equals("")) {
				oneTuple = toIntArray(tuple.split("\\s+"));
				// is the following OK?
//				if (oneTuple.length != arity)
//					throw new CSPFormatException("At least one tuple doesn't have the right arity.");
				getCB().addRelationTuple(oneTuple);
			}
		}
		getCB().endRelation();
	}

	private int[] toIntArray(String[] str) {
		int[] res = new int[str.length];
		for (int i = 0; i < str.length; i++)
			res[i] = Integer.parseInt(str[i]);
		return res;
	}

}