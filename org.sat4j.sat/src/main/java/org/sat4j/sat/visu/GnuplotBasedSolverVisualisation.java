package org.sat4j.sat.visu;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.sat4j.minisat.core.ICDCLLogger;

public class GnuplotBasedSolverVisualisation implements SolverVisualisation{

	private VisuPreferences visuPreferences;
	private int nVar;
	private Process gnuplotProcess;
	private String dataPath;
	private ICDCLLogger logger; 
	
	public GnuplotBasedSolverVisualisation(VisuPreferences visuPref, int nbVar, String path, ICDCLLogger logger){
		visuPreferences = visuPref;
		nVar = nbVar;
		dataPath = path;
		this.logger=logger;
	}
	
	public void start() {
		traceGnuplot();
	}

	public void end() {
		stopGnuplot();
	}

	public void traceGnuplot(){


		if(gnuplotProcess==null)
			try {

				double verybottom=0.0;
				double bottom=0.33;
				double top=0.66;
				double left=0.0;
				double middle=0.33;
				double right=0.66;

				double width=0.33;
				double height=0.33;



				PrintStream out = new PrintStream(new FileOutputStream(dataPath+"-gnuplot.gnuplot"));
				out.println("set terminal x11");
				out.println("set multiplot");
				out.println("set autoscale x");
				out.println("set autoscale y");
				out.println("set nologscale x");
				out.println("set nologscale y");
				out.println("set ytics auto");

				GnuplotFunction f = new GnuplotFunction("2", Color.black, "");

				//bottom right: Decision Level when conflict
				if(visuPreferences.isDisplayConflictsDecision()){
					out.println("set size "+width + "," + height);
					out.println("set origin "+right + "," + bottom);
					out.println("set title \"Decision level at which the conflict occurs\"");
					out.println("set autoscale ymax"); 
					out.println("set y2range [0:]");
					GnuplotDataFile conflictLevelDF = new GnuplotDataFile(dataPath+ "-conflict-level.dat",Color.magenta,"Conflict Level");
					GnuplotDataFile conflictLevelRestartDF = new GnuplotDataFile(dataPath+ "-conflict-level-restart.dat",Color.gray,"Restart","impulses");
					GnuplotDataFile conflictLevelCleanDF = new GnuplotDataFile(dataPath+ "-conflict-level-clean.dat",Color.orange,"Clean","impulses");
					//out.println(gnuplotPreferences.generatePlotLine(conflictLevelDF, true));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{conflictLevelDF}, new GnuplotDataFile[]{conflictLevelRestartDF,conflictLevelCleanDF}, true));
					//					out.println(visuPreferences.generatePlotLine(conflictLevelDF,f, instancePath+ "-conflict-level-restart.dat", true));
				}

				//top left: size of learned clause
				if(visuPreferences.isDisplayClausesSize()){
					out.println("unset autoscale");
					out.println("set autoscale x");
					out.println("set autoscale ymax"); 
					out.println("set y2range [0:]");
					//					out.println("set y2range[0:"+nbVariables+"]");
					//					out.println("set autoscale y2");
					//					out.println("set nologscale x");
					//					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+left + "," + top);
					out.println("set title \"Size of the clause learned (after minimization if any)\"");
					GnuplotDataFile learnedClausesDF = new GnuplotDataFile(dataPath+ "-learned-clauses-size.dat",Color.blue,"Size");
					GnuplotDataFile learnedClausesRestartDF = new GnuplotDataFile(dataPath+ "-learned-clauses-size-restart.dat",Color.gray,"Restart","impulses");
					GnuplotDataFile learnedClausesCleanDF = new GnuplotDataFile(dataPath+ "-learned-clauses-size-clean.dat",Color.orange,"Clean","impulses");
					//out.println(gnuplotPreferences.generatePlotLine(learnedClausesDF, true));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{learnedClausesDF}, new GnuplotDataFile[]{learnedClausesCleanDF,learnedClausesRestartDF}, true));
				}

				//top middle: clause activity
				if(visuPreferences.isDisplayConflictsDecision()){
					out.println("set autoscale x");
					out.println("set autoscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+middle + "," + top);
					out.println("set title \"Value of learned clauses evaluation\"");
					GnuplotDataFile learnedDF = new GnuplotDataFile(dataPath+ "-learned.dat",Color.blue,"Evaluation");
					out.println(visuPreferences.generatePlotLine(learnedDF,f,"", false));
				}

				// for bottom graphs, y range should be O-maxVar
				out.println("set autoscale x");
				out.println("set nologscale x");
				out.println("set nologscale y");
				out.println("set autoscale y");
				out.println("set yrange [1:"+nVar+"]");
				out.println("set ytics add (1,"+ nVar +")");

				//bottom left: index decision variable
				if(visuPreferences.isDisplayDecisionIndexes()){
					out.println("unset autoscale");
					out.println("if(system(\"head "+ dataPath+ "-decision-indexes-pos.dat | wc -l\")!=0){set autoscale x;}");
					out.println("if(system(\"head "+ dataPath+ "-decision-indexes-pos.dat | wc -l\")!=0){set yrange [1:"+nVar+"]};");
					//					out.println("set nologscale x");
					//					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+left + "," + bottom);
					out.println("set title \"Index of the decision variables\"");
					GnuplotDataFile negativeDF = new GnuplotDataFile(dataPath+ "-decision-indexes-neg.dat", Color.red,"Negative Decision");
					GnuplotDataFile decisionRestartDF = new GnuplotDataFile(dataPath+ "-decision-indexes-restart.dat",Color.gray,"Restart","impulses");
					GnuplotDataFile decisionCleanDF = new GnuplotDataFile(dataPath+ "-decision-indexes-clean.dat",Color.orange,"Clean","impulses");
					//					out.println(gnuplotPreferences.generatePlotLine(negativeDF, true));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{negativeDF}, new GnuplotDataFile[]{decisionRestartDF,decisionCleanDF}, true, visuPreferences.getNbLinesRead()*4));
					//					out.println(visuPreferences.generatePlotLine(negativeDF,f,instancePath+ "-decision-indexes-restart.dat" , true, visuPreferences.getNbLinesRead()*4));

					//verybottom left: index decision variable
					out.println("unset autoscale");
					out.println("if(system(\"head "+ dataPath+ "-decision-indexes-pos.dat | wc -l\")!=0){set autoscale x;set yrange [1:"+nVar+"]; set y2range [0:]; }");
					//					out.println("set autoscale y");

					//					out.println("if(system(\"head "+ instancePath+ "-decision-indexes-pos.dat | wc -l\")!=0){set yrange [1:"+nbVariables+"];}");
					//					out.println("set nologscale x");
					//					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+left + "," + verybottom);
					out.println("set title \"Index of the decision variables\"");
					GnuplotDataFile positiveDF = new GnuplotDataFile(dataPath+ "-decision-indexes-pos.dat", Color.green,"Positive Decision");
					//					out.println(gnuplotPreferences.generatePlotLine(positiveDF, true));
					//					out.println(visuPreferences.generatePlotLine(positiveDF,f,instancePath+ "-decision-indexes-restart.dat", true, visuPreferences.getNbLinesRead()*4));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{positiveDF}, new GnuplotDataFile[]{decisionRestartDF,decisionCleanDF}, true, visuPreferences.getNbLinesRead()*4));
				}

				//top right: depth search when conflict
				if(visuPreferences.isDisplayConflictsTrail()){
					out.println("set autoscale x");
					out.println("set autoscale y");
					out.println("set nologscale x");
					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+right + "," + top);
					out.println("set title \"Trail level when the conflict occurs\"");
					out.println("set y2range [0:]");
					GnuplotDataFile trailLevelDF = new GnuplotDataFile(dataPath+ "-conflict-depth.dat", Color.magenta, "Trail level");
					GnuplotFunction nbVar2 = new GnuplotFunction(""+nVar/2, Color.green, "#Var/2");
					GnuplotDataFile trailLevelRestartDF = new GnuplotDataFile(dataPath+ "-conflict-depth-restart.dat",Color.gray,"Restart","impulses");
					GnuplotDataFile trailLevelCleanDF = new GnuplotDataFile(dataPath+ "-conflict-depth-clean.dat",Color.orange,"Clean","impulses");
					//					out.println(gnuplotPreferences.generatePlotLine(trailLevelDF,true));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{trailLevelDF}, new GnuplotDataFile[]{trailLevelRestartDF,trailLevelCleanDF}, new GnuplotFunction[]{nbVar2}, true));
					out.println(visuPreferences.generatePlotLine(trailLevelDF, 
							nbVar2, dataPath+ "-conflict-level-restart.dat",true));
				}

				//bottom middle: variable activity
				if(visuPreferences.isDisplayVariablesEvaluation()){
					out.println("unset autoscale");
					out.println("set autoscale y");
					out.println("set nologscale x");
					out.println("set nologscale y");
					out.println("set yrange [1:"+nVar+"]");
					out.println("set xrange [0.5:*]");
					out.println("set size "+width + "," + height);
					out.println("set origin "+middle + "," + bottom);
					out.println("set title \"Value of variables activity\"");
					GnuplotDataFile heuristicsDF = new GnuplotDataFile(dataPath+ "-heuristics.dat",Color.red,"Activity","lines");
					out.println(visuPreferences.generatePlotLine(heuristicsDF, f,"",false));
				}
				//				out.println("plot \"" + instancePath+ "-heuristics.dat\" with lines title \"Activity\"");


				if(visuPreferences.isDisplaySpeed()){
					out.println("set autoscale x");
					out.println("set nologscale x");
					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+middle + "," + verybottom);
					out.println("set title \"Number of propagations per second\"");
					out.println("set y2range [0:]");
					GnuplotDataFile speedDF = new GnuplotDataFile(dataPath+"-speed.dat",Color.cyan,"Speed","lines");
					GnuplotDataFile cleanDF = new GnuplotDataFile(dataPath +"-speed-clean.dat",Color.orange, "Clean", "impulses");
					GnuplotDataFile restartDF = new GnuplotDataFile(dataPath +"-speed-restart.dat",Color.gray, "Restart", "impulses");
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{speedDF}, new GnuplotDataFile[]{cleanDF,restartDF}, true,50));
				}
				out.println("unset multiplot");
				double pauseTime = visuPreferences.getRefreshTime()/1000;
				out.println("pause " + pauseTime);
				out.println("reread");
				out.close();


				logger.log("Gnuplot will start in a few seconds.");



				Thread errorStreamThread = new Thread(){
					public void run(){

						try{
							try {
								Thread.sleep(visuPreferences.getTimeBeforeLaunching());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							logger.log("reads " + dataPath+"-gnuplot.gnuplot");
							gnuplotProcess = Runtime.getRuntime().exec(
									visuPreferences.createCommandLine(dataPath+"-gnuplot.gnuplot"));

							logger.log("Gnuplot should have started now.");

							BufferedReader gnuInt = new BufferedReader(new InputStreamReader(gnuplotProcess.getErrorStream()));
							String s;

							while((s=gnuInt.readLine())!=null){
								if(s.trim().length()>0 && !s.toLowerCase().contains("warning") && !s.toLowerCase().contains("plot"))
									System.out.println(s);
							}
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				};
				errorStreamThread.start();




			} catch (IOException e) { 
				e.printStackTrace();
			}
	}

	public void stopGnuplot(){
		if(gnuplotProcess!=null){
			gnuplotProcess.destroy();
			logger.log("Gnuplot should be deactivated...");
		}
		gnuplotProcess=null;
	}
	
	public void setnVar(int n){
		nVar=n;
	}
	
}
