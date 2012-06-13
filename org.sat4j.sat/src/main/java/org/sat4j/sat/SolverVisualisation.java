package org.sat4j.sat;

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

public class SolverVisualisation {

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
//	private Chart2D solutionValueChart;
	
	private MyChartPanel variablesEvaluationPanel;
	private MyChartPanel clausesEvaluationPanel;
	private MyChartPanel learnedClausesSizePanel;
	private MyChartPanel decisionLevelWhenConflictPanel;
	private MyChartPanel trailLevelWhenConflictPanel;
	private MyChartPanel positiveDecisionVariablePanel;
	private MyChartPanel negativeDecisionVariablePanel;
	private MyChartPanel propagationPerSecondPanel;
	
	
	private ITrace2D positiveDecisionTrace;
	private ITrace2D negativeDecisionTrace;
	private ITrace2D restartPosDecisionTrace;
	private ITrace2D restartNegDecisionTrace;
	
	private ITrace2D learnedClausesSizeTrace;
	private ITrace2D learnedClausesSizeRestartTrace;
	
	private ITrace2D conflictDepthTrace;
	private ITrace2D conflictDepthRestartTrace;
	
	private ITrace2D clausesEvaluationTrace;
	
	private ITrace2D conflictLevelTrace;
	private ITrace2D conflictLevelRestartTrace;
	
	private ITrace2D heuristicsTrace;
	
	private ITrace2D speedTrace;
	private ITrace2D speedCleanTrace;
	private ITrace2D speedRestartTrace;
	
	private VisuPreferences pref;
	private RemoteControlFrame listeningFrame;
	
