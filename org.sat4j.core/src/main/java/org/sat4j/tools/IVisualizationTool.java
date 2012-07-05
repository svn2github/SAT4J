package org.sat4j.tools;

public interface IVisualizationTool {

    public final static Integer NOTGOOD = Integer.MIN_VALUE;

    public void addPoint(double x, double y);

    public void addInvisiblePoint(double x, double y);

    public void init();

    public void end();

}
