import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.sat4j.csp.xml.CspXmlParser;
import org.xml.sax.SAXException;

public class Test {

	public static void main(String[] args) throws ParserConfigurationException  {

		try {
			CspXmlParser.parse(new SimpleCallback(),args[0]);
		} catch (IOException e) {
			System.out.println("pbe d'entree-sortie");
			return;
		} catch (SAXException e) {
			System.out.println(e.getMessage());
			System.out.println("pbe du parseur");
			return;
		}

	}

}
