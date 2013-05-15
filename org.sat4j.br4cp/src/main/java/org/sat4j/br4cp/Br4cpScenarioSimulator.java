package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IGroupSolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.AllMUSes;

/**
 * This class is a launcher for BR4CP scenario problems.
 * 
 * @author lonca
 * 
 */
public class Br4cpScenarioSimulator {

	private IGroupSolver solver;
	private int currentScenarioIndex;
	private long startTime;
	private ConfigVarMap varMap;

	private List<Integer> nbRemovedValues;

	private PrintStream outStream = System.out;
	private final AllMUSes muses = new AllMUSes(true,SolverFactory.instance());
	
	public Br4cpScenarioSimulator(String instance, String scenario)
			throws IOException, TimeoutException {
		solver = muses.getSolverInstance();
		varMap = new ConfigVarMap(solver);
		this.outStream = Options.getInstance().getOutStream();
		this.startTime = System.currentTimeMillis();
		readInstance(instance, solver, varMap);
		IBr4cpBackboneComputer backboneComputer = Options.getInstance().getBackboneComputer(solver, varMap);
		printNewlyAsserted(backboneComputer, this.solver.getLogPrefix()
				+ "rootPropagated:", this.solver.getLogPrefix()
				+ "rootReduced:");
		this.outStream.printf(this.solver.getLogPrefix()
				+ "problem backbone computed in %.3fs.\n",
				(System.currentTimeMillis() - startTime) / 1000.);
		BufferedReader reader = new BufferedReader(new FileReader(scenario));
		String line;
		currentScenarioIndex = 0;
		while ((line = reader.readLine()) != null) {
			if ("".equals(line.trim())) {
				continue;
			}
			++currentScenarioIndex;
			backboneComputer.clearAssumptions();
			this.nbRemovedValues = new ArrayList<Integer>();
			this.nbRemovedValues.add(Integer.valueOf(backboneComputer
					.newCspDomainReductions().size()));
			long scenarioProcessingTime = System.currentTimeMillis();
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
		}
		this.outStream.printf(this.solver.getLogPrefix()
				+ "solving done in %.3fs.\n",
				(System.currentTimeMillis() - startTime) / 1000.);
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
		String assump = word.replaceAll("_", ".");
		assump = assump.replaceAll("=", ".");
		if (this.varMap.isOutOfDomainConfigVar(assump)) {
			backboneComputer.setOptionalConfigVarAsNotInstalled(assump);
		} else if (!this.varMap.isConfigVar(assump)) {
			backboneComputer.addAdditionalVarAssumption(assump);
		} else {
			try {
				backboneComputer.addAssumption(assump);
			} catch (ContradictionException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		this.outStream.println(this.solver.getLogPrefix() + "selected : "
				+ word);
		printNewlyAsserted(backboneComputer);
	}

	private void printNewlyAsserted(IBr4cpBackboneComputer backboneComputer) {
		printNewlyAsserted(backboneComputer, this.solver.getLogPrefix()
				+ "propagated :", this.solver.getLogPrefix() + "reduced :");
		this.nbRemovedValues.add(Integer.valueOf(backboneComputer
				.newCspDomainReductions().size()));
	}

	private void printNewlyAsserted(IBr4cpBackboneComputer backboneComputer,
			String asserted, String removed) {
		Set<String> newlyAsserted = backboneComputer.newPropagatedConfigVars();
		Set<String> newBooleanAssertions = backboneComputer
				.newPropagatedAdditionalVars();
		if (!newlyAsserted.isEmpty() || !newBooleanAssertions.isEmpty()) {
			this.outStream.print(asserted);
			for (String s : newlyAsserted) {
				this.outStream.print(" " + s);
			}
			for (String s : newBooleanAssertions) {
				this.outStream.print(" " + s);
			}
			this.outStream.println();
		}
		Set<String> newlyAssertedFalse = backboneComputer.newDomainReductions();
		newBooleanAssertions = backboneComputer.newReducedAdditionalVars();
		if (!newlyAssertedFalse.isEmpty() || !newBooleanAssertions.isEmpty()) {
			this.outStream.print(removed);
			for (String s : newlyAssertedFalse) {
				this.outStream.print(" " + s);
			}
			for (String s : newBooleanAssertions) {
				this.outStream.print(" " + s);
			}
			this.outStream.println();
		}
	}

	private void readInstance(String instance, IGroupSolver solver,
			ConfigVarMap varMap) {
		Br4cpAraliaReader reader = new Br4cpAraliaReader(solver, varMap);
		try {
			reader.parseInstance(instance);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
