package org.sat4j.pb.reader;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.reader.JSONReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class JSONPBReader extends JSONReader<IPBSolver> {
	public static final String WLITERAL = "\\[(-?\\d+),(-?\\d+)\\]";
	public static final String WCLAUSE = "(\\[(" + WLITERAL + "(," + WLITERAL
			+ ")*)?\\])";
	public static final String PB = "(\\[" + WCLAUSE + ",'[=<>]=?',-?\\d+\\])";

	public static final String OBJ = "(\\[('min'|'max')," + WCLAUSE + "\\])";

	public static final Pattern pseudo = Pattern.compile(PB);
	public static final Pattern wclause = Pattern.compile(WCLAUSE);
	public static final Pattern wliteral = Pattern.compile(WLITERAL);
	public static final Pattern obj = Pattern.compile(OBJ);

	public JSONPBReader(IPBSolver solver) {
		super(solver);
	}

	@Override
	protected void handleNotHandled(String constraint)
			throws ParseFormatException, ContradictionException {
		if (pseudo.matcher(constraint).matches()) {
			handlePB(constraint);
		} else if (obj.matcher(constraint).matches()) {
			handleObj(constraint);
		} else {
			throw new UnsupportedOperationException("Wrong formula "
					+ constraint);
		}
	}

	private void handleObj(String constraint) {
		Matcher matcher = wclause.matcher(constraint);
		if (matcher.find()) {
			String weightedLiterals = matcher.group();
			constraint = matcher.replaceFirst("");
			matcher = wliteral.matcher(weightedLiterals);
			IVecInt literals = new VecInt();
			String[] pieces = constraint.split(",");
			boolean negate = pieces[0].contains("max");
			IVec<BigInteger> coefs = new Vec<BigInteger>();
			BigInteger coef;
			while (matcher.find()) {
				literals.push(Integer.valueOf(matcher.group(2)));
				coef = new BigInteger(matcher.group(1));
				coefs.push(negate ? coef.negate() : coef);
			}
			solver.setObjectiveFunction(new ObjectiveFunction(literals, coefs));
		}

	}

	private void handlePB(String constraint) throws ContradictionException {
		Matcher matcher = wclause.matcher(constraint);
		if (matcher.find()) {
			String weightedLiterals = matcher.group();
			constraint = matcher.replaceFirst("");
			matcher = wliteral.matcher(weightedLiterals);
			IVecInt literals = new VecInt();
			IVecInt coefs = new VecInt();
			while (matcher.find()) {
				literals.push(Integer.valueOf(matcher.group(2)));
				coefs.push(Integer.valueOf(matcher.group(1)));
			}
			String[] pieces = constraint.split(",");
			String comp = pieces[1].substring(1, pieces[1].length() - 1);
			int degree = Integer.valueOf(pieces[2].substring(0,
					pieces[2].length() - 1));
			if ("=".equals(comp) || "==".equals(comp)) {
				solver.addExactly(literals, coefs, degree);
			} else if ("<=".equals(comp)) {
				solver.addAtMost(literals, coefs, degree);
			} else if ("<".equals(comp)) {
				solver.addAtMost(literals, coefs, degree - 1);
			} else if (">=".equals(comp)) {
				solver.addAtLeast(literals, coefs, degree);
			} else {
				assert ">".equals(comp);
				solver.addAtLeast(literals, coefs, degree + 1);
			}
		}

	}

	@Override
	protected String constraintPattern() {
		return "(" + CLAUSE + "|" + CARD + "|" + PB + "|" + OBJ + ")";
	}
}
