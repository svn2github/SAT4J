package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public class HotSolverCommandComponent extends CommandComponent {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private SolverController controller;

    private JCheckBox keepSolverHotCB;
    private static final String KEEP_SOLVER_HOT = "Keep solver hot";
    private JButton applyHotSolver;
    private static final String HOT_APPLY = "Apply";

    public HotSolverCommandComponent(String name, SolverController controller) {
        this.setName(name);
        this.controller = controller;
        createPanel();
    }

    @Override
    public void createPanel() {
        createHotSolverPanel();
    }

    public void createHotSolverPanel() {
        this.setBorder(new CompoundBorder(new TitledBorder(null,
                this.getName(), TitledBorder.LEFT, TitledBorder.TOP),
                DetailedCommandPanel.BORDER5));

        this.setLayout(new BorderLayout());

        this.keepSolverHotCB = new JCheckBox(KEEP_SOLVER_HOT);
        this.add(this.keepSolverHotCB, BorderLayout.CENTER);

        JPanel tmpPanel = new JPanel();

        this.applyHotSolver = new JButton(HOT_APPLY);
        tmpPanel.add(this.applyHotSolver);
        this.add(tmpPanel, BorderLayout.SOUTH);

        this.applyHotSolver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HotSolverCommandComponent.this.controller
                        .setKeepSolverHot(HotSolverCommandComponent.this.keepSolverHotCB
                                .isSelected());
            }
        });
    }

    public void setKeepSolverHotPanelEnabled(boolean enabled) {
        this.keepSolverHotCB.setEnabled(enabled);
        this.applyHotSolver.setEnabled(enabled);
        this.repaint();
    }

}
