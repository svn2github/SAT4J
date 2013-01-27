package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.sat4j.minisat.core.LearnedConstraintsEvaluationType;

public class CleanCommandComponent extends CommandComponent {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JSlider cleanSlider;

    private static final String EVALUATION_TYPE = "Clauses evaluation type";
    private static final String ACTIVITY_BASED = "Activity";
    private static final String LBD_BASED = "LBD";
    private static final String LBD2_BASED = "LBD 2";
    private JLabel evaluationLabel;
    private JRadioButton activityRadio;
    private JRadioButton lbdRadio;
    private JRadioButton lbd2Radio;

    private JButton cleanAndEvaluationApplyButton;

    private JButton cleanButton;
    private static final String CLEAN = "Clean now";
    private static final String MANUAL_CLEAN = "Manual clean: ";
    private JLabel manualCleanLabel;

    private JLabel speedLabel;
    private JLabel speedNameLabel;
    private static final String SPEED = "Speed :";
    private JLabel speedUnitLabel;
    private static final String SPEED_UNIT = " propagations per second";

    private final JLabel deleteClauseLabel = new JLabel(DELETE_CLAUSES);
    private static final String DELETE_CLAUSES = "Automated clean: ";

    private final JLabel clean5000Label = new JLabel(CLEAN_5000);
    private final JLabel clean10000Label = new JLabel(CLEAN_10000);
    private final JLabel clean20000Label = new JLabel(CLEAN_20000);
    private final JLabel clean50000Label = new JLabel(CLEAN_50000);
    private final JLabel clean100000Label = new JLabel(CLEAN_100000);
    private final JLabel clean500000Label = new JLabel(CLEAN_500000);
    private static final int[] CLEAN_VALUES = { 5000, 10000, 20000, 50000,
            100000, 500000 };
    private static final int CLEAN_MIN = 0;
    private static final int CLEAN_MAX = 5;
    private static final int CLEAN_INIT = 1;
    private static final int CLEAN_SPACE = 1;

    private static final String CLEAN_5000 = "5000";
    private static final String CLEAN_10000 = "10000";
    private static final String CLEAN_20000 = "20000";
    private static final String CLEAN_50000 = "50000";
    private static final String CLEAN_100000 = "100000";
    private static final String CLEAN_500000 = "500000";

    private SolverController controller;

    private JCheckBox cleanUseOriginalStrategyCB;
    private static final String USE_ORIGINAL_STRATEGY = "Use solver's original deletion strategy";

    public CleanCommandComponent(String name, SolverController controller) {
        super();
        setName(name);
        this.controller = controller;
        createPanel();
    }

