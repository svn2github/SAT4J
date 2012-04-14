package org.sat4j.sat;

import java.awt.Color;
import java.util.List;

public class GnuplotPreferences {

	private Color backgroundColor;
	private Color borderColor;
	/**
	 * Time is expressed in ms
	 */
	private int timeBeforeLaunching;

	/**
	 * Time is expressed in ms
	 */
	private int refreshTime;
	private int nbLinesRead;
	private boolean displayRestarts;
	private Color restartColor;
	private boolean slidingWindows;
	
	private boolean displayDecisionIndexes;
	private boolean displaySpeed;
	private boolean displayConflictsTrail;
	private boolean displayConflictsDecision;
	private boolean displayVariablesEvaluation;
	private boolean displayClausesEvaluation;
	private boolean displayClausesSize;


	public boolean isDisplayClausesSize() {
		return displayClausesSize;
	}

	public void setDisplayClausesSize(boolean displayClausesSize) {
		this.displayClausesSize = displayClausesSize;
	}

	public GnuplotPreferences() {
		this.backgroundColor=Color.black;
		this.borderColor=Color.white;
		this.nbLinesRead=11000;
		this.refreshTime=500;
		this.timeBeforeLaunching=8000;
		this.displayRestarts=true;
		this.restartColor=new Color(0x4F4F4F);
		this.slidingWindows=true;
		
		
		this.displayDecisionIndexes=true;
		this.displayConflictsTrail=true;
		this.displaySpeed=false;
		this.displayConflictsDecision=true;
		this.displayVariablesEvaluation=true;
		this.displayClausesEvaluation=true;
		this.displayClausesSize=true;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public int getTimeBeforeLaunching() {
		return timeBeforeLaunching;
	}

	public void setTimeBeforeLaunching(int timeBeforeLaunching) {
		this.timeBeforeLaunching = timeBeforeLaunching;
	}

	public int getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}


	public int getNbLinesRead() {
		return nbLinesRead;
	}

	public void setNbLinesRead(int nbLinesRead) {
		this.nbLinesRead = nbLinesRead;
	}



	public boolean isDisplayRestarts() {
		return displayRestarts;
	}

	public void setDisplayRestarts(boolean displayRestarts) {
		this.displayRestarts = displayRestarts;
	}

	public Color getRestartColor() {
		return restartColor;
	}

	public void setRestartColor(Color restartColor) {
		this.restartColor = restartColor;
	}

	public boolean isSlidingWindows() {
		return slidingWindows;
	}

	public void setSlidingWindows(boolean slidingWindows) {
		this.slidingWindows = slidingWindows;
	}
	
	public boolean isDisplayDecisionIndexes() {
		return displayDecisionIndexes;
	}

	public void setDisplayDecisionIndexes(boolean displayDecisionIndexes) {
		this.displayDecisionIndexes = displayDecisionIndexes;
	}

	public boolean isDisplaySpeed() {
		return displaySpeed;
	}

	public void setDisplaySpeed(boolean displaySpeed) {
		this.displaySpeed = displaySpeed;
	}

	public boolean isDisplayConflictsTrail() {
		return displayConflictsTrail;
	}

	public void setDisplayConflictsTrail(boolean displayConflictsTrail) {
		this.displayConflictsTrail = displayConflictsTrail;
	}

	public boolean isDisplayConflictsDecision() {
		return displayConflictsDecision;
	}

	public void setDisplayConflictsDecision(boolean displayConflictsDecision) {
		this.displayConflictsDecision = displayConflictsDecision;
	}

	public boolean isDisplayVariablesEvaluation() {
		return displayVariablesEvaluation;
	}

	public void setDisplayVariablesEvaluation(boolean displayVariablesEvaluation) {
		this.displayVariablesEvaluation = displayVariablesEvaluation;
	}

	public boolean isDisplayClausesEvaluation() {
		return displayClausesEvaluation;
	}

	public void setDisplayClausesEvaluation(boolean displayClausesEvaluation) {
		this.displayClausesEvaluation = displayClausesEvaluation;
	}

