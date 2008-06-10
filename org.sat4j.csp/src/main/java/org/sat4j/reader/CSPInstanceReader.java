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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Locale;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

/**
 * An reader having the responsability to choose the right reader according to
 * the input.
 * 
 * @author leberre
 */
public class CSPInstanceReader extends InstanceReader {

    private CSPReader csp;

    private CSPReader csp2;

    private CSPReader csp3;

    private XMLCSPReader xmlcsp;

    private Reader reader = null;

    private final ISolver solver;

    public CSPInstanceReader(ISolver solver) {
        super(solver);
        this.solver = solver;
    }


    private Reader getCSPReader1() {
        if (csp == null) {
            csp = new CSPReader(solver);
        }
        return csp;
    }

    private Reader getCSPReader2() {
        if (csp2 == null) {
            csp2 = new CSPSupportReader(solver);
        }
        return csp2;
    }

    private Reader getCSPReader3() {
        if (csp3 == null) {
            csp3 = new CSPExtSupportReader(solver);
        }
        return csp3;
    }

    private Reader getXMLCSPReader() {
        if (xmlcsp == null) {
            xmlcsp = new XMLCSPReader(solver);
        }
        return xmlcsp;
    }

 
    @Override
    public IProblem parseInstance(String filename)
            throws FileNotFoundException, ParseFormatException, IOException,
            ContradictionException {
        String fname;
        boolean isHttp = false;
        String tempFileName = "";
        String prefix = "";

        if (filename.startsWith("http://")) {
            isHttp = true;
            tempFileName = filename;
            filename = filename.substring(filename.lastIndexOf('/'), filename
                    .length() - 1);
        }

        if (filename.indexOf(':') != -1) {

            String[] parts = filename.split(":");
            filename = parts[1];
            prefix = parts[0].toUpperCase(Locale.getDefault());

        }

        if (filename.endsWith(".gz")) {
            fname = filename.substring(0, filename.lastIndexOf('.'));
        } else {
            fname = filename;
        }
        if ("CSP".equals(prefix)) {
            reader = getCSPReader1();
        } else if ("CSP3".equals(prefix)) {
            reader = getCSPReader3();
        } else if (fname.endsWith(".txt") || "CSP2".equals(prefix)) {
            reader = getCSPReader2();
        } if (fname.endsWith(".xml")) {
            reader = getXMLCSPReader();
        } else {
            return super.parseInstance(filename);
        }
            
        if (isHttp) {
            return reader.parseInstance((new URL(tempFileName)).openStream());
        }
        return reader.parseInstance(filename);
    }

    @Override
    @Deprecated
    public String decode(int[] model) {
        return reader.decode(model);
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        reader.decode(model, out);
    }

    @Override
    public IProblem parseInstance(java.io.Reader in)
            throws ParseFormatException, ContradictionException, IOException {
        throw new UnsupportedOperationException();
    }
}