	public SolverVisualisation(VisuPreferences pref){
		
		visuFrame = new JFrame("Visualisation");
		
		this.pref=pref;
		
		Container c = visuFrame.getContentPane();
		
		int nbGraphs = pref.getNumberOfDisplayedGraphs();
		int[] nbLinesTab = new int[]{1,2,3,2};
	
		int nbLines = 3;
		if(nbGraphs<5)
			nbLines = nbLinesTab[nbGraphs-1];
		
		int nbCols = (nbGraphs-1)/3 + 1;
		
		c.setLayout(new GridLayout(nbLines,nbCols,5,5));
		
		initCharts();
		
		addChartsToFrame();
		
		initTraces();
		
		visuFrame.setBackground(pref.getBackgroundColor());
		visuFrame.setForeground(pref.getBorderColor());
		
		visuFrame.setSize(800,400);
		
		// Enable the termination button [cross on the upper right edge]: 
		visuFrame.addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						
					}
				}
				);
		
		
		visuFrame.setVisible(true); 
		
		
	}
	
	public void initCharts(){
		variablesEvaluationChart = new Chart2D();
		clausesEvaluationChart = new Chart2D();
		learnedClausesSizeChart = new Chart2D();
		decisionLevelWhenConflictChart = new Chart2D();
		trailLevelWhenConflictChart = new Chart2D();
		positiveDecisionVariableChart = new Chart2D();
		negativeDecisionVariableChart = new Chart2D();
		propagationPerSecondChart = new Chart2D();
//		solutionValueChart = new Chart2D();
		
		AxisTitle voidTitle = new AxisTitle("");
		variablesEvaluationChart.getAxisX().setAxisTitle(voidTitle);
		variablesEvaluationChart.getAxisY().setAxisTitle(voidTitle);
		clausesEvaluationChart.getAxisX().setAxisTitle(voidTitle);
		clausesEvaluationChart.getAxisY().setAxisTitle(voidTitle);
		learnedClausesSizeChart.getAxisX().setAxisTitle(voidTitle);
		learnedClausesSizeChart.getAxisY().setAxisTitle(voidTitle);
		decisionLevelWhenConflictChart.getAxisX().setAxisTitle(voidTitle);
		decisionLevelWhenConflictChart.getAxisY().setAxisTitle(voidTitle);
		trailLevelWhenConflictChart.getAxisX().setAxisTitle(voidTitle);
		trailLevelWhenConflictChart.getAxisY().setAxisTitle(voidTitle);
		positiveDecisionVariableChart.getAxisX().setAxisTitle(voidTitle);
		positiveDecisionVariableChart.getAxisY().setAxisTitle(voidTitle);
		negativeDecisionVariableChart.getAxisX().setAxisTitle(voidTitle);
		negativeDecisionVariableChart.getAxisY().setAxisTitle(voidTitle);
		propagationPerSecondChart.getAxisX().setAxisTitle(voidTitle);
		propagationPerSecondChart.getAxisY().setAxisTitle(voidTitle);
//		solutionValueChart.getAxisX().setAxisTitle(voidTitle);
//		solutionValueChart.getAxisY().setAxisTitle(voidTitle);
	}
	
	public void addChartsToFrame(){
		
		variablesEvaluationPanel = new MyChartPanel(variablesEvaluationChart, "Variables evaluation", pref.getBackgroundColor(), pref.getBorderColor());
		clausesEvaluationPanel = new MyChartPanel(clausesEvaluationChart, "Clauses evaluation",pref.getBackgroundColor(), pref.getBorderColor());
		learnedClausesSizePanel = new MyChartPanel(learnedClausesSizeChart, "Size of learned clauses",pref.getBackgroundColor(), pref.getBorderColor());
		decisionLevelWhenConflictPanel = new MyChartPanel(decisionLevelWhenConflictChart, "Decision level when conflict",pref.getBackgroundColor(), pref.getBorderColor());
		trailLevelWhenConflictPanel = new MyChartPanel(trailLevelWhenConflictChart, "Trail level when conflict",pref.getBackgroundColor(), pref.getBorderColor());
		positiveDecisionVariablePanel = new MyChartPanel(positiveDecisionVariableChart, "Positive decision phases",pref.getBackgroundColor(), pref.getBorderColor());
		negativeDecisionVariablePanel = new MyChartPanel(negativeDecisionVariableChart, "Negative decision phases",pref.getBackgroundColor(), pref.getBorderColor());
		propagationPerSecondPanel = new MyChartPanel(propagationPerSecondChart, "Number of propagations per second",pref.getBackgroundColor(), pref.getBorderColor());
		
		
		if(pref.isDisplayClausesSize())
			visuFrame.add(learnedClausesSizePanel);
		if(pref.isDisplayClausesEvaluation())
			visuFrame.add(clausesEvaluationPanel);
		if(pref.isDisplayConflictsTrail())
			visuFrame.add(trailLevelWhenConflictPanel);
		if(pref.isDisplayDecisionIndexes())
			visuFrame.add(negativeDecisionVariablePanel);
		if(pref.isDisplayVariablesEvaluation())
			visuFrame.add(variablesEvaluationPanel);
		if(pref.isDisplayConflictsDecision())
			visuFrame.add(decisionLevelWhenConflictPanel);
		if(pref.isDisplayDecisionIndexes())
			visuFrame.add(positiveDecisionVariablePanel);
		if(pref.isDisplaySpeed())
			visuFrame.add(propagationPerSecondPanel);
//		visuFrame.add(solutionValueChart);
	}
	
	public void initTraces(){
		//positiveDecisionTrace = new Trace2DLtd(5000, "Positive decision");
		positiveDecisionTrace = new Trace2DSimple("Positive decision");
		positiveDecisionTrace.setTracePainter(new TracePainterPlus());
		positiveDecisionTrace.setColor(new Color(0f,0.78f,0.09f));
		
		negativeDecisionTrace = new Trace2DSimple("Negative decision");
		negativeDecisionTrace.setTracePainter(new TracePainterPlus());
		negativeDecisionTrace.setColor(Color.RED);

		
		
		restartPosDecisionTrace = new Trace2DSimple("Restart");
		restartPosDecisionTrace.setTracePainter(new TracePainterVerticalBar(2,positiveDecisionVariableChart));
		restartPosDecisionTrace.setColor(Color.LIGHT_GRAY);
		
		restartNegDecisionTrace = new Trace2DSimple("Restart");
		restartNegDecisionTrace.setTracePainter(new TracePainterVerticalBar(2,negativeDecisionVariableChart));
		restartNegDecisionTrace.setColor(Color.LIGHT_GRAY);
		
		
		positiveDecisionVariableChart.addTrace(positiveDecisionTrace);
		positiveDecisionVariableChart.addTrace(restartPosDecisionTrace);
		//positiveDecisionVariableChart.set
		positiveDecisionTrace.setZIndex(ITrace2D.ZINDEX_MAX);
		
//		positiveDecisionVariableChart.addTrace(negativeDecisionTrace);
//		positiveDecisionVariableChart.getAxisY().setRange(new Range(0,nVar));
//		positiveDecisionVariableChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0,nVar)));
		positiveDecisionVariableChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(8000));
		
		//positiveDecisionVariableChart.addTrace(restartDecisionTrace);
		
		negativeDecisionVariableChart.addTrace(restartNegDecisionTrace);
		negativeDecisionVariableChart.addTrace(negativeDecisionTrace);
		negativeDecisionTrace.setZIndex(ITrace2D.ZINDEX_MAX);
