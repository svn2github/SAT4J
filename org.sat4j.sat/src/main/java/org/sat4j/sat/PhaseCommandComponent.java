package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.sat4j.minisat.core.IPhaseSelectionStrategy;

public class PhaseCommandComponent extends CommandComponent {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String currentPhaseSelectionStrategy;

    private JComboBox phaseList;
    private JLabel phaseListLabel;
    private static final String PHASE_STRATEGY = "Choose phase strategy :";

    private JButton phaseApplyButton;
    private static final String PHASE_APPLY = "Apply";

    private static final String PHASE_STRATEGY_CLASS = "org.sat4j.minisat.core.IPhaseSelectionStrategy";
    private static final String PHASE_PATH_SAT = "org.sat4j.minisat.orders";

    private SolverController solverController;

    public PhaseCommandComponent(String name, SolverController commandPanel,
            String initialPhaseStrategyName) {
        this.currentPhaseSelectionStrategy = initialPhaseStrategyName;
        this.solverController = commandPanel;
        this.setName(name);
        createPanel();
    }

    @Override
    public void createPanel() {
        createPhasePanel();
    }

    public void createPhasePanel() {

        this.setBorder(new CompoundBorder(new TitledBorder(null,
                this.getName(), TitledBorder.LEFT, TitledBorder.TOP),
                DetailedCommandPanel.BORDER5));

        this.setLayout(new BorderLayout());

        JPanel tmpPanel1 = new JPanel();
        tmpPanel1.setLayout(new FlowLayout());

        this.phaseListLabel = new JLabel(PHASE_STRATEGY);

        this.phaseList = new JComboBox(getListOfPhaseStrategies().toArray());
        this.phaseList.setSelectedItem(this.currentPhaseSelectionStrategy);

        tmpPanel1.add(this.phaseListLabel);
        tmpPanel1.add(this.phaseList);

        this.phaseApplyButton = new JButton(PHASE_APPLY);

        this.phaseApplyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hasClickedOnApplyPhase();
            }
        });

        JPanel tmpPanel2 = new JPanel();
        tmpPanel2.add(this.phaseApplyButton);

        this.add(tmpPanel1, BorderLayout.CENTER);
        this.add(tmpPanel2, BorderLayout.SOUTH);
    }

    public void hasClickedOnApplyPhase() {
        String phaseName = (String) this.phaseList.getSelectedItem();
        this.currentPhaseSelectionStrategy = phaseName;
        IPhaseSelectionStrategy phase = null;
        try {
            phase = (IPhaseSelectionStrategy) Class.forName(
                    PHASE_PATH_SAT + "." + phaseName).newInstance();
            phase.init(this.solverController.getNVar() + 1);
            this.solverController.setPhaseSelectionStrategy(phase);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    public void setPhasePanelEnabled(boolean enabled) {
        this.phaseList.setEnabled(enabled);
        this.phaseListLabel.setEnabled(enabled);
        this.phaseApplyButton.setEnabled(enabled);
        repaint();
    }

    public List<String> getListOfPhaseStrategies() {
        List<String> resultRTSI = RTSI.find(PHASE_STRATEGY_CLASS);
        List<String> finalResult = new ArrayList<String>();

        for (String s : resultRTSI) {
            if (!s.contains("Remote")) {
                finalResult.add(s);
            }
        }

        return finalResult;
    }

    public void setPhaseListSelectedItem(String name) {
        this.currentPhaseSelectionStrategy = name;
        this.phaseList.setSelectedItem(this.currentPhaseSelectionStrategy);
        repaint();
    }
}
