package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class Br4cpBackbonesCLI {

    private static Set<String> lastBackbones = new HashSet<String>();

	/**
     * @param args
     * @throws TimeoutException 
     */
    public static void main(String[] args) throws TimeoutException {
        ISolver solver = SolverFactory.newDefault();
        ConfigVarIdMap varMap = new ConfigVarIdMap(solver);
        Br4cpAraliaReader reader = new Br4cpAraliaReader(solver, varMap);
        try {
			reader.parseInstance(args[0]);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        BufferedReader inReader = new BufferedReader(new InputStreamReader(
                System.in));
        System.out.println("console commands :");
        System.out.println("#clear : clear assumps");
        System.out.println("#quit : quit program");
        System.out.println("#assumps : show assumptions");
        System.out.println("#backbones : compute the backbones");
        System.out.println("optionName : assumps this option");
        System.out.println();
        Br4cpBackboneComputer backbonesFinder = new Br4cpBackboneComputer(solver, varMap);
        for (;;) {
            try {
                System.out.print("$ ");
                String line = inReader.readLine();
                if ("#assumps".startsWith(line)) {
                	Iterator<String> it = backbonesFinder.getAssumptions().iterator();
                	System.out.print("assumptions:");
                	while(it.hasNext()){
                		System.out.print(' ');
                		System.out.print(it.next());
                	}
                	System.out.println();
                }else if ("#clear".startsWith(line)) {
                    backbonesFinder.clearAssumptions();
                } else if ("#quit".startsWith(line)) {
                    break;
                } else {
                	try {
                		if(lastBackbones.contains("-"+line)){
                			System.out.println("error : -"+line+" is implied !");
                			continue;
                		}
                		backbonesFinder.addAssumption(line);
                        System.out.println("backbones:");
                        lastBackbones = backbonesFinder.getBackbones();
    					for(Iterator<String> it = lastBackbones.iterator(); it.hasNext(); ){
                        	System.out.print(' ');
                        	System.out.print(it.next());
                        }
                        System.out.println();
                	}catch(IllegalArgumentException e){
                		System.err.println(e.getMessage());
                	}
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}
