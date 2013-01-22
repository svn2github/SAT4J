package org.sat4j.sat.visu;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.sat4j.specs.ILogAble;

public class GnuplotBasedSolverVisualisation implements SolverVisualisation {

    private static final double TWO_THIRDS = 0.66;

    private static final double ONE_THIRD = 0.33;

    private static final double ZERO = 0.0;

    private static final String SPEED = "Speed";

    private static final String SET_XRANGE_0_5 = "set xrange [0.5:*]";

    private static final String IMPULSES = "impulses";

    private static final String EVALUATION = "Evaluation";

    private static final String SIZE = "Size";

    private static final String CLEAN = "Clean";

    private static final String RESTART = "Restart";

    private static final String CONFLICT_LEVEL = "Conflict Level";

    private static final String GNUPLOT_GNUPLOT = "-gnuplot.gnuplot";

    private static final String SET_MULTIPLOT = "set multiplot";

    private static final String SET_TERMINAL_X11 = "set terminal x11";

    private static final String SPEED_RESTART_DAT = "-speed-restart.dat";

    private static final String SPEED_DAT = "-speed.dat";

    private static final String GNUPLOT_SHOULD_BE_DEACTIVATED = "Gnuplot should be deactivated...";

    private static final String GNUPLOT_SHOULD_HAVE_STARTED_NOW = "Gnuplot should have started now.";

    private static final String GNUPLOT_WILL_START_IN_A_FEW_SECONDS = "Gnuplot will start in a few seconds.";

    private static final String REREAD = "reread";

    private static final String SET_YTICS_AUTO = "set ytics auto";

    private static final String UNSET_MULTIPLOT = "unset multiplot";

    private static final String UNSET_AUTOSCALE = "unset autoscale";

    private static final String SET_Y2RANGE_0 = "set y2range [0:]";

    private static final String SET_AUTOSCALE_YMAX = "set autoscale ymax";

    private static final String SET_NOLOGSCALE_Y = "set nologscale y";

    private static final String SET_NOLOGSCALE_X = "set nologscale x";

    private static final String SET_AUTOSCALE_Y = "set autoscale y";

    private static final String SET_AUTOSCALE_X = "set autoscale x";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private VisuPreferences visuPreferences;
    private int nVar;
    private Process gnuplotProcess;
    private String dataPath;
    private ILogAble logger;

    public GnuplotBasedSolverVisualisation(VisuPreferences visuPref, int nbVar,
            String path, ILogAble logger) {
        this.visuPreferences = visuPref;
        this.nVar = nbVar;
        this.dataPath = path;
        this.logger = logger;
    }

    public void start() {
        traceGnuplot();
    }

    public void end() {
        stopGnuplot();
    }

