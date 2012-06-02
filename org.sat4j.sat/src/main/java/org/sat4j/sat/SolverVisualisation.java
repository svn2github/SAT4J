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
	
//	private Chart2D variablesEvaluationChart;
//	private Chart2D clausesEvaluationChart;
//	private Chart2D learnedClausesSizeChart;
//	private Chart2D decisionLevelWhenConflictChart;
	private Chart2D trailLevelWhenConflictChart;
	private Chart2D positiveDecisionVariableChart;
	private Chart2D negativeDecisionVariableChart;
//	private Chart2D propagationPerSecondChart;
//	private Chart2D solutionValueChart;
	
	
	private ITrace2D positiveDecisionTrace;
	private ITrace2D negativeDecisionTrace;
	private ITrace2D restartDecisionTrace;
	
	private ITrace2D conflictDepthTrace;
	
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
//		variablesEvaluationChart = new Chart2D();
//		clausesEvaluationChart = new Chart2D();
//		learnedClausesSizeChart = new Chart2D();
//		decisionLevelWhenConflictChart = new Chart2D();
		trailLevelWhenConflictChart = new Chart2D();
		positiveDecisionVariableChart = new Chart2D();
		negativeDecisionVariableChart = new Chart2D();
//		propagationPerSecondChart = new Chart2D();
//		solutionValueChart = new Chart2D();
	}
	
	public void addChartsToFrame(){
//		visuFrame.add(learnedClausesSizeChart);
//		visuFrame.add(clausesEvaluationChart);
		visuFrame.add(trailLevelWhenConflictChart);
		visuFrame.add(negativeDecisionVariableChart);
//		visuFrame.add(variablesEvaluationChart);
//		visuFrame.add(decisionLevelWhenConflictChart);
		visuFrame.add(positiveDecisionVariableChart);
//		visuFrame.add(propagationPerSecondChart);
//		visuFrame.add(solutionValueChart);
	}
	
	public void initTraces(){
		//positiveDecisionTrace = new Trace2DLtd(5000, "Positive decision");
		positiveDecisionTrace = new Trace2DSimple("positive");
		positiveDecisionTrace.setTracePainter(new TracePainterDisc());
		positiveDecisionTrace.setColor(Color.GREEN);
		
		negativeDecisionTrace = new Trace2DSimple("Negative decision");
		negativeDecisionTrace.setTracePainter(new TracePainterDisc());
		negativeDecisionTrace.setColor(Color.RED);
		
		restartDecisionTrace = new Trace2DSimple("Restart");
		restartDecisionTrace.setTracePainter(new TracePainterVerticalBar(3,positiveDecisionVariableChart));
		restartDecisionTrace.setColor(Color.GRAY);
		
		
		positiveDecisionVariableChart.addTrace(restartDecisionTrace);
		positiveDecisionVariableChart.addTrace(positiveDecisionTrace);
//		positiveDecisionVariableChart.addTrace(negativeDecisionTrace);
		positiveDecisionVariableChart.getAxisY().setRange(new Range(0,nVar));
		positiveDecisionVariableChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0,nVar)));
		positiveDecisionVariableChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(5000));
		
		//positiveDecisionVariableChart.addTrace(restartDecisionTrace);
		
		negativeDecisionVariableChart.addTrace(negativeDecisionTrace);
//		negativeDecisionVariableChart.addTrace(restartDecisionTrace);
		negativeDecisionVariableChart.getAxisX().setRangePolicy(positiveDecisionVariableChart.getAxisX().getRangePolicy());
		negativeDecisionVariableChart.getAxisY().setRange(new Range(0,nVar));
//		negativeDecisionVariableChart.addTrace(negativeDecisionTrace);
		//negativeDecisionVariableChart.addTrace(restartDecisionTrace);
		
		conflictDepthTrace = new Trace2DLtd(2000);
		conflictDepthTrace.setTracePainter(new TracePainterDisc());
		conflictDepthTrace.setColor(Color.MAGENTA);
		
		trailLevelWhenConflictChart.addTrace(conflictDepthTrace);
		
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

	
	
	
	
	
	public int getnVar() {
		return nVar;
	}

	public void setnVar(int nVar) {
		this.nVar = nVar;
	}
	
	
	
	
	
}
