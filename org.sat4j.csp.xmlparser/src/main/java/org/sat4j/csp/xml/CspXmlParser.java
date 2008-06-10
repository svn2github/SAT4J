package org.sat4j.csp.xml;

import java.io.FileNotFoundException;
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
			throws SAXException, ParserConfigurationException,
			FileNotFoundException, IOException {
		// le producteur
		XMLReader prod;
		// le consommateur
		DefaultHandler cons;

		// obtenir un parser
		SAXParserFactory saxpf = SAXParserFactory.newInstance();
		// associer un schema pour la validation
		URL url = CspXmlParser.class.getResource("/instance_2_0.xsd");
		// System.out.println(url);
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
