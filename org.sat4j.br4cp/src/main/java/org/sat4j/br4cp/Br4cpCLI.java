package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IGroupSolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.AllMUSes;

public class Br4cpCLI {

	private IGroupSolver solver;
	private ConfigVarMap varMap;
	private PrintStream outStream;
	private boolean quit = false;
	IBr4cpBackboneComputer backboneComputer;

	private Set<String> assumptions = new TreeSet<String>(
			new ConfigVarComparator());
	private final AllMUSes muses = new AllMUSes(true, SolverFactory.instance());
	private Br4cpAraliaReader reader;

	public Br4cpCLI(String instance) throws Exception {
		solver = muses.getSolverInstance();
		varMap = new ConfigVarMap(solver);
		this.outStream = Options.getInstance().getOutStream();
		readInstance(instance, solver, varMap);
		long startTime = System.currentTimeMillis();
		this.outStream.println("computing problem backbone...");
		backboneComputer = Options.getInstance().getBackboneComputer(solver,
				varMap);
		printAsserted(backboneComputer, "rootPropagated:", "rootReduced:");
		this.outStream.printf("done in %.3fs.\n\n",
				(System.currentTimeMillis() - startTime) / 1000.);
		runCLI();
	}

	private void printAsserted(IBr4cpBackboneComputer backboneComputer,
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
		reader = new Br4cpAraliaReader(solver, varMap);
		try {
			reader.parseInstance(instance);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void runCLI() throws Exception {
		this.outStream.println("available commands : ");
		this.outStream.println("  #restart       : clean all assumptions");
		this.outStream
				.println("  #explain vX=Y  : explain the assignement of value Y to variable X");
		this.outStream
				.println("  #explain -vX=Y : explain the reduction of value Y in variable X");
		this.outStream.println("  #quit          : exit this program");
		this.outStream.println();
		BufferedReader inReader = new BufferedReader(new InputStreamReader(
				System.in));
		while (!this.quit) {
			this.outStream.print("$> ");
			this.outStream.flush();
			String line = inReader.readLine();
			if (line == null)
				break;
			if (line.length() == 0) {
				continue;
			}
			try {
				Method toCall = getClass().getDeclaredMethod(
						line.substring(1).split(" ")[0],
						new Class<?>[] { String.class });
				toCall.invoke(this, new Object[] { line });
			} catch (NoSuchMethodException e) {
				assume(line);
			}
		}
		this.outStream.println("Exiting. Bye!");
	}

	@SuppressWarnings("unused")
	private void restart(String line) {
		this.backboneComputer.clearAssumptions();
		this.assumptions.clear();
		this.outStream.println("assumptions cleaned\n");
	}

	@SuppressWarnings("unused")
	private void quit(String line) {
		this.quit = true;
	}

	@SuppressWarnings("unused")
	private void explain(String line) {
		muses.reset();
		IVecInt assumptions = new VecInt();
		String[] words = line.split(" ");
		for (int i = 1; i < words.length; i++) {
			String assump = words[i].replaceAll("_", ".");
			assump = assump.replaceAll("=", ".");
			assumptions.push((assump.charAt(0) == '-') ? (this.varMap
					.getSolverVar(assump.substring(1))) : (-this.varMap
					.getSolverVar(assump)));
		}
		for (Set<Integer> assumpsSet : this.backboneComputer
				.getSolverAssumptions()) {
			for (Integer n : assumpsSet) {
				assumptions.push(n);
			}
		}
		try {
			if (solver.isSatisfiable(assumptions)) {
				this.outStream
						.println("ERROR: satisfiable, nothing to explain");
			} else {
				for (String constraint : reader.decode(solver
						.unsatExplanation()))
					this.outStream.println(constraint);
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

	}

	private void assume(String line) throws Exception {
		String assump = line.replaceAll("_", ".");
		assump = assump.replaceAll("=", ".");
		try {
			if (this.varMap.isAdditionalVar(assump)) {
				backboneComputer.addAdditionalVarAssumption(assump);
			} else if (this.varMap.isOutOfDomainConfigVar(assump)) {
				backboneComputer.setOptionalConfigVarAsNotInstalled(assump);
			} else if (this.varMap.isAdditionalVar(assump)) {
				backboneComputer.addAdditionalVarAssumption(assump);
			} else if (this.varMap.isConfigVar(assump)) {
				addAssumption(assump);
			} else {
				this.outStream.println(assump + " is not defined");
				return;
			}
			for (Iterator<String> it = this.assumptions.iterator(); it
					.hasNext();) {
				String next = it.next();
				if (next.substring(0, next.lastIndexOf("=")).equals(
						line.substring(0, line.lastIndexOf('=')))) {
					it.remove();
					break;
				}
			}
			this.assumptions.add(line);
			printAssumptions();
			printAsserted(backboneComputer);

		} catch (IllegalArgumentException e) {
			this.outStream.println("ERROR: " + e.getMessage());
		}
	}

	private void printAssumptions() {
		this.outStream.print("d");
		for (Iterator<String> it = this.assumptions.iterator(); it.hasNext();) {
			this.outStream.print(' ');
			this.outStream.print(it.next());
		}
		this.outStream.println();
	}

	private void printAsserted(IBr4cpBackboneComputer backboneComputer) {
		printAsserted(backboneComputer, "a", "r");
	}

	private void addAssumption(String assump) throws TimeoutException {
		try {
			backboneComputer.addAssumption(assump);
		} catch (ContradictionException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

}