    public void traceGnuplot() {

        if (this.gnuplotProcess == null) {
            try {

                double verybottom = ZERO;
                double bottom = ONE_THIRD;
                double top = TWO_THIRDS;
                double left = ZERO;
                double middle = ONE_THIRD;
                double right = TWO_THIRDS;

                double width = ONE_THIRD;
                double height = ONE_THIRD;

                PrintStream out = new PrintStream(new FileOutputStream(
                        this.dataPath + GNUPLOT_GNUPLOT));
                out.println(SET_TERMINAL_X11);
                out.println(SET_MULTIPLOT);
                out.println(SET_AUTOSCALE_X);
                out.println(SET_AUTOSCALE_Y);
                out.println(SET_NOLOGSCALE_X);
                out.println(SET_NOLOGSCALE_Y);
                out.println(SET_YTICS_AUTO);

                GnuplotFunction f = new GnuplotFunction("2", Color.black, "");

                // bottom right: Decision Level when conflict
                if (this.visuPreferences.isDisplayConflictsDecision()) {
                    out.println(setSize(width, height));
                    out.println(setOrigin(bottom, right));
                    out.println(setTitle("Decision level at which the conflict occurs"));
                    out.println(SET_AUTOSCALE_YMAX);
                    out.println(SET_Y2RANGE_0);
                    GnuplotDataFile conflictLevelDF = new GnuplotDataFile(
                            this.dataPath + "-conflict-level.dat",
                            Color.magenta, CONFLICT_LEVEL);
                    GnuplotDataFile conflictLevelRestartDF = new GnuplotDataFile(
                            this.dataPath + "-conflict-level-restart.dat",
                            Color.gray, RESTART, IMPULSES);
                    GnuplotDataFile conflictLevelCleanDF = new GnuplotDataFile(
                            this.dataPath + "-conflict-level-clean.dat",
                            Color.orange, CLEAN, IMPULSES);
                    out.println(this.visuPreferences
                            .generatePlotLineOnDifferenteAxes(
                                    new GnuplotDataFile[] { conflictLevelDF },
                                    new GnuplotDataFile[] {
                                            conflictLevelRestartDF,
                                            conflictLevelCleanDF }, true));
                }

                // top left: size of learned clause
                if (this.visuPreferences.isDisplayClausesSize()) {
                    out.println(UNSET_AUTOSCALE);
                    out.println(SET_AUTOSCALE_X);
                    out.println(SET_AUTOSCALE_YMAX);
                    out.println(SET_Y2RANGE_0);
                    out.println(setSize(width, height));
                    out.println(setOrigin(top, left));
                    out.println(setTitle("Size of the clause learned (after minimization if any)"));
                    GnuplotDataFile learnedClausesDF = new GnuplotDataFile(
                            this.dataPath + "-learned-clauses-size.dat",
                            Color.blue, SIZE);
                    GnuplotDataFile learnedClausesRestartDF = new GnuplotDataFile(
                            this.dataPath + "-learned-clauses-size-restart.dat",
                            Color.gray, RESTART, IMPULSES);
                    GnuplotDataFile learnedClausesCleanDF = new GnuplotDataFile(
                            this.dataPath + "-learned-clauses-size-clean.dat",
                            Color.orange, CLEAN, IMPULSES);
                    out.println(this.visuPreferences
                            .generatePlotLineOnDifferenteAxes(
                                    new GnuplotDataFile[] { learnedClausesDF },
                                    new GnuplotDataFile[] {
                                            learnedClausesCleanDF,
                                            learnedClausesRestartDF }, true));
                }

                // top middle: clause activity
                if (this.visuPreferences.isDisplayConflictsDecision()) {
                    out.println(SET_AUTOSCALE_X);
                    out.println(SET_AUTOSCALE_Y);
                    out.println(setSize(width, height));
                    out.println(setOrigin(top, middle));
                    out.println(setTitle("Value of learned clauses evaluation"));
                    GnuplotDataFile learnedDF = new GnuplotDataFile(
                            this.dataPath + "-learned.dat", Color.blue,
                            EVALUATION);
                    out.println(this.visuPreferences.generatePlotLine(
                            learnedDF, f, "", false));
                }

                // for bottom graphs, y range should be O-maxVar
                out.println(SET_AUTOSCALE_X);
                out.println(SET_NOLOGSCALE_X);
                out.println(SET_NOLOGSCALE_Y);
                out.println(SET_AUTOSCALE_Y);
                out.println(setYRangeFrom1ToNvar());
                out.println(setYTicsFrom1ToNVar());

                // bottom left: index decision variable
                if (this.visuPreferences.isDisplayDecisionIndexes()) {
                    out.println(UNSET_AUTOSCALE);
                    out.println("if(system(\"head "
                            + this.dataPath
                            + "-decision-indexes-pos.dat | wc -l\")!=0){set autoscale x;}");
                    out.println("if(system(\"head "
                            + this.dataPath
                            + "-decision-indexes-pos.dat | wc -l\")!=0){set yrange [1:"
                            + this.nVar + "]};");
                    out.println(setSize(width, height));
                    out.println(setOrigin(bottom, left));
                    out.println(setTitle("Index of the decision variables"));
                    GnuplotDataFile negativeDF = new GnuplotDataFile(
                            this.dataPath + "-decision-indexes-neg.dat",
                            Color.red, "Negative Decision");
                    GnuplotDataFile decisionRestartDF = new GnuplotDataFile(
                            this.dataPath + "-decision-indexes-restart.dat",
                            Color.gray, RESTART, IMPULSES);
                    GnuplotDataFile decisionCleanDF = new GnuplotDataFile(
                            this.dataPath + "-decision-indexes-clean.dat",
                            Color.orange, CLEAN, IMPULSES);
                    out.println(this.visuPreferences
                            .generatePlotLineOnDifferenteAxes(
                                    new GnuplotDataFile[] { negativeDF },
                                    new GnuplotDataFile[] { decisionRestartDF,
                                            decisionCleanDF }, true,
                                    this.visuPreferences.getNbLinesRead() * 4));

                    // verybottom left: index decision variable
                    out.println(UNSET_AUTOSCALE);
                    out.println("if(system(\"head "
                            + this.dataPath
                            + "-decision-indexes-pos.dat | wc -l\")!=0){set autoscale x;set yrange [1:"
                            + this.nVar + "]; set y2range [0:]; }");
                    out.println(setSize(width, height));
                    out.println(setOrigin(verybottom, left));
                    out.println(setTitle("Index of the decision variables"));
                    GnuplotDataFile positiveDF = new GnuplotDataFile(
                            this.dataPath + "-decision-indexes-pos.dat",
                            Color.green, "Positive Decision");
                    out.println(this.visuPreferences
                            .generatePlotLineOnDifferenteAxes(
                                    new GnuplotDataFile[] { positiveDF },
                                    new GnuplotDataFile[] { decisionRestartDF,
                                            decisionCleanDF }, true,
                                    this.visuPreferences.getNbLinesRead() * 4));
                }

                // top right: depth search when conflict
                if (this.visuPreferences.isDisplayConflictsTrail()) {
                    out.println(SET_AUTOSCALE_X);
                    out.println(SET_AUTOSCALE_Y);
                    out.println(SET_NOLOGSCALE_X);
                    out.println(SET_NOLOGSCALE_Y);
                    out.println(setSize(width, height));
                    out.println(setOrigin(top, right));
                    out.println(setTitle("Trail level when the conflict occurs"));
                    out.println(SET_Y2RANGE_0);
                    GnuplotDataFile trailLevelDF = new GnuplotDataFile(
                            this.dataPath + "-conflict-depth.dat",
                            Color.magenta, "Trail level");
                    GnuplotFunction nbVar2 = new GnuplotFunction("" + this.nVar
                            / 2, Color.green, "#Var/2");
                    GnuplotDataFile trailLevelRestartDF = new GnuplotDataFile(
                            this.dataPath + "-conflict-depth-restart.dat",
                            Color.gray, RESTART, IMPULSES);
                    GnuplotDataFile trailLevelCleanDF = new GnuplotDataFile(
                            this.dataPath + "-conflict-depth-clean.dat",
                            Color.orange, CLEAN, IMPULSES);
                    out.println(this.visuPreferences
                            .generatePlotLineOnDifferenteAxes(
                                    new GnuplotDataFile[] { trailLevelDF },
                                    new GnuplotDataFile[] {
                                            trailLevelRestartDF,
                                            trailLevelCleanDF },
                                    new GnuplotFunction[] { nbVar2 }, true));
                    out.println(this.visuPreferences.generatePlotLine(
                            trailLevelDF, nbVar2, this.dataPath
                                    + "-conflict-level-restart.dat", true));
                }

                // bottom middle: variable activity
                if (this.visuPreferences.isDisplayVariablesEvaluation()) {
                    out.println(UNSET_AUTOSCALE);
                    out.println(SET_AUTOSCALE_Y);
                    out.println(SET_NOLOGSCALE_X);
                    out.println(SET_NOLOGSCALE_Y);
                    out.println(setYRangeFrom1ToNvar());
                    out.println(SET_XRANGE_0_5);
                    out.println(setSize(width, height));
                    out.println(setOrigin(bottom, middle));
                    out.println(setTitle("Value of variables activity"));
                    GnuplotDataFile heuristicsDF = new GnuplotDataFile(
                            this.dataPath + "-heuristics.dat", Color.red,
                            "Activity", "lines");
                    out.println(this.visuPreferences.generatePlotLine(
                            heuristicsDF, f, "", false));
                }

                if (this.visuPreferences.isDisplaySpeed()) {
                    out.println(SET_AUTOSCALE_X);
                    out.println(SET_NOLOGSCALE_X);
                    out.println(SET_NOLOGSCALE_Y);
                    out.println(setSize(width, height));
                    out.println(setOrigin(verybottom, middle));
                    out.println(setTitle("Number of propagations per second"));
                    out.println(SET_Y2RANGE_0);
                    GnuplotDataFile speedDF = new GnuplotDataFile(this.dataPath
                            + SPEED_DAT, Color.cyan, SPEED, "lines");
                    GnuplotDataFile cleanDF = new GnuplotDataFile(this.dataPath
                            + "-speed-clean.dat", Color.orange, CLEAN, IMPULSES);
                    GnuplotDataFile restartDF = new GnuplotDataFile(
                            this.dataPath + SPEED_RESTART_DAT, Color.gray,
                            RESTART, IMPULSES);
                    out.println(this.visuPreferences
                            .generatePlotLineOnDifferenteAxes(
                                    new GnuplotDataFile[] { speedDF },
                                    new GnuplotDataFile[] { cleanDF, restartDF },
                                    true, 50));
                }
                out.println(UNSET_MULTIPLOT);
                int pauseTime = this.visuPreferences.getRefreshTime() / 1000;
                out.println("pause " + pauseTime);
                out.println(REREAD);
                out.close();

                this.logger.log(GNUPLOT_WILL_START_IN_A_FEW_SECONDS);

                Thread errorStreamThread = new Thread() {
                    @Override
                    public void run() {

                        try {
                            try {
                                Thread.sleep(GnuplotBasedSolverVisualisation.this.visuPreferences
                                        .getTimeBeforeLaunching());
                            } catch (InterruptedException e) {
                                GnuplotBasedSolverVisualisation.this.logger
                                        .log(e.getMessage());
                            }

                            GnuplotBasedSolverVisualisation.this.logger
                                    .log("reads "
                                            + GnuplotBasedSolverVisualisation.this.dataPath
                                            + GNUPLOT_GNUPLOT);
                            GnuplotBasedSolverVisualisation.this.gnuplotProcess = Runtime
                                    .getRuntime()
                                    .exec(GnuplotBasedSolverVisualisation.this.visuPreferences
                                            .createCommandLine(GnuplotBasedSolverVisualisation.this.dataPath
                                                    + GNUPLOT_GNUPLOT));

                            GnuplotBasedSolverVisualisation.this.logger
                                    .log(GNUPLOT_SHOULD_HAVE_STARTED_NOW);

                            BufferedReader gnuInt = new BufferedReader(
                                    new InputStreamReader(
                                            GnuplotBasedSolverVisualisation.this.gnuplotProcess
                                                    .getErrorStream()));
                            String s;

                            while ((s = gnuInt.readLine()) != null) {
                                if (s.trim().length() > 0
                                        && !s.toLowerCase().contains("warning")
                                        && !s.toLowerCase().contains("plot")) {
                                    GnuplotBasedSolverVisualisation.this.logger
                                            .log(s);
                                }
                            }
                            gnuInt.close();
                        } catch (IOException e) {
                            GnuplotBasedSolverVisualisation.this.logger.log(e
                                    .getMessage());
                        }
                    }
                };
                errorStreamThread.start();

            } catch (IOException e) {
                GnuplotBasedSolverVisualisation.this.logger.log(e.getMessage());
            }
        }
    }

    private String setYTicsFrom1ToNVar() {
        return "set ytics add (1," + this.nVar + ")";
    }

    private String setYRangeFrom1ToNvar() {
        return "set yrange [1:" + this.nVar + "]";
    }

    private String setOrigin(double bottom, double right) {
        return "set origin " + right + "," + bottom;
    }

    private String setSize(double width, double height) {
        return "set size " + width + "," + height;
    }

    private String setTitle(String title) {
        return "set title \"" + title + "\"";

    }

    public void stopGnuplot() {
        if (this.gnuplotProcess != null) {
            this.gnuplotProcess.destroy();
            this.logger.log(GNUPLOT_SHOULD_BE_DEACTIVATED);
        }
        this.gnuplotProcess = null;
    }

    public void setnVar(int n) {
        this.nVar = n;
    }

}