    @Override
    public void createPanel() {

        this.setBorder(new CompoundBorder(new TitledBorder(null,
                this.getName(), TitledBorder.LEFT, TitledBorder.TOP),
                DetailedCommandPanel.BORDER5));

        this.setLayout(new BorderLayout());

        this.cleanSlider = new JSlider(SwingConstants.HORIZONTAL, CLEAN_MIN,
                CLEAN_MAX, CLEAN_INIT);

        this.cleanSlider.setMajorTickSpacing(CLEAN_SPACE);
        this.cleanSlider.setPaintTicks(true);

        // Create the label table
        Dictionary<Integer, JLabel> cleanValuesTable = new Hashtable<Integer, JLabel>();
        cleanValuesTable.put(0, this.clean5000Label);
        cleanValuesTable.put(1, this.clean10000Label);
        cleanValuesTable.put(2, this.clean20000Label);
        cleanValuesTable.put(3, this.clean50000Label);
        cleanValuesTable.put(4, this.clean100000Label);
        cleanValuesTable.put(5, this.clean500000Label);
        this.cleanSlider.setLabelTable(cleanValuesTable);

        this.cleanSlider.setPaintLabels(true);
        this.cleanSlider.setSnapToTicks(true);

        this.cleanSlider.setPreferredSize(new Dimension(400, 50));

        JPanel tmpPanel1 = new JPanel();
        tmpPanel1.add(this.deleteClauseLabel);
        tmpPanel1.add(this.cleanSlider);

        JPanel tmpPanel4 = new JPanel();

        this.evaluationLabel = new JLabel(EVALUATION_TYPE);
        ButtonGroup evaluationGroup = new ButtonGroup();
        this.activityRadio = new JRadioButton(ACTIVITY_BASED);
        this.lbdRadio = new JRadioButton(LBD_BASED);
        this.lbd2Radio = new JRadioButton(LBD2_BASED);

        evaluationGroup.add(this.activityRadio);
        evaluationGroup.add(this.lbdRadio);
        evaluationGroup.add(this.lbd2Radio);

        tmpPanel4.add(this.evaluationLabel);
        tmpPanel4.add(this.activityRadio);
        tmpPanel4.add(this.lbdRadio);
        tmpPanel4.add(this.lbd2Radio);

        this.cleanAndEvaluationApplyButton = new JButton("Apply changes");
        this.cleanAndEvaluationApplyButton
                .addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        hasChangedCleaningValue();
                    }
                });

        JPanel tmpPanel5 = new JPanel();
        tmpPanel5.add(this.cleanAndEvaluationApplyButton);

        JPanel tmpPanel = new JPanel();
        tmpPanel.setBorder(new CompoundBorder(new TitledBorder(null, "",
                TitledBorder.LEFT, TitledBorder.TOP),
                DetailedCommandPanel.BORDER5));

        tmpPanel.setLayout(new BorderLayout());

        tmpPanel.add(tmpPanel1, BorderLayout.NORTH);
        tmpPanel.add(tmpPanel4, BorderLayout.CENTER);
        tmpPanel.add(tmpPanel5, BorderLayout.SOUTH);

        JPanel tmpPanel2 = new JPanel();

        this.manualCleanLabel = new JLabel(MANUAL_CLEAN);

        this.cleanButton = new JButton(CLEAN);

        this.cleanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hasClickedOnClean();
            }
        });

        tmpPanel2.add(this.manualCleanLabel);
        tmpPanel2.add(this.cleanButton);

        JPanel tmpPanel3 = new JPanel();
        this.cleanUseOriginalStrategyCB = new JCheckBox(USE_ORIGINAL_STRATEGY);
        this.cleanUseOriginalStrategyCB.setSelected(true);

        this.cleanUseOriginalStrategyCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hasClickedOnUseOriginalStrategy();
            }
        });

        tmpPanel3.add(this.cleanUseOriginalStrategyCB);

        JPanel tmpPanel6 = new JPanel();
        this.speedLabel = new JLabel("");
        this.speedNameLabel = new JLabel(SPEED);
        this.speedUnitLabel = new JLabel(SPEED_UNIT);

        tmpPanel6.add(this.speedNameLabel);
        tmpPanel6.add(this.speedLabel);
        tmpPanel6.add(this.speedUnitLabel);

        JPanel tmpPanel7 = new JPanel();
        tmpPanel7.setLayout(new BorderLayout());

        tmpPanel7.add(tmpPanel2, BorderLayout.SOUTH);
        tmpPanel7.add(tmpPanel6, BorderLayout.CENTER);

        this.add(tmpPanel3, BorderLayout.NORTH);
        this.add(tmpPanel7, BorderLayout.CENTER);
        this.add(tmpPanel, BorderLayout.SOUTH);

    }

    public void hasChangedCleaningValue() {
        int nbConflicts = CLEAN_VALUES[this.cleanSlider.getValue()];
        this.controller.setNbClausesAtWhichWeShouldClean(nbConflicts);
        if (this.activityRadio.isSelected()) {
            this.controller
                    .setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType.ACTIVITY);
        } else if (this.lbdRadio.isSelected()) {
            this.controller
                    .setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType.LBD);
        } else if (this.lbd2Radio.isSelected()) {
            this.controller
                    .setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType.LBD2);
        }
    }

    public void hasClickedOnUseOriginalStrategy() {
        int nbConflicts = CLEAN_VALUES[this.cleanSlider.getValue()];
        this.controller.setNbClausesAtWhichWeShouldClean(nbConflicts);

        this.controller
                .setUseTelecomStrategyAsLearnedConstraintsDeletionStrategy();

        this.controller
                .setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType.ACTIVITY);
        this.activityRadio.setSelected(true);

        setCleanPanelOriginalStrategyEnabled(false);
    }

    public int getCleanSliderValue() {
        return CLEAN_VALUES[this.cleanSlider.getValue()];
    }

    public void hasClickedOnClean() {
        this.controller.shouldCleanNow();
    }

    public void setCleanPanelEnabled(boolean enabled) {
        this.manualCleanLabel.setEnabled(enabled);
        this.deleteClauseLabel.setEnabled(enabled);
        this.cleanSlider.setEnabled(enabled);
        this.cleanButton.setEnabled(enabled);
        this.evaluationLabel.setEnabled(enabled);
        this.activityRadio.setEnabled(enabled);
        this.lbdRadio.setEnabled(enabled);
        this.lbd2Radio.setEnabled(enabled);
        this.cleanAndEvaluationApplyButton.setEnabled(enabled);
        this.cleanUseOriginalStrategyCB.setEnabled(enabled);
        this.speedLabel.setEnabled(enabled);
        this.speedUnitLabel.setEnabled(enabled);
        this.speedNameLabel.setEnabled(enabled);
        this.repaint();
    }

    public void setCleanPanelOriginalStrategyEnabled(boolean enabled) {
        this.cleanUseOriginalStrategyCB.setEnabled(enabled);
        this.manualCleanLabel.setEnabled(!enabled);
        this.deleteClauseLabel.setEnabled(!enabled);
        this.activityRadio.setEnabled(!enabled);
        this.evaluationLabel.setEnabled(!enabled);
        this.lbdRadio.setEnabled(!enabled);
        this.lbd2Radio.setEnabled(!enabled);
        this.cleanAndEvaluationApplyButton.setEnabled(!enabled);
        this.cleanSlider.setEnabled(!enabled);
        this.cleanButton.setEnabled(!enabled);
        this.repaint();
    }

    public void setSpeedLabeltext(String speed) {
        this.speedLabel.setText(speed);
        this.speedLabel.invalidate();
    }

}
