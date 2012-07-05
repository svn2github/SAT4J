package org.sat4j.sat.visu;

import org.sat4j.tools.IVisualizationTool;

public class ChartBasedVisualizationTool implements IVisualizationTool {

    private ITrace2D trace;
    private int i;

    public ChartBasedVisualizationTool(ITrace2D trace) {
        this.trace = trace;
        this.i = 0;
    }

    public void addPoint(double x, double y) {
        // if(i==4){
        this.trace.addPoint(x, y);
        this.i = 0;
        // }
        this.i++;
    }

    public void addInvisiblePoint(double x, double y) {
        // trace.addPoint(x, 0);
    }

    public void init() {
        this.trace.removeAllPoints();
        this.i = 0;

    }

    public void end() {

    }

}
