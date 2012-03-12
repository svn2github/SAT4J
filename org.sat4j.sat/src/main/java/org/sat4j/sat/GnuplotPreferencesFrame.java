
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

/*
 * DialogDemo.java requires these files:
 *   CustomDialog.java
 *   images/middle.gif
 */
public class GnuplotPreferencesFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private GnuplotPreferences preferences;
	private JPanel mainPanel;

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

		mainPanel.setLayout(new GridLayout(0, 2,5,5));

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

		mainPanel.add(backgroundColorLabel);
		mainPanel.add(bgButton);

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

		mainPanel.add(borderColorLabel);
		mainPanel.add(borderButton);

		slidingWindows = new JCheckBox(SLIDING_WINDOWS);
		slidingWindows.setSelected(preferences.isSlidingWindows());

		slidingWindows.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nbLinesReadLabel.setEnabled(slidingWindows.isSelected());
				nbLinesTextField.setEnabled(slidingWindows.isSelected());
				preferences.setSlidingWindows(slidingWindows.isSelected());
			}
		});

		mainPanel.add(slidingWindows);
		mainPanel.add(new JLabel());

		nbLinesReadLabel = new JLabel(NB_LINE);
		nbLinesTextField = new JTextField(preferences.getNbLinesRead()+"");
		
		nbLinesReadLabel.setEnabled(slidingWindows.isSelected());
		nbLinesTextField.setEnabled(slidingWindows.isSelected());

		mainPanel.add(nbLinesReadLabel);
		mainPanel.add(nbLinesTextField);

		refreshTimeLabel = new JLabel(REFRESH_TIME);
		refreshTimeField = new JTextField(preferences.getRefreshTime()+"");

		mainPanel.add(refreshTimeLabel);
		mainPanel.add(refreshTimeField);

		timeBeforeLaunchLabel = new JLabel(TIME_BEFORE_LAUNCHING);
		timeBeforeLaunchField = new JTextField(preferences.getTimeBeforeLaunching()+"");

		mainPanel.add(timeBeforeLaunchLabel);
		mainPanel.add(timeBeforeLaunchField);

		displayRestartsCheckBox = new JCheckBox(DISPLAY_RESTARTS);
		displayRestartsCheckBox.setSelected(preferences.isDisplayRestarts());

		displayRestartsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restartColorLabel.setEnabled(displayRestartsCheckBox.isSelected());
				restartButton.setEnabled(displayRestartsCheckBox.isSelected());
				preferences.setDisplayRestarts(displayRestartsCheckBox.isSelected());
			}
		});

		mainPanel.add(displayRestartsCheckBox);
		mainPanel.add(new JLabel());

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

		mainPanel.add(restartColorLabel);
		mainPanel.add(restartButton);

		okButton = new JButton(OK);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getFrame().setVisible(false);
			}
		});
		
		mainPanel.add(new JLabel());
		mainPanel.add(okButton);
	}

	public JFrame getFrame(){
		return this;
	}





}