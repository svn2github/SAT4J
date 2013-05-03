package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * This class is a launcher for BR4CP scenario problems.
 * 
 * @author lonca
 *
 */
public class Br4cpScenarioSimulator {

	private boolean isVerbose = false;
	private ISolver solver;
	private int currentScenarioIndex;
	private long startTime;
	private ConfigVarMap varMap;
	
	private List<Integer> nbRemovedValues;

	public static void main(String[] args) throws IOException, TimeoutException {
		new Br4cpScenarioSimulator(args[0], args[1], args.length>2);
	}
	
	private Br4cpScenarioSimulator(String instance, String scenario, boolean isVerbose) throws IOException, TimeoutException{
		this.isVerbose = isVerbose; 
        solver = SolverFactory.newDefault();
        varMap = new ConfigVarMap(solver);
        if(this.isVerbose){
        	System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] parsing instance "+instance+"\n", 0., this.currentScenarioIndex);
        }
        this.startTime = System.currentTimeMillis();
        readInstance(instance, solver, varMap);
    	IBr4cpBackboneComputer backboneComputer = new AssumptionsBasedBr4cpBackboneComputer(solver, varMap);
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
        	backboneComputer.clearAssumptions();
        	this.nbRemovedValues = new ArrayList<Integer>();
        	this.nbRemovedValues.add(Integer.valueOf(backboneComputer.newCspDomainReductions().size()));
        	long scenarioProcessingTime = System.currentTimeMillis();
        	processScenario(backboneComputer, line);
        	scenarioProcessingTime = System.currentTimeMillis()-scenarioProcessingTime;
        	System.out.printf("d %.3fs\n", scenarioProcessingTime/1000.);
        	System.out.println("c nbRemovedValues="+this.nbRemovedValues.toString().substring(1,this.nbRemovedValues.toString().length()-1).replaceAll(",", ""));
        	if(this.isVerbose){
    			System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] scenario %d processed\n", (System.currentTimeMillis()-startTime)/1000., this.currentScenarioIndex);
    		}
        }
        System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] solving done.\n", (System.currentTimeMillis()-startTime)/1000.);
        reader.close();
	}
	
	private void processScenario(IBr4cpBackboneComputer backboneComputer, String line) throws TimeoutException{
		if(this.isVerbose){
			System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] processing scenario %d\n", (System.currentTimeMillis()-startTime)/1000., this.currentScenarioIndex);
		}
		System.out.println("s "+this.currentScenarioIndex);
    	line = line.replaceAll("\\s+", " ");
    	String[] words = line.split(" ");
    	// nbInstances=words[0], words[1].equals("decisions")
    	for(int i=3; i<words.length; ++i){
    		processWord(backboneComputer, words[i]);
    	}
    	backboneComputer.clearAssumptions();
	}

	private void processWord(IBr4cpBackboneComputer backboneComputer,
			String word) throws TimeoutException {
		String assump = word.replaceAll("_", ".");
		assump = assump.replaceAll("=", ".");
		if(this.varMap.isOutOfDomainConfigVar(assump)){
			backboneComputer.setOptionalConfigVarAsNotInstalled(assump);
		} else if(!this.varMap.isConfigVar(assump)){
			backboneComputer.addAdditionalVarAssumption(assump);
		}else{
			backboneComputer.addAssumption(assump);
		}
		System.out.println("p "+assump);
		if(isVerbose) {
			System.out.printf(this.solver.getLogPrefix()+"[%7.3fs] assumed "+assump+"\n", (System.currentTimeMillis()-startTime)/1000.);
		}
		printNewlyAsserted(backboneComputer);
	}

	private void printNewlyAsserted(IBr4cpBackboneComputer backboneComputer) {
		printNewlyAsserted(backboneComputer, 'a', 'r');
		System.out.println("=> "+backboneComputer.newCspDomainReductions().size());
		this.nbRemovedValues.add(Integer.valueOf(backboneComputer.newCspDomainReductions().size()));
	}
	
	private void printNewlyAsserted(IBr4cpBackboneComputer backboneComputer, char asserted, char removed) {
		Set<String> newlyAsserted = backboneComputer.newPropagatedConfigVars();
		Set<String> newBooleanAssertions = backboneComputer.newPropagatedAdditionalVars();
		if(!newlyAsserted.isEmpty() || !newBooleanAssertions.isEmpty()){
			System.out.print(asserted);
			for(String s : newlyAsserted){
				System.out.print(" "+s);
			}
			for(String s : newBooleanAssertions){
				System.out.print(" "+s);
			}
			System.out.println();
		}
		Set<String> newlyAssertedFalse = backboneComputer.newDomainReductions();
		if(!newlyAssertedFalse.isEmpty() || !newBooleanAssertions.isEmpty()){
			System.out.print(removed);
			for(String s : newlyAssertedFalse){
				System.out.print(" "+s);
			}
			System.out.println();
		}
	}

	private void readInstance(String instance, ISolver solver,
			ConfigVarMap varMap) {
		Br4cpAraliaReader reader = new Br4cpAraliaReader(solver, varMap);
        try {
			reader.parseInstance(instance);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
