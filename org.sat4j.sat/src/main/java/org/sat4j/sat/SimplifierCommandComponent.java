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

public class SimplifierCommandComponent extends CommandComponent{

	private SolverController controller;
	
	private final static String SIMPLIFICATION_APPLY = "Apply";
	public final static String SIMPLIFICATION_NO = "No reason simplification";
	public final static String SIMPLIFICATION_SIMPLE = "Simple reason simplification";
	public final static String SIMPLIFICATION_EXPENSIVE = "Expensive reason simplification";

	private JButton simplificationApplyButton;
	private ButtonGroup simplificationGroup;
	private JRadioButton simplificationNoRadio;
	private JRadioButton simplificationSimpleRadio;
	private JRadioButton simplificationExpensiveRadio;
	
	
	public SimplifierCommandComponent(String name, SolverController controller){
		this.setName(name);
		this.controller=controller;
		createPanel();
	}
	
	@Override
	public void createPanel() {
		createSimplifierPanel();
	}

	
	public void createSimplifierPanel(){
		
		this.setBorder(new CompoundBorder(new TitledBorder(null, this.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), DetailedCommandPanel.border5));

		this.setLayout(new BorderLayout());

		//		simplificationRadio = new Radio
		simplificationGroup = new ButtonGroup();
		simplificationExpensiveRadio = new JRadioButton(SIMPLIFICATION_EXPENSIVE);
		simplificationNoRadio = new JRadioButton(SIMPLIFICATION_NO);
		simplificationSimpleRadio = new JRadioButton(SIMPLIFICATION_SIMPLE);

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.setLayout(new BoxLayout(tmpPanel1, BoxLayout.Y_AXIS));

		simplificationGroup.add(simplificationNoRadio);
		simplificationGroup.add(simplificationSimpleRadio);
		simplificationGroup.add(simplificationExpensiveRadio);


		tmpPanel1.add(simplificationNoRadio);
		tmpPanel1.add(simplificationSimpleRadio);
		tmpPanel1.add(simplificationExpensiveRadio);

		simplificationApplyButton = new JButton(SIMPLIFICATION_APPLY);

		simplificationApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnApplySimplification();
			}
		});

		JPanel tmpPanel2 = new JPanel();
		tmpPanel2.add(simplificationApplyButton);

		this.add(tmpPanel1,BorderLayout.NORTH);
		this.add(tmpPanel2,BorderLayout.SOUTH);

	}
	
	public void setSelectedSimplification(String simplification){
		if(simplification.equals(SIMPLIFICATION_EXPENSIVE)){
			simplificationExpensiveRadio.setSelected(true);
		}
		else if(simplification.equals(SIMPLIFICATION_SIMPLE)){
			simplificationSimpleRadio.setSelected(true);
		}
		else{
			simplificationNoRadio.setSelected(true);
		}
	}
	
	public void hasClickedOnApplySimplification(){
		if(simplificationSimpleRadio.isSelected()){
			controller.setSimplifier(SimplificationType.SIMPLE_SIMPLIFICATION);
		}
		else if(simplificationExpensiveRadio.isSelected()){
			controller.setSimplifier(SimplificationType.EXPENSIVE_SIMPLIFICATION);
		}
		else{
			controller.setSimplifier(SimplificationType.NO_SIMPLIFICATION);
		}

	}
	
	public void setSimplifierPanelEnabled(boolean enabled){
		simplificationNoRadio.setEnabled(enabled);
		simplificationExpensiveRadio.setEnabled(enabled);
		simplificationSimpleRadio.setEnabled(enabled);
		simplificationApplyButton.setEnabled(enabled);
		this.repaint();
	}
	
	
}
