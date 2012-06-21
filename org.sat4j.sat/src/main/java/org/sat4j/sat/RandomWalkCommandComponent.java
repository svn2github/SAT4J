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

public class RandomWalkCommandComponent extends CommandComponent{

	private SolverController controller;
	
	private JLabel probaRWLabel;
	private JTextField probaRWField;

	private JButton applyRWButton;

	
	private final static String RW_LABEL = "Probabilty : ";
	private final static String RW_APPLY = "Apply";

	
	public RandomWalkCommandComponent(String name, SolverController controller){
		this.controller = controller;
		this.setName(name);
		createPanel();
	}
	
	
	@Override
	public void createPanel() {
		createRWPanel();
	}
	
	public void createRWPanel(){
		
		
		this.setBorder(new CompoundBorder(new TitledBorder(null, this.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), DetailedCommandPanel.border5));

		this.setLayout(new BorderLayout());

		probaRWLabel = new JLabel(RW_LABEL);
		probaRWField = new JTextField("0",10);

		probaRWLabel.setLabelFor(probaRWField);

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.setLayout(new FlowLayout());

		tmpPanel1.add(probaRWLabel);
		tmpPanel1.add(probaRWField);

		JPanel tmpPanel2 = new JPanel();
		applyRWButton = new JButton(RW_APPLY);

		applyRWButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnApplyRW();
			}
		});

		tmpPanel2.add(applyRWButton);

		this.add(tmpPanel1, BorderLayout.CENTER);
		this.add(tmpPanel2, BorderLayout.SOUTH);


	}
	
	public void hasClickedOnApplyRW(){
		double proba=0;
		if(probaRWField!=null)
			proba = Double.parseDouble(probaRWField.getText());

		controller.setRandomWalkProba(proba);
	}

	
	public void setRWPanelEnabled(boolean enabled){
		probaRWLabel.setEnabled(enabled);
		probaRWField.setEnabled(enabled);
		applyRWButton.setEnabled(enabled);
		this.repaint();
	}
	
	public void setProba(double proba){
		probaRWField.setText(proba+"");
		this.repaint();
	}
	
}
