
package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
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
public class GnuplotPreferencesFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private GnuplotPreferences preferences;
	private JPanel mainPanel;
	private JPanel gnuplotOptionsPanel;
	private JPanel graphPanel;

	private JLabel backgroundColorLabel;
	private final static String BACKGROUND_COLOR = "Background color: ";
	private JButton bgButton;
	private JLabel borderColorLabel;
	private final static String BORDER_COLOR = "Border color: ";
	private JButton borderButton;

	private JLabel nbLinesReadLabel;
	private final static String NB_LINE = "Number of lines that should be displayed: ";
	private JTextField nbLinesTextField;

	private JLabel refreshTimeLabel;
	private final static String REFRESH_TIME = "Refresh Time (in ms): ";
	private JTextField refreshTimeField;

	private JLabel timeBeforeLaunchLabel;
	private final static String TIME_BEFORE_LAUNCHING = "Time before launching gnuplot (in ms): ";
	private JTextField timeBeforeLaunchField;

	private JCheckBox displayRestartsCheckBox;
	private final static String DISPLAY_RESTARTS = "Display restarts";

	private JLabel restartColorLabel;
	private final static String RESTART_COLOR = "Restart color";
	private JButton restartButton;

	private JCheckBox slidingWindows;
	private final static String SLIDING_WINDOWS = "Use sliding windows"; 

	private JCheckBox displayDecisionIndexesCB;
	private final static String DECISION_INDEX = "Show index of decision variables";
	private JCheckBox displaySpeedCB;
	private final static String SPEED = "Show number of assignments per second";
	private JCheckBox displayConflictsTrailCB;
	private final static String CONFLICTS_TRAIL = "Show trail level when a conflict occurs";
	private JCheckBox displayConflictsDecisionCB;
	private final static String CONFLICTS_DECISION = "Show decision level when a conflict occurs";
	private JCheckBox displayVariablesEvaluationCB;
	private final static String VARIABLE_EVALUATION = "Show variables evaluation";
	private JCheckBox displayClausesEvaluationCB;
	private final static String CLAUSES_EVALUATION = "Show clauses evauluation";
	private JCheckBox displayClausesSizeCB;
	private final static String CLAUSES_SIZE = "Show size of learned clauses";
	
	private JButton okButton;
	private final static String OK = "OK";

	public GnuplotPreferencesFrame(){
		this(new GnuplotPreferences());
	}

	public GnuplotPreferencesFrame(GnuplotPreferences pref){
		super("Gnuplot preferences");
		this.preferences = pref;
		createAndShowGUI();
	}

	public void createAndShowGUI(){
		Container c = this.getContentPane();
		c.setLayout(new BorderLayout());

		createMainPanel();

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		this.add(scrollPane);

		this.pack();
		this.setVisible(false);
	}

	public void createMainPanel(){
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		gnuplotOptionsPanel = new JPanel();
		
		gnuplotOptionsPanel.setName("Gnuplot options");
		gnuplotOptionsPanel.setBorder(new CompoundBorder(new TitledBorder(null, gnuplotOptionsPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));

		gnuplotOptionsPanel.setLayout(new GridLayout(0, 2,5,5));

		backgroundColorLabel = new JLabel(BACKGROUND_COLOR);
		bgButton = new JButton("");
		bgButton.setOpaque(true);
		bgButton.setBorderPainted(false);
		bgButton.setBackground(preferences.getBackgroundColor());

		bgButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(getFrame(), "Background Color", 
						bgButton.getBackground());
				preferences.setBackgroundColor(color);
				bgButton.setBackground(color);
			}
		});

		gnuplotOptionsPanel.add(backgroundColorLabel);
		gnuplotOptionsPanel.add(bgButton);

		borderColorLabel = new JLabel(BORDER_COLOR);
		borderButton=new JButton("");
		borderButton.setOpaque(true);
		borderButton.setBorderPainted(false);
		borderButton.setBackground(preferences.getBorderColor());

		borderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(getFrame(), "Border Color", 
						borderButton.getBackground());
				preferences.setBorderColor(color);
				borderButton.setBackground(color);
			}
		});

		gnuplotOptionsPanel.add(borderColorLabel);
		gnuplotOptionsPanel.add(borderButton);

		slidingWindows = new JCheckBox(SLIDING_WINDOWS);
		slidingWindows.setSelected(preferences.isSlidingWindows());

		slidingWindows.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nbLinesReadLabel.setEnabled(slidingWindows.isSelected());
				nbLinesTextField.setEnabled(slidingWindows.isSelected());
				preferences.setSlidingWindows(slidingWindows.isSelected());
			}
		});

		gnuplotOptionsPanel.add(slidingWindows);
		gnuplotOptionsPanel.add(new JLabel());

		nbLinesReadLabel = new JLabel(NB_LINE);
		nbLinesTextField = new JTextField(preferences.getNbLinesRead()+"");
		
		nbLinesReadLabel.setEnabled(slidingWindows.isSelected());
		nbLinesTextField.setEnabled(slidingWindows.isSelected());

		gnuplotOptionsPanel.add(nbLinesReadLabel);
		gnuplotOptionsPanel.add(nbLinesTextField);

		refreshTimeLabel = new JLabel(REFRESH_TIME);
		refreshTimeField = new JTextField(preferences.getRefreshTime()+"");

		gnuplotOptionsPanel.add(refreshTimeLabel);
		gnuplotOptionsPanel.add(refreshTimeField);

		timeBeforeLaunchLabel = new JLabel(TIME_BEFORE_LAUNCHING);
		timeBeforeLaunchField = new JTextField(preferences.getTimeBeforeLaunching()+"");

		gnuplotOptionsPanel.add(timeBeforeLaunchLabel);
		gnuplotOptionsPanel.add(timeBeforeLaunchField);

		displayRestartsCheckBox = new JCheckBox(DISPLAY_RESTARTS);
		displayRestartsCheckBox.setSelected(preferences.isDisplayRestarts());

		displayRestartsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restartColorLabel.setEnabled(displayRestartsCheckBox.isSelected());
				restartButton.setEnabled(displayRestartsCheckBox.isSelected());
				preferences.setDisplayRestarts(displayRestartsCheckBox.isSelected());
			}
		});

		gnuplotOptionsPanel.add(displayRestartsCheckBox);
		gnuplotOptionsPanel.add(new JLabel());

		restartColorLabel = new JLabel(RESTART_COLOR);
		restartButton = new JButton("");
		restartButton.setOpaque(true);
		restartButton.setBorderPainted(false);
		restartButton.setBackground(preferences.getRestartColor());
		
		restartColorLabel.setEnabled(displayRestartsCheckBox.isSelected());
		restartButton.setEnabled(displayRestartsCheckBox.isSelected());

		restartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(getFrame(), "Restart Color", 
						restartButton.getBackground());
				preferences.setRestartColor(color);
				restartButton.setBackground(color);
			}
		});

		gnuplotOptionsPanel.add(restartColorLabel);
		gnuplotOptionsPanel.add(restartButton);

		okButton = new JButton(OK);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getFrame().setVisible(false);
			}
		});
		
