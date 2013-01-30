/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;

import org.sat4j.sat.visu.VisuPreferencesFrame;
import org.sat4j.specs.ILogAble;

/**
 * 
 * JFrame for the remote control.
 * 
 * @author sroussel
 * 
 */
public class RemoteControlFrame extends JFrame implements ILogAble {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private JMenuItem activateTracing;

    private JRadioButtonMenuItem gnuplotBasedRadio;
    private JRadioButtonMenuItem jChartBasedRadio;

    private DetailedCommandPanel commandePanel;
    private String filename;

    private String ramdisk;

    private String[] args;
    private VisuPreferencesFrame visuFrame;

    private static final String ACTIVATE = "Activate Tracing";
    private static final String DEACTIVATE = "Deactivate Tracing";

    public RemoteControlFrame(String filename, String ramdisk, String[] args) {
        super("Remote Control");

        this.filename = filename;
        this.ramdisk = ramdisk;
        this.args = args.clone();

        JFrame.setDefaultLookAndFeelDecorated(true);

        createAndShowGUI();
    }

    public RemoteControlFrame(String filename, String ramdisk) {
        this(filename, ramdisk, new String[] {});
    }

    public RemoteControlFrame(String filename) {
        this(filename, "", new String[] {});
    }

    public RemoteControlFrame(String filename, String[] args) {
        this(filename, "", args);
    }

    public void reinitialiser() {
    }

    public void setActivateGnuplot(boolean b) {
        this.activateTracing.setSelected(b);
        activateTracing(b);
    }

    private void createAndShowGUI() {
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());

        createMenuBar();

        this.commandePanel = new DetailedCommandPanel(this.filename,
                this.ramdisk, this.args, this);

        this.commandePanel.setChartBased(true);
        this.commandePanel.activateGnuplotTracing(true);

        this.visuFrame = new VisuPreferencesFrame(
                this.commandePanel.getGnuplotPreferences());

        JScrollPane scrollPane = new JScrollPane(this.commandePanel);

        this.add(scrollPane);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                RemoteControlFrame.this.commandePanel.stopVisu();
                setVisible(false);
                dispose();
            }

        });

        this.pack();
        this.setVisible(true);
    }

    public void clickOnAboutSolver() {
        if (this.commandePanel.getSolver() != null) {
            JOptionPane.showMessageDialog(this, this.commandePanel.getSolver()
                    .toString());
        } else {
            JOptionPane.showMessageDialog(this,
                    "No solver is running at the moment. Please start solver.");
        }
    }

    public void setActivateTracingEditableUnderCondition(boolean b) {
        if (this.activateTracing.getText().equals(ACTIVATE)) {
            this.activateTracing.setEnabled(b);
        }
    }

    public void setActivateTracingEditable(boolean b) {
        this.activateTracing.setEnabled(b);
    }

    public void setActivateRadioTracing(boolean activate) {
        gnuplotBasedRadio.setEnabled(activate);
        jChartBasedRadio.setEnabled(activate);
    }

    public void createMenuBar() {
        JMenuBar barreMenu = new JMenuBar();
        JMenu menu = new JMenu("File");
        barreMenu.add(menu);

        this.activateTracing = new JMenuItem(DEACTIVATE);
        menu.add(this.activateTracing);

        this.activateTracing.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activateTracing(RemoteControlFrame.this.activateTracing
                        .getText().equals(ACTIVATE));
            }
        });

        menu.addSeparator();

        gnuplotBasedRadio = new JRadioButtonMenuItem("Trace with Gnuplot");
        jChartBasedRadio = new JRadioButtonMenuItem("Trace with Java");

        ButtonGroup visuGroup = new ButtonGroup();
        visuGroup.add(gnuplotBasedRadio);
        visuGroup.add(jChartBasedRadio);

        menu.add(gnuplotBasedRadio);

        gnuplotBasedRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RemoteControlFrame.this.commandePanel.setGnuplotBased(true);
                RemoteControlFrame.this.commandePanel.setChartBased(false);
                RemoteControlFrame.this.commandePanel
                        .activateGnuplotTracing(RemoteControlFrame.this.activateTracing
                                .getText().equals(DEACTIVATE));
                log("Use gnuplot tracing");
            }
        });
        jChartBasedRadio.setSelected(true);

        menu.add(jChartBasedRadio);

        jChartBasedRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RemoteControlFrame.this.commandePanel.setGnuplotBased(false);
                RemoteControlFrame.this.commandePanel.setChartBased(true);
                RemoteControlFrame.this.commandePanel
                        .activateGnuplotTracing(RemoteControlFrame.this.activateTracing
                                .getText().equals(DEACTIVATE));
                log("Use java tracing");
            }
        });

        menu.addSeparator();

        JMenuItem quit = new JMenuItem("Exit");
        menu.add(quit);

        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RemoteControlFrame.this.commandePanel.stopVisu();
                System.exit(NORMAL);
            }
        });

        JMenu preferences = new JMenu("Preferences");
        JMenuItem gnuplotPreferencesItem = new JMenuItem(
                "Visualisation preferences");

        gnuplotPreferencesItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RemoteControlFrame.this.visuFrame.setVisible(true);
            }
        });

        preferences.add(gnuplotPreferencesItem);

        barreMenu.add(preferences);

        barreMenu.add(Box.createHorizontalGlue());

        // ...create the rightmost menu...
        JLabel version = new JLabel(getVersion());
        barreMenu.add(version);

        this.setJMenuBar(barreMenu);

    }

    public void log(String message) {
        this.commandePanel.log(message);
    }

    private String getVersion() {
        URL url = RemoteControlFrame.class.getResource("/sat4j.version"); //$NON-NLS-1$
        String s = "";
        if (url == null) {
            s = "no version file found!!!"; //$NON-NLS-1$			
        } else {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(url.openStream()));
                s = "version " + in.readLine(); //$NON-NLS-1$
            } catch (IOException e) {
                s = "c ERROR: " + e.getMessage();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        s = "c ERROR: " + e.getMessage();
                    }
                }
            }
        }
        return s;
    }

    public void activateTracing(boolean b) {
        if (b) {
            log("Activated tracing");
            this.activateTracing.setText(DEACTIVATE);
            this.commandePanel.setPlotActivated(true);
        } else {
            log("Deactivated tracing.");
            this.activateTracing.setText(ACTIVATE);
            this.commandePanel.stopVisu();
            this.commandePanel.setPlotActivated(false);
        }
        if (this.commandePanel.getStartStopText().equals("Stop")
                && this.activateTracing.getText().equals(ACTIVATE)) {
            this.activateTracing.setEnabled(false);
        } else {
            this.activateTracing.setEnabled(true);
        }
    }

    public void setOptimisationMode(boolean optimizationMode) {
        this.commandePanel.setOptimisationMode(optimizationMode);
    }

}