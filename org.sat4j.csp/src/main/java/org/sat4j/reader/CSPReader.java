/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le
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
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.sat4j.core.Vec;
import org.sat4j.csp.Clausifiable;
import org.sat4j.csp.Constant;
import org.sat4j.csp.Domain;
import org.sat4j.csp.EnumeratedDomain;
import org.sat4j.csp.Evaluable;
import org.sat4j.csp.Predicate;
import org.sat4j.csp.RangeDomain;
import org.sat4j.csp.Var;
import org.sat4j.csp.constraints.AllDiff;
import org.sat4j.csp.constraints.Nogoods;
import org.sat4j.csp.constraints.Relation;
import org.sat4j.csp.constraints.WalshSupports;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;

/**
 * This class is a CSP to SAT translator that is able to read a CSP problem
 * using the First CSP solver competition input format and that translates it
 * into clausal and cardinality (equality) constraints.
 * 
 * That code has not been tested very thoroughly yet and was written very
 * quickly to meet the competition deadline :=)) There is plenty of room for
 * improvement.
 * 
 * @author leberre
 * 
 */
public class CSPReader extends Reader implements org.sat4j.csp.xml.ICSPCallback {

    private final ISolver solver;

    // private Var[] vars;

    protected Relation[] relations;

    private int valueindex;

    // private int varindex;

    private int relindex;

    private int[] currentdomain;

    private Domain rangedomain;

    private String currentdomainid;

    private int currentdomainsize;

    private final Map<String, Domain> domainmapping = new HashMap<String, Domain>();

    private final Map<String, Var> varmapping = new LinkedHashMap<String, Var>();

    private final Map<String, Integer> relmapping = new HashMap<String, Integer>();

    private final Map<String, Clausifiable> predmapping = new HashMap<String, Clausifiable>();

    private int nbvarstocreate;

    private int tupleindex;

    private Clausifiable currentclausifiable;

    private Predicate currentpredicate;

    private final IVec<Evaluable> variables = new Vec<Evaluable>();

    private final IVec<Var> scope = new Vec<Var>();

    private int nbvars;

    private int nbconstraints;

    private int currentconstraint;

    public CSPReader(ISolver solver) {
        this.solver = solver;
        Clausifiable allDiff = new AllDiff(); 
        predmapping.put("global:allDifferent",allDiff); // XCSP 2.0 compatibility
        predmapping.put("global:alldifferent",allDiff);
    }

    @Override
    public final IProblem parseInstance(final java.io.Reader in)
            throws ParseFormatException, ContradictionException, IOException {
        return parseInstance(new LineNumberReader(in));

    }

    private IProblem parseInstance(LineNumberReader in)
            throws ParseFormatException {
        solver.reset();
        try {
            readProblem(in);
            return solver;
        } catch (NumberFormatException e) {
            throw new ParseFormatException("integer value expected on line "
                    + in.getLineNumber(), e);
        }
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        // verifier que les variables sont bien dans le bon ordre !!!!
        for (Var v : varmapping.values()) {
            out.print(v.findValue(model));
            out.print(" ");
        }
    }

    @Override
    public String decode(int[] model) {
        StringBuilder stb = new StringBuilder();
        // verifier que les variables sont bien dans le bon ordre !!!!
        for (Var v : varmapping.values()) {
            stb.append(v.findValue(model));
            stb.append(" ");
        }
        return stb.toString();
    }

