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
* 
* Based on the pseudo boolean algorithms described in:
* A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
* Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
* Volume 24, Issue 3, March 2005 Page(s): 305 - 317
* 
* and 
* Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
* Framework. Ph.D. Dissertation, University of Oregon.
*******************************************************************************/
package org.sat4j.pb.reader;

import java.io.IOException;
import java.text.ParseException;

import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.IVecInt;

/**
 * @author anne
 *
 */
public class OPBEclipseReader2007 extends OPBReader2007 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IVecInt varExplain = new VecInt();
	/**
	 * @param solver
	 */
	public OPBEclipseReader2007(IPBSolver solver) {
		super(solver);
	}

    /**
     * callback called before we read the list for variables explanation
     */
    protected void beginListOfVariables() {
    }

    /**
     * callback called after we've read the list for variable explanation
     */
    protected void endListOfVariables() {
    }

    /**
     * read the list for variables explanation (if any) calls beginListOfVariables and
     * endListOfVariables
     * 
     * @throws IOException
     * @throws ParseException
     */
    @Override
	protected void readVariablesExplanation()  throws IOException, ParseFormatException{
        char c;
        StringBuffer var = new StringBuffer();

        // read variables line (if any)
        // if the problem is unsatisfiable, and if these variables are part of the reason of the conflict
        // then an explanation should be produced for these variables

        skipSpaces();
        c = get();
        if (c != 'e') {
            // no variables line
            putback(c);
            return;
        }

        hasVariablesExplanation = true;
        if (get() == 'x' && get() == 'p' && get() == 'l' && get() == 'a' && get() == 'i' && get() == 'n' && get() == ':') {
            beginListOfVariables(); // callback

            while (!eof()) {
				readIdentifier(var);
				varExplain.push(translateVarToId(var.toString()));
				
                skipSpaces();
                c = get();
                if (c == ';')
                    break; // end of list of variables
                putback(c);
            }

            endListOfVariables();
        } else
            throw new ParseFormatException(
                    "input format error: 'explain:' expected");

	}

    @Override
	public IVecInt getListOfVariables() {
        if (hasVariablesExplanation){
        	IVecInt tmp = new VecInt();
            varExplain.moveTo(tmp);
            return tmp;
        }
        return null;
    }

    
}
