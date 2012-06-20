package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.sat4j.minisat.core.ConflictTimer;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.ICDCLLogger;
import org.sat4j.minisat.core.LearnedConstraintsEvaluationType;

public class CleanCommandComponent extends CommandComponent{

//	private ICDCLLogger logger;
	
	private final static String CLEAN_PANEL = "Learned Constraint Deletion Strategy";

	private JSlider cleanSlider;

	private final static String EVALUATION_TYPE = "Clauses evaluation type";
	private final static String ACTIVITY_BASED = "Activity";
	private final static String LBD_BASED = "LBD";
	private final static String LBD2_BASED = "LBD 2";
	private JLabel evaluationLabel;
	private ButtonGroup evaluationGroup;
	private JRadioButton activityRadio;
	private JRadioButton lbdRadio;
	private JRadioButton lbd2Radio;

	private JButton cleanAndEvaluationApplyButton;

	private JButton cleanButton;
	private final static String CLEAN = "Clean now";
	private final static String MANUAL_CLEAN = "Manual clean: ";
	private JLabel manualCleanLabel;

	private JLabel speedLabel;
	private JLabel speedNameLabel;
	private final static String SPEED = "Speed :";
	private JLabel speedUnitLabel;
	private final static String SPEED_UNIT = " propagations per second";

	private final JLabel deleteClauseLabel = new JLabel(DELETE_CLAUSES);
	private final static String DELETE_CLAUSES = "Automated clean: ";

	private Hashtable<Integer, JLabel> cleanValuesTable;
	private final JLabel clean5000Label = new JLabel(CLEAN_5000);
	private final JLabel clean10000Label = new JLabel(CLEAN_10000);
	private final JLabel clean20000Label = new JLabel(CLEAN_20000);
	private final JLabel clean50000Label = new JLabel(CLEAN_50000);
	private final JLabel clean100000Label = new JLabel(CLEAN_100000);
	private final JLabel clean500000Label = new JLabel(CLEAN_500000);
	private final static int[] cleanValues ={5000,10000,20000,50000,100000,500000};
	private final static int CLEAN_MIN = 0;
	private final static int CLEAN_MAX = 5;
	private final static int CLEAN_INIT = 1;
	private final static int CLEAN_SPACE = 1;

	private final static String CLEAN_5000 = "5000";
	private final static String CLEAN_10000 = "10000";
	private final static String CLEAN_20000 = "20000";
	private final static String CLEAN_50000 = "50000";
	private final static String CLEAN_100000 = "100000";
	private final static String CLEAN_500000 = "500000";

//	private ICDCL solver;
	private RemoteControlStrategy telecomStrategy; 
	private DetailedCommandPanel commandPanel;
	
	private JCheckBox cleanUseOriginalStrategyCB;
	private final static String USE_ORIGINAL_STRATEGY = "Use solver's original deletion strategy";


	
	public CleanCommandComponent(String name, RemoteControlStrategy telecomStrategy, DetailedCommandPanel commandPanel){
		super();
		setName(name);
//		this.logger = logger;
//		this.solver = solver;
		this.telecomStrategy = telecomStrategy;
		this.commandPanel = commandPanel;
		createPanel();
	}
	
