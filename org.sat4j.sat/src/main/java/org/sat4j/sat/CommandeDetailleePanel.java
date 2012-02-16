/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 * 
 * Based on the original MiniSat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 * 
 *******************************************************************************/
package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.restarts.LubyRestarts;
import org.sat4j.minisat.restarts.NoRestarts;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ConflictDepthTracing;
import org.sat4j.tools.ConflictLevelTracing;
import org.sat4j.tools.DecisionTracing;
import org.sat4j.tools.LearnedClausesSizeTracing;
import org.sat4j.tools.MultiTracing;


/**
 * 
 * This panel contains buttons that control restart and clean on solver.
 * It also displays history of commands.
 * 
 * @author sroussel
 *
 */
public class CommandeDetailleePanel extends JPanel implements ILog{


	private static final long serialVersionUID = 1L;

	private static final EmptyBorder border5 = new EmptyBorder(5,5,5,5);

	private TelecommandeStrategy telecomStrategy;
	private RandomWalkDecorator randomWalk;
	private ICDCL solver;

	private JPanel instancePanel;
	private final static String INSTANCE_PANEL = "Instance";
	private JLabel instanceLabel;
	private final static String INSTANCE  = "Path to instance: ";
	private JTextField instancePathField;
	private String instancePath;
	private JButton browseButton;
	private final static String BROWSE = "Browse";


	private final static String MINISAT_PREFIX = "minisat";
	private final static String PB_PREFIX = "pb";
	private JPanel choixSolverPanel;
	private final static String CHOIX_SOLVER_PANEL = "Solver";
	private JLabel choixSolver;
	private final static String CHOIX_SOLVER  = "Choix du solver: ";
	private String selectedSolver;
	private JComboBox listeSolvers;

	private JButton startStopButton;
	private static final String START = "Start";
	private static final String STOP = "Stop";


	private final static String RESTART_PANEL = "Restart strategy";	
	private final static String RESTART = "Restart";

	private JPanel restartPanel;

	private JPanel restartPropertiesPanel;
	private JPanel restartButtonPanel;

	private JLabel chooseRestartStrategyLabel;
	private final static String CHOOSE_RESTART_STRATEGY = "Choose restart strategy: ";

	private final static String NO_PARAMETER_FOR_THIS_STRATEGY = "No paramaters for this strategy";
	private JLabel noParameterLabel;

	private JComboBox listeRestarts;
	private String currentRestart;
	//	private final static String RESTART_NO_STRATEGY = "No strategy";
	private final static String RESTART_DEFAULT = "NoRestarts";
	private final static String RESTART_STRATEGY_CLASS = "org.sat4j.minisat.core.RestartStrategy";
	private final static String RESTART_PATH="org.sat4j.minisat.restarts";


	//	private JPanel lubyPanel;
	private JLabel factorLabel;
	private final static String FACTOR = "Factor: ";
	private JTextField factorField;


	private JButton restartButton;

	private JPanel rwPanel;

	private JLabel probaRWLabel;
	private JTextField probaRWField;

	private JButton applyRWButton;

	private final static String RW_PANEL = "Random Walk";
	private final static String RW_LABEL = "Probabilty : ";
	private final static String RW_APPLY = "Apply";


	private JPanel cleanPanel;
	private final static String CLEAN_PANEL = "Learned Constraint Deletion Strategy";

	private JSlider cleanSlider;


	private JButton cleanButton;
	private final static String CLEAN = "Clean now";

	private final JLabel deleteClauseLabel = new JLabel(DELETE_CLAUSES);
	private final static String DELETE_CLAUSES = "# of conflicts before cleaning: ";

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


	private JTextArea console;
	private JScrollPane scrollPane;

	public CommandeDetailleePanel(String filename){
		super();

		this.telecomStrategy = new TelecommandeStrategy();

		this.randomWalk = null;

		this.telecomStrategy.setLogger(this);

		this.instancePath=filename;

		this.setPreferredSize(new Dimension(700,950));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


		createInstancePanel();
		createChoixSolverPanel();
		createRestartPanel();
		createRWPanel();
		createCleanPanel();

		console = new JTextArea();
		
		scrollPane = new JScrollPane(console);

		//scrollPane.setMinimumSize(new Dimension(100,100));
		scrollPane.setPreferredSize(new Dimension(400,200));
		scrollPane.getVerticalScrollBar().setValue(
	               scrollPane.getVerticalScrollBar().getMaximum());
	//	scrollPane.setAutoscrolls(true);

		initFactorParam();

		restartPropertiesPanel.setPreferredSize(new Dimension(100,50));


		this.add(instancePanel);
		this.add(choixSolverPanel);
		this.add(restartPanel);
		this.add(rwPanel);
		this.add(cleanPanel);
		this.add(scrollPane);
		
		setRestartPanelEnabled(false);
		setRWPanelEnabled(false);
		setCleanPanelEnabled(false);

	}

