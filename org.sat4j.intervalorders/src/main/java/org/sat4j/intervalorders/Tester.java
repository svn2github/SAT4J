package org.sat4j.intervalorders;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Iterator;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.SolverFactory;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class Tester {
	public static void main(String[] args) throws FileNotFoundException,
			ParseFormatException, IOException, ContradictionException {
		if (args.length < 1) {
			System.out
					.println("Usage: java -jar intervalorders.jar <filename>");
			return;
		}
		String outputsolfile;

		IPBSolver solver = new OptToPBSATAdapter(new PseudoOptDecorator(
				SolverFactory.newCuttingPlanes()));
		solver.setTimeout(600);
		CompactMatrixReader reader = new CompactMatrixReader(solver);

		if (args.length >= 2) {
			System.out.println("Generating random cases with " + args[0]
					+ " alternatives and " + args[1] + " voters");
			int n = Integer.valueOf(args[0]);
			int nbv = Integer.valueOf(args[1]);
			outputsolfile = "random-interval-orders-" + args[0] + "-" + args[1];
			if (args.length == 3) {
				System.out.println("Using seed " + args[2]
						+ " to initialize random generator");
				reader.generateRandomCase(n, nbv, Long.valueOf(args[2]));
				outputsolfile += "-" + args[2];
			} else {
				reader.generateRandomCase(n, nbv);
			}

		} else {
			String filename = args[0];
			System.out.println("Reading problem ...");
			reader.parseInstance(filename);
			outputsolfile = filename.substring(0, filename.lastIndexOf('.'));
		}
		PrintStream out = new PrintStream(new FileOutputStream(outputsolfile
				+ ".opb"));
		out.println(solver.toString());
		out.close();
		// System.out.println(solver);
		System.out.println("Number of alternatives:\t"
				+ reader.numberOfAlternatives());
		System.out.println("Number of variables:\t" + reader.nVars());
		System.out.println("Variables mapping:\t" + reader.mapping());
		System.out.println("Number of constraints:\t" + reader.nConstraints());
		System.out.println("Objective Function:\t"
				+ reader.getObjectiveFunction());
		long sum = sumOfWeights(solver);
		System.out.println("Sum of weights:\t\t" + sum);
		long cost;
		int i = 1;
		boolean first = true;
		try {
			while (reader.hasSolution()) {
				IVec<IRelation> solution = reader.getSolution();
				System.out.println(solution + " > "
						+ (cost = reader.getSolutionCost()) + "/"
						+ (sum - cost));
				convertToDotFile(solution, outputsolfile, reader.nbVoters(),
						i++, cost);
				reader.discardLastSolution();
				if (first) {
					reader.discardNotOptimalValues(cost);
					first = false;
				}
			}
		} catch (TimeoutException te) {
			IVec<IRelation> solution = reader.getSolution();
			System.out.println(solution + " > non optimal "
					+ (cost = reader.getSolutionCost()) + "/" + (sum - cost));
			convertToDotFile(solution, args[0], reader.nbVoters(), i++, cost);
		}
	}

	private static void convertToDotFile(IVec<IRelation> solution,
			String filename, int nbVoters, int i, long cost) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(filename
					+ ".sol" + i + ".dot"));
			writer.println("digraph G {");
			writer.println(" legend [shape= plaintext, label=\"Solution " + i
					+ ", penalty " + cost + "\"]");
			for (Iterator<IRelation> it = solution.iterator(); it.hasNext();) {
				writer.println(it.next().toDot(nbVoters));
			}
			writer.println("}");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static long sumOfWeights(IPBSolver solver) {
		IVec<BigInteger> coefs = solver.getObjectiveFunction().getCoeffs();
		long sum = 0;
		for (Iterator<BigInteger> it = coefs.iterator(); it.hasNext();) {
			sum += it.next().intValue();
		}
		return sum;
	}
}
