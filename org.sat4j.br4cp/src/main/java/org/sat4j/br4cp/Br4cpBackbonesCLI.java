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
        // solver.setVerbose(true);
        Br4cpLPReader reader = new Br4cpLPReader(solver);
        try {
            reader.parseInstance(args[0]);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                if ("#clear".equals(line)) {
                    assumps = new VecInt();
                } else if ("#quit".equals(line)) {
                    break;
                } else if ("#assumps".equals(line)) {
                    System.out.print("assumps:");
                    IteratorInt iterator = assumps.iterator();
                    while (iterator.hasNext())
                        System.out.print(" "
                                + reader.getVarName(iterator.next()));
                    System.out.println();
                } else if ("#backbones".equals(line)) {
                    try {
                        IVecInt backbones = Backbone.compute(solver, assumps);
                        System.out.print("assumps:");
                        IteratorInt iterator = backbones.iterator();
                        while (iterator.hasNext()) {
                            System.out.print(" ");
                            int next = iterator.next();
                            if (next < 0) {
                                System.out
                                        .print("-" + reader.getVarName(-next));
                            } else {
                                System.out.print(" " + reader.getVarName(next));
                            }
                        }
                        System.out.println();
                    } catch (TimeoutException e) {
                        System.err.println("Timeout occured. Sorry :-/");
                    }
                } else {
                    Integer id = reader.getVarId(line);
                    if (id == null) {
                        System.out.println("unknown option : " + line);
                        continue;
                    }
                    assumps.push(id);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}
