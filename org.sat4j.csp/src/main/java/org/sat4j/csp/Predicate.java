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
package org.sat4j.csp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.sat4j.core.Vec;
import org.sat4j.csp.encodings.BinarySupportEncoding;
import org.sat4j.csp.encodings.DirectEncoding;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;

/**
 * A predicate is a formula given in intension.
 * 
 * @author daniel
 */
public class Predicate implements Clausifiable {

    private String expr;

    private Encoding encoding;

    private final IVec<String> variables = new Vec<String>();

    private static Context cx;

    private static Scriptable scope;

    static {
        cx = Context.enter(); 
        scope = cx.initStandardObjects();
        try {
            URL url = Predicate.class.getResource("predefinedfunctions.js");
            cx.evaluateReader(scope, new InputStreamReader(url.openStream()),
                    "predefinedfunctions.js", 1, null);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public Predicate() {
    }

    public void setExpression(String expr) {
        this.expr = expr.replaceAll("if\\(", "ite(");
    }

    public void addVariable(String name) {
        variables.push(name);
    }

    private boolean evaluate(int[] values) {
        assert values.length == variables.size();
        for (int i = 0; i < variables.size(); i++) {
            scope.put(variables.get(i), scope, values[i]);
        }
        Object result = myscript.exec(cx, scope);
        return Context.toBoolean(result);
    }

    public void toClause(ISolver solver, IVec<Var> vscope, IVec<Evaluable> vars)
            throws ContradictionException {
        if (myscript == null) {
            myscript = cx.compileString(expr, "rhino.log", 1, null);
        }
        if (vscope.size() == 2) {
            encoding = BinarySupportEncoding.instance();
        } else {
            encoding = DirectEncoding.instance();
        }
        encoding.onInit(solver, vscope);
        int[] tuple = new int[vars.size()];
        valuemapping.clear();
        find(tuple, 0, vscope, vars, solver);
        encoding.onFinish(solver, vscope);
    }

    private final Map<Evaluable, Integer> valuemapping = new HashMap<Evaluable, Integer>();

    private Script myscript;

    private void find(int[] tuple, int n, IVec<Var> theScope,
            IVec<Evaluable> vars, ISolver solver) throws ContradictionException {
        if (valuemapping.size() == theScope.size()) {
            for (int i = 0; i < tuple.length; i++) {
                Evaluable ev = vars.get(i);
                Integer value = valuemapping.get(ev);
                if (value == null) {
                    tuple[i] = ev.domain().get(0);
                } else {
                    tuple[i] = value;
                }
            }
            if (evaluate(tuple)) {
                encoding.onSupport(solver, theScope, valuemapping);
            } else {
                encoding.onNogood(solver, theScope, valuemapping);
            }
        } else {
            Var var = theScope.get(n);
            Domain domain = var.domain();
            for (int i = 0; i < domain.size(); i++) {
                valuemapping.put(var, domain.get(i));
                find(tuple, n + 1, theScope, vars, solver);
            }
            valuemapping.remove(var);
        }
    }
}
