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

public class JSONReader extends Reader {

    private final ISolver solver;

    private static final String CLAUSE = "(\\[(-?(\\d+)(,-?(\\d+))*)?\\])";

    private static final String CARD = "(\\[" + CLAUSE
            + ",'[=<>]=?',-?\\d+\\])";

    private static final String CONSTRAINT = "(" + CLAUSE + "|" + CARD + ")";

    private static final String FORMULA = "^\\[(" + CONSTRAINT + "(,"
            + CONSTRAINT + ")*)?\\]$";

    private static final Pattern clause = Pattern.compile(CLAUSE);

    private static final Pattern card = Pattern.compile(CARD);

    public JSONReader(ISolver solver) {
        this.solver = solver;
    }

    private void handleClause(String constraint) throws ParseFormatException,
            ContradictionException {
        solver.addClause(getLiterals(constraint));
    }

    private IVecInt getLiterals(String constraint) throws ParseFormatException {
        String trimmed = constraint.trim();
        if (!(trimmed.charAt(0) == '[')
                || !(trimmed.charAt(trimmed.length() - 1) == ']')) {
            throw new ParseFormatException("Wrong clause " + constraint);
        }
        trimmed = trimmed.substring(1, trimmed.length() - 1);
        String[] literals = trimmed.split(",");
        IVecInt clause = new VecInt();
        for (String literal : literals) {
            if (literal.length() > 0)
                clause.push(Integer.valueOf(literal.trim()));
        }
        return clause;
    }

    private void handleCard(String constraint) throws ParseFormatException,
            ContradictionException {
        String trimmed = constraint.trim();
        if (!(trimmed.charAt(0) == '[')
                || !(trimmed.charAt(trimmed.length() - 1) == ']')) {
            throw new ParseFormatException("Wrong card " + constraint);
        }
        trimmed = trimmed.substring(1, trimmed.length() - 1);
        Matcher matcher = clause.matcher(trimmed);
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
        if (!trimmed.matches(FORMULA)) {
            throw new ParseFormatException("Wrong input " + json);
        }

        Matcher matcher = card.matcher(trimmed);
        while (matcher.find()) {
            handleCard(matcher.group());
        }
        trimmed = matcher.replaceAll("");
        matcher = clause.matcher(trimmed);
        while (matcher.find()) {
            handleClause(matcher.group());
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
