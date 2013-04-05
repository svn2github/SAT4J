package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class Br4cpScenarioSimulator {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws TimeoutException 
	 */
	public static void main(String[] args) throws IOException, TimeoutException {
		new Br4cpScenarioSimulator(args[0], args[1]);
	}
	
	private Br4cpScenarioSimulator(String instance, String scenario) throws IOException, TimeoutException{
        ISolver solver = SolverFactory.newDefault();
        ConfigVarIdMap varMap = new ConfigVarIdMap(solver);
        readInstance(instance, solver, varMap);
        BufferedReader reader = new BufferedReader(new FileReader(scenario));
        String line;
        int scenarioCpt = 0;
        while((line = reader.readLine()) != null) {
        	if("".equals(line.trim())){
        		continue;
        	}
        	++scenarioCpt;
        	long initTime = System.currentTimeMillis();
        	processScenario(solver, varMap, line, initTime);
        	long timeOffset = System.currentTimeMillis() - initTime;
    		System.out.println("scenario "+scenarioCpt+" completed in "+Double.valueOf(timeOffset/1000.)+"s");
        }
        reader.close();
	}
	
	private void processScenario(ISolver solver, ConfigVarIdMap varMap, String line, long initTime) throws TimeoutException{
    	Br4cpBackboneComputer backboneComputer = new Br4cpBackboneComputer(solver, varMap);
//    	long timeOffset = System.currentTimeMillis() - initTime;
//    	System.out.print("["+Double.valueOf(timeOffset/1000.)+"s] initial backbones:");
//    	printBackbones(backboneComputer);
    	line = line.replaceAll("\\s+", " ");
    	String[] words = line.split(" ");
    	// nbInstances=words[0], words[1].equals("decisions")
    	for(int i=3; i<words.length; ++i){
    		String assump = words[i].replaceAll("_", ".");
    		assump = assump.replaceAll("=", ".");
    		try {
    			backboneComputer.addAssumption(assump);
    		}catch(IllegalArgumentException e){
    			backboneComputer.addAssumption(words[i].substring(0, words[i].indexOf("=")));
    		}
//    		timeOffset = System.currentTimeMillis() - initTime;
//    		System.out.print("["+Double.valueOf(timeOffset/1000.)+"s] step "+Integer.toString(i-2)+", backbones:");
//        	printBackbones(backboneComputer);
    	}
    	backboneComputer.clearAssumptions();
	}

	private void printBackbones(Br4cpBackboneComputer backboneComputer) {
		for(Iterator<String> it = backboneComputer.getBackbones().iterator(); it.hasNext(); ){
        	System.out.print(' ');
        	System.out.print(it.next());
        }
        System.out.println();
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
