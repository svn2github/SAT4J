package org.sat4j.sat.visu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/*
 * DialogDemo.java requires these files:
 *   CustomDialog.java
 *   images/middle.gif
 */
public class VisuPreferencesFrame extends JFrame {

    private static final GridLayout GRIDLAYOUT = new GridLayout(0, 2, 5, 5);

    private static final EmptyBorder BORDER5 = new EmptyBorder(5, 5, 5, 5);

    private static final long serialVersionUID = 1L;

    private VisuPreferences preferences;
    private JPanel mainPanel;

    private static final String BACKGROUND_COLOR = "Background color: ";
    private JButton bgButton;

    private static final String BORDER_COLOR = "Border color: ";
    private JButton borderButton;

    private JLabel nbLinesReadLabel;
    private static final String NB_LINE = "Number of lines that should be displayed: ";
    private JTextField nbLinesTextField;

    private static final String REFRESH_TIME = "Refresh Time (in ms): ";

    private static final String TIME_BEFORE_LAUNCHING = "Time before launching gnuplot (in ms): ";

    private JCheckBox displayRestartsCheckBox;
    private static final String DISPLAY_RESTARTS = "Display restarts";

    private JLabel restartColorLabel;
    private static final String RESTART_COLOR = "Restart color";
    private JButton restartButton;

    private JCheckBox slidingWindows;
    private static final String SLIDING_WINDOWS = "Use sliding windows";

    private JCheckBox displayDecisionIndexesCB;
    private static final String DECISION_INDEX = "Show index of decision variables";
    private JCheckBox displaySpeedCB;
    private static final String SPEED = "Show number of propagations per second";
    private JCheckBox displayConflictsTrailCB;
    private static final String CONFLICTS_TRAIL = "Show trail level when a conflict occurs";
    private JCheckBox displayConflictsDecisionCB;
    private static final String CONFLICTS_DECISION = "Show decision level when a conflict occurs";
    private JCheckBox displayVariablesEvaluationCB;
    private static final String VARIABLE_EVALUATION = "Show variables evaluation";
    private JCheckBox displayClausesEvaluationCB;
    private static final String CLAUSES_EVALUATION = "Show clauses evauluation";
    private JCheckBox displayClausesSizeCB;
    private static final String CLAUSES_SIZE = "Show size of learned clauses";

    private static final String OK = "OK";

    private JPanel gnuplotOptionsPanel;

    private JPanel generalOptionsPanel;

    public VisuPreferencesFrame() {
        this(new VisuPreferences());
    }

    public VisuPreferencesFrame(VisuPreferences pref) {
        super("Visualisation preferences");
        this.preferences = pref;
        createGUI();
    }

    private void createGUI() {
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());

        createMainPanel();

        JScrollPane scrollPane = new JScrollPane(this.mainPanel);
        this.add(scrollPane);

