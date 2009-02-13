package org.sat4j.apps.sudoku;

import javax.swing.JMenuBar;

public interface MainProgramWindow {
    public void setMainWindowSize(int width, int height);

    public void maximize();

    public boolean fileAccess();

    public String readFile();

    public void writeFile(String s);

    public void pack();

    public boolean isApplet();

    public boolean isApplication();

    public boolean isWebStart();

    public JMenuBar getJMenuBar();
}
