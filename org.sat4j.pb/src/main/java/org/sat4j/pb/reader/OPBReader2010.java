package org.sat4j.pb.reader;

import java.io.IOException;
import java.math.BigInteger;

import org.sat4j.pb.IPBSolver;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;

/**
 * @since 2.2
 */
public class OPBReader2010 extends OPBReader2007 {

	public static final BigInteger SAT4J_MAX_BIG_INTEGER = new BigInteger(
			"100000000000000000000000000000000000000000");

	private boolean isWbo = false;

	private BigInteger softLimit = SAT4J_MAX_BIG_INTEGER;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OPBReader2010(IPBSolver solver) {
		super(solver);
	}

	/**
	 * read the first comment line to get the number of variables and the number
	 * of constraints in the file calls metaData with the data that was read
	 * 
	 * @throws IOException
	 * @throws ParseFormatException
	 */
	@Override
	protected void readMetaData() throws IOException, ParseFormatException {
		char c;
		String s;

		// get the number of variables and constraints
		c = get();
		if (c != '*')
			throw new ParseFormatException(
					"First line of input file should be a comment");
		s = readWord();
		if (eof() || !"#variable=".equals(s))
			throw new ParseFormatException(
					"First line should contain #variable= as first keyword");

		nbVars = Integer.parseInt(readWord());
		nbNewSymbols = nbVars + 1;

		s = readWord();
		if (eof() || !"#constraint=".equals(s))
			throw new ParseFormatException(
					"First line should contain #constraint= as second keyword");

		nbConstr = Integer.parseInt(readWord());
		charAvailable = false;
		if (!eol()) {
			String rest = in.readLine();

			if (rest.contains("#soft")) {
				isWbo = true;
				hasObjFunc = true;
			}
			if (rest != null && rest.indexOf("#product=") != -1) {
				String[] splitted = rest.trim().split(" ");
				if (splitted[0].equals("#product=")) {
					Integer.parseInt(splitted[1]);
				}

				// if (splitted[2].equals("sizeproduct="))
				// readWord();

			}
		}
		// callback to transmit the data
		metaData(nbVars, nbConstr);
	}

	@Override
	protected void readObjective() throws IOException, ParseFormatException {
		if (isWbo) {
			readSoftLine();
		} else {
			super.readObjective();
		}
	}

	private void readSoftLine() throws IOException, ParseFormatException {
		String s = readWord();
		if (s == null || !"soft:".equals(s)) {
			throw new ParseFormatException("Did not find expected soft: line");
		}
		s = readWord().trim();
		if (s != null && !";".equals(s)) {
			softLimit = new BigInteger(s);
		}
		skipSpaces();
		if (get() != ';') {
			throw new ParseFormatException(
					"soft: line should end with a semicolon");
		}
	}

	@Override
	protected void beginConstraint() {
		super.beginConstraint();
		try {
			if (isWbo) {
				skipSpaces();
				char c = get();
				putback(c);
				if (c == '[') {
					String s = readWord();
					if (!s.endsWith("]"))
						throw new ParseFormatException(
								"Expecting end of weight ");
					BigInteger coeff = new BigInteger(s.substring(1,
							s.length() - 1));
					getCoeffs().push(coeff);
					int varId = nbNewSymbols++;
					getVars().push(varId);
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void endConstraint() throws ContradictionException {
		if (isWbo) {
			int varId = nbNewSymbols - 1;
			BigInteger constrWeight = d;
			if ("<=".equals(operator)) {
				constrWeight = constrWeight.negate();
			}
			coeffs.push(constrWeight);
			lits.push(varId);
		}
		super.endConstraint();
	}

	@Override
	public IProblem parseInstance(final java.io.Reader input)
			throws ParseFormatException, ContradictionException {
		super.parseInstance(input);
		if (isWbo && softLimit != SAT4J_MAX_BIG_INTEGER) {
			solver.addPseudoBoolean(getVars(), getCoeffs(), false,
					softLimit.subtract(BigInteger.ONE));
		}
		return solver;
	}
}
