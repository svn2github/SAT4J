package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public class HotSolverCommandComponent extends CommandComponent{

	private SolverController controller;
	
	private JCheckBox keepSolverHotCB;
	private final static String KEEP_SOLVER_HOT = "Keep solver hot";
	private JButton applyHotSolver;
	private final static String HOT_APPLY = "Apply";
	
	public HotSolverCommandComponent(String name, SolverController controller){
		this.setName(name);
		this.controller = controller;
		createPanel();
	} 
	
	@Override
	public void createPanel() {
		createHotSolverPanel();
	}
	
	public void createHotSolverPanel(){
		this.setBorder(new CompoundBorder(new TitledBorder(null, this.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), DetailedCommandPanel.border5));

		this.setLayout(new BorderLayout());

		keepSolverHotCB = new JCheckBox(KEEP_SOLVER_HOT);
		this.add(keepSolverHotCB,BorderLayout.CENTER);

		JPanel tmpPanel = new JPanel();

		applyHotSolver = new JButton(HOT_APPLY);
		tmpPanel.add(applyHotSolver);
		this.add(tmpPanel,BorderLayout.SOUTH);


		applyHotSolver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setKeepSolverHot(keepSolverHotCB.isSelected());
			}
		});
	}
	
	public void setKeepSolverHotPanelEnabled(boolean enabled){
		keepSolverHotCB.setEnabled(enabled);
		applyHotSolver.setEnabled(enabled);
		this.repaint();
	}

}
