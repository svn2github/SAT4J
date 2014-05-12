package org.sat4j.tools;

public interface IBackboneProgressListener {

    public void start(int litsToTest);

    public void inProgress(int processed, int initLitsToTest);

    public void end();

}