    private void readProblem(LineNumberReader in) {
        Scanner input = new Scanner(in);
        // discard problem name
        beginInstance(input.nextLine());

        // read number of domain
        int nbdomain = input.nextInt();
        beginDomainsSection(nbdomain);

        for (int i = 0; i < nbdomain; i++) {
            // read domain number
            int dnum = input.nextInt();
            assert dnum == i;
            // read domain
            int[] domain = readArrayOfInt(input);
            beginDomain("" + dnum, domain.length);
            for (int j = 0; j < domain.length; j++) {
                addDomainValue(domain[j]);
            }
            endDomain();
        }
        endDomainsSection();
        int nbvar = input.nextInt();
        beginVariablesSection(nbvar);
        for (int i = 0; i < nbvar; i++) {
            // read var number
            int varnum = input.nextInt();
            assert varnum == i;
            // read var domain
            int vardom = input.nextInt();
            addVariable("" + varnum, "" + vardom);
        }
        endVariablesSection();

        // relation definition
        // read number of relations
        int nbrel = input.nextInt();
        beginRelationsSection(nbrel);
        for (int i = 0; i < nbrel; i++) {

            // read relation number
            int relnum = input.nextInt();
            assert relnum == i;

            boolean forbidden = input.nextInt() == 1 ? false : true;
            int[] rdomains = readArrayOfInt(input);

            int nbtuples = input.nextInt();

            beginRelation("" + relnum, rdomains.length, nbtuples, !forbidden);

            for (int j = 0; j < nbtuples; j++) {
                int[] tuple = readArrayOfInt(input, relations[relnum].arity());
                // do something with tuple
                addRelationTuple(tuple);
            }

            endRelation();
        }
        endRelationsSection();
        int nbconstr = input.nextInt();
        beginConstraintsSection(nbconstr);
        // constraint definition
        for (int i = 0; i < nbconstr; i++) {
            int[] theVariables = readArrayOfInt(input);
            beginConstraint("" + i, theVariables.length);
            int relnum = input.nextInt();
            // manage constraint
            constraintReference("" + relnum);
            for (int v : theVariables) {
                addEffectiveParameter("" + v);
            }
            endConstraint();
        }
        endConstraintsSection();
        endInstance();
    }

    protected void manageAllowedTuples(int relnum, int arity, int nbtuples) {
        relations[relnum] = new WalshSupports(arity, nbtuples);
    }

    // private Var[] intToVar(int[] variables) {
    // Var[] nvars = new Var[variables.length];
    // for (int i = 0; i < variables.length; i++)
    // nvars[i] = vars[variables[i]];
    // return nvars;
    // }

    private int[] readArrayOfInt(Scanner input) {
        int size = input.nextInt();
        return readArrayOfInt(input, size);
    }

    private int[] readArrayOfInt(Scanner input, int size) {
        int[] tab = new int[size];
        for (int i = 0; i < size; i++)
            tab[i] = input.nextInt();
        return tab;
    }

    public void beginInstance(String arg0) {
        System.out.println("c reading problem named " + arg0);
    }

    public void beginDomainsSection(int nbdomain) {
        System.out.print("c reading domains");
    }

    public void beginDomain(String id, int size) {
        currentdomainsize = size;
        currentdomain = null;
        valueindex = -1;
        currentdomainid = id;
        rangedomain = null;
    }

    public void addDomainValue(int arg0) {
        if (currentdomain == null) {
            currentdomain = new int[currentdomainsize];
        }
        if (rangedomain != null) {
            for (int i = 0; i < rangedomain.size(); i++)
                currentdomain[++valueindex] = rangedomain.get(i);
            rangedomain = null;
        }
        currentdomain[++valueindex] = arg0;
    }

    public void addDomainValue(int begin, int end) {
        if (currentdomainsize == end - begin + 1)
            rangedomain = new RangeDomain(begin, end);
        else {
            if (currentdomain == null) {
                currentdomain = new int[currentdomainsize];
            }
            for (int v = begin; v <= end; v++)
                currentdomain[++valueindex] = v;
        }
    }

    public void endDomain() {
        assert rangedomain != null || valueindex == currentdomain.length - 1;
        if (rangedomain == null)
            domainmapping.put(currentdomainid, new EnumeratedDomain(
                    currentdomain));
        else
            domainmapping.put(currentdomainid, rangedomain);
    }

    public void endDomainsSection() {
        System.out.println(" done.");
    }

    public void beginVariablesSection(int expectedNumberOfVariables) {
        System.out.print("c reading variables");
        nbvarstocreate = 0;
        this.nbvars = expectedNumberOfVariables;
    }

    public void addVariable(String idvar, String iddomain) {
        Domain vardom = domainmapping.get(iddomain);
        varmapping.put(idvar, new Var(idvar, vardom, nbvarstocreate));
        nbvarstocreate += vardom.size();
        if (isVerbose())
            System.out.print("\rc reading variables " + varmapping.size() + "/"
                    + nbvars);

    }