	public void createInstancePanel(){
		instancePanel = new JPanel();

		instancePanel.setName(INSTANCE_PANEL);
		instancePanel.setBorder(new CompoundBorder(new TitledBorder(null, instancePanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		instancePanel.setLayout(new BorderLayout(0,0));

		instanceLabel = new JLabel(INSTANCE);
		instancePathField = new JTextField(20);
		instancePathField.setText(instancePath);

		instanceLabel.setLabelFor(instancePathField);

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.add(instanceLabel);
		tmpPanel1.add(instancePathField);

		browseButton = new JButton(BROWSE);

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFileChooser();
			}
		});

		JPanel tmpPanel2 = new JPanel();
		tmpPanel2.add(browseButton);

		instancePanel.add(tmpPanel1,BorderLayout.CENTER);
		instancePanel.add(tmpPanel2,BorderLayout.SOUTH);


	}

	public void createChoixSolverPanel(){
		choixSolverPanel = new JPanel();

		choixSolverPanel.setName(CHOIX_SOLVER_PANEL);
		choixSolverPanel.setBorder(new CompoundBorder(new TitledBorder(null, choixSolverPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		choixSolverPanel.setLayout(new BorderLayout());

		choixSolver = new JLabel(CHOIX_SOLVER);
		updateListOfSolvers();
		

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.add(choixSolver);
		tmpPanel1.add(listeSolvers);

		startStopButton = new JButton(START);

		startStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(startStopButton.getText().equals(START)){
					launchSolver();
					setInstancePanelEnabled(false);
					setRestartPanelEnabled(true);
					setRWPanelEnabled(true);
					setCleanPanelEnabled(true);
					startStopButton.setText(STOP);
					getThis().paintAll(getThis().getGraphics());
				}
				else {
					setInstancePanelEnabled(true);
					setRestartPanelEnabled(false);
					setRWPanelEnabled(false);
					setCleanPanelEnabled(false);
					startStopButton.setText(START);
					getThis().paintAll(getThis().getGraphics());
				}
			}
		});

		JPanel tmpPanel2 = new JPanel();
		tmpPanel2.add(startStopButton);

		choixSolverPanel.add(tmpPanel1,BorderLayout.CENTER);
		choixSolverPanel.add(tmpPanel2,BorderLayout.SOUTH);

	}


	public void createRestartPanel(){
		restartPanel = new JPanel();

		restartPanel.setName(RESTART_PANEL);
		restartPanel.setBorder(new CompoundBorder(new TitledBorder(null, restartPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		restartPanel.setLayout(new BorderLayout());

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.setLayout(new FlowLayout());

		chooseRestartStrategyLabel = new JLabel(CHOOSE_RESTART_STRATEGY);

		listeRestarts = new JComboBox(getListOfRestartStrategies().toArray());	
		currentRestart = telecomStrategy.getRestartStrategy().getClass().getSimpleName();
		listeRestarts.setSelectedItem(RESTART_DEFAULT);

		listeRestarts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifyRestartParamPanel();
			}
		});

		tmpPanel1.add(chooseRestartStrategyLabel);
		tmpPanel1.add(listeRestarts);

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
		restartButtonPanel.add(restartButton);

		restartPanel.add(tmpPanel1,BorderLayout.NORTH);
		restartPanel.add(restartPropertiesPanel,BorderLayout.CENTER);
		restartPanel.add(restartButtonPanel,BorderLayout.SOUTH);

	}

