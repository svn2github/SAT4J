package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public class RandomWalkCommandComponent extends CommandComponent {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private SolverController controller;

    private JLabel probaRWLabel;
    private JTextField probaRWField;

    private JButton applyRWButton;

    private static final String RW_LABEL = "Probabilty : ";
    private static final String RW_APPLY = "Apply";

    public RandomWalkCommandComponent(String name, SolverController controller) {
        this.controller = controller;
        this.setName(name);
        createPanel();
    }

    @Override
    public void createPanel() {
        createRWPanel();
    }

    public void createRWPanel() {

        this.setBorder(new CompoundBorder(new TitledBorder(null,
                this.getName(), TitledBorder.LEFT, TitledBorder.TOP),
                DetailedCommandPanel.BORDER5));

        this.setLayout(new BorderLayout());

        this.probaRWLabel = new JLabel(RW_LABEL);
        this.probaRWField = new JTextField("0", 10);

        this.probaRWLabel.setLabelFor(this.probaRWField);

        JPanel tmpPanel1 = new JPanel();
        tmpPanel1.setLayout(new FlowLayout());

        tmpPanel1.add(this.probaRWLabel);
        tmpPanel1.add(this.probaRWField);

        JPanel tmpPanel2 = new JPanel();
        this.applyRWButton = new JButton(RW_APPLY);

        this.applyRWButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hasClickedOnApplyRW();
            }
        });

        tmpPanel2.add(this.applyRWButton);

        this.add(tmpPanel1, BorderLayout.CENTER);
        this.add(tmpPanel2, BorderLayout.SOUTH);

    }

    public void hasClickedOnApplyRW() {
        double proba = 0;
        if (this.probaRWField != null) {
            proba = Double.parseDouble(this.probaRWField.getText());
        }

        this.controller.setRandomWalkProba(proba);
    }

    public void setRWPanelEnabled(boolean enabled) {
        this.probaRWLabel.setEnabled(enabled);
        this.probaRWField.setEnabled(enabled);
        this.applyRWButton.setEnabled(enabled);
        this.repaint();
    }

    public void setProba(double proba) {
        this.probaRWField.setText(proba + "");
        this.repaint();
    }

}
