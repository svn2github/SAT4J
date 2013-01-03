package org.sat4j.sat.visu;

import java.io.Serializable;

public interface SolverVisualisation extends Serializable {

    public void start();

    public void end();

    public void setnVar(int nbVar);

}
