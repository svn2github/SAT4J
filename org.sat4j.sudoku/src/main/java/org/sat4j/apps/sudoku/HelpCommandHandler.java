package org.sat4j.apps.sudoku;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class HelpCommandHandler extends CommandHandler<HelpCommand> {

    public HelpCommandHandler(SuDoku sudoku) {
        super(sudoku);
    }

    void processWebPage(String url) {
        try {
            if (sudoku.getMainProgramWindow().isApplet()) {
                ((JApplet) sudoku.getMainProgramWindow()).getAppletContext()
                        .showDocument(new URL(url), "SD_HELP_WINDOW");
            } else {
                // if (Desktop.isDesktopSupported()) {
                //   Desktop.getDesktop().browse(new URI(url)); 
                // } else {
                   WebPage webPage = new WebPage((JFrame)sudoku.getMainProgramWindow(),url);
                   webPage.setVisible(true);
                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
            sudoku.getGui().setResult(e.toString());
        }
    }

    void processAboutPage() {
        ShowVersions showVersions = new ShowVersions();
        String aboutMessage = "<html>SuDoku "
                + showVersions.getVersion("sudoku") + " at " + sudokuURL
                + "<p>" + "satj4 " + showVersions.getVersion("sat4j") + " at "
                + sat4jURL + " (includes source code)";
        JPanel panelall = new JPanel();
        panelall.setLayout(new GridLayout(2, 1));
        JPanel logos = new JPanel();
        logos.setLayout(new GridLayout(1, 3));
        logos.setBackground(Color.WHITE);
        logos.setOpaque(true);
        ImageIcon sat4jIcon, ecitIcon, crilIcon, objectWebIcon, eventIcon;
        SuDokuResources suDokuResources = sudoku.getSuDokuResources();
        sat4jIcon = suDokuResources.getSat4jIcon();
        ecitIcon = suDokuResources.getECITIcon();
        crilIcon = suDokuResources.getCRILIcon();
        objectWebIcon = suDokuResources.getObjectWebIcon();
        eventIcon = suDokuResources.getEventIcon();
        ImageIcon smallSat4jIcon, smallEcitIcon, smallCrilIcon, smallObjectWebIcon, smallEventIcon;
        int imageWidth = 150;
        smallSat4jIcon = new ImageIcon(sat4jIcon.getImage().getScaledInstance(
                imageWidth, -1, 0));
        smallEcitIcon = new ImageIcon(ecitIcon.getImage().getScaledInstance(
                imageWidth, -1, 0));
        smallCrilIcon = new ImageIcon(crilIcon.getImage().getScaledInstance(
                imageWidth, -1, 0));
        smallObjectWebIcon = new ImageIcon(objectWebIcon.getImage()
                .getScaledInstance(imageWidth, -1, 0));
        smallEventIcon = new ImageIcon(eventIcon.getImage().getScaledInstance(imageWidth, -1, 0));
        
        logos.add(new JLabel(smallEcitIcon));
        logos.add(new JLabel(smallCrilIcon));
        logos.add(new JLabel(smallObjectWebIcon));
        logos.add(new JLabel(smallEventIcon));
        panelall.add(logos);
        panelall.add(new JLabel(aboutMessage));
        JOptionPane.showMessageDialog(null, panelall, sudoku
                .getSuDokuResources().getStringFromKey("HELP_ABOUT"),
                JOptionPane.INFORMATION_MESSAGE, smallSat4jIcon);
        // AboutPage aboutPage = new AboutPage();
    }

    @Override
    public void execute(Enum<HelpCommand> command) {
        switch ((HelpCommand) command) {
        case ABOUT:

            processAboutPage();
            break;

        /*
         * ShowVersions showVersions = new ShowVersions();
         * JOptionPane.showMessageDialog(null, "<html>SuDoku " +
         * showVersions.getVersion("sudoku") + " at " + sudokuURL + "<p>" +
         * "satj4 " + showVersions.getVersion("sat4j") + " at " + sat4jURL + "
         * (includes source code)", "About", JOptionPane.INFORMATION_MESSAGE);
         */

        case SUDOKU:
            processWebPage(sudokuURL);
            break;

        case COMPLETE_SUDOKU:
            processWebPage(completeSudokuURL);
            break;

        case SAT4J:
            processWebPage(sat4jURL);
            break;

        }
    }

    class WebPage extends JDialog {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        WebPage(Frame frame,String url) throws Exception {
            super(frame,"Help");
            setPreferredSize(new Dimension(600, 500));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JEditorPane output = new JEditorPane();
            output.setContentType( "text/html" );
            output.setEditable(false);
            output.setPage(url);
            
//            Box panel = Box.createVerticalBox();
//
//            Box heading = Box.createHorizontalBox();
//            heading.add(new JLabel("Contents of web page "));
//            JTextField address = new JTextField(url);
//            address.setEditable(false);
//            address.setColumns(url.length());
//            heading.add(address);
//            panel.add(heading);
//            Box heading2 = Box.createHorizontalBox();
//            heading2
//                    .add(new JLabel(
//                            "Note that this window does not support hyperlinks - you can copy the above address to paste into a browser with <Ctrl-A><Ctrl-C>"));
//            heading2.add(Box.createHorizontalGlue());
//            panel.add(heading2);
//            panel.add(Box.createVerticalStrut(4));
//            JEditorPane pane = null;
//            setSize(new Dimension(800, 600));
//            pane = new JEditorPane(url);
//            pane.setEditable(false);
//            pane.setPreferredSize(new Dimension(200, 10000));
//            panel.add(pane);
            
            JLabel label = new JLabel(
            "<html>Note that this window does not support hyperlinks.<p>You can copy the following address to paste into a browser if needed:<p>"+url);
            label.setBackground(Color.YELLOW);
            getContentPane().add(BorderLayout.NORTH,label);
                    
            getContentPane()
                    .add(BorderLayout.CENTER,
                            new JScrollPane(
                                    output,
                                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        }
    }

    static String sudokuURL = "http://www.cs.qub.ac.uk/~i.spence/SuDoku/SuDoku.html";

    static String completeSudokuURL = "http://www.cs.qub.ac.uk/~i.spence/SuDoku/Complete.html";

    static String sat4jURL = "http://www.sat4j.org/";

}
