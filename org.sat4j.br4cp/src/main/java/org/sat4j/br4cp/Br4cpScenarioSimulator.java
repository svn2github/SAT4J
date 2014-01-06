package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IGroupSolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.AllMUSes;

/**
 * This class is a launcher for BR4CP scenario problems.
 * 
 * @author lonca
 * @author leberre
 * 
 */
public class Br4cpScenarioSimulator {

	private IGroupSolver solver;
	private int currentScenarioIndex;
	private long startTime;
	private ConfigVarMap varMap;
	private IPBSolver pbSolver;
	private final AllMUSes muses = new AllMUSes(true, SolverFactory.instance());

	private List<Integer> nbRemovedValues;

	private PrintStream outStream = System.out;
	private int totalNumberofSATCalls;
	private int overalTotalNumberOfSATCalls;
	
	public Br4cpScenarioSimulator(String instance, String scenario)
			throws IOException, TimeoutException {
		solver = muses.getSolverInstance();
		pbSolver = (IPBSolver) solver.getSolvingEngine();
		varMap = new ConfigVarMap(solver);
		varMap = new ConfigVarMap(solver);
		this.startTime = System.currentTimeMillis();
		readInstance(instance, solver, varMap);
		IBr4cpBackboneComputer backboneComputer = new DefaultBr4cpBackboneComputer(
				solver, varMap);
		printNewlyAsserted(backboneComputer, this.solver.getLogPrefix()
				+ "rootPropagated:", this.solver.getLogPrefix()
				+ "rootReduced:");
		this.outStream.printf(this.solver.getLogPrefix()
				+ "problem backbone computed in %.3fs.\n",
				(System.currentTimeMillis() - startTime) / 1000.);
		BufferedReader reader = new BufferedReader(new FileReader(scenario));
		String line;
		currentScenarioIndex = 0;
		overalTotalNumberOfSATCalls = 0;
		while ((line = reader.readLine()) != null) {
			if ("".equals(line.trim())) {
				continue;
			}
			++currentScenarioIndex;
			backboneComputer.clearAssumptions();
			this.nbRemovedValues = new ArrayList<Integer>();
			this.nbRemovedValues.add(Integer.valueOf(backboneComputer
					.domainReductions().size()));
			long scenarioProcessingTime = System.currentTimeMillis();
			this.totalNumberofSATCalls = 0;
			processScenario(backboneComputer, line);
			scenarioProcessingTime = System.currentTimeMillis()
					- scenarioProcessingTime;
			this.outStream
					.println(this.solver.getLogPrefix()
							+ "nbRemovedValues="
							+ this.nbRemovedValues
									.toString()
									.substring(
											1,
											this.nbRemovedValues.toString()
													.length() - 1)
									.replaceAll(",", ""));
			this.outStream.printf(this.solver.getLogPrefix()
					+ "scenario processed in %.3fs\n",
					scenarioProcessingTime / 1000.);
			this.outStream.printf("Total number of SAT calls: %d%n",
					totalNumberofSATCalls);
			overalTotalNumberOfSATCalls += totalNumberofSATCalls;
		}
		this.outStream.printf(this.solver.getLogPrefix()
				+ "solving done in %.3fs.\n",
				(System.currentTimeMillis() - startTime) / 1000.);
		this.outStream.printf("Total number of SAT calls for the scenario: %d%n",
				overalTotalNumberOfSATCalls);
		reader.close();
	}

	private void processScenario(IBr4cpBackboneComputer backboneComputer,
			String line) throws TimeoutException {
		this.outStream.println(this.solver.getLogPrefix() + "scenario #"
				+ this.currentScenarioIndex);
		line = line.replaceAll("\\s+", " ");
		String[] words = line.split(" ");
		// nbInstances=words[0], words[1].equals("decisions")
		for (int i = 3; i < words.length; ++i) {
			processWord(backboneComputer, words[i]);
		}
		backboneComputer.clearAssumptions();
	}

	private void processWord(IBr4cpBackboneComputer backboneComputer,
			String word) throws TimeoutException {
		this.outStream.println(this.solver.getLogPrefix() + "selected : "
				+ word);
		String assump = word.replaceAll("_", ".");
		assump = assump.replaceAll("=", ".");
		try {
			if (this.varMap.isAdditionalVar(assump)) {
				backboneComputer.addAdditionalVarAssumption(assump);
			} else if (this.varMap.isOutOfDomainConfigVar(assump)) {
				backboneComputer.setOptionalConfigVarAsNotInstalled(assump);
			} else if (this.varMap.isConfigVar(assump)) {
				try {
					backboneComputer.addAssumption(assump);
				} catch (ContradictionException e) {
					throw new IllegalArgumentException(e.getMessage());
				}
			} else {
				this.outStream.println(assump + " is not defined");
				return;
			}

			printNewlyAsserted(backboneComputer);
		} catch (IllegalArgumentException e) {
			this.outStream.println("ERROR: " + e.getMessage());
		}
	}

	private void printNewlyAsserted(IBr4cpBackboneComputer backboneComputer) {
		this.outStream.print(this.solver.getLogPrefix());
		this.outStream.print("number of SAT calls: ");
		this.outStream.println(backboneComputer
				.getNumberOfSATCalls());
		totalNumberofSATCalls += backboneComputer
				.getNumberOfSATCalls();
		printNewlyAsserted(backboneComputer, this.solver.getLogPrefix()
				+ "propagated :", this.solver.getLogPrefix() + "reduced :");
		this.nbRemovedValues.add(Integer.valueOf(backboneComputer
				.domainReductions().size()));
	}

	private void printNewlyAsserted(IBr4cpBackboneComputer backboneComputer,
			String assertedPrefix, String removedPrefix) {
		Set<String> asserted = backboneComputer.propagatedConfigVars();
		Set<String> propagatedAdditionalVars = backboneComputer
				.propagatedAdditionalVars();
		if (!asserted.isEmpty() || !propagatedAdditionalVars.isEmpty()) {
			this.outStream.print(assertedPrefix);
			for (String s : asserted) {
				this.outStream.print(" " + s);
			}
			for (String s : propagatedAdditionalVars) {
				this.outStream.print(" " + s);
			}
			this.outStream.println();
		}
		Set<String> assertedFalse = backboneComputer.domainReductions();
		Set<String> unavailableAdditionalVars = backboneComputer
				.unavailableAdditionalVars();
		if (!assertedFalse.isEmpty() || !unavailableAdditionalVars.isEmpty()) {
			this.outStream.print(removedPrefix);
			for (String s : assertedFalse) {
				this.outStream.print(" " + s);
			}
			for (String s : unavailableAdditionalVars) {
				this.outStream.print(" " + s.replaceAll("=99", "=1"));
			}
			this.outStream.println();
		}
	}

	private void readInstance(String instance, IGroupSolver solver,
			ConfigVarMap varMap) {
		Br4cpAraliaReader reader = new Br4cpAraliaReader(solver, pbSolver,
				varMap);
		try {
			reader.parseInstance(instance);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
