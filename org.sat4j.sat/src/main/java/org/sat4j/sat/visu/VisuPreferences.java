package org.sat4j.sat.visu;

import java.awt.Color;

public class VisuPreferences {

    private static final int DEFAULT_TIME_BEFORE_LAUNCHING = 8000;
    private static final int DEFAULT_REFRESH_TIME = 500;
    private static final int DEFAULT_LINES_READ = 11000;
    private static final String AXIS_X1Y1 = "\" axis x1y1";
    private static final String TITLE = "\" title \"";
    private static final String LC_RGB = " lc rgb \"#";
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
        return this.displayClausesSize;
    }

    public void setDisplayClausesSize(boolean displayClausesSize) {
        this.displayClausesSize = displayClausesSize;
    }

    public VisuPreferences() {
        this.backgroundColor = Color.white;
        this.borderColor = Color.black;
        this.nbLinesRead = DEFAULT_LINES_READ;
        this.refreshTime = DEFAULT_REFRESH_TIME;
        this.timeBeforeLaunching = DEFAULT_TIME_BEFORE_LAUNCHING;
        this.displayRestarts = true;
        this.restartColor = Color.LIGHT_GRAY;
        this.slidingWindows = true;

        this.displayDecisionIndexes = true;
        this.displayConflictsTrail = true;
        this.displaySpeed = false;
        this.displayConflictsDecision = true;
        this.displayVariablesEvaluation = false;
        this.displayClausesEvaluation = true;
        this.displayClausesSize = true;
    }

    public int getNumberOfDisplayedGraphs() {
        int n = 0;

        if (this.displayClausesEvaluation) {
            n++;
        }
        if (this.displayConflictsTrail) {
            n++;
        }
        if (this.displayConflictsDecision) {
            n++;
        }
        if (this.displayDecisionIndexes) {
            n += 2;
        }
        if (this.displaySpeed) {
            n++;
        }
        if (this.displayVariablesEvaluation) {
            n++;
        }
        if (this.displayClausesSize) {
            n++;
        }

        return n;
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getBorderColor() {
        return this.borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public int getTimeBeforeLaunching() {
        return this.timeBeforeLaunching;
    }

    public void setTimeBeforeLaunching(int timeBeforeLaunching) {
        this.timeBeforeLaunching = timeBeforeLaunching;
    }

    public int getRefreshTime() {
        return this.refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public int getNbLinesRead() {
        return this.nbLinesRead;
    }

    public void setNbLinesRead(int nbLinesRead) {
        this.nbLinesRead = nbLinesRead;
    }

    public boolean isDisplayRestarts() {
        return this.displayRestarts;
    }

    public void setDisplayRestarts(boolean displayRestarts) {
        this.displayRestarts = displayRestarts;
    }

    public Color getRestartColor() {
        return this.restartColor;
    }

    public void setRestartColor(Color restartColor) {
        this.restartColor = restartColor;
    }

    public boolean isSlidingWindows() {
        return this.slidingWindows;
    }

    public void setSlidingWindows(boolean slidingWindows) {
        this.slidingWindows = slidingWindows;
    }

    public boolean isDisplayDecisionIndexes() {
        return this.displayDecisionIndexes;
    }

    public void setDisplayDecisionIndexes(boolean displayDecisionIndexes) {
        this.displayDecisionIndexes = displayDecisionIndexes;
    }

    public boolean isDisplaySpeed() {
        return this.displaySpeed;
    }

    public void setDisplaySpeed(boolean displaySpeed) {
        this.displaySpeed = displaySpeed;
    }

    public boolean isDisplayConflictsTrail() {
        return this.displayConflictsTrail;
    }

    public void setDisplayConflictsTrail(boolean displayConflictsTrail) {
        this.displayConflictsTrail = displayConflictsTrail;
    }

    public boolean isDisplayConflictsDecision() {
        return this.displayConflictsDecision;
    }

    public void setDisplayConflictsDecision(boolean displayConflictsDecision) {
        this.displayConflictsDecision = displayConflictsDecision;
    }

    public boolean isDisplayVariablesEvaluation() {
        return this.displayVariablesEvaluation;
    }

    public void setDisplayVariablesEvaluation(boolean displayVariablesEvaluation) {
        this.displayVariablesEvaluation = displayVariablesEvaluation;
    }

    public boolean isDisplayClausesEvaluation() {
        return this.displayClausesEvaluation;
    }

    public void setDisplayClausesEvaluation(boolean displayClausesEvaluation) {
        this.displayClausesEvaluation = displayClausesEvaluation;
    }

    @Override
    public String toString() {
        return "GnuplotPreferences [backgroundColor=" + this.backgroundColor
                + ", borderColor=" + this.borderColor
                + ", timeBeforeLaunching=" + this.timeBeforeLaunching
                + ", refreshTime=" + this.refreshTime + ", slidingWindows="
                + this.slidingWindows + ", nbLinesRead=" + this.nbLinesRead
                + ", displayRestarts=" + this.displayRestarts + "]";
    }

    public String[] createCommandLine(String gnuplotFilename) {
        String rgb = Integer.toHexString(this.backgroundColor.getRGB());
        rgb = rgb.substring(2, rgb.length());
        String rgbBorder = Integer.toHexString(this.borderColor.getRGB());
        rgbBorder = rgbBorder.substring(2, rgbBorder.length());

        String[] cmd = new String[6];
        cmd[0] = "gnuplot";
        cmd[1] = "-bg";
        cmd[2] = "#" + rgb;
        cmd[3] = "-xrm";
        cmd[4] = "gnuplot*borderColor:#" + rgbBorder;
        cmd[5] = gnuplotFilename;

        return cmd;
    }

    public String generatePlotLine(GnuplotDataFile file,
            boolean slidingThisWindow) {
        return generatePlotLine(new GnuplotDataFile[] { file }, "",
                slidingThisWindow);
    }

    public String generatePlotLine(GnuplotDataFile file) {
        return generatePlotLine(new GnuplotDataFile[] { file }, "",
                this.slidingWindows);
    }

    public String generatePlotLine(GnuplotDataFile file, String restartFile) {
        return generatePlotLine(new GnuplotDataFile[] { file }, restartFile,
                this.slidingWindows);
    }

    public String generatePlotLine(GnuplotDataFile file, String restartFile,
            boolean slidingThisWindows) {
        return generatePlotLine(new GnuplotDataFile[] { file }, restartFile,
                slidingThisWindows);
    }

    public String generatePlotLine(GnuplotDataFile file, String restartFile,
            boolean slidingThisWindows, int nbLinesToShow) {
        return generatePlotLine(new GnuplotDataFile[] { file },
                new GnuplotFunction[] {}, restartFile, slidingThisWindows,
                nbLinesToShow);
    }

    public String generatePlotLine(GnuplotDataFile[] dataFilesArray,
            String restartFileName, boolean slidingThisWindows) {
        return generatePlotLine(dataFilesArray, new GnuplotFunction[] {},
                restartFileName, slidingThisWindows);
    }

    public String generatePlotLine(GnuplotDataFile file,
            GnuplotFunction function, String restartFile,
            boolean slidingThisWindows, int nbLinesToShow) {
        return generatePlotLine(new GnuplotDataFile[] { file },
                new GnuplotFunction[] { function }, restartFile,
                slidingThisWindows, nbLinesToShow);
    }

    public String generatePlotLine(GnuplotDataFile dataFile,
            GnuplotFunction function, String restartFileName,
            boolean slidingThisWindows) {
        return generatePlotLine(new GnuplotDataFile[] { dataFile },
                new GnuplotFunction[] { function }, restartFileName,
                slidingThisWindows);
    }

    public String generatePlotLine(GnuplotDataFile[] dataFilesArray,
            GnuplotFunction[] functions, String restartFileName,
            boolean slidingThisWindows) {
        return generatePlotLine(dataFilesArray, functions, restartFileName,
                slidingThisWindows, this.nbLinesRead);
    }

    public String generatePlotLine(GnuplotDataFile[] dataFilesArray,
            GnuplotFunction[] functions, String restartFileName,
            boolean slidingThisWindows, int nbLinesTosShow) {
        String result;
        if (restartFileName.length() == 0) {
            result = "if(system(\"head " + dataFilesArray[0].getFilename()
                    + " | wc -l\")!=0){";
        } else {
            result = "if(system(\"head " + dataFilesArray[0].getFilename()
                    + " | wc -l\")!=0 && system(\"head " + restartFileName
                    + " | wc -l\")!=0){";
        }

        StringBuffer buf = new StringBuffer();

        buf.append("plot");

        String restartString;
        String tailString = "";
        if (this.slidingWindows && slidingThisWindows) {
            tailString = "< tail -" + nbLinesTosShow + " ";
        }
        boolean useRestart = this.displayRestarts
                && restartFileName.length() > 0;
        if (useRestart) {
            String rgb = Integer.toHexString(this.restartColor.getRGB());
            rgb = rgb.substring(2, rgb.length());
            restartString = "\"" + tailString + restartFileName + "\""
                    + " with impulses lc rgb \"#" + rgb
                    + "\" title \"Restart\" axis x1y2";
            buf.append(restartString);
        }
        for (int i = 0; i < dataFilesArray.length; i++) {
            String rgb = Integer.toHexString(dataFilesArray[i].getColor()
                    .getRGB());
            rgb = rgb.substring(2, rgb.length());
            String comma = "";
            if (useRestart || i != 0) {
                comma = ",";
            }
            String style = "";
            if (dataFilesArray[i].getStyle().length() > 0) {
                style = " with " + dataFilesArray[i].getStyle();
            }
            buf.append(comma + "\"" + tailString
                    + dataFilesArray[i].getFilename() + "\"" + style
                    + LC_RGB + rgb + TITLE
                    + dataFilesArray[i].getTitle() + AXIS_X1Y1);
        }

        for (int i = 0; i < functions.length; i++) {
            String rgb = Integer.toHexString(functions[i].getColor().getRGB());
            rgb = rgb.substring(2, rgb.length());
            String comma = "";
            if (dataFilesArray.length > 0 || useRestart || i != 0) {
                comma = ",";
            }
            buf.append(comma + functions[i].getFunctionExpression()
                    + LC_RGB + rgb + TITLE
                    + functions[i].getFunctionLegend() + AXIS_X1Y1);
        }
        result += buf.toString() + "}";
        return result;
    }

    public String generatePlotLineOnDifferenteAxes(GnuplotDataFile[] dfArray1,
            GnuplotDataFile[] dfArray2, boolean slidingThisWindow) {
        return generatePlotLineOnDifferenteAxes(dfArray1, dfArray2,
                slidingThisWindow, this.nbLinesRead);
    }

    public String generatePlotLineOnDifferenteAxes(GnuplotDataFile[] dfArray1,
            GnuplotDataFile[] dfArray2, boolean slidingThisWindow, int nbLines) {
        return generatePlotLineOnDifferenteAxes(dfArray1, dfArray2,
                new GnuplotFunction[] {}, slidingThisWindow, nbLines);
    }

    public String generatePlotLineOnDifferenteAxes(GnuplotDataFile[] dfArray1,
            GnuplotDataFile[] dfArray2, GnuplotFunction[] functions,
            boolean slidingThisWindow) {
        return generatePlotLineOnDifferenteAxes(dfArray1, dfArray2,
                new GnuplotFunction[] {}, slidingThisWindow, this.nbLinesRead);
    }

    public String generatePlotLineOnDifferenteAxes(GnuplotDataFile[] dfArray1,
            GnuplotDataFile[] dfArray2, GnuplotFunction[] functions,
            boolean slidingThisWindows, int nbLines) {

        StringBuffer buf = new StringBuffer();

        buf.append("plot ");

        String tailString = "";
        if (this.slidingWindows && slidingThisWindows) {
            tailString = "< tail -" + nbLines + " ";
        }

        for (int i = 0; i < dfArray2.length; i++) {
            String rgb = Integer.toHexString(dfArray2[i].getColor().getRGB());
            rgb = rgb.substring(2, rgb.length());
            String comma = "";
            if (i != 0) {
                comma = ",";
            }

            String style = "";
            if (dfArray2[i].getStyle().length() > 0) {
                style = " with " + dfArray2[i].getStyle();
            }
            buf.append(comma + "\"" + tailString + dfArray2[i].getFilename()
                    + "\"" + style + LC_RGB + rgb + TITLE
                    + dfArray2[i].getTitle() + "\" axis x1y2");
        }

        for (int i = 0; i < dfArray1.length; i++) {
            String rgb = Integer.toHexString(dfArray1[i].getColor().getRGB());
            rgb = rgb.substring(2, rgb.length());
            String comma = "";
            if (dfArray2.length > 0 || i != 0) {
                comma = ",";
            }
            String style = "";
            if (dfArray1[i].getStyle().length() > 0) {
                style = " with " + dfArray1[i].getStyle();
            }
            buf.append(comma + "\"" + tailString + dfArray1[i].getFilename()
                    + "\"" + style + LC_RGB + rgb + TITLE
                    + dfArray1[i].getTitle() + AXIS_X1Y1);
        }

        for (int i = 0; i < functions.length; i++) {
            String rgb = Integer.toHexString(functions[i].getColor().getRGB());
            rgb = rgb.substring(2, rgb.length());
            String comma = "";
            if (dfArray1.length > 0 || dfArray2.length > 0 || i != 0) {
                comma = ",";
            }
            buf.append(comma + functions[i].getFunctionExpression()
                    + LC_RGB + rgb + TITLE
                    + functions[i].getFunctionLegend() + AXIS_X1Y1);
        }

        return buf.toString();
    }

}
