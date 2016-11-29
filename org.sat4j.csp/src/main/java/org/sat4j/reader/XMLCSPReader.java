/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
*******************************************************************************/
package org.sat4j.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.sat4j.csp.xml.CspXmlParser;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.xml.sax.SAXException;

public class XMLCSPReader extends org.sat4j.reader.Reader {

    private final CSPReader cspreader;

    public XMLCSPReader(ISolver solver, boolean allDiffCard) {
        cspreader = new CSPSupportReader(solver,allDiffCard);
    }

    @Override
    public String decode(int[] model) {
        return cspreader.decode(model);
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        cspreader.decode(model, out);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.Reader#parseInstance(java.lang.String)
     */
    @Override
    public IProblem parseInstance(String filename)
            throws ParseFormatException, IOException,
            ContradictionException {
        try {
            CspXmlParser.parse(cspreader, filename);
        } catch (SAXException e) {
            throw new ParseFormatException(e);
        } catch (ParserConfigurationException e) {
            throw new ParseFormatException(e);
        }
        return cspreader.getProblem();
    }

    @Override
    public IProblem parseInstance(Reader in) throws ParseFormatException,
            ContradictionException, IOException {
        throw new UnsupportedOperationException();
    }

	@Override
	public IProblem parseInstance(final InputStream in)
			throws ParseFormatException, ContradictionException, IOException {
		return parseInstance(new InputStreamReader(in));
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.Reader#setVerbosity(boolean)
     */
    @Override
    public void setVerbosity(boolean b) {
        super.setVerbosity(b);
        cspreader.setVerbosity(b);
    }

	public boolean hasAMapping() {
		return cspreader.hasAMapping();
	}

	public Map<Integer, String> getMapping() {
		return cspreader.getMapping();
	}

}
