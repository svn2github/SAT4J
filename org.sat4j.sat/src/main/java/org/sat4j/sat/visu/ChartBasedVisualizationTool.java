package org.sat4j.sat.visu;

import info.monitorenter.gui.chart.ITrace2D;

import org.sat4j.tools.IVisualizationTool;

public class ChartBasedVisualizationTool implements IVisualizationTool {

    private static final long serialVersionUID = 1L;

    private ITrace2D trace;

    public ChartBasedVisualizationTool(ITrace2D trace) {
        this.trace = trace;
    }

    public void addPoint(double x, double y) {
        this.trace.addPoint(x, y);
    }

    public void addInvisiblePoint(double x, double y) {
    }

    public void init() {
        this.trace.removeAllPoints();
    }

    public void end() {

    }

}