//		negativeDecisionVariableChart.addTrace(restartDecisionTrace);
		negativeDecisionVariableChart.getAxisX().setRangePolicy(positiveDecisionVariableChart.getAxisX().getRangePolicy());
//		negativeDecisionVariableChart.getAxisY().setRange(new Range(0,nVar));
//		negativeDecisionVariableChart.addTrace(negativeDecisionTrace);
		//negativeDecisionVariableChart.addTrace(restartDecisionTrace);
		
		
		conflictDepthTrace = new Trace2DLtd(15000,"Trail level");
		conflictDepthTrace.setTracePainter(new TracePainterPlus());
		conflictDepthTrace.setColor(Color.MAGENTA);
		trailLevelWhenConflictChart.setName("Trail level when the conflict occurs");
		trailLevelWhenConflictChart.addTrace(conflictDepthTrace);
		conflictDepthTrace.setZIndex(ITrace2D.ZINDEX_MAX);
		
		conflictDepthRestartTrace = new Trace2DSimple("Restart");
		conflictDepthRestartTrace.setTracePainter(new TracePainterVerticalBar(2,trailLevelWhenConflictChart));
		conflictDepthRestartTrace.setColor(Color.LIGHT_GRAY);
//		conflictLevelRestartTrace.setZIndex(ITrace2D.Z_INDEX_MIN);
		//conflictLevelRestartTrace.get
		trailLevelWhenConflictChart.addTrace(conflictDepthRestartTrace);
		trailLevelWhenConflictChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(2000));
		
		conflictLevelTrace = new Trace2DSimple("Decision level");
		conflictLevelTrace.setTracePainter(new TracePainterPlus());
		conflictLevelTrace.setColor(Color.MAGENTA);
		decisionLevelWhenConflictChart.setName("Decision level chen the conflict occurs");
		decisionLevelWhenConflictChart.addTrace(conflictLevelTrace);
		conflictLevelTrace.setZIndex(ITrace2D.ZINDEX_MAX);
		
		conflictLevelRestartTrace = new Trace2DSimple("Restart");
		conflictLevelRestartTrace.setTracePainter(new TracePainterVerticalBar(2,decisionLevelWhenConflictChart));
		conflictLevelRestartTrace.setColor(Color.LIGHT_GRAY);
