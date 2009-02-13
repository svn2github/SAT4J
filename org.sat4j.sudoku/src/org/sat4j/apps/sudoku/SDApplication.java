package org.sat4j.apps.sudoku;

import java.awt.Frame;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

/*
 * * Author: Ivor Spence, Queen's University Belfast *
 */

@SuppressWarnings("serial")
public class SDApplication extends JFrame implements MainProgramWindow {

    SuDoku suDoku;

    JFileChooser inputFileChooser, outputFileChooser;

    JMenuBar menuBar;

    FileMenu fileMenu;

    EditMenu editMenu;

    public static void main(String argv[]) throws Exception {
        new SDApplication(36);
    }

    public void setMainWindowSize(int width, int height) {
    }

    public void maximize() {
        setExtendedState(Frame.MAXIMIZED_BOTH);
        validate();
        repaint();
    }

    public boolean fileAccess() {
        return true;
    }

    public String readFile() {
        StringBuffer result = new StringBuffer();
        int returnVal = inputFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                FileInputStream is = new FileInputStream(inputFileChooser
                        .getSelectedFile());

                int ch = is.read();
                while (ch >= 0) {
                    result.append((char) ch);
                    ch = is.read();
                }
            } catch (Exception e) {
                result = new StringBuffer("File not read");
            }
        }
        return result.toString();
    }

    public class StringInputStream extends InputStream {
        StringReader sr;

        StringInputStream(String s) {
            sr = new StringReader(s);
        }

        @Override
        public int read() throws IOException {
            return sr.read();
        }
    }

    public void writeFile(String s) {
        int returnVal = outputFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                FileOutputStream os = new FileOutputStream(outputFileChooser
                        .getSelectedFile());

                for (int i = 0; i < s.length(); i++) {
                    os.write((byte) s.charAt(i));
                }
            } catch (IOException e) {
                suDoku.getGui().setResult("File not written");
            }
        } else {
            suDoku.getGui().setResult("File not written");
        }

    }

    public boolean isApplet() {
        return false;
    }

    public boolean isApplication() {
        return true;
    }

    public boolean isWebStart() {
        return false;
    }

    SDApplication(int maxSide) {

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        suDoku = new SuDoku(this, maxSide, true);
        getContentPane().add(new JScrollPane(suDoku.getGui()));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(800, 650);
        inputFileChooser = new JFileChooser();
        outputFileChooser = new JFileChooser();

        setVisible(true);
    }

}
