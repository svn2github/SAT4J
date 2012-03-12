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


	public GnuplotPreferences() {
		this.backgroundColor=Color.black;
		this.borderColor=Color.white;
		this.nbLinesRead=11000;
		this.refreshTime=500;
		this.timeBeforeLaunching=8000;
		this.displayRestarts=true;
		this.restartColor=new Color(0x4F4F4F);
		this.slidingWindows=true;
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

	public String generatePlotLine(GnuplotDataFile[] dataFilesArray,String restartFileName, boolean slidingThisWindows){
		return generatePlotLine(dataFilesArray, new GnuplotFunction[]{}, restartFileName, slidingThisWindows);
	}
	
	
	public String generatePlotLine(GnuplotDataFile[] dataFilesArray,GnuplotFunction[] functions, String restartFileName, boolean slidingThisWindows){
		String s = "plot ";
		String restartString;
		String tailString = "";
		if(slidingWindows && slidingThisWindows){
			tailString = "< tail -" +nbLinesRead+ " ";
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
			s+= comma + "\"" + tailString + dataFilesArray[i].getFilename() 
					+ "\" lc rgb \"#"+rgb + "\" title \"" + dataFilesArray[i].getTitle() + "\" axis x1y1";
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

		return s;
	}


}
