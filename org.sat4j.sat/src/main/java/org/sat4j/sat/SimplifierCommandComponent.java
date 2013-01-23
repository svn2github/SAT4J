package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.sat4j.minisat.core.SimplificationType;

public class SimplifierCommandComponent extends CommandComponent {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private SolverController controller;

    private static final String SIMPLIFICATION_APPLY = "Apply";
    public static final String SIMPLIFICATION_NO = "No reason simplification";
    public static final String SIMPLIFICATION_SIMPLE = "Simple reason simplification";
    public static final String SIMPLIFICATION_EXPENSIVE = "Expensive reason simplification";

    private JButton simplificationApplyButton;
    private ButtonGroup simplificationGroup;
    private JRadioButton simplificationNoRadio;
    private JRadioButton simplificationSimpleRadio;
    private JRadioButton simplificationExpensiveRadio;

    public SimplifierCommandComponent(String name, SolverController controller) {
        this.setName(name);
        this.controller = controller;
        createPanel();
    }

    @Override
    public void createPanel() {
        createSimplifierPanel();
    }

    public void createSimplifierPanel() {

        this.setBorder(new CompoundBorder(new TitledBorder(null,
                this.getName(), TitledBorder.LEFT, TitledBorder.TOP),
                DetailedCommandPanel.BORDER5));

        this.setLayout(new BorderLayout());

        // simplificationRadio = new Radio
        this.simplificationGroup = new ButtonGroup();
        this.simplificationExpensiveRadio = new JRadioButton(
                SIMPLIFICATION_EXPENSIVE);
        this.simplificationNoRadio = new JRadioButton(SIMPLIFICATION_NO);
        this.simplificationSimpleRadio = new JRadioButton(SIMPLIFICATION_SIMPLE);

        JPanel tmpPanel1 = new JPanel();
        tmpPanel1.setLayout(new BoxLayout(tmpPanel1, BoxLayout.Y_AXIS));

        this.simplificationGroup.add(this.simplificationNoRadio);
        this.simplificationGroup.add(this.simplificationSimpleRadio);
        this.simplificationGroup.add(this.simplificationExpensiveRadio);

        tmpPanel1.add(this.simplificationNoRadio);
        tmpPanel1.add(this.simplificationSimpleRadio);
        tmpPanel1.add(this.simplificationExpensiveRadio);

        this.simplificationApplyButton = new JButton(SIMPLIFICATION_APPLY);

        this.simplificationApplyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hasClickedOnApplySimplification();
            }
        });

        JPanel tmpPanel2 = new JPanel();
        tmpPanel2.add(this.simplificationApplyButton);

        this.add(tmpPanel1, BorderLayout.NORTH);
        this.add(tmpPanel2, BorderLayout.SOUTH);

    }

    public void setSelectedSimplification(String simplification) {
        if (simplification.equals(SIMPLIFICATION_EXPENSIVE)) {
            this.simplificationExpensiveRadio.setSelected(true);
        } else if (simplification.equals(SIMPLIFICATION_SIMPLE)) {
            this.simplificationSimpleRadio.setSelected(true);
        } else {
            this.simplificationNoRadio.setSelected(true);
        }
    }

    public void hasClickedOnApplySimplification() {
        if (this.simplificationSimpleRadio.isSelected()) {
            this.controller
                    .setSimplifier(SimplificationType.SIMPLE_SIMPLIFICATION);
        } else if (this.simplificationExpensiveRadio.isSelected()) {
            this.controller
                    .setSimplifier(SimplificationType.EXPENSIVE_SIMPLIFICATION);
        } else {
            this.controller.setSimplifier(SimplificationType.NO_SIMPLIFICATION);
        }

    }

    public void setSimplifierPanelEnabled(boolean enabled) {
        this.simplificationNoRadio.setEnabled(enabled);
        this.simplificationExpensiveRadio.setEnabled(enabled);
        this.simplificationSimpleRadio.setEnabled(enabled);
        this.simplificationApplyButton.setEnabled(enabled);
        this.repaint();
    }

}
