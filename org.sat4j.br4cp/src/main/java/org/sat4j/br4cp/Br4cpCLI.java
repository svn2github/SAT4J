package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class Br4cpCLI {

	private ISolver solver;
	private ConfigVarMap varMap;
	private PrintStream outStream = System.out;
	private boolean quit = false;
	IBr4cpBackboneComputer backboneComputer;
	
	private List<String> assumptions = new ArrayList<String>();

	public Br4cpCLI(String instance) throws Exception {
		solver = SolverFactory.newDefault();
		varMap = new ConfigVarMap(solver);
		readInstance(instance, solver, varMap);
		long startTime = System.currentTimeMillis();
		this.outStream.println("computing problem backbone...");
		backboneComputer = new AssumptionsBasedBr4cpBackboneComputer(solver,
				varMap);
		printNewlyAsserted(backboneComputer, this.solver.getLogPrefix()
				+ "rootPropagated:", this.solver.getLogPrefix()
				+ "rootReduced:");
		this.outStream.printf("done in %.3fs.\n\n",
				(System.currentTimeMillis() - startTime) / 1000.);
		runCLI();
	}

	private void readInstance(String instance, ISolver solver,
			ConfigVarMap varMap) {
		Br4cpAraliaReader reader = new Br4cpAraliaReader(solver, varMap);
		try {
			reader.parseInstance(instance);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void printNewlyAsserted(IBr4cpBackboneComputer backboneComputer) {
		printNewlyAsserted(backboneComputer, this.solver.getLogPrefix()
				+ "propagated :", this.solver.getLogPrefix() + "reduced :");
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

	private void runCLI() throws Exception {
		this.outStream.println("available commands : ");
		this.outStream.println("  #assumps : write the list of assumptions");
		this.outStream.println("  #restart : clean all assumptions");
		this.outStream.println("  #quit    : exit this program");
		this.outStream.println();
		BufferedReader inReader = new BufferedReader(new InputStreamReader(
				System.in));
		while (!this.quit) {
			this.outStream.print("$> ");
			this.outStream.flush();
			String line = inReader.readLine();
			if(line == null)
				break;
			if(line.length() == 0){
				continue;
			}
			try {
				Method toCall = getClass().getDeclaredMethod(line.substring(1),
						new Class<?>[0]);
				toCall.invoke(this, (Object[]) null);
			} catch (NoSuchMethodException e) {
				assume(line);
			}
		}
		this.outStream.println("Exiting. Bye!");
	}
	
	@SuppressWarnings("unused")
	private void assumps() {
		this.outStream.print("assumptions :");
		for(Iterator<String> it = this.assumptions.iterator(); it.hasNext(); ){
			this.outStream.print(" "+it.next());
		}
		this.outStream.println();
	}

	@SuppressWarnings("unused")
	private void restart() {
		this.backboneComputer.clearAssumptions();
		this.assumptions.clear();
		this.outStream.println("assumptions cleaned\n");
	}

	@SuppressWarnings("unused")
	private void quit() {
		this.quit = true;
	}

	private void assume(String line) throws Exception {
		String assump = line.replaceAll("_", ".");
		assump = assump.replaceAll("=", ".");
		try {
			if (this.varMap.isOutOfDomainConfigVar(assump)) {
				backboneComputer.setOptionalConfigVarAsNotInstalled(assump);
			} else if (this.varMap.isAdditionalVar(assump)) {
				backboneComputer.addAdditionalVarAssumption(assump);
			} else if (this.varMap.isConfigVar(assump)){
				addAssumption(assump);
			} else {
				this.outStream.println(assump+" is not defined");
				return;
			}
			if(!this.assumptions.contains(assump)) {
				this.assumptions.add(assump);
			}
			printNewlyAsserted(backboneComputer);
		} catch (IllegalArgumentException e) {
			this.outStream.println("ERROR: " + e.getMessage());
		}
	}

	private void addAssumption(String assump) throws TimeoutException {
		try {
			backboneComputer.addAssumption(assump);
		} catch (ContradictionException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

}