        this.pack();
        this.setVisible(false);
    }

    private void createGeneralOptionsPanel() {
        this.generalOptionsPanel = new JPanel();

        this.generalOptionsPanel.setName("General options");
        this.generalOptionsPanel.setBorder(new CompoundBorder(new TitledBorder(
                null, this.generalOptionsPanel.getName(), TitledBorder.LEFT,
                TitledBorder.TOP), BORDER5));

        this.generalOptionsPanel.setLayout(GRIDLAYOUT);

        JLabel backgroundColorLabel;

        backgroundColorLabel = new JLabel(BACKGROUND_COLOR);
        this.bgButton = new JButton("");
        this.bgButton.setOpaque(true);
        this.bgButton.setBorderPainted(false);
        this.bgButton.setBackground(this.preferences.getBackgroundColor());

        this.bgButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = JColorChooser.showDialog(getFrame(),
                        "Background Color",
                        VisuPreferencesFrame.this.bgButton.getBackground());
                VisuPreferencesFrame.this.preferences.setBackgroundColor(color);
                VisuPreferencesFrame.this.bgButton.setBackground(color);
            }
        });

        this.generalOptionsPanel.add(backgroundColorLabel);
        this.generalOptionsPanel.add(this.bgButton);

        JLabel borderColorLabel = new JLabel(BORDER_COLOR);

        this.borderButton = new JButton("");
        this.borderButton.setOpaque(true);
        this.borderButton.setBorderPainted(false);
        this.borderButton.setBackground(this.preferences.getBorderColor());

        this.borderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = JColorChooser.showDialog(getFrame(),
                        "Border Color",
                        VisuPreferencesFrame.this.borderButton.getBackground());
                VisuPreferencesFrame.this.preferences.setBorderColor(color);
                VisuPreferencesFrame.this.borderButton.setBackground(color);
            }
        });

        this.generalOptionsPanel.add(borderColorLabel);
        this.generalOptionsPanel.add(this.borderButton);

        this.restartColorLabel = new JLabel(RESTART_COLOR);
        this.restartButton = new JButton("");
        this.restartButton.setOpaque(true);
        this.restartButton.setBorderPainted(false);
        this.restartButton.setBackground(this.preferences.getRestartColor());

        this.restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = JColorChooser
                        .showDialog(getFrame(), "Restart Color",
                                VisuPreferencesFrame.this.restartButton
                                        .getBackground());
                VisuPreferencesFrame.this.preferences.setRestartColor(color);
                VisuPreferencesFrame.this.restartButton.setBackground(color);
            }
        });

        this.generalOptionsPanel.add(this.restartColorLabel);
        this.generalOptionsPanel.add(this.restartButton);
    }

    private void createGnuplotOptionsPanel() {
        this.gnuplotOptionsPanel = new JPanel();

        this.gnuplotOptionsPanel.setName("Gnuplot options");
        this.gnuplotOptionsPanel.setBorder(new CompoundBorder(new TitledBorder(
                null, this.gnuplotOptionsPanel.getName(), TitledBorder.LEFT,
                TitledBorder.TOP), BORDER5));

        this.gnuplotOptionsPanel.setLayout(GRIDLAYOUT);

        this.slidingWindows = new JCheckBox(SLIDING_WINDOWS);
        this.slidingWindows.setSelected(this.preferences.isSlidingWindows());

        this.slidingWindows.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VisuPreferencesFrame.this.nbLinesReadLabel
                        .setEnabled(VisuPreferencesFrame.this.slidingWindows
                                .isSelected());
                VisuPreferencesFrame.this.nbLinesTextField
                        .setEnabled(VisuPreferencesFrame.this.slidingWindows
                                .isSelected());
                VisuPreferencesFrame.this.preferences
                        .setSlidingWindows(VisuPreferencesFrame.this.slidingWindows
                                .isSelected());
            }
        });

        this.gnuplotOptionsPanel.add(this.slidingWindows);
        this.gnuplotOptionsPanel.add(new JLabel());

        this.nbLinesReadLabel = new JLabel(NB_LINE);
        this.nbLinesTextField = new JTextField(
                this.preferences.getNbLinesRead() + "");

        this.nbLinesReadLabel.setEnabled(this.slidingWindows.isSelected());
        this.nbLinesTextField.setEnabled(this.slidingWindows.isSelected());

        this.gnuplotOptionsPanel.add(this.nbLinesReadLabel);
        this.gnuplotOptionsPanel.add(this.nbLinesTextField);

        JLabel refreshTimeLabel = new JLabel(REFRESH_TIME);
        JTextField refreshTimeField = new JTextField(
                this.preferences.getRefreshTime() + "");

        this.gnuplotOptionsPanel.add(refreshTimeLabel);
        this.gnuplotOptionsPanel.add(refreshTimeField);

        JLabel timeBeforeLaunchLabel = new JLabel(TIME_BEFORE_LAUNCHING);
        JTextField timeBeforeLaunchField = new JTextField(
                this.preferences.getTimeBeforeLaunching() + "");

        this.gnuplotOptionsPanel.add(timeBeforeLaunchLabel);
        this.gnuplotOptionsPanel.add(timeBeforeLaunchField);

        this.displayRestartsCheckBox = new JCheckBox(DISPLAY_RESTARTS);
        this.displayRestartsCheckBox.setSelected(this.preferences
                .isDisplayRestarts());

        this.displayRestartsCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VisuPreferencesFrame.this.restartColorLabel
                        .setEnabled(VisuPreferencesFrame.this.displayRestartsCheckBox
                                .isSelected());
                VisuPreferencesFrame.this.restartButton
                        .setEnabled(VisuPreferencesFrame.this.displayRestartsCheckBox
                                .isSelected());
                VisuPreferencesFrame.this.preferences
                        .setDisplayRestarts(VisuPreferencesFrame.this.displayRestartsCheckBox
                                .isSelected());
            }
        });

        this.gnuplotOptionsPanel.add(this.displayRestartsCheckBox);
        this.gnuplotOptionsPanel.add(new JLabel());

    }

    public void createMainPanel() {
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BorderLayout());

        JPanel graphPanel;

        createGeneralOptionsPanel();

        createGnuplotOptionsPanel();

        JButton okButton;

        okButton = new JButton(OK);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getFrame().setVisible(false);
            }
        });

        graphPanel = new JPanel();
        graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));

        graphPanel.setName("Possible Graphs");
        graphPanel.setBorder(new CompoundBorder(new TitledBorder(null,
                graphPanel.getName(), TitledBorder.LEFT, TitledBorder.TOP),
                BORDER5));

        this.displayClausesEvaluationCB = new JCheckBox(CLAUSES_EVALUATION);
        graphPanel.add(this.displayClausesEvaluationCB);
        this.displayClausesEvaluationCB.setSelected(this.preferences
                .isDisplayClausesEvaluation());
        this.displayClausesEvaluationCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VisuPreferencesFrame.this.preferences
                        .setDisplayClausesEvaluation(VisuPreferencesFrame.this.displayClausesEvaluationCB
                                .isSelected());
            }
        });

        this.displayClausesSizeCB = new JCheckBox(CLAUSES_SIZE);
        graphPanel.add(this.displayClausesSizeCB);
        this.displayClausesSizeCB.setSelected(this.preferences
                .isDisplayClausesSize());
        this.displayClausesSizeCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VisuPreferencesFrame.this.preferences
                        .setDisplayClausesSize(VisuPreferencesFrame.this.displayClausesSizeCB
                                .isSelected());
            }
        });

        this.displayConflictsDecisionCB = new JCheckBox(CONFLICTS_DECISION);
        graphPanel.add(this.displayConflictsDecisionCB);
        this.displayConflictsDecisionCB.setSelected(this.preferences
                .isDisplayConflictsDecision());
        this.displayConflictsDecisionCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VisuPreferencesFrame.this.preferences
                        .setDisplayConflictsDecision(VisuPreferencesFrame.this.displayConflictsDecisionCB
                                .isSelected());
            }
        });

        this.displayConflictsTrailCB = new JCheckBox(CONFLICTS_TRAIL);
        graphPanel.add(this.displayConflictsTrailCB);
        this.displayConflictsTrailCB.setSelected(this.preferences
                .isDisplayConflictsTrail());
        this.displayConflictsTrailCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VisuPreferencesFrame.this.preferences
                        .setDisplayConflictsTrail(VisuPreferencesFrame.this.displayConflictsTrailCB
                                .isSelected());
            }
        });

        this.displayDecisionIndexesCB = new JCheckBox(DECISION_INDEX);
        graphPanel.add(this.displayDecisionIndexesCB);
        this.displayDecisionIndexesCB.setSelected(this.preferences
                .isDisplayDecisionIndexes());
        this.displayDecisionIndexesCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VisuPreferencesFrame.this.preferences
                        .setDisplayDecisionIndexes(VisuPreferencesFrame.this.displayDecisionIndexesCB
                                .isSelected());
            }
        });

        this.displaySpeedCB = new JCheckBox(SPEED);
        graphPanel.add(this.displaySpeedCB);
        this.displaySpeedCB.setSelected(this.preferences.isDisplaySpeed());
        this.displaySpeedCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VisuPreferencesFrame.this.preferences
                        .setDisplaySpeed(VisuPreferencesFrame.this.displaySpeedCB
                                .isSelected());
            }
        });

        this.displayVariablesEvaluationCB = new JCheckBox(VARIABLE_EVALUATION);
        graphPanel.add(this.displayVariablesEvaluationCB);
        this.displayVariablesEvaluationCB.setSelected(this.preferences
                .isDisplayVariablesEvaluation());
        this.displayVariablesEvaluationCB
                .addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        VisuPreferencesFrame.this.preferences
                                .setDisplayVariablesEvaluation(VisuPreferencesFrame.this.displayVariablesEvaluationCB
                                        .isSelected());
                    }
                });

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        topPanel.add(this.generalOptionsPanel);
        topPanel.add(this.gnuplotOptionsPanel);

        this.mainPanel.add(topPanel, BorderLayout.NORTH);
        this.mainPanel.add(graphPanel, BorderLayout.CENTER);
        this.mainPanel.add(okButton, BorderLayout.SOUTH);
    }

    public JFrame getFrame() {
        return this;
    }

}