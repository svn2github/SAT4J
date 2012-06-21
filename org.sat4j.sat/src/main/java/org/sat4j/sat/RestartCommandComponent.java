package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.restarts.LubyRestarts;
import org.sat4j.minisat.restarts.NoRestarts;
import org.sat4j.pb.tools.RTSI;

public class RestartCommandComponent extends CommandComponent{
	
	
	private static final long serialVersionUID = 1L;
	
	private JPanel restartPropertiesPanel;
	private JPanel restartButtonPanel;

	private JLabel chooseRestartStrategyLabel;
	private JLabel noParameterLabel;
	private JComboBox listeRestarts;
	private JButton restartButton;
	
	private JButton changeRestartMode;
	
	private JLabel factorLabel;
	private final static String FACTOR = "Factor: ";
	private JTextField factorField;
	
	public String currentRestart;
	
	private final static String RESTART = "Restart";
	private final static String CHOOSE_RESTART_STRATEGY = "Choose restart strategy: ";
	private final static String CHANGE_RESTART_STRATEGY = "Apply";
	private final static String MANUAL_RESTART = "Manual Restart";
	private final static String NO_PARAMETER_FOR_THIS_STRATEGY = "No paramaters for this strategy";
	private final static String RESTART_STRATEGY_CLASS = "org.sat4j.minisat.core.RestartStrategy";
	private final static String RESTART_PATH="org.sat4j.minisat.restarts";
	
	private SolverController controller;
	
	public RestartCommandComponent(String name, SolverController controller, String initialRestartStrategy) {
		this.setName(name);
		currentRestart = initialRestartStrategy;
		this.controller = controller;
		createPanel();
		initFactorParam();
	}

	
	public void createPanel(){
		this.setBorder(new CompoundBorder(new TitledBorder(null, this.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), DetailedCommandPanel.border5));

		this.setLayout(new BorderLayout());

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.setLayout(new FlowLayout());

		chooseRestartStrategyLabel = new JLabel(CHOOSE_RESTART_STRATEGY);

		listeRestarts = new JComboBox(getListOfRestartStrategies().toArray());	
		
		listeRestarts.setSelectedItem(currentRestart);

		listeRestarts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifyRestartParamPanel();
			}
		});

		tmpPanel1.add(chooseRestartStrategyLabel);
		tmpPanel1.add(listeRestarts);
		
		changeRestartMode = new JButton(CHANGE_RESTART_STRATEGY);
		
		changeRestartMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnChange();
			}
		});
		
		tmpPanel1.add(changeRestartMode);

		noParameterLabel = new JLabel(NO_PARAMETER_FOR_THIS_STRATEGY);

		Font newLabelFont=new Font(noParameterLabel.getFont().getName(),Font.ITALIC,noParameterLabel.getFont().getSize());

		noParameterLabel.setFont(newLabelFont);

		restartPropertiesPanel = new JPanel();
		restartPropertiesPanel.add(noParameterLabel);

		

		restartButton = new JButton(RESTART);

		restartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnRestart();
			}
		});

		restartButtonPanel = new JPanel();
		restartButtonPanel.setName(MANUAL_RESTART);
		restartButtonPanel.setBorder(new CompoundBorder(new TitledBorder(null, restartButtonPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), DetailedCommandPanel.border5));

		restartButtonPanel.add(restartButton);
		
		restartPropertiesPanel.setPreferredSize(new Dimension(100,50));

		this.add(tmpPanel1,BorderLayout.NORTH);
		this.add(restartPropertiesPanel,BorderLayout.CENTER);
		this.add(restartButtonPanel,BorderLayout.SOUTH);
	}
	
	
	public void initFactorParam(){
		//		lubyPanel = new JPanel();
		//		//		lubyPanel.setLayout(new FlowLayout());

		factorLabel = new JLabel(FACTOR);
		factorField = new JTextField(LubyRestarts.DEFAULT_LUBY_FACTOR+"",5);
		//factorField.setMargin(new Insets(0, 0, 0, 0));
		//factorLabel.setLabelFor(factorField);

		//		lubyPanel.add(factorLabel);
		//		lubyPanel.add(factorField);

	}
	
	public void modifyRestartParamPanel(){
		restartPropertiesPanel.removeAll();
		if(listeRestarts.getSelectedItem().equals("LubyRestarts")){
			restartPropertiesPanel.add(factorLabel);
			restartPropertiesPanel.add(factorField);
		}
		else{
			restartPropertiesPanel.add(noParameterLabel);
		}
		setRestartPropertiesPanelEnabled(true);
		restartPropertiesPanel.repaint();
		this.repaint();
		this.paintAll(this.getGraphics());
		this.repaint();
	}
	
	public void setRestartPanelEnabled(boolean enabled){
		listeRestarts.setEnabled(enabled);
		restartButton.setEnabled(enabled);
		chooseRestartStrategyLabel.setEnabled(enabled);
		setRestartPropertiesPanelEnabled(enabled);
		this.repaint();
	}

	public void setRestartPropertiesPanelEnabled(boolean enabled){
		for(Component c:restartPropertiesPanel.getComponents()){
			c.setEnabled(enabled);
		}
		restartPropertiesPanel.repaint();
	}
	
	public void updateRestartStrategyPanel(){
		listeRestarts.setSelectedItem(currentRestart);
	}
	
	public void hasClickedOnChange(){
		controller.shouldRestartNow();
	
		String choix = (String)listeRestarts.getSelectedItem();

		boolean isNotSameRestart = !choix.equals(currentRestart);
		boolean shouldInit = isNotSameRestart;

		RestartStrategy restart = new NoRestarts();
		SearchParams params = controller.getSearchParams();

		if(choix.equals("LubyRestarts")){
			boolean factorChanged = false;
			int factor = LubyRestarts.DEFAULT_LUBY_FACTOR;
			if(factorField.getText()!=null){
				factor = Integer.parseInt(factorField.getText());
			}
			// if the current restart is a LubyRestart
			if(isNotSameRestart){
				restart = new LubyRestarts(factor);
				controller.setRestartStrategy(restart);
			}
			else{
				factorChanged = !(factor==((LubyRestarts)controller.getRestartStrategy()).getFactor());
			}
			// if the factor has changed
			if(factorChanged){
				restart = controller.getRestartStrategy();
				((LubyRestarts)restart).setFactor(factor);
			}
			shouldInit = isNotSameRestart || factorChanged;

			if(shouldInit){
				controller.init(params);
			}

		}

		else try{
			restart = (RestartStrategy)Class.forName(RESTART_PATH+"."+choix).newInstance();
			assert restart!=null;
			controller.setRestartStrategy(restart);
			controller.init(params);

		}
		catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		catch(IllegalAccessException e){
			e.printStackTrace();
		}
		catch(InstantiationException e){
			e.printStackTrace();
		}

		currentRestart = choix;


		//		if(shouldInit)
		//			telecomStrategy.setRestartStrategy(restart,params);

		
	}
	
	public void hasClickedOnRestart(){
		controller.shouldRestartNow();	
	}
	
	public List<String> getListOfRestartStrategies(){
		List<String> resultRTSI = RTSI.find(RESTART_STRATEGY_CLASS);
		List<String> finalResult = new ArrayList<String>();

		//		finalResult.add(RESTART_NO_STRATEGY);

		for(String s:resultRTSI){
			if(!s.contains("Remote")){
				finalResult.add(s);
			}
		}

		return finalResult;
	}
	
	public String getCurrentRestart(){
		return currentRestart;
	}
	
	public void setCurrentRestart(String currentRestart){
		this.currentRestart = currentRestart;
		updateRestartStrategyPanel();
		modifyRestartParamPanel();
	}
}