	@Override
	public String toString() {
		return "GnuplotPreferences [backgroundColor=" + backgroundColor
				+ ", borderColor=" + borderColor + ", timeBeforeLaunching="
				+ timeBeforeLaunching + ", refreshTime=" + refreshTime
				+ ", slidingWindows=" + slidingWindows + ", nbLinesRead="
				+ nbLinesRead + ", displayRestarts=" + displayRestarts + "]";
	}

	public String[] createCommandLine(String gnuplotFilename){
		String rgb = Integer.toHexString(backgroundColor.getRGB());
		rgb = rgb.substring(2, rgb.length());
		String rgbBorder = Integer.toHexString(borderColor.getRGB());
		rgbBorder = rgbBorder.substring(2, rgbBorder.length());

		String[] cmd = new String[6];
		cmd[0] = "gnuplot";
		cmd[1] = "-bg";
		cmd[2] = "#"+rgb;
		cmd[3] = "-xrm";
		cmd[4] = "gnuplot*borderColor:#"+rgbBorder;
		cmd[5] = gnuplotFilename;

		return cmd;
	}
	

	public String generatePlotLine(GnuplotDataFile file, boolean slidingThisWindow){
		return generatePlotLine(new GnuplotDataFile[]{file},"",slidingThisWindow);
	}
	
	public String generatePlotLine(GnuplotDataFile file){
		return generatePlotLine(new GnuplotDataFile[]{file},"",this.slidingWindows);
	}
	
	public String generatePlotLine(GnuplotDataFile file, String restartFile){
		return generatePlotLine(new GnuplotDataFile[]{file},restartFile,this.slidingWindows);
	}
	
	public String generatePlotLine(GnuplotDataFile file, String restartFile, boolean slidingThisWindows){
		return generatePlotLine(new GnuplotDataFile[]{file},restartFile,slidingThisWindows);
	}
	
	public String generatePlotLine(GnuplotDataFile file, String restartFile, boolean slidingThisWindows,int nbLinesToShow){
		return generatePlotLine(new GnuplotDataFile[]{file},new GnuplotFunction[]{},restartFile,slidingThisWindows,nbLinesToShow);
	}

	public String generatePlotLine(GnuplotDataFile[] dataFilesArray,String restartFileName, boolean slidingThisWindows){
		return generatePlotLine(dataFilesArray, new GnuplotFunction[]{}, restartFileName, slidingThisWindows);
	}
	
	public String generatePlotLine(GnuplotDataFile file, GnuplotFunction function, String restartFile, boolean slidingThisWindows,int nbLinesToShow){
		return generatePlotLine(new GnuplotDataFile[]{file},new GnuplotFunction[]{function},restartFile,slidingThisWindows,nbLinesToShow);
	}
	
	public String generatePlotLine(GnuplotDataFile dataFile, GnuplotFunction function, String restartFileName, boolean slidingThisWindows){
		return generatePlotLine(new GnuplotDataFile[]{dataFile}, new GnuplotFunction[]{function}, restartFileName, slidingThisWindows);
	}
	
	public String generatePlotLine(GnuplotDataFile[] dataFilesArray,GnuplotFunction[] functions, String restartFileName, boolean slidingThisWindows){
		return generatePlotLine(dataFilesArray, functions, restartFileName, slidingThisWindows,nbLinesRead);
	}
	
