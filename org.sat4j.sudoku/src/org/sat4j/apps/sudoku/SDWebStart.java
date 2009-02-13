package org.sat4j.apps.sudoku;

import java.awt.Frame;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.FileSaveService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

public class SDWebStart extends JFrame implements MainProgramWindow {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    FileSaveService fss;

    FileOpenService fos;

    SuDoku suDoku;

    public static void main(String argv[]) throws Exception {
        if (argv.length == 0) {
            new SDWebStart(16);
        } else {
            try {
                new SDWebStart(Integer.parseInt(argv[0]));
            } catch (Exception e) {
            }
        }
    }

    public void setMainWindowSize(int width, int height) {
        /*
         * setSize (width, height); validate(); repaint();
         */
    }

    public boolean isApplet() {
        return false;
    }

    public boolean isApplication() {
        return false;
    }

    public boolean isWebStart() {
        return true;
    }

    public void maximize() {
        setExtendedState(Frame.MAXIMIZED_BOTH);
        validate();
        repaint();
    }

    public boolean fileAccess() {
        try {
            fos = (FileOpenService) ServiceManager
                    .lookup("javax.jnlp.FileOpenService");
            fss = (FileSaveService) ServiceManager
                    .lookup("javax.jnlp.FileSaveService");
        } catch (UnavailableServiceException e) {
            fss = null;
            fos = null;
        }

        return (fos != null) && (fss != null);
    }

    public String readFile() {
        StringBuffer result = new StringBuffer();
        try {
            FileContents fc = fos.openFileDialog(null, null);
            InputStream is = fc.getInputStream();

            int ch = is.read();
            while (ch >= 0) {
                result.append((char) ch);
                ch = is.read();
            }
        } catch (Exception e) {
            result = new StringBuffer("File not read");
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

    static String extensions[] = { ".cnf" };

    public void writeFile(String s) {
        try {
            StringInputStream is = new StringInputStream(s);
            fss.saveFileDialog(null, extensions, is, "newFileName.cnf");
        } catch (IOException e) {
            suDoku.getGui().setResult("File not written");
        }
    }

    SDWebStart(int maxSide) {
        /*
         * Obtain IP address in case of future logging String ip; try { ip =
         * InetAddress.getLocalHost().getCanonicalHostName(); } catch (Exception
         * e) { ip = "unknown"; }
         */
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        suDoku = new SuDoku(this, maxSide, false);
        getContentPane().add(new JScrollPane(suDoku.getGui()));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);
    }

    JMenuBar menuBar;
}
