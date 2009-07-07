package org.sat4j.intervalorders;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.sat4j.core.Vec;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.tools.DependencyHelper;
import org.sat4j.reader.EfficientScanner;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class CompactMatrixReader extends Reader {

	private EfficientScanner scanner;

	private final IPBSolver pbsolver;

	private final DependencyHelper<IRelation, String> helper;

	private Prefer[] prefers;

	private Indifferent[] indifferents;

	private int numberOfAlternatives;

	private int numberOfVoters;

	private static final Random rand = new Random();

	public CompactMatrixReader(IPBSolver pbsolver) {
		this.pbsolver = pbsolver;
		this.helper = new DependencyHelper<IRelation, String>(pbsolver, false);
		this.helper.setNegator(new RelationNegator());
	}

	@Override
	public String decode(int[] model) {
		return helper.getSolution().toString();
	}

	@Override
	public void decode(int[] model, PrintWriter out) {
		out.println(helper.getSolution());
	}

	/**
	 * Generate a compact matrix for a given number of alternatives and voters.
	 * 
	 * @param n
	 *            the number of alternatives
	 * @param nbv
	 *            the number of voters
	 * @returnn compact matrix representing all the votes, each individually
	 *          respecting Ferrer's relation.
	 */
	private int[][] generateRandomCompactMatrix(int n, int nbv) {
		int[][] agregate = new int[n][n];
		int[][] singleVote;
		int correctVotes = 0;
		int triedVotes = 0;
		while (correctVotes < nbv) {
			singleVote = generateRandomVote(n);
			if (checkVote(singleVote)) {
				correctVotes++;
				if (nbv <= 100) {
					System.out.println("Voter " + correctVotes);
					displayMatrix(singleVote);
				}
				agregateVotes(singleVote, agregate);
			}
			triedVotes++;
		}
		System.out.printf(
				"Generated %d random votes to reach %d correct votes\n",
				triedVotes, correctVotes);
		return agregate;
	}

	public void generateRandomCase(int n, int nbv, long seed)
			throws ContradictionException {
		rand.setSeed(seed);
		generateRandomCase(n, nbv);
	}

	public void generateRandomCase(int n, int nbv)
			throws ContradictionException {
		String[] alternatives = new String[n];
		for (int i = 0; i < n; i++) {
			alternatives[i] = "alt" + (i + 1);
		}
		createPreferenceObjects(alternatives);
		numberOfAlternatives = n;
		numberOfVoters = nbv;
		createStructuralConstraints(n);
		int[][] matrix = generateRandomCompactMatrix(n, nbv);
		System.out.println("Compact Matrix");
		displayMatrix(matrix);
		handleMatrix(numberOfAlternatives, numberOfVoters, matrix);
		System.out.println("Creating hard constraints");
		createHardConstraints(numberOfAlternatives, numberOfVoters);
		System.out.println("Creating optimization function");
		createOptimizationFunction(numberOfAlternatives, numberOfVoters);
	}

	private boolean checkVote(int[][] vote) {
		IVec<IRelation> assumptions = new Vec<IRelation>(vote.length
				* vote.length);
		for (int i = 0; i < vote.length; i++) {
			for (int j = i + 1; j < vote[i].length; j++) {
				if (vote[i][j] == 1) {
					assumptions.push(prefers(i, j));
				} else if (vote[j][i] == 1) {
					assumptions.push(prefers(j, i));
				} else {
					assumptions.push(indif(i, j));
				}
			}
			assumptions.push(indif(i, i));
		}
		try {
			return helper.hasASolution(assumptions);
		} catch (TimeoutException e) {
			return false;
		}
	}

	/**
	 * Create a vote for n alternatives
	 * 
	 * @param n
	 *            the number of alternatives
	 * @return a vote
	 */
	private int[][] generateRandomVote(int n) {
		int[][] vote = new int[n][n];
		Interval[] intervals = new Interval[n];
		int a, b, high, low;
		final int maxIntervalValue = n * 100;
		// generate n intervals
		for (int i = 0; i < n; i++) {
			a = rand.nextInt(maxIntervalValue) + 1;
			b = a + rand.nextInt(n) + 1;
			high = b;
			low = a;
			intervals[i] = new Interval(high, low);
		}
		System.out.println(Arrays.asList(intervals));
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				switch (intervals[i].compareTo(intervals[j])) {
				case 0:
					vote[i][j] = 0;
					vote[j][i] = 0;
					break;
				case -1:
					vote[i][j] = 1;
					vote[j][i] = 0;
					break;
				case 1:
					vote[i][j] = 0;
					vote[j][i] = 1;
					break;
				default:
					throw new IllegalStateException("Should never happen");
				}
			}
			vote[i][i] = 0;
		}
		return vote;
	}

	/**
	 * Add all the information regarding a single vote to the compact matrix
	 * representing all the votes.
	 * 
	 * @param singleVote
	 *            a unique vote satisfying Ferrer's relation.
	 * @param allVotes
	 *            the compact matrix representing all the votes.
	 */
	private void agregateVotes(int[][] singleVote, int[][] allVotes) {
		for (int i = 0; i < allVotes.length; i++) {
			for (int j = 0; j < allVotes[i].length; j++) {
				allVotes[i][j] += singleVote[i][j];
				singleVote[i][j] = 0;
			}
		}
	}

	private void displayMatrix(int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.printf("%5d", matrix[i][j]);
			}
			System.out.println();
		}
	}

	@Override
	public IProblem parseInstance(InputStream in) throws ParseFormatException,
			ContradictionException, IOException {
		int[][] matrix = readMatrixFromFile(in);
		createStructuralConstraints(numberOfAlternatives);
		handleMatrix(numberOfAlternatives, numberOfVoters, matrix);
		System.out.println("Creating hard constraints");
		createHardConstraints(numberOfAlternatives, numberOfVoters);
		System.out.println("Creating optimization function");
		createOptimizationFunction(numberOfAlternatives, numberOfVoters);
		return pbsolver;
	}

	private int[][] readMatrixFromFile(InputStream in) throws IOException,
			ParseFormatException {
		scanner = new EfficientScanner(in);
		String line = scanner.nextLine();
		String[] alternatives = line.split("\\s+");
		int n = createPreferenceObjects(alternatives);
		numberOfAlternatives = n;
		numberOfVoters = scanner.nextInt();
		System.out.println("Reading vote data");
		int[][] matrix = new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				matrix[i][j] = scanner.nextInt();
			}
		}
		return matrix;
	}

	private void handleMatrix(int n, int nbVoters, int[][] matrix) {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
					prefers(i, j).setNumberOfVotes(matrix[i][j]);
				} else {
					if (matrix[i][j] != 0) {
						throw new IllegalStateException(
								"Cannot prefer an alternative to itself.");
					}
				}
			}
		}
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				indif(i, j).setNumberOfVotes(
						nbVoters - prefers(i, j).getNumberOfVotes()
								- prefers(j, i).getNumberOfVotes());
			}
		}
	}

	private int createPreferenceObjects(String[] alternatives) {

		int size = alternatives.length * alternatives.length;
		prefers = new Prefer[size];
		indifferents = new Indifferent[size];
		for (int i = 0; i < alternatives.length; i++) {
			for (int j = 0; j < alternatives.length; j++) {
				if (i != j) {
					prefers[i * alternatives.length + j] = new Prefer(
							alternatives[i], alternatives[j]);
				}
				indifferents[i * alternatives.length + j] = new Indifferent(
						alternatives[i], alternatives[j]);
			}
		}
		return alternatives.length;
	}

	private Prefer prefers(int i, int j) {
		Prefer p = prefers[i * numberOfAlternatives + j];
		if (p == null) {
			throw new IllegalStateException();
		}
		return p;
	}

	private Indifferent indif(int i, int j) {
		Indifferent ind = indifferents[i * numberOfAlternatives + j];
		if (ind == null) {
			throw new IllegalStateException();
		}
		return ind;
	}

	private static IRelation neg(IRelation rel) {
		return new NegRelation(rel);
	}

	private void createStructuralConstraints(int n)
			throws ContradictionException {
		int nbconstrs = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				helper.clause("Completeness", prefers(i, j), indif(i, j),
						prefers(j, i));
				helper.clause("Assymetry", neg(prefers(i, j)),
						neg(prefers(j, i)));
				helper.clause("Exclusivity", neg(prefers(i, j)),
						neg(indif(i, j)));
				helper.clause("Exclusivity", neg(prefers(j, i)),
						neg(indif(i, j)));
				nbconstrs += 4;
			}
			helper.setTrue(indif(i, i), "Reflexivity");
			nbconstrs++;
		}
		System.out.printf("Created %d constraints without Ferrer condition\n",
				nbconstrs);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					for (int l = 0; l < n; l++) {
						if (i != j && k != l && i != l && j != l && i != k) {
							helper.implication(prefers(i, j), indif(j, k),
									prefers(k, l)).implies(prefers(i, l))
									.named("Ferrers");
							nbconstrs++;
						}

					}
				}
			}
		}
		System.out.printf("Created %d hard constraints\n", nbconstrs);
	}

	private void createInstanceConstraints(int n, int nbVoters)
			throws ContradictionException {
		int nbconstrs = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (prefers(i, j).getNumberOfVotes() == nbVoters) {
					helper.setTrue(prefers(i, j), "Unanimity P");
					nbconstrs++;
				} else if (prefers(j, i).getNumberOfVotes() == nbVoters) {
					helper.setTrue(prefers(j, i), "Unanimity P");
					nbconstrs++;
				} else {
					if (prefers(i, j).getNumberOfVotes() == 0
							&& prefers(j, i).getNumberOfVotes() == 0) {
						helper.setTrue(indif(i, j), "Unanimity I");
						nbconstrs++;
					}
				}
			}
		}
		System.out.printf("Created %d instance specific constraints\n",
				nbconstrs);
	}

	private void createHardConstraints(int n, int nbVoters)
			throws ContradictionException {
		createInstanceConstraints(n, nbVoters);
	}

	private void createOptimizationFunction(int n, int nbVoters) {
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				prefers(i, j).setPenalty(
						nbVoters + prefers(j, i).getNumberOfVotes()
								- prefers(i, j).getNumberOfVotes());
				helper.addToObjectiveFunction(prefers(i, j), prefers(i, j)
						.getPenalty());
				prefers(j, i).setPenalty(
						nbVoters + prefers(i, j).getNumberOfVotes()
								- prefers(j, i).getNumberOfVotes());
				helper.addToObjectiveFunction(prefers(j, i), prefers(j, i)
						.getPenalty());
				indif(i, j).setPenalty(
						prefers(i, j).getNumberOfVotes()
								+ prefers(j, i).getNumberOfVotes());
				helper.addToObjectiveFunction(indif(i, j), indif(i, j)
						.getPenalty());
			}
		}
	}

	@Override
	public IProblem parseInstance(java.io.Reader in)
			throws ParseFormatException, ContradictionException, IOException {
		throw new UnsupportedOperationException();
	}

	public boolean hasSolution() throws TimeoutException {
		return helper.hasASolution();
	}

	public IVec<IRelation> getSolution() throws ContradictionException {
		return helper.getSolution();
	}

	public void discardLastSolution() throws ContradictionException {
		helper.discard(helper.getSolution());
	}

	public Set<String> why() throws TimeoutException {
		return helper.why();
	}

	public int getSolutionCost() {
		return helper.getSolutionCost().intValue();
	}

	public String getObjectiveFunction() {
		return helper.getObjectiveFunction();
	}

	public int numberOfAlternatives() {
		return numberOfAlternatives;
	}

	public int nbVoters() {
		return numberOfVoters;
	}

	public int nVars() {
		return helper.getNumberOfVariables();
	}

	public int nConstraints() {
		return helper.getNumberOfConstraints();
	}

	public Map<Integer, IRelation> mapping() {
		return helper.getVariablesMapping();
	}

	public void discardNotOptimalValues(long cost)
			throws ContradictionException {
		helper.discardSolutionsWithObjectiveValueGreaterThan(cost);
	}
}