	public String generatePlotLine(GnuplotDataFile[] dataFilesArray,GnuplotFunction[] functions, String restartFileName, boolean slidingThisWindows, int nbLinesTosShow){
		String result;
		if(restartFileName.length()==0)
			result="if(system(\"head " + dataFilesArray[0].getFilename() + " | wc -l\")!=0){";
		else{
			result="if(system(\"head " + dataFilesArray[0].getFilename() + " | wc -l\")!=0 && system(\"head " + restartFileName + " | wc -l\")!=0){";
		}
		String s = "plot ";
		String restartString;
		String tailString = "";
		if(slidingWindows && slidingThisWindows){
			tailString = "< tail -" +nbLinesTosShow+ " ";
		}
		boolean useRestart = displayRestarts && restartFileName.length()>0; 
		if(useRestart){
			String rgb = Integer.toHexString(restartColor.getRGB());
			rgb = rgb.substring(2, rgb.length());
			restartString = "\"" +tailString + restartFileName 
					+"\""+" with impulses lc rgb \"#"+ rgb +"\" title \"Restart\" axis x1y2";
			s+=restartString + "";
		}
		for(int i=0; i<dataFilesArray.length;i++){
			String rgb = Integer.toHexString(dataFilesArray[i].getColor().getRGB());
			rgb = rgb.substring(2, rgb.length());
			String comma = "";
			if(useRestart || i!=0){
				comma=",";
			}
			String style="";
			if(dataFilesArray[i].getStyle().length()>0){
				style = " with " + dataFilesArray[i].getStyle();
			}
			s+= comma + "\"" + tailString + dataFilesArray[i].getFilename() 
					+ "\"" + style + " lc rgb \"#"+rgb + "\" title \"" + dataFilesArray[i].getTitle() + "\" axis x1y1";
		}
		
		for(int i=0; i<functions.length;i++){
			String rgb = Integer.toHexString(functions[i].getColor().getRGB());
			rgb = rgb.substring(2, rgb.length());
			String comma = "";
			if(dataFilesArray.length>0 || useRestart || i!=0){
				comma=",";
			}
			s+= comma + functions[i].getFunctionExpression() + " lc rgb \"#"+rgb 
					+ "\" title \"" + functions[i].getFunctionLegend()+ "\" axis x1y1";
		}
		result+=s+"}";
		return result;
	}
	
	
	
	public String generatePlotLineOnDifferenteAxes(GnuplotDataFile[] dfArray1, GnuplotDataFile[] dfArray2, boolean slidingThisWindow){
		return generatePlotLineOnDifferenteAxes(dfArray1, dfArray2, slidingThisWindow, nbLinesRead);
	}
	
	public String generatePlotLineOnDifferenteAxes(GnuplotDataFile[] dfArray1, GnuplotDataFile[] dfArray2, boolean slidingThisWindow, int nbLines){		
		String s = "plot ";
		String tailString = "";
		if(slidingWindows && slidingThisWindow){
			tailString = "< tail -" +nbLines+ " ";
		}
		for(int i=0; i<dfArray1.length;i++){
			String rgb = Integer.toHexString(dfArray1[i].getColor().getRGB());
			rgb = rgb.substring(2, rgb.length());
			String comma = "";
			if(i!=0){
				comma=",";
			}
			String style="";
			if(dfArray1[i].getStyle().length()>0){
				style = " with " + dfArray1[i].getStyle();
			}
			s+= comma + "\"" + tailString + dfArray1[i].getFilename() 
					+ "\"" + style + " lc rgb \"#"+rgb + "\" title \"" + dfArray1[i].getTitle() + "\" axis x1y1";
		}
		
		for(int i=0; i<dfArray2.length;i++){
			String rgb = Integer.toHexString(dfArray2[i].getColor().getRGB());
			rgb = rgb.substring(2, rgb.length());
			String comma = "";
			if(dfArray1.length>0 || i!=0){
				comma=",";
			}
			String style="";
			if(dfArray2[i].getStyle().length()>0){
				style = " with " + dfArray2[i].getStyle();
			}
			s+= comma + "\"" + tailString + dfArray2[i].getFilename() 
					+ "\"" + style + " lc rgb \"#"+rgb + "\" title \"" + dfArray2[i].getTitle() + "\" axis x1y2";
		}
		

		return s;
	}
	
//	public String generatePlotLineOnDifferenteAxes(GnuplotDataFile df1, GnuplotDataFile df2, boolean slidingThisWindow){
//		return generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{df1}, new GnuplotDataFile[]{df2}, slidingThisWindow);
//	}


}