	public void createRWPanel(){
		rwPanel = new JPanel();

		rwPanel.setName(RW_PANEL);
		rwPanel.setBorder(new CompoundBorder(new TitledBorder(null, rwPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		rwPanel.setLayout(new BorderLayout());

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

		rwPanel.add(tmpPanel1, BorderLayout.CENTER);
		rwPanel.add(tmpPanel2, BorderLayout.SOUTH);


	}

	public void createCleanPanel(){
		cleanPanel = new JPanel();

		cleanPanel.setName(CLEAN_PANEL);
		cleanPanel.setBorder(new CompoundBorder(new TitledBorder(null, cleanPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		cleanPanel.setLayout(new BorderLayout());


		cleanSlider = new JSlider(JSlider.HORIZONTAL,CLEAN_MIN,CLEAN_MAX,CLEAN_INIT);

		cleanSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				 if (!cleanSlider.getValueIsAdjusting()) {
					 hasChangedCleaningValue();
			        }
			}
		});
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


		JPanel tmpPanel2 = new JPanel();

		cleanButton = new JButton(CLEAN);

		cleanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnClean();
			}
		});

		tmpPanel2.add(cleanButton);

		cleanPanel.add(tmpPanel1,BorderLayout.CENTER);
		cleanPanel.add(tmpPanel2,BorderLayout.SOUTH);

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

	public void launchSolver(){
		selectedSolver = (String)listeSolvers.getSelectedItem();
		String[] partsSelectedSolver = selectedSolver.split("\\.");

		assert partsSelectedSolver.length==2;
		assert (partsSelectedSolver[0].equals(MINISAT_PREFIX) || partsSelectedSolver[0].equals(PB_PREFIX)) ;

		ASolverFactory factory;

		if(partsSelectedSolver[0].equals(MINISAT_PREFIX)){
			factory = org.sat4j.minisat.SolverFactory.instance();
		}
		else{
			factory = org.sat4j.pb.SolverFactory.instance();
		}
		solver = (ICDCL)factory.createSolverByName(partsSelectedSolver[1]);
		//log(solver.toString());

		solver.setVerbose(true);

		telecomStrategy.setSolver(solver);
		telecomStrategy.setRestartStrategy(solver.getRestartStrategy());
		currentRestart = telecomStrategy.getRestartStrategy().getClass().getSimpleName();

		solver.setRestartStrategy(telecomStrategy);
		
		int nbConflicts = cleanValues[cleanSlider.getValue()];
		telecomStrategy.setNbClausesAtWhichWeShouldClean(nbConflicts);
		
		solver.setLearnedConstraintsDeletionStrategy(telecomStrategy);
		
		updateRestartStrategyPanel();

		//pbSolver.setNeedToReduceDB(true);



		solver.setSearchListener(new MultiTracing(
				new ConflictLevelTracing(instancePath
						+ "-conflict-level"), new DecisionTracing(
								instancePath + "-decision-indexes"),
								new LearnedClausesSizeTracing(instancePath
										+ "-learned-clauses-size"),
										new ConflictDepthTracing(instancePath
												+ "-conflict-depth")));

		double proba=0;
		if(probaRWField.getText()!=null){
			proba = Double.parseDouble(probaRWField.getText());
		}
		randomWalk = new RandomWalkDecorator((VarOrderHeap)((Solver)solver).getOrder(), proba);

		solver.setOrder(randomWalk);
		
		log("# Started solver " + solver.getClass().getSimpleName());
		log("# on instance " + instancePath);
		log("# Optimisation = false");
		log("# Restart strategy = " + telecomStrategy.getRestartStrategy().getClass().getSimpleName());
		log("# Random walk probability = " +proba);
		log("# Number of conflicts before cleaning = " + nbConflicts);

		Thread t = new Thread() {
			public void run() {
				try{
					Reader reader = createReader(solver, instancePath);

					IProblem problem = reader.parseInstance(instancePath);
					if(problem.isSatisfiable()){
						log("Satisfiable !");
						reader.decode(problem.model(), new PrintWriter(System.out));
					}
					else{
						log("Unsatisfiable !");
					}


				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ParseFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ContradictionException e) {
					log("Unsatisfiable (trivial)!");
				} catch (TimeoutException e) {
					log("Timeout, sorry!");      
				}
			}
		};
		t.start();


	}

	public void hasClickedOnRestart(){
		telecomStrategy.setHasClickedOnRestart(true);
		String choix = (String)listeRestarts.getSelectedItem();

		boolean isNotSameRestart = !choix.equals(currentRestart);
		boolean shouldInit = isNotSameRestart;

		RestartStrategy restart = new NoRestarts();
		SearchParams params = new SearchParams();

		if(choix.equals("LubyRestarts")){
			boolean factorChanged = false;
			int factor = LubyRestarts.DEFAULT_LUBY_FACTOR;
			if(factorField.getText()!=null){
				factor = Integer.parseInt(factorField.getText());
			}
			// if the current restart is a LubyRestart
			if(isNotSameRestart){
				restart = new LubyRestarts(factor);
				telecomStrategy.setRestartStrategy(restart);
			}
			else{
				factorChanged = !(factor==((LubyRestarts)telecomStrategy.getRestartStrategy()).getFactor());
			}
			// if the factor has changed
			if(factorChanged){
				restart = telecomStrategy.getRestartStrategy();
				((LubyRestarts)restart).setFactor(factor);
			}
			shouldInit = isNotSameRestart || factorChanged;

			if(shouldInit){
				restart.init(params);
				log("Init restart");
			}

		}

		else try{
			restart = (RestartStrategy)Class.forName(RESTART_PATH+"."+choix).newInstance();
			assert restart!=null;
			telecomStrategy.setRestartStrategy(restart);

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

		//log("Has clicked on " + RESTART + " with "+ choix);
	}

	public void hasClickedOnApplyRW(){
		double proba=0;
		if(probaRWField!=null)
			proba = Double.parseDouble(probaRWField.getText());

		randomWalk.setProbability(proba);
		log("Set probability to " + proba);
	}

	public void hasChangedCleaningValue(){
		int nbConflicts = cleanValues[cleanSlider.getValue()];
		telecomStrategy.setNbClausesAtWhichWeShouldClean(nbConflicts);
		log("Changed number of conflicts before cleaning to " + nbConflicts);
	}

	public void hasClickedOnClean(){
		log("Told the solver to clean");
		telecomStrategy.setHasClickedOnClean(true);
		//log("Has clicked on " + CLEAN);
	}



	public List<String> getListOfRestartStrategies(){
		List<String> resultRTSI = RTSI.find(RESTART_STRATEGY_CLASS);
		List<String> finalResult = new ArrayList<String>();

		//		finalResult.add(RESTART_NO_STRATEGY);

		for(String s:resultRTSI){
			if(!s.contains("Telecommande")){
				finalResult.add(s);
			}
		}

		return finalResult;
	}


	public List<String> getListOfSolvers(){
		ASolverFactory factory;

		List<String> result = new ArrayList<String>();

		factory = org.sat4j.minisat.SolverFactory.instance();
		for(String s:factory.solverNames()){
			result.add(MINISAT_PREFIX+"."+s);
		}

		factory = org.sat4j.pb.SolverFactory.instance();
		for(String s:factory.solverNames()){
			result.add(PB_PREFIX+"."+s);
		}

		Collections.sort(result);
		
		return result;
	}

	public List<String> getListOfPBSolvers(){
		ASolverFactory factory;

		List<String> result = new ArrayList<String>();

		factory = org.sat4j.pb.SolverFactory.instance();
		for(String s:factory.solverNames()){
			result.add(PB_PREFIX+"."+s);
		}

		Collections.sort(result);
		
		return result;
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
		restartPropertiesPanel.repaint();
		restartPanel.repaint();
		restartPanel.paintAll(restartPanel.getGraphics());
		this.repaint();
	}

	public void log(String message){
		console.append(message +"\n");
//		scrollPane.getVerticalScrollBar().setValue(
//	               scrollPane.getVerticalScrollBar().getMaximum()+1);
		console.setCaretPosition(console.getDocument().getLength() );
		console.repaint();
		this.repaint();
	}


	public void openFileChooser(){
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showDialog(this, "Choose instance");
		if(returnVal==JFileChooser.APPROVE_OPTION){
			File file = fc.getSelectedFile();
			instancePath = file.getAbsolutePath();
			instancePathField.setText(instancePath);
			updateListOfSolvers();
		}
	}

	protected Reader createReader(ICDCL theSolver, String problemname) {
		if (theSolver instanceof IPBSolver) {
			return new PBInstanceReader((IPBSolver) theSolver);
		}
		return new InstanceReader(theSolver);
	}
	
	public void updateListOfSolvers(){
		if(instancePath.endsWith(".opb")){
			if(instancePath.endsWith(".opb")){
				listeSolvers = new JComboBox(getListOfPBSolvers().toArray());
				listeSolvers.setSelectedItem("pb.Default");
				selectedSolver = "pb.Default";
			}
			else{
				listeSolvers = new JComboBox(getListOfSolvers().toArray());
				listeSolvers.setSelectedItem("minisat.Default");
				selectedSolver = "minisat.Default";
			}
		}
	}
	
	public void updateRestartStrategyPanel(){
		listeRestarts.setSelectedItem(currentRestart);
		
	}
	
	public void setInstancePanelEnabled(boolean enabled){
		for(Component c:instancePanel.getComponents()){
			c.setEnabled(enabled);
		}
		instancePanel.repaint();
	}
	
	public void setChoixSolverPanelEnabled(boolean enabled){
		for(Component c:choixSolverPanel.getComponents()){
			c.setEnabled(enabled);
		}
		choixSolverPanel.repaint();
	}
	
	public void setRestartPanelEnabled(boolean enabled){
		for(Component c:restartPanel.getComponents()){
			c.setEnabled(enabled);
		}
		restartPanel.repaint();
	}
	
	public void setRWPanelEnabled(boolean enabled){
		for(Component c:rwPanel.getComponents()){
			c.setEnabled(enabled);
		}
		rwPanel.repaint();
	}
	
	public void setCleanPanelEnabled(boolean enabled){
		for(Component c:cleanPanel.getComponents()){
			c.setEnabled(enabled);
		}
		cleanPanel.setEnabled(enabled);
		cleanPanel.repaint();
	}
	
	public CommandeDetailleePanel getThis(){
		return this;
	}





}
