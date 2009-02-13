package org.sat4j.apps.sudoku;

import java.awt.BorderLayout;

import javax.swing.JApplet;
import javax.swing.JMenuBar;

public class SDApplet extends JApplet implements MainProgramWindow {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public void setMainWindowSize(int width, int height) {
    }

    public void maximize() {
    }

    public boolean fileAccess() {
        return false;
    }

    public String readFile() {
        return "";
    }

    public void writeFile(String s) {
    }

    public void pack() {
    }

    public boolean isApplet() {
        return true;
    }

    public boolean isApplication() {
        return false;
    }

    public boolean isWebStart() {
        return false;
    }

    @Override
    public void init() {
        /*
         * Obtain IP address in case of future logging String ip = null; try {
         * Socket s = new Socket(getDocumentBase().getHost(), 80); ip =
         * s.getLocalAddress().getCanonicalHostName(); s.close(); } catch
         * (Exception e) { ip = "unknown"; }
         */
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        SuDoku s = new SuDoku(this, 16, false);
        setLayout(new BorderLayout());
        add(s.getGui(), BorderLayout.CENTER);
    }

    JMenuBar menuBar;
}