    public void endVariablesSection() {
        if (isVerbose())
            System.out.println("\rc reading variables (" + nbvars + ") done.");
        else
            System.out.println(" done.");
        solver.newVar(nbvarstocreate);
        try {
            for (Evaluable v : varmapping.values()) {
                v.toClause(solver);
            }
        } catch (ContradictionException ce) {
            assert false;
        }

    }

    public void beginRelationsSection(int nbrel) {
        System.out.print("c reading relations");
        relations = new Relation[nbrel];
        relindex = -1;
    }

    public void beginRelation(String name, int arity, int nbTuples,
            boolean isSupport) {
        relmapping.put(name, ++relindex);
        if (isVerbose())
            System.out.print("\rc reading relations " + relindex + "/"
                    + relations.length);
        if (isSupport) {
            manageAllowedTuples(relindex, arity, nbTuples);
        } else {
            relations[relindex] = new Nogoods(arity, nbTuples);
        }
        tupleindex = -1;
    }

    public void addRelationTuple(int[] tuple) {
        relations[relindex].addTuple(++tupleindex, tuple);
    }

    public void endRelation() {
    }

    public void endRelationsSection() {
        if (isVerbose())
            System.out.println("\rc reading relations (" + relations.length
                    + ") done.");
        else
            System.out.println(" done.");
    }

    public void beginPredicatesSection(int arg0) {
        System.out.print("c reading predicates ");
    }

    public void beginPredicate(String name) {
        currentpredicate = new Predicate();
        predmapping.put(name, currentpredicate);
        if (isVerbose())
            System.out.print("\rc reading predicate " + predmapping.size());
    }

    public void addFormalParameter(String name, String type) {
        currentpredicate.addVariable(name);

    }

    public void predicateExpression(String expr) {
        currentpredicate.setExpression(expr);
    }

    public void endPredicate() {
        // TODO Auto-generated method stub
    }

    public void endPredicatesSection() {
        if (isVerbose())
            System.out.println("\rc reading relations (" + predmapping.size()
                    + ") done.");
        else
            System.out.println(" done.");
    }

    public void beginConstraintsSection(int arg0) {
        System.out.println("c reading constraints");
        nbconstraints = arg0;
        currentconstraint = 0;
    }

    public void beginConstraint(String name, int arity) {
        variables.clear();
        variables.ensure(arity);
        scope.clear();
        scope.ensure(arity);
        if (isVerbose())
            System.out.print("\rc grounding constraint " + name + "("
                    + (++currentconstraint * 100 / nbconstraints) + "%)");
    }

    public void constraintReference(String ref) {
        Integer id = relmapping.get(ref);
        if (id == null) {
            currentclausifiable = predmapping.get(ref);
        } else {
            currentclausifiable = relations[id];
        }
        if (currentclausifiable ==null) {
        	throw new IllegalArgumentException("Reference not supported:  "+ref);
        }
    }

    public void addVariableToConstraint(String arg0) {
        scope.push(varmapping.get(arg0));
    }

    public void addEffectiveParameter(String arg0) {
        variables.push(varmapping.get(arg0));
    }

    public void addEffectiveParameter(int arg0) {
        variables.push(new Constant(arg0));
    }

    public void beginParameterList() {
        throw new UnsupportedOperationException(
                "I do not handle parameter list yet!");

    }

    public void addIntegerItem(int arg0) {
        // TODO Auto-generated method stub

    }

    public void addVariableItem(String arg0) {
        // TODO Auto-generated method stub

    }

    public void endParamaterList() {
        // TODO Auto-generated method stub

    }

    public void addConstantParameter(String arg0, int arg1) {
        throw new UnsupportedOperationException(
                "I do not handle constant parameter yet!");
    }

    public void constraintExpression(String arg0) {
        throw new UnsupportedOperationException(
                "I do not handle constraint expression yet!");
    }

    public void endConstraint() {
        try {
            currentclausifiable.toClause(solver, scope, variables);
        } catch (ContradictionException e) {
            System.err.println("c INSTANCE TRIVIALLY UNSAT");
        }
    }

    public void endConstraintsSection() {
        if (isVerbose())
            System.out.println("\rc reading constraints done.");
        else
            System.out.println("c done with constraints.");
    }

    public void endInstance() {
        // TODO Auto-generated method stub
    }

    IProblem getProblem() {
        return solver;
    }
}
