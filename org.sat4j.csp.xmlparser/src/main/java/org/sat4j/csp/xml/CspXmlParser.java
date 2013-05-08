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

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class CspXmlParser {

	public static void parse(ICSPCallback callback, String filename)
			throws SAXException, ParserConfigurationException, IOException {
		// le producteur
		XMLReader prod;
		// le consommateur
		DefaultHandler cons;

		// obtenir un parser
		SAXParserFactory saxpf = SAXParserFactory.newInstance();
		// associer un schema pour la validation
		URL url = CspXmlParser.class.getResource("/instance_2_0.xsd");
		// System.out.println(url);
		if (url == null) {
			throw new IllegalStateException(
					"Cannot locate schema file instance_2_0.xsd");
		}
		saxpf.setSchema(SchemaFactory.newInstance(
				javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(url));
		prod = saxpf.newSAXParser().getXMLReader();

		cons = new InstanceParser(callback);
		prod.setContentHandler(cons);
		prod.setErrorHandler(cons);

		// et c'est parti!
		prod.parse(new InputSource(new FileReader(filename)));
	}
}
