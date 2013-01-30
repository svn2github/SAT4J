package org.sat4j.sat.visu;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class JChartBasedSolverVisualisation implements SolverVisualisation {

    private static final String CLEAN = "Clean";

    private static final String RESTART = "Restart";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JFrame visuFrame;

    private int nVar;

    private Chart2D variablesEvaluationChart;
    private Chart2D clausesEvaluationChart;
    private Chart2D learnedClausesSizeChart;
    private Chart2D decisionLevelWhenConflictChart;
    private Chart2D trailLevelWhenConflictChart;
    private Chart2D positiveDecisionVariableChart;
    private Chart2D negativeDecisionVariableChart;
    private Chart2D propagationPerSecondChart;

    private ITrace2D positiveDecisionTrace;
    private ITrace2D negativeDecisionTrace;
    private ITrace2D restartPosDecisionTrace;
    private ITrace2D restartNegDecisionTrace;
    private ITrace2D cleanPosDecisionTrace;
    private ITrace2D cleanNegDecisionTrace;

    private ITrace2D learnedClausesSizeTrace;
    private ITrace2D learnedClausesSizeRestartTrace;
    private ITrace2D learnedClausesSizeCleanTrace;

    private ITrace2D conflictDepthTrace;
    private ITrace2D conflictDepthRestartTrace;
    private ITrace2D conflictDepthCleanTrace;

    private ITrace2D clausesEvaluationTrace;

    private ITrace2D conflictLevelTrace;
    private ITrace2D conflictLevelRestartTrace;
    private ITrace2D conflictLevelCleanTrace;

    private ITrace2D heuristicsTrace;

    private ITrace2D speedTrace;
    private ITrace2D speedCleanTrace;
    private ITrace2D speedRestartTrace;

    private VisuPreferences pref;

    public JChartBasedSolverVisualisation(VisuPreferences pref) {
        this.pref = pref;
        init();
    }

    public void init() {
        this.visuFrame = new JFrame("Visualisation");

        Container c = this.visuFrame.getContentPane();

        int nbGraphs = this.pref.getNumberOfDisplayedGraphs();
        int[] nbLinesTab = new int[] { 1, 2, 3, 2 };

        int nbLines = 3;
        if (nbGraphs < 5) {
            nbLines = nbLinesTab[nbGraphs - 1];
        }

        int nbCols = (nbGraphs - 1) / 3 + 1;

        c.setLayout(new GridLayout(nbLines, nbCols, 5, 5));

        initCharts();

        addChartsToFrame();

        initTraces();

        this.visuFrame.setBackground(this.pref.getBackgroundColor());
        this.visuFrame.setForeground(this.pref.getBorderColor());

        this.visuFrame.setSize(800, 400);

        // Enable the termination button [cross on the upper right edge]:
        this.visuFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                visuFrame.setVisible(false);
            }
        });

    }

    public void initCharts() {
        this.variablesEvaluationChart = new Chart2D();
        this.clausesEvaluationChart = new Chart2D();
        this.learnedClausesSizeChart = new Chart2D();
        this.decisionLevelWhenConflictChart = new Chart2D();
        this.trailLevelWhenConflictChart = new Chart2D();
        this.positiveDecisionVariableChart = new Chart2D();
        this.negativeDecisionVariableChart = new Chart2D();
        this.propagationPerSecondChart = new Chart2D();

        AxisTitle voidTitle = new AxisTitle("");
        this.variablesEvaluationChart.getAxisX().setAxisTitle(voidTitle);
        this.variablesEvaluationChart.getAxisY().setAxisTitle(voidTitle);
        this.clausesEvaluationChart.getAxisX().setAxisTitle(voidTitle);
        this.clausesEvaluationChart.getAxisY().setAxisTitle(voidTitle);
        this.learnedClausesSizeChart.getAxisX().setAxisTitle(voidTitle);
        this.learnedClausesSizeChart.getAxisY().setAxisTitle(voidTitle);
        this.decisionLevelWhenConflictChart.getAxisX().setAxisTitle(voidTitle);
        this.decisionLevelWhenConflictChart.getAxisY().setAxisTitle(voidTitle);
        this.trailLevelWhenConflictChart.getAxisX().setAxisTitle(voidTitle);
        this.trailLevelWhenConflictChart.getAxisY().setAxisTitle(voidTitle);
        this.positiveDecisionVariableChart.getAxisX().setAxisTitle(voidTitle);
        this.positiveDecisionVariableChart.getAxisY().setAxisTitle(voidTitle);
        this.negativeDecisionVariableChart.getAxisX().setAxisTitle(voidTitle);
        this.negativeDecisionVariableChart.getAxisY().setAxisTitle(voidTitle);
        this.propagationPerSecondChart.getAxisX().setAxisTitle(voidTitle);
        this.propagationPerSecondChart.getAxisY().setAxisTitle(voidTitle);
    }

    public void addChartsToFrame() {
        MyChartPanel variablesEvaluationPanel = new MyChartPanel(
                this.variablesEvaluationChart, "Variables evaluation",
                this.pref.getBackgroundColor(), this.pref.getBorderColor());
        MyChartPanel clausesEvaluationPanel = new MyChartPanel(
                this.clausesEvaluationChart, "Clauses evaluation",
                this.pref.getBackgroundColor(), this.pref.getBorderColor());
        MyChartPanel learnedClausesSizePanel = new MyChartPanel(
                this.learnedClausesSizeChart, "Size of learned clauses",
                this.pref.getBackgroundColor(), this.pref.getBorderColor());
        MyChartPanel decisionLevelWhenConflictPanel = new MyChartPanel(
                this.decisionLevelWhenConflictChart,
                "Decision level when conflict", this.pref.getBackgroundColor(),
                this.pref.getBorderColor());
        MyChartPanel trailLevelWhenConflictPanel = new MyChartPanel(
                this.trailLevelWhenConflictChart, "Trail level when conflict",
                this.pref.getBackgroundColor(), this.pref.getBorderColor());
        MyChartPanel positiveDecisionVariablePanel = new MyChartPanel(
                this.positiveDecisionVariableChart, "Positive decision phases",
                this.pref.getBackgroundColor(), this.pref.getBorderColor());
        MyChartPanel negativeDecisionVariablePanel = new MyChartPanel(
                this.negativeDecisionVariableChart, "Negative decision phases",
                this.pref.getBackgroundColor(), this.pref.getBorderColor());
        MyChartPanel propagationPerSecondPanel = new MyChartPanel(
                this.propagationPerSecondChart,
                "Number of propagations per second",
                this.pref.getBackgroundColor(), this.pref.getBorderColor());

        if (this.pref.isDisplayClausesSize()) {
            this.visuFrame.add(learnedClausesSizePanel);
        }
        if (this.pref.isDisplayClausesEvaluation()) {
            this.visuFrame.add(clausesEvaluationPanel);
        }
        if (this.pref.isDisplayConflictsTrail()) {
            this.visuFrame.add(trailLevelWhenConflictPanel);
        }
        if (this.pref.isDisplayDecisionIndexes()) {
            this.visuFrame.add(negativeDecisionVariablePanel);
        }
        if (this.pref.isDisplayVariablesEvaluation()) {
            this.visuFrame.add(variablesEvaluationPanel);
        }
        if (this.pref.isDisplayConflictsDecision()) {
            this.visuFrame.add(decisionLevelWhenConflictPanel);
        }
        if (this.pref.isDisplayDecisionIndexes()) {
            this.visuFrame.add(positiveDecisionVariablePanel);
        }
        if (this.pref.isDisplaySpeed()) {
            this.visuFrame.add(propagationPerSecondPanel);
        }
    }

    public void initTraces() {
        this.positiveDecisionTrace = new Trace2DSimple("Positive decision");
        this.positiveDecisionTrace.setTracePainter(new TracePainterPlus());
        this.positiveDecisionTrace.setColor(new Color(0f, 0.78f, 0.09f));

        this.negativeDecisionTrace = new Trace2DSimple("Negative decision");
        this.negativeDecisionTrace.setTracePainter(new TracePainterPlus());
        this.negativeDecisionTrace.setColor(Color.RED);

        this.restartPosDecisionTrace = new Trace2DSimple(RESTART);
        this.restartPosDecisionTrace
                .setTracePainter(new TracePainterVerticalBar(2,
                        this.positiveDecisionVariableChart));
        this.restartPosDecisionTrace.setColor(Color.LIGHT_GRAY);

        this.restartNegDecisionTrace = new Trace2DSimple(RESTART);
        this.restartNegDecisionTrace
                .setTracePainter(new TracePainterVerticalBar(2,
                        this.negativeDecisionVariableChart));
        this.restartNegDecisionTrace.setColor(Color.LIGHT_GRAY);

        this.cleanPosDecisionTrace = new Trace2DSimple(CLEAN);
        this.cleanPosDecisionTrace.setTracePainter(new TracePainterVerticalBar(
                2, this.positiveDecisionVariableChart));
        this.cleanPosDecisionTrace.setColor(Color.ORANGE);

        this.cleanNegDecisionTrace = new Trace2DSimple(CLEAN);
        this.cleanNegDecisionTrace.setTracePainter(new TracePainterVerticalBar(
                2, this.negativeDecisionVariableChart));
        this.cleanNegDecisionTrace.setColor(Color.ORANGE);

        this.positiveDecisionVariableChart.addTrace(this.positiveDecisionTrace);
        this.positiveDecisionVariableChart
                .addTrace(this.restartPosDecisionTrace);
        this.positiveDecisionVariableChart.addTrace(this.cleanPosDecisionTrace);
        this.positiveDecisionTrace.setZIndex(ITrace2D.ZINDEX_MAX);

        this.positiveDecisionVariableChart.getAxisX().setRangePolicy(
                new RangePolicyHighestValues(8000));

        this.negativeDecisionVariableChart
                .addTrace(this.restartNegDecisionTrace);
        this.negativeDecisionVariableChart.addTrace(this.cleanNegDecisionTrace);
        this.negativeDecisionVariableChart.addTrace(this.negativeDecisionTrace);
        this.negativeDecisionTrace.setZIndex(ITrace2D.ZINDEX_MAX);
        this.negativeDecisionVariableChart.getAxisX().setRangePolicy(
                this.positiveDecisionVariableChart.getAxisX().getRangePolicy());

        this.conflictDepthTrace = new Trace2DLtd(15000, "Trail level");
        this.conflictDepthTrace.setTracePainter(new TracePainterPlus());
        this.conflictDepthTrace.setColor(Color.MAGENTA);
        this.trailLevelWhenConflictChart
                .setName("Trail level when the conflict occurs");
        this.trailLevelWhenConflictChart.addTrace(this.conflictDepthTrace);
        this.conflictDepthTrace.setZIndex(ITrace2D.ZINDEX_MAX);

        this.conflictDepthRestartTrace = new Trace2DSimple(RESTART);
        this.conflictDepthRestartTrace
                .setTracePainter(new TracePainterVerticalBar(2,
                        this.trailLevelWhenConflictChart));
        this.conflictDepthRestartTrace.setColor(Color.LIGHT_GRAY);
        this.trailLevelWhenConflictChart
                .addTrace(this.conflictDepthRestartTrace);

        this.conflictDepthCleanTrace = new Trace2DSimple(CLEAN);
        this.conflictDepthCleanTrace
                .setTracePainter(new TracePainterVerticalBar(2,
                        this.trailLevelWhenConflictChart));
        this.conflictDepthCleanTrace.setColor(Color.ORANGE);
        this.trailLevelWhenConflictChart.addTrace(this.conflictDepthCleanTrace);
        this.trailLevelWhenConflictChart.getAxisX().setRangePolicy(
                new RangePolicyHighestValues(2000));

        this.conflictLevelTrace = new Trace2DSimple("Decision level");
        this.conflictLevelTrace.setTracePainter(new TracePainterPlus());
        this.conflictLevelTrace.setColor(Color.MAGENTA);
        this.decisionLevelWhenConflictChart
                .setName("Decision level chen the conflict occurs");
        this.decisionLevelWhenConflictChart.addTrace(this.conflictLevelTrace);
        this.conflictLevelTrace.setZIndex(ITrace2D.ZINDEX_MAX);

        this.conflictLevelRestartTrace = new Trace2DSimple(RESTART);
        this.conflictLevelRestartTrace
                .setTracePainter(new TracePainterVerticalBar(2,
                        this.decisionLevelWhenConflictChart));
        this.conflictLevelRestartTrace.setColor(Color.LIGHT_GRAY);
        this.decisionLevelWhenConflictChart
                .addTrace(this.conflictLevelRestartTrace);

        this.conflictLevelCleanTrace = new Trace2DSimple(CLEAN);
        this.conflictLevelCleanTrace
                .setTracePainter(new TracePainterVerticalBar(2,
                        this.decisionLevelWhenConflictChart));
        this.conflictLevelCleanTrace.setColor(Color.ORANGE);
        this.decisionLevelWhenConflictChart
                .addTrace(this.conflictLevelCleanTrace);

        this.decisionLevelWhenConflictChart.getAxisX().setRangePolicy(
                new RangePolicyHighestValues(2000));

        this.learnedClausesSizeTrace = new Trace2DSimple("Size");
        this.learnedClausesSizeTrace.setTracePainter(new TracePainterPlus());
        this.learnedClausesSizeTrace.setColor(Color.BLUE);
        this.learnedClausesSizeChart.setName("Learned clauses size");
        this.learnedClausesSizeChart.addTrace(this.learnedClausesSizeTrace);
        this.learnedClausesSizeTrace.setZIndex(ITrace2D.ZINDEX_MAX);
        this.learnedClausesSizeChart.getAxisX().setRangePolicy(
                new RangePolicyHighestValues(2000));

        this.learnedClausesSizeRestartTrace = new Trace2DSimple(RESTART);
        this.learnedClausesSizeRestartTrace
                .setTracePainter(new TracePainterVerticalBar(2,
                        this.learnedClausesSizeChart));
        this.learnedClausesSizeRestartTrace.setColor(Color.LIGHT_GRAY);
        this.learnedClausesSizeChart
                .addTrace(this.learnedClausesSizeRestartTrace);

        this.learnedClausesSizeCleanTrace = new Trace2DSimple(CLEAN);
        this.learnedClausesSizeCleanTrace
                .setTracePainter(new TracePainterVerticalBar(2,
                        this.learnedClausesSizeChart));
        this.learnedClausesSizeCleanTrace.setColor(Color.ORANGE);
        this.learnedClausesSizeChart
                .addTrace(this.learnedClausesSizeCleanTrace);

        this.clausesEvaluationTrace = new Trace2DSimple("Evaluation");
        this.clausesEvaluationTrace.setTracePainter(new TracePainterPlus());
        this.clausesEvaluationTrace.setColor(Color.BLUE);
        this.clausesEvaluationTrace.setName("Clauses evaluation");
        this.clausesEvaluationChart.addTrace(this.clausesEvaluationTrace);

        this.heuristicsTrace = new Trace2DSimple("Evaluation");
        this.heuristicsTrace.setTracePainter(new TracePainterPlus());
        this.heuristicsTrace.setColor(Color.ORANGE);
        this.variablesEvaluationChart.setName("Variables evaluation");
        this.variablesEvaluationChart.addTrace(this.heuristicsTrace);

        this.speedTrace = new Trace2DSimple("Speed");
        this.speedTrace.setColor(new Color(0.02f, .78f, .76f));
        this.speedCleanTrace = new Trace2DSimple(CLEAN);
        this.speedCleanTrace.setColor(Color.ORANGE);
        this.speedCleanTrace.setTracePainter(new TracePainterVerticalBar(2,
                this.propagationPerSecondChart));
        this.speedRestartTrace = new Trace2DSimple(RESTART);
        this.speedRestartTrace.setColor(Color.LIGHT_GRAY);
        this.speedRestartTrace.setTracePainter(new TracePainterVerticalBar(2,
                this.propagationPerSecondChart));

        this.propagationPerSecondChart.addTrace(this.speedCleanTrace);
        this.propagationPerSecondChart.addTrace(this.speedRestartTrace);
        this.propagationPerSecondChart.addTrace(this.speedTrace);
        this.propagationPerSecondChart.getAxisX().setRangePolicy(
                new RangePolicyHighestValues(30));

        this.speedTrace.setZIndex(ITrace2D.ZINDEX_MAX);

    }

    public void setVisible(boolean b) {
        this.visuFrame.setVisible(b);
    }

    public ITrace2D getPositiveDecisionTrace() {
        return this.positiveDecisionTrace;
    }

    public void setPositiveDecisionTrace(ITrace2D positiveDecisionTrace) {
        this.positiveDecisionTrace = positiveDecisionTrace;
    }

    public ITrace2D getNegativeDecisionTrace() {
        return this.negativeDecisionTrace;
    }

    public void setNegativeDecisionTrace(ITrace2D negativeDecisionTrace) {
        this.negativeDecisionTrace = negativeDecisionTrace;
    }

    public ITrace2D getRestartNegDecisionTrace() {
        return this.restartNegDecisionTrace;
    }

    public void setRestartNegDecisionTrace(ITrace2D restartNegDecisionTrace) {
        this.restartNegDecisionTrace = restartNegDecisionTrace;
    }

    public ITrace2D getRestartPosDecisionTrace() {
        return this.restartPosDecisionTrace;
    }

    public void setRestartPosDecisionTrace(ITrace2D restartPosDecisionTrace) {
        this.restartPosDecisionTrace = restartPosDecisionTrace;
    }

    public ITrace2D getConflictDepthTrace() {
        return this.conflictDepthTrace;
    }

    public void setConflictDepthTrace(ITrace2D conflictDepthTrace) {
        this.conflictDepthTrace = conflictDepthTrace;
    }

    public ITrace2D getLearnedClausesSizeTrace() {
        return this.learnedClausesSizeTrace;
    }

    public void setLearnedClausesSizeTrace(ITrace2D learnedClausesSizeTrace) {
        this.learnedClausesSizeTrace = learnedClausesSizeTrace;
    }

    public ITrace2D getClausesEvaluationTrace() {
        return this.clausesEvaluationTrace;
    }

    public void setClausesEvaluationTrace(ITrace2D clausesEvaluationTrace) {
        this.clausesEvaluationTrace = clausesEvaluationTrace;
    }

    public ITrace2D getConflictLevelTrace() {
        return this.conflictLevelTrace;
    }

    public void setConflictLevelTrace(ITrace2D conflictLevelTrace) {
        this.conflictLevelTrace = conflictLevelTrace;
    }

    public ITrace2D getConflictLevelRestartTrace() {
        return this.conflictLevelRestartTrace;
    }

    public void setConflictLevelRestartTrace(ITrace2D conflictLevelRestartTrace) {
        this.conflictLevelRestartTrace = conflictLevelRestartTrace;
    }

    public ITrace2D getHeuristicsTrace() {
        return this.heuristicsTrace;
    }

    public void setHeuristicsTrace(ITrace2D heuristicsTrace) {
        this.heuristicsTrace = heuristicsTrace;
    }

    public ITrace2D getSpeedTrace() {
        return this.speedTrace;
    }

    public void setSpeedTrace(ITrace2D speedTrace) {
        this.speedTrace = speedTrace;
    }

    public ITrace2D getSpeedCleanTrace() {
        return this.speedCleanTrace;
    }

    public void setSpeedCleanTrace(ITrace2D speedCleanTrace) {
        this.speedCleanTrace = speedCleanTrace;
    }

    public ITrace2D getSpeedRestartTrace() {
        return this.speedRestartTrace;
    }

    public void setSpeedRestartTrace(ITrace2D speedRestartTrace) {
        this.speedRestartTrace = speedRestartTrace;
    }

    public ITrace2D getConflictDepthRestartTrace() {
        return this.conflictDepthRestartTrace;
    }

    public void setConflictDepthRestartTrace(ITrace2D conflictDepthRestartTrace) {
        this.conflictDepthRestartTrace = conflictDepthRestartTrace;
    }

    public ITrace2D getLearnedClausesSizeRestartTrace() {
        return this.learnedClausesSizeRestartTrace;
    }

    public void setLearnedClausesSizeRestartTrace(
            ITrace2D learnedClausesSizeRestartTrace) {
        this.learnedClausesSizeRestartTrace = learnedClausesSizeRestartTrace;
    }

    public ITrace2D getLearnedClausesSizeCleanTrace() {
        return this.learnedClausesSizeCleanTrace;
    }

    public void setLearnedClausesSizeCleanTrace(
            ITrace2D learnedClausesSizeCleanTrace) {
        this.learnedClausesSizeCleanTrace = learnedClausesSizeCleanTrace;
    }

    public ITrace2D getConflictLevelCleanTrace() {
        return this.conflictLevelCleanTrace;
    }

    public void setConflictLevelCleanTrace(ITrace2D conflictLevelCleanTrace) {
        this.conflictLevelCleanTrace = conflictLevelCleanTrace;
    }

    public ITrace2D getConflictDepthCleanTrace() {
        return this.conflictDepthCleanTrace;
    }

    public void setConflictDepthCleanTrace(ITrace2D conflictDepthCleanTrace) {
        this.conflictDepthCleanTrace = conflictDepthCleanTrace;
    }

    public ITrace2D getCleanPosDecisionTrace() {
        return this.cleanPosDecisionTrace;
    }

    public void setCleanPosDecisionTrace(ITrace2D cleanPosDecisionTrace) {
        this.cleanPosDecisionTrace = cleanPosDecisionTrace;
    }

    public ITrace2D getCleanNegDecisionTrace() {
        return this.cleanNegDecisionTrace;
    }

    public void setCleanNegDecisionTrace(ITrace2D cleanNegDecisionTrace) {
        this.cleanNegDecisionTrace = cleanNegDecisionTrace;
    }

    public int getnVar() {
        return this.nVar;
    }

    public void setnVar(int nVar) {
        this.nVar = nVar;
    }

    public void start() {
        this.visuFrame.setVisible(true);
    }

    public void end() {
        this.visuFrame.setVisible(false);
    }

}
