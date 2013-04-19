package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class Br4cpScenarioSimulator {

	private boolean isVerbose = false;
	private ISolver solver;
	private int currentScenarioIndex;
	private long startTime;

	public static void main(String[] args) throws IOException, TimeoutException {
		new Br4cpScenarioSimulator(args[0], args[1], args.length>2);
	}
	
	private Br4cpScenarioSimulator(String instance, String scenario, boolean isVerbose) throws IOException, TimeoutException{
		this.isVerbose = isVerbose; 
        solver = SolverFactory.newDefault();
        ConfigVarIdMap varMap = new ConfigVarIdMap(solver);
        if(this.isVerbose){
        	System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] parsing instance "+instance+"\n", 0., this.currentScenarioIndex);
        }
        this.startTime = System.currentTimeMillis();
        readInstance(instance, solver, varMap);
    	Br4cpBackboneComputer backboneComputer = new Br4cpBackboneComputer(solver, varMap);
    	if(this.isVerbose){
			System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] scenario parsed and level 0 backbone computed\n", (System.currentTimeMillis()-startTime)/1000., this.currentScenarioIndex);
		}
    	printNewlyAsserted(backboneComputer, 'A', 'R');
        BufferedReader reader = new BufferedReader(new FileReader(scenario));
        String line;
        currentScenarioIndex = 0;
        while((line = reader.readLine()) != null) {
        	if("".equals(line.trim())){
        		continue;
        	}
        	++currentScenarioIndex;
        	long scenarioProcessingTime = System.currentTimeMillis();
        	processScenario(backboneComputer, line);
        	scenarioProcessingTime = System.currentTimeMillis()-scenarioProcessingTime;
        	System.out.printf("d %.3fs", scenarioProcessingTime/1000.);
        	if(this.isVerbose){
    			System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] scenario %d processed\n", (System.currentTimeMillis()-startTime)/1000., this.currentScenarioIndex);
    		}
        }
        System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] solving done.\n", (System.currentTimeMillis()-startTime)/1000.);
        reader.close();
	}
	
	private void processScenario(Br4cpBackboneComputer backboneComputer, String line) throws TimeoutException{
		if(this.isVerbose){
			System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] processing scenario %d\n", (System.currentTimeMillis()-startTime)/1000., this.currentScenarioIndex);
		}
		System.out.println("s "+this.currentScenarioIndex);
    	line = line.replaceAll("\\s+", " ");
    	String[] words = line.split(" ");
    	// nbInstances=words[0], words[1].equals("decisions")
    	for(int i=3; i<words.length; ++i){
    		String assump = words[i].replaceAll("_", ".");
    		assump = assump.replaceAll("=", ".");
    		try {
    			backboneComputer.addAssumption(assump);
    		}catch(IllegalArgumentException e){
    			assump = words[i].substring(0, words[i].indexOf("="));
    			backboneComputer.addAssumption(assump);
    		}
    		System.out.println("p "+assump);
    		if(isVerbose) {
    			System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] assumed "+assump+"\n", (System.currentTimeMillis()-startTime)/1000.);
    		}
    		printNewlyAsserted(backboneComputer);
    	}
    	backboneComputer.clearAssumptions();
	}

	private void printNewlyAsserted(Br4cpBackboneComputer backboneComputer) {
		printNewlyAsserted(backboneComputer, 'a', 'r');
	}
	
	private void printNewlyAsserted(Br4cpBackboneComputer backboneComputer, char asserted, char removed) {
		Set<String> newlyAsserted = backboneComputer.newlyAsserted();
		if(!newlyAsserted.isEmpty()){
			System.out.print(asserted);
			for(String s : newlyAsserted){
				System.out.print(" "+s);
			}
			System.out.println();
		}
		Set<String> newlyAssertedFalse = backboneComputer.newlyAssertedFalse();
		if(!newlyAssertedFalse.isEmpty()){
			System.out.print(removed);
			for(String s : newlyAssertedFalse){
				System.out.print(" "+s);
			}
			System.out.println();
		}
	}

	private void readInstance(String instance, ISolver solver,
			ConfigVarIdMap varMap) {
		Br4cpAraliaReader reader = new Br4cpAraliaReader(solver, varMap);
        try {
			reader.parseInstance(instance);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
