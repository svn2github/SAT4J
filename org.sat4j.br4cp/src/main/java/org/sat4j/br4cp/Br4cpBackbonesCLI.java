package org.sat4j.br4cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.Backbone;

public class Br4cpBackbonesCLI {

    /**
     * @param args
     */
    public static void main(String[] args) {
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
        IVecInt assumps = new VecInt();
        System.out.println("console commands :");
        System.out.println("#clear : clear assumps");
        System.out.println("#quit : quit program");
        System.out.println("#assumps : show assumptions");
        System.out.println("#backbones : compute the backbones");
        System.out.println("optionName : assumps this option");
        System.out.println();
        for (;;) {
            try {
                System.out.print("$ ");
                String line = inReader.readLine();
                if ("#assumps".startsWith(line)) {
                	System.out.print("assumps:");
                    IteratorInt iterator = assumps.iterator();
                    while (iterator.hasNext())
                        System.out.print(" "
                                + varMap.getName(iterator.next()));
                    System.out.println();
                }else if ("#clear".startsWith(line)) {
                    assumps = new VecInt();
                } else if ("#quit".startsWith(line)) {
                    break;
                } else if ("#backbones".startsWith(line)) {
                    try {
                        IVecInt backbones = Backbone.compute(solver, assumps);
                        System.out.print("backbones:");
                        IteratorInt iterator = backbones.iterator();
                        while (iterator.hasNext()) {
                            int next = iterator.next();
                            String toWrite;
                            if (next < 0) {
                            	if(varMap.getName(-next) != null){
                            		toWrite = " -"+varMap.getName(-next);                            		
                            	}else{
                            		toWrite = "";
                            	}
                            } else {
                            	if(varMap.getName(next) != null){
                            		toWrite = " "+varMap.getName(next);                            		
                            	}else{
                            		toWrite = "";
                            	}
                            }
                            System.out.print(toWrite);
                        }
                        System.out.println();
                    } catch (TimeoutException e) {
                        System.err.println("Timeout occured. Sorry :-/");
                    }
                } else {
                    Integer id = varMap.getVar(line);
                    if (id == null) {
                        System.out.println("unknown option : " + line);
                        continue;
                    }
                    System.out.println("assumps "+id.toString());
                    assumps.push(id);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}
