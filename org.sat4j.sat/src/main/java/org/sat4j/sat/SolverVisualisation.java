package org.sat4j.sat;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;

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
	
	
	private ITrace2D positiveDecisionTrace;
	private ITrace2D negativeDecisionTrace;
	private ITrace2D restartDecisionTrace;
	
	private ITrace2D learnedClausesSizeTrace;
	
	private ITrace2D conflictDepthTrace;
	
	private ITrace2D clausesEvaluationTrace;
	
	private ITrace2D conflictLevelTrace;
	private ITrace2D conflictLevelRestartTrace;
	
	private ITrace2D heuristicsTrace;
	
	private ITrace2D speedTrace;
	private ITrace2D speedCleanTrace;
	private ITrace2D speedRestartTrace;
	
	public SolverVisualisation(){
		
		visuFrame = new JFrame("Visualisation");
		
		
		Container c = visuFrame.getContentPane();
		c.setLayout(new GridLayout(3,3));
		
		initCharts();
		
		addChartsToFrame();
		
		initTraces();
		
		visuFrame.setSize(800,400);
		
		// Enable the termination button [cross on the upper right edge]: 
//		visuFrame.addWindowListener(
//				new WindowAdapter(){
//					public void windowClosing(WindowEvent e){
//						System.exit(0);
//					}
//				}
//				);
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
	}
	
	public void addChartsToFrame(){
		visuFrame.add(learnedClausesSizeChart);
		visuFrame.add(clausesEvaluationChart);
		visuFrame.add(trailLevelWhenConflictChart);
		visuFrame.add(negativeDecisionVariableChart);
		visuFrame.add(variablesEvaluationChart);
		visuFrame.add(decisionLevelWhenConflictChart);
		visuFrame.add(positiveDecisionVariableChart);
		visuFrame.add(propagationPerSecondChart);
//		visuFrame.add(solutionValueChart);
	}
	
	public void initTraces(){
		//positiveDecisionTrace = new Trace2DLtd(5000, "Positive decision");
		positiveDecisionTrace = new Trace2DSimple("Positive decision");
		positiveDecisionTrace.setTracePainter(new TracePainterDisc());
		positiveDecisionTrace.setColor(Color.GREEN);
		
		negativeDecisionTrace = new Trace2DSimple("Negative decision");
		negativeDecisionTrace.setTracePainter(new TracePainterDisc());
		negativeDecisionTrace.setColor(Color.RED);
		
		restartDecisionTrace = new Trace2DSimple("Restart");
		restartDecisionTrace.setTracePainter(new TracePainterVerticalBar(1,positiveDecisionVariableChart));
		restartDecisionTrace.setColor(Color.GRAY);
		
		
		positiveDecisionVariableChart.addTrace(restartDecisionTrace);
		positiveDecisionVariableChart.addTrace(positiveDecisionTrace);
//		positiveDecisionVariableChart.addTrace(negativeDecisionTrace);
//		positiveDecisionVariableChart.getAxisY().setRange(new Range(0,nVar));
//		positiveDecisionVariableChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0,nVar)));
		positiveDecisionVariableChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(8000));
		
		//positiveDecisionVariableChart.addTrace(restartDecisionTrace);
		
		negativeDecisionVariableChart.addTrace(negativeDecisionTrace);
//		negativeDecisionVariableChart.addTrace(restartDecisionTrace);
		negativeDecisionVariableChart.getAxisX().setRangePolicy(positiveDecisionVariableChart.getAxisX().getRangePolicy());
//		negativeDecisionVariableChart.getAxisY().setRange(new Range(0,nVar));
//		negativeDecisionVariableChart.addTrace(negativeDecisionTrace);
		//negativeDecisionVariableChart.addTrace(restartDecisionTrace);
		
		
		conflictDepthTrace = new Trace2DLtd(2000,"Trail level");
		conflictDepthTrace.setTracePainter(new TracePainterDisc());
		conflictDepthTrace.setColor(Color.MAGENTA);
		trailLevelWhenConflictChart.setName("Trail level when the conflict occurs");
		trailLevelWhenConflictChart.addTrace(conflictDepthTrace);
		
		conflictLevelTrace = new Trace2DLtd(2000, "Decision level");
		conflictLevelTrace.setTracePainter(new TracePainterDisc());
		conflictLevelTrace.setColor(Color.MAGENTA);
		decisionLevelWhenConflictChart.setName("Decision level chen the conflict occurs");
		decisionLevelWhenConflictChart.addTrace(conflictLevelTrace);
		
		conflictLevelRestartTrace = new Trace2DSimple("Restart");
		conflictLevelRestartTrace.setTracePainter(new TracePainterVerticalBar(3,decisionLevelWhenConflictChart));
		conflictLevelRestartTrace.setColor(Color.GRAY);
		//conflictLevelRestartTrace.get
		decisionLevelWhenConflictChart.addTrace(conflictLevelRestartTrace);
		
		learnedClausesSizeTrace = new Trace2DLtd(2000,"Size");
		learnedClausesSizeTrace.setTracePainter(new TracePainterDisc());
		learnedClausesSizeTrace.setColor(Color.BLUE);
		learnedClausesSizeChart.setName("Learned clauses size");
		learnedClausesSizeChart.addTrace(learnedClausesSizeTrace);
		
		clausesEvaluationTrace = new Trace2DLtd(2000, "Evaluation");
		clausesEvaluationTrace.setTracePainter(new TracePainterDisc());
		clausesEvaluationTrace.setColor(Color.BLUE);
		clausesEvaluationTrace.setName("Clauses evaluation");
		clausesEvaluationChart.addTrace(clausesEvaluationTrace);
		
		heuristicsTrace = new Trace2DSimple("Evaluation");
		heuristicsTrace.setTracePainter(new TracePainterDisc());
		heuristicsTrace.setColor(Color.ORANGE);
		variablesEvaluationChart.setName("Variables evaluation");
		variablesEvaluationChart.addTrace(heuristicsTrace);
		
		speedTrace  = new Trace2DSimple("Speed");
		speedTrace.setColor(Color.CYAN);
		speedCleanTrace = new Trace2DSimple("Clean");
		speedCleanTrace.setColor(Color.ORANGE);
		speedCleanTrace.setTracePainter(new TracePainterVerticalBar(1,propagationPerSecondChart));
		speedRestartTrace = new Trace2DSimple("Restart");
		speedRestartTrace.setColor(Color.GRAY);
		speedRestartTrace.setTracePainter(new TracePainterVerticalBar(1,propagationPerSecondChart));
		
		propagationPerSecondChart.addTrace(speedCleanTrace);
		propagationPerSecondChart.addTrace(speedRestartTrace);
		propagationPerSecondChart.addTrace(speedTrace);
		
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

	public ITrace2D getRestartDecisionTrace() {
		return restartDecisionTrace;
	}

	public void setRestartDecisionTrace(ITrace2D restartDecisionTrace) {
		this.restartDecisionTrace = restartDecisionTrace;
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

	public int getnVar() {
		return nVar;
	}

	public void setnVar(int nVar) {
		this.nVar = nVar;
	}
	
	
	
	
	
}
