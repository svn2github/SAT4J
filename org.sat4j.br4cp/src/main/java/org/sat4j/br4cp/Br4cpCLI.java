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

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IGroupSolver;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.AllMUSes;

public class Br4cpCLI {

	private IGroupSolver solver;
	private ConfigVarMap varMap;
	private PrintStream outStream;
	private boolean quit = false;
	IBr4cpBackboneComputer backboneComputer;
	
	private List<String> assumptions = new ArrayList<String>();
	private final AllMUSes muses = new AllMUSes(true,SolverFactory.instance());
	private Br4cpAraliaReader reader;
	
	public Br4cpCLI(String instance) throws Exception {
		solver = muses.getSolverInstance();
		varMap = new ConfigVarMap(solver);
		this.outStream = Options.getInstance().getOutStream();
		readInstance(instance, solver, varMap);
		long startTime = System.currentTimeMillis();
		this.outStream.println("computing problem backbone...");
		backboneComputer = Options.getInstance().getBackboneComputer(solver, varMap);
		printNewlyAsserted(backboneComputer, this.solver.getLogPrefix()
				+ "rootPropagated:", this.solver.getLogPrefix()
				+ "rootReduced:");
		this.outStream.printf("done in %.3fs.\n\n",
				(System.currentTimeMillis() - startTime) / 1000.);
		runCLI();
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
		this.outStream.println("  #assumps      : write the list of assumptions");
		this.outStream.println("  #restart      : clean all assumptions");
		this.outStream.println("  #explain vX=Y : explain the reduction of value Y in variable X");
		this.outStream.println("  #quit         : exit this program");
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
				Method toCall = getClass().getDeclaredMethod(line.substring(1).split(" ")[0],
						new Class<?>[] {String.class});
				toCall.invoke(this, new Object[]{line});
			} catch (NoSuchMethodException e) {
				assume(line);
			}
		}
		this.outStream.println("Exiting. Bye!");
	}
	
	@SuppressWarnings("unused")
	private void assumps(String line) {
		this.outStream.print("assumptions :");
		for(Iterator<String> it = this.assumptions.iterator(); it.hasNext(); ){
			this.outStream.print(" "+it.next());
		}
		this.outStream.println();
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
		for (int i=1;i<words.length;i++) {
			String assump = words[i].replaceAll("_", ".");
			assump = assump.replaceAll("=", ".");
			assumptions.push(this.varMap.getSolverVar(assump));
		}
		try {
			this.outStream.println("satisfiable? " +solver.isSatisfiable(assumptions));
			this.outStream.println(reader.decode(solver.unsatExplanation()));
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
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