//		conflictLevelRestartTrace.setZIndex(ITrace2D.Z_INDEX_MIN);
		//conflictLevelRestartTrace.get
		decisionLevelWhenConflictChart.addTrace(conflictLevelRestartTrace);
		decisionLevelWhenConflictChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(2000));
		
		learnedClausesSizeTrace = new Trace2DSimple("Size");
		learnedClausesSizeTrace.setTracePainter(new TracePainterPlus());
		learnedClausesSizeTrace.setColor(Color.BLUE);
		learnedClausesSizeChart.setName("Learned clauses size");
		learnedClausesSizeChart.addTrace(learnedClausesSizeTrace);
		learnedClausesSizeTrace.setZIndex(ITrace2D.ZINDEX_MAX);
		learnedClausesSizeChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(2000));
		
		learnedClausesSizeRestartTrace = new Trace2DSimple("Restart");
		learnedClausesSizeRestartTrace.setTracePainter(new TracePainterVerticalBar(2,learnedClausesSizeChart));
		learnedClausesSizeRestartTrace.setColor(Color.LIGHT_GRAY);
		learnedClausesSizeChart.addTrace(learnedClausesSizeRestartTrace);
		
		clausesEvaluationTrace = new Trace2DSimple("Evaluation");
		clausesEvaluationTrace.setTracePainter(new TracePainterPlus());
		clausesEvaluationTrace.setColor(Color.BLUE);
		clausesEvaluationTrace.setName("Clauses evaluation");
		clausesEvaluationChart.addTrace(clausesEvaluationTrace);
		
		heuristicsTrace = new Trace2DSimple("Evaluation");
		heuristicsTrace.setTracePainter(new TracePainterPlus());
		heuristicsTrace.setColor(Color.ORANGE);
		variablesEvaluationChart.setName("Variables evaluation");
		variablesEvaluationChart.addTrace(heuristicsTrace);
		
		speedTrace  = new Trace2DSimple("Speed");
		speedTrace.setColor(new Color(0.02f,.78f,.76f));
		speedCleanTrace = new Trace2DSimple("Clean");
		speedCleanTrace.setColor(Color.ORANGE);
		speedCleanTrace.setTracePainter(new TracePainterVerticalBar(2,propagationPerSecondChart));
		speedRestartTrace = new Trace2DSimple("Restart");
		speedRestartTrace.setColor(Color.LIGHT_GRAY);
		speedRestartTrace.setTracePainter(new TracePainterVerticalBar(2,propagationPerSecondChart));
		
		propagationPerSecondChart.addTrace(speedCleanTrace);
		propagationPerSecondChart.addTrace(speedRestartTrace);
		propagationPerSecondChart.addTrace(speedTrace);
		propagationPerSecondChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(30));
		
		speedTrace.setZIndex(ITrace2D.ZINDEX_MAX);
		
	}

	public ITrace2D getPositiveDecisionTrace() {
		return positiveDecisionTrace;
	}

	public void setPositiveDecisionTrace(ITrace2D positiveDecisionTrace) {
		this.positiveDecisionTrace = positiveDecisionTrace;
	}

	public ITrace2D getNegativeDecisionTrace() {
		return negativeDecisionTrace;
	}

	public void setNegativeDecisionTrace(ITrace2D negativeDecisionTrace) {
		this.negativeDecisionTrace = negativeDecisionTrace;
	}

	public ITrace2D getRestartNegDecisionTrace() {
		return restartNegDecisionTrace;
	}

	public void setRestartNegDecisionTrace(ITrace2D restartNegDecisionTrace) {
		this.restartNegDecisionTrace = restartNegDecisionTrace;
	}
	
	public ITrace2D getRestartPosDecisionTrace() {
		return restartPosDecisionTrace;
	}

	public void setRestartPosDecisionTrace(ITrace2D restartPosDecisionTrace) {
		this.restartPosDecisionTrace = restartPosDecisionTrace;
	}

	public ITrace2D getConflictDepthTrace() {
		return conflictDepthTrace;
	}

	public void setConflictDepthTrace(ITrace2D conflictDepthTrace) {
		this.conflictDepthTrace = conflictDepthTrace;
	}


	public ITrace2D getLearnedClausesSizeTrace() {
		return learnedClausesSizeTrace;
	}

	public void setLearnedClausesSizeTrace(ITrace2D learnedClausesSizeTrace) {
		this.learnedClausesSizeTrace = learnedClausesSizeTrace;
	}

	public ITrace2D getClausesEvaluationTrace() {
		return clausesEvaluationTrace;
	}

	public void setClausesEvaluationTrace(ITrace2D clausesEvaluationTrace) {
		this.clausesEvaluationTrace = clausesEvaluationTrace;
	}
	
	public ITrace2D getConflictLevelTrace() {
		return conflictLevelTrace;
	}

	public void setConflictLevelTrace(ITrace2D conflictLevelTrace) {
		this.conflictLevelTrace = conflictLevelTrace;
	}

	public ITrace2D getConflictLevelRestartTrace() {
		return conflictLevelRestartTrace;
	}

	public void setConflictLevelRestartTrace(ITrace2D conflictLevelRestartTrace) {
		this.conflictLevelRestartTrace = conflictLevelRestartTrace;
	}

	public ITrace2D getHeuristicsTrace() {
		return heuristicsTrace;
	}

	public void setHeuristicsTrace(ITrace2D heuristicsTrace) {
		this.heuristicsTrace = heuristicsTrace;
	}

	public ITrace2D getSpeedTrace() {
		return speedTrace;
	}

	public void setSpeedTrace(ITrace2D speedTrace) {
		this.speedTrace = speedTrace;
	}
	
	public ITrace2D getSpeedCleanTrace() {
		return speedCleanTrace;
	}

	public void setSpeedCleanTrace(ITrace2D speedCleanTrace) {
		this.speedCleanTrace = speedCleanTrace;
	}

	public ITrace2D getSpeedRestartTrace() {
		return speedRestartTrace;
	}

	public void setSpeedRestartTrace(ITrace2D speedRestartTrace) {
		this.speedRestartTrace = speedRestartTrace;
	}
	
	public ITrace2D getConflictDepthRestartTrace() {
		return conflictDepthRestartTrace;
	}

	public void setConflictDepthRestartTrace(ITrace2D conflictDepthRestartTrace) {
		this.conflictDepthRestartTrace = conflictDepthRestartTrace;
	}
	
	public ITrace2D getLearnedClausesSizeRestartTrace() {
		return learnedClausesSizeRestartTrace;
	}

	public void setLearnedClausesSizeRestartTrace(
			ITrace2D learnedClausesSizeRestartTrace) {
		this.learnedClausesSizeRestartTrace = learnedClausesSizeRestartTrace;
	}

	public int getnVar() {
		return nVar;
	}

	public void setnVar(int nVar) {
		this.nVar = nVar;
	}
	
	
	
	
	
}