	@Override
	public void createPanel() {
		
		this.setBorder(new CompoundBorder(new TitledBorder(null, this.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), DetailedCommandPanel.border5));

		this.setLayout(new BorderLayout());


		cleanSlider = new JSlider(JSlider.HORIZONTAL,CLEAN_MIN,CLEAN_MAX,CLEAN_INIT);

		cleanSlider.setMajorTickSpacing(CLEAN_SPACE);
		cleanSlider.setPaintTicks(true);

		//Create the label table
		cleanValuesTable = new Hashtable<Integer, JLabel>();
		cleanValuesTable.put(new Integer(0),clean5000Label);
		cleanValuesTable.put(new Integer(1),clean10000Label);
		cleanValuesTable.put(new Integer(2),clean20000Label);
		cleanValuesTable.put(new Integer(3),clean50000Label);
		cleanValuesTable.put(new Integer(4),clean100000Label);
		cleanValuesTable.put(new Integer(5),clean500000Label);
		cleanSlider.setLabelTable(cleanValuesTable);

		cleanSlider.setPaintLabels(true);
		cleanSlider.setSnapToTicks(true);

		cleanSlider.setPreferredSize(new Dimension(400,50));

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.add(deleteClauseLabel);
		tmpPanel1.add(cleanSlider);

		JPanel tmpPanel4 = new JPanel();

		evaluationLabel = new JLabel(EVALUATION_TYPE);
		evaluationGroup = new ButtonGroup();
		activityRadio = new JRadioButton(ACTIVITY_BASED);
		lbdRadio = new JRadioButton(LBD_BASED);
		lbd2Radio = new JRadioButton(LBD2_BASED);

		evaluationGroup.add(activityRadio);
		evaluationGroup.add(lbdRadio);
		evaluationGroup.add(lbd2Radio);

		tmpPanel4.add(evaluationLabel);
		tmpPanel4.add(activityRadio);
		tmpPanel4.add(lbdRadio);
		tmpPanel4.add(lbd2Radio);


		cleanAndEvaluationApplyButton = new JButton("Apply changes");
		cleanAndEvaluationApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasChangedCleaningValue();
			}
		});

		JPanel tmpPanel5 = new JPanel();
		tmpPanel5.add(cleanAndEvaluationApplyButton);

		JPanel tmpPanel = new JPanel();
		tmpPanel.setBorder(new CompoundBorder(new TitledBorder(null, "", 
				TitledBorder.LEFT, TitledBorder.TOP), DetailedCommandPanel.border5));

		tmpPanel.setLayout(new BorderLayout());

		tmpPanel.add(tmpPanel1,BorderLayout.NORTH);
		tmpPanel.add(tmpPanel4,BorderLayout.CENTER);
		tmpPanel.add(tmpPanel5,BorderLayout.SOUTH);

		JPanel tmpPanel2 = new JPanel();

		manualCleanLabel = new JLabel(MANUAL_CLEAN);

		cleanButton = new JButton(CLEAN);

		cleanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnClean();
			}
		});


		tmpPanel2.add(manualCleanLabel);
		tmpPanel2.add(cleanButton);

		JPanel tmpPanel3 = new JPanel();
		cleanUseOriginalStrategyCB = new JCheckBox(USE_ORIGINAL_STRATEGY);
		cleanUseOriginalStrategyCB.setSelected(true);

		cleanUseOriginalStrategyCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnUseOriginalStrategy();
			}
		});

		tmpPanel3.add(cleanUseOriginalStrategyCB);


		JPanel tmpPanel6 = new JPanel();
		speedLabel = new JLabel("");
		speedNameLabel = new JLabel(SPEED);
		speedUnitLabel = new JLabel(SPEED_UNIT);

		tmpPanel6.add(speedNameLabel);
		tmpPanel6.add(speedLabel);
		tmpPanel6.add(speedUnitLabel);

		JPanel tmpPanel7 = new JPanel();
		tmpPanel7.setLayout(new BorderLayout());

		tmpPanel7.add(tmpPanel2,BorderLayout.SOUTH);
		tmpPanel7.add(tmpPanel6,BorderLayout.CENTER);


		this.add(tmpPanel3,BorderLayout.NORTH);
		this.add(tmpPanel7,BorderLayout.CENTER);
		this.add(tmpPanel,BorderLayout.SOUTH);

	}
	
	public void hasChangedCleaningValue(){
		int nbConflicts = cleanValues[cleanSlider.getValue()];
		telecomStrategy.setNbClausesAtWhichWeShouldClean(nbConflicts);
		commandPanel.log("Changed number of conflicts before cleaning to " + nbConflicts);
		if(activityRadio.isSelected()){
			commandPanel.setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType.ACTIVITY);
			commandPanel.log("Changed clauses evaluation type to activity");
		}
		else if(lbdRadio.isSelected()){
			commandPanel.setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType.LBD);
			commandPanel.log("Changed clauses evaluation type to lbd");
		}
		else if(lbd2Radio.isSelected()){
			commandPanel.setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType.LBD2);
			commandPanel.log("Changed clauses evaluation type to lbd2");
		}
	}
	
	public void hasClickedOnUseOriginalStrategy(){
		int nbConflicts = cleanValues[cleanSlider.getValue()];
		telecomStrategy.setNbClausesAtWhichWeShouldClean(nbConflicts);

		telecomStrategy.setUseTelecomStrategyAsLearnedConstraintsDeletionStrategy(true);

		commandPanel.setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType.ACTIVITY);
		activityRadio.setSelected(true);

		commandPanel.log("Solver now cleans clauses every " + cleanValues[cleanSlider.getValue()] + " conflicts and bases evaluation of clauses on activity");

		setCleanPanelOriginalStrategyEnabled(false);
	}
	
	public void hasClickedOnClean(){
		commandPanel.log("Told the solver to clean");
		telecomStrategy.setHasClickedOnClean(true);
	}
	
	public void setCleanPanelEnabled(boolean enabled){
		manualCleanLabel.setEnabled(enabled);
		deleteClauseLabel.setEnabled(enabled);
		cleanSlider.setEnabled(enabled);
		cleanButton.setEnabled(enabled);
		evaluationLabel.setEnabled(enabled);
		activityRadio.setEnabled(enabled);
		lbdRadio.setEnabled(enabled);
		lbd2Radio.setEnabled(enabled);
		cleanAndEvaluationApplyButton.setEnabled(enabled);
		cleanUseOriginalStrategyCB.setEnabled(enabled);
		speedLabel.setEnabled(enabled);
		speedUnitLabel.setEnabled(enabled);
		speedNameLabel.setEnabled(enabled);
		this.repaint();
	}

	public void setCleanPanelOriginalStrategyEnabled(boolean enabled){
		cleanUseOriginalStrategyCB.setEnabled(enabled);
		manualCleanLabel.setEnabled(!enabled);
		deleteClauseLabel.setEnabled(!enabled);
		activityRadio.setEnabled(!enabled);
		evaluationLabel.setEnabled(!enabled);
		lbdRadio.setEnabled(!enabled);
		lbd2Radio.setEnabled(!enabled);
		cleanAndEvaluationApplyButton.setEnabled(!enabled);
		cleanSlider.setEnabled(!enabled);
		cleanButton.setEnabled(!enabled);
		this.repaint();
	}
	
	public void setSpeedLabeltext(String speed){
		speedLabel.setText(speed);
		speedLabel.invalidate();
	}

}