//		graphOptionsPanel.add(new JLabel());
		graphPanel = new JPanel();
		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
		
		graphPanel.setName("Possible Graphs");
		graphPanel.setBorder(new CompoundBorder(new TitledBorder(null, graphPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5)));
		
		
		displayClausesEvaluationCB = new JCheckBox(CLAUSES_EVALUATION);
		graphPanel.add(displayClausesEvaluationCB);
		displayClausesEvaluationCB.setSelected(preferences.isDisplayClausesEvaluation());
		displayClausesEvaluationCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences.setDisplayClausesEvaluation(displayClausesEvaluationCB.isSelected());
			}
		});
		
		displayClausesSizeCB = new JCheckBox(CLAUSES_SIZE);
		graphPanel.add(displayClausesSizeCB);
		displayClausesSizeCB.setSelected(preferences.isDisplayClausesSize());
		displayClausesSizeCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences.setDisplayClausesSize(displayClausesSizeCB.isSelected());
			}
		});
		
		displayConflictsDecisionCB = new JCheckBox(CONFLICTS_DECISION);
		graphPanel.add(displayConflictsDecisionCB);
		displayConflictsDecisionCB.setSelected(preferences.isDisplayConflictsDecision());
		displayConflictsDecisionCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences.setDisplayConflictsDecision(displayConflictsDecisionCB.isSelected());
			}
		});
		
		displayConflictsTrailCB = new JCheckBox(CONFLICTS_TRAIL);
		graphPanel.add(displayConflictsTrailCB);
		displayConflictsTrailCB.setSelected(preferences.isDisplayConflictsTrail());
		displayConflictsTrailCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences.setDisplayConflictsTrail(displayConflictsTrailCB.isSelected());
			}
		});
		
		displayDecisionIndexesCB = new JCheckBox(DECISION_INDEX);
		graphPanel.add(displayDecisionIndexesCB);
		displayDecisionIndexesCB.setSelected(preferences.isDisplayDecisionIndexes());
		displayDecisionIndexesCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences.setDisplayDecisionIndexes(displayDecisionIndexesCB.isSelected());
			}
		});
		
		displaySpeedCB = new JCheckBox(SPEED);
		graphPanel.add(displaySpeedCB);
		displaySpeedCB.setSelected(preferences.isDisplaySpeed());
		displaySpeedCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences.setDisplaySpeed(displaySpeedCB.isSelected());
			}
		});
		
		displayVariablesEvaluationCB = new JCheckBox(VARIABLE_EVALUATION);
		graphPanel.add(displayVariablesEvaluationCB);
		displayVariablesEvaluationCB.setSelected(preferences.isDisplayVariablesEvaluation());
		displayVariablesEvaluationCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences.setDisplayVariablesEvaluation(displayVariablesEvaluationCB.isSelected());
			}
		});
		
		
		mainPanel.add(gnuplotOptionsPanel,BorderLayout.NORTH);
		mainPanel.add(graphPanel,BorderLayout.CENTER);
		mainPanel.add(okButton,BorderLayout.SOUTH);
	}

	public JFrame getFrame(){
		return this;
	}





}