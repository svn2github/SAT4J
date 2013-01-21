package org.sat4j.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public class JSONReader<S extends ISolver> extends Reader {

    protected final S solver;

    public static final String CLAUSE = "(\\[(-?(\\d+)(,-?(\\d+))*)?\\])";

    public static final String CARD = "(\\[" + CLAUSE + ",'[=<>]=?',-?\\d+\\])";

    public final String constraint;

    public final String formula;

    private static final Pattern clausePattern = Pattern.compile(CLAUSE);

    private static final Pattern cardPattern = Pattern.compile(CARD);

    private final Pattern constraintPattern;

    public JSONReader(S solver) {
        this.solver = solver;
        constraint = constraintRegexp();
        formula = "^\\[(" + constraint + "(," + constraint + ")*)?\\]$";
        constraintPattern = Pattern.compile(constraint);
    }

    protected String constraintRegexp() {
        return "(" + CLAUSE + "|" + CARD + ")";
    }

    private void handleConstraint(String constraint)
            throws ParseFormatException, ContradictionException {
        if (cardPattern.matcher(constraint).matches()) {
            handleCard(constraint);
        } else if (clausePattern.matcher(constraint).matches()) {
            handleClause(constraint);
        } else {
            handleNotHandled(constraint);
        }
    }

    protected void handleNotHandled(String constraint)
            throws ParseFormatException, ContradictionException {
        throw new ParseFormatException("Unknown constraint: " + constraint);
    }

    private void handleClause(String constraint) throws ParseFormatException,
            ContradictionException {
        solver.addClause(getLiterals(constraint));
    }

    protected IVecInt getLiterals(String constraint)
            throws ParseFormatException {
        String trimmed = constraint.trim();
        trimmed = trimmed.substring(1, trimmed.length() - 1);
        String[] literals = trimmed.split(",");
        IVecInt clause = new VecInt();
        for (String literal : literals) {
            if (literal.length() > 0)
                clause.push(Integer.valueOf(literal.trim()));
        }
        return clause;
    }

    protected void handleCard(String constraint) throws ParseFormatException,
            ContradictionException {
        String trimmed = constraint.trim();
        trimmed = trimmed.substring(1, trimmed.length() - 1);
        Matcher matcher = clausePattern.matcher(trimmed);
        if (matcher.find()) {
            IVecInt clause = getLiterals(matcher.group());
            trimmed = matcher.replaceFirst("");
            String[] str = trimmed.split(",");

            int degree = Integer.valueOf(str[2]);
            String comparator = str[1].substring(1, str[1].length() - 1);
            if ("=".equals(comparator) || ("==".equals(comparator))) {
                solver.addExactly(clause, degree);
            } else if ("<=".equals(comparator)) {
                solver.addAtMost(clause, degree);
            } else if ("<".equals(comparator)) {
                solver.addAtMost(clause, degree - 1);
            } else if (">=".equals(comparator)) {
                solver.addAtLeast(clause, degree);
            } else if (">".equals(comparator)) {
                solver.addAtLeast(clause, degree + 1);
            }
        }
    }

    @Override
    public IProblem parseInstance(InputStream in) throws ParseFormatException,
            ContradictionException, IOException {
        StringWriter out = new StringWriter();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        return parseString(out.toString());
    }

    public ISolver parseString(String json) throws ParseFormatException,
            ContradictionException {
        String trimmed = json.trim();
        if (!trimmed.matches(formula)) {
            throw new ParseFormatException("Wrong input " + json);
        }
        Matcher matcher = constraintPattern.matcher(trimmed);
        while (matcher.find()) {
            handleConstraint(matcher.group());
        }
        return solver;
    }

    @Override
    @Deprecated
    public String decode(int[] model) {
        return "[" + new VecInt(model) + "]";
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        out.print("[");
        out.print(new VecInt(model));
        out.print("]");
    }

}
