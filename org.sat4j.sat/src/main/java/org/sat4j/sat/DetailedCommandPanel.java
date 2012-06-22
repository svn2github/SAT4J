/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.ICDCLLogger;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.core.LearnedConstraintsEvaluationType;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.SimplificationType;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.core.IPBCDCLSolver;
import org.sat4j.pb.orders.RandomWalkDecoratorObjective;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.sat.visu.ChartBasedVisualizationTool;
import org.sat4j.sat.visu.GnuplotBasedSolverVisualisation;
import org.sat4j.sat.visu.JChartBasedSolverVisualisation;
import org.sat4j.sat.visu.SolverVisualisation;
import org.sat4j.sat.visu.TraceComposite;
import org.sat4j.sat.visu.VisuPreferences;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ConflictDepthTracing;
import org.sat4j.tools.ConflictLevelTracing;
import org.sat4j.tools.DecisionTracing;
import org.sat4j.tools.FileBasedVisualizationTool;
import org.sat4j.tools.HeuristicsTracing;
import org.sat4j.tools.LearnedClausesSizeTracing;
import org.sat4j.tools.LearnedTracing;
import org.sat4j.tools.MultiTracing;
import org.sat4j.tools.SpeedTracing;


/**
 * 
 * This panel contains buttons that control restart and clean on solver.
 * It also displays history of commands.
 * 
 * @author sroussel
 *
 */
public class DetailedCommandPanel extends JPanel implements SolverController,SearchListener,ICDCLLogger{


	private static final long serialVersionUID = 1L;

	public static final EmptyBorder border5 = new EmptyBorder(5,5,5,5);

	private String ramdisk;

	private RemoteControlStrategy telecomStrategy;
	private RandomWalkDecorator randomWalk;
	private ICDCL solver;
	private Reader reader;
	private IProblem problem;
	private boolean optimizationMode=false;

	private String[] commandLines;

	private boolean firstStart;

//	private JChartBasedSolverVisualisation visu;

	//	private boolean useCustomizedSolver;
	private StartSolverEnum startConfig;

	private Thread solveurThread;

	private StringWriter stringWriter;

	private MyTabbedPane tabbedPane;

	private JPanel aboutSolverPanel;
	private JTextArea textArea;

	private JPanel instancePanel;
	private final static String INSTANCE_PANEL = "Instance";
	private JLabel instanceLabel;
	private final static String INSTANCE  = "Path to instance: ";
	private JTextField instancePathField;
	private String instancePath;
	private JButton browseButton;
	private final static String BROWSE = "Browse";

	private String whereToWriteFiles;


	private final static String MINISAT_PREFIX = "minisat";
	private final static String PB_PREFIX = "pb";
	private JPanel choixSolverPanel;
	private final static String CHOIX_SOLVER_PANEL = "Solver";
	private JLabel choixSolver;
	private final static String CHOIX_SOLVER  = "Choose solver: ";
	private String selectedSolver;
	private JComboBox listeSolvers;

	private final static String OPTMIZATION_MODE = "Use optimization mode";
	private JCheckBox optimisationModeCB;

	//	private JCheckBox useCustomizedSolverCB;
	//	private final static String USE_CUSTOMIZED_SOLVER = "Use customized solver";

	private JRadioButton solverLineParamLineRadio;
	private JRadioButton solverLineParamRemoteRadio;
	private JRadioButton solverListParamListRadio;
	private JRadioButton solverListParamRemoteRadio;
	private ButtonGroup solverConfigGroup;

	private final static String SOLVER_LINE_PARAM_LINE_CONFIG = "Start customized solver as given in command line";
	private final static String SOLVER_LINE_PARAM_REMOTE_CONFIG = "Start customized solver as given in command line with configuration given in the remote";
	private final static String SOLVER_LIST_PARAM_LIST_CONFIG = "Start solver as chosen in list with its default configuration";
	private final static String SOLVER_LIST_PARAM_REMOTE_CONFIG = "Start solver as chosen in list with configuration given in the remote";

	private JLabel chooseStartConfigLabel;
	private final static String CHOOSE_START_CONFIG = "Choose start configuration : ";

	private JButton startStopButton;
	private static final String START = "Start";
	private static final String STOP = "Stop";

	private JButton pauseButton;
	private static final String PAUSE = "Pause";
	private static final String RESUME = "Resume";
	//	private boolean isInterrupted;

	private final static String RESTART_PANEL = "Restart strategy";
	private RestartCommandComponent restartPanel;

	private final static String RW_PANEL = "Random Walk";
	private RandomWalkCommandComponent rwPanel;

	private final static String CLEAN_PANEL = "Learned Constraint Deletion Strategy";
	private CleanCommandComponent cleanPanel;
	
	private PhaseCommandComponent phasePanel;
	private final static String PHASE_PANEL = "Phase Strategy";

	private SimplifierCommandComponent simplifierPanel;
	private final static String SIMPLIFIER_PANEL = "Simplification strategy";

	private HotSolverCommandComponent hotSolverPanel;
	private final static String HOT_SOLVER_PANEL = "Hot solver";

	private JTextArea console;
	private JScrollPane scrollPane;

	private boolean isPlotActivated;

	private SolverVisualisation solverVisu;
	private VisuPreferences visuPreferences;

	private boolean gnuplotBased = false;
	private boolean chartBased = false;

	private RemoteControlFrame frame;

	public DetailedCommandPanel(String filename, RemoteControlFrame frame){
		this(filename,"",frame);
	}

	public DetailedCommandPanel(String filename, String ramdisk, RemoteControlFrame frame){
		this(filename,ramdisk,null,frame);
	}

	public DetailedCommandPanel(String filename, String ramdisk, String[] args, RemoteControlFrame frame){
		super();

		this.frame = frame;

		this.visuPreferences = new VisuPreferences();

		this.telecomStrategy = new RemoteControlStrategy(this);
		this.instancePath=filename;
		this.ramdisk = ramdisk;

		console = new JTextArea();
		
		this.commandLines = args;
		if(args.length>0)
			this.solver=Solvers.configureSolver(args, this);

		this.isPlotActivated=false;

		if(this.solver!=null)
			startConfig=StartSolverEnum.SOLVER_LINE_PARAM_LINE;
		else 
			startConfig=StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT;

		this.firstStart=true;


		this.setPreferredSize(new Dimension(750,800));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


		createInstancePanel();
		createChoixSolverPanel();
		
		restartPanel = new RestartCommandComponent(RESTART_PANEL, this, telecomStrategy.getRestartStrategy().getClass().getSimpleName());
		rwPanel = new RandomWalkCommandComponent(RW_PANEL,this);
		cleanPanel = new CleanCommandComponent(CLEAN_PANEL,this);
		phasePanel = new PhaseCommandComponent(PHASE_PANEL,this,telecomStrategy.getPhaseSelectionStrategy().getClass().getSimpleName());
		simplifierPanel = new SimplifierCommandComponent(SIMPLIFIER_PANEL,this);
		hotSolverPanel = new HotSolverCommandComponent(HOT_SOLVER_PANEL, this);

		scrollPane = new JScrollPane(console);

		//scrollPane.setMinimumSize(new Dimension(100,100));
		scrollPane.setPreferredSize(new Dimension(400,200));
		scrollPane.getVerticalScrollBar().setValue(
				scrollPane.getVerticalScrollBar().getMaximum());
		//	scrollPane.setAutoscrolls(true);

		tabbedPane = new MyTabbedPane();

		JPanel solverBigPanel = new JPanel();
		solverBigPanel.setLayout(new BoxLayout(solverBigPanel, BoxLayout.Y_AXIS));
		solverBigPanel.add(instancePanel);
		solverBigPanel.add(choixSolverPanel);

		tabbedPane.addTab("Solver", null, solverBigPanel, "instance & solver options");


		JPanel restartBigPanel = new JPanel();
		restartBigPanel.setLayout(new BoxLayout(restartBigPanel, BoxLayout.Y_AXIS));
		restartBigPanel.add(restartPanel);
		//restartBigPanel.add(hotSolverPanel);

		tabbedPane.addTab("Restart",null,restartBigPanel, "restart strategy & options");

		JPanel rwPhaseBigPanel = new JPanel();
		rwPhaseBigPanel.setLayout(new BoxLayout(rwPhaseBigPanel, BoxLayout.Y_AXIS));
		rwPhaseBigPanel.add(rwPanel);
		rwPhaseBigPanel.add(phasePanel);
		rwPhaseBigPanel.add(hotSolverPanel);

		tabbedPane.addTab("Heuristics",null,rwPhaseBigPanel, "random walk and phase strategy");

		JPanel clausesBigPanel = new JPanel();
		clausesBigPanel.setLayout(new BoxLayout(clausesBigPanel, BoxLayout.Y_AXIS));
		clausesBigPanel.add(cleanPanel);
		clausesBigPanel.add(simplifierPanel);

		tabbedPane.addTab("Learned Constraints",null,clausesBigPanel, "deletion and simplification strategy");

		aboutSolverPanel = new JPanel();
		textArea = new JTextArea("No solver is running at the moment");
		textArea.setColumns(50);
		aboutSolverPanel.add(textArea);

		tabbedPane.addTab("About Solver",null,aboutSolverPanel, "information about solver");

		this.add(tabbedPane);
		this.add(scrollPane);



		restartPanel.setRestartPanelEnabled(false);
		rwPanel.setRWPanelEnabled(false);
		cleanPanel.setCleanPanelEnabled(false);
		phasePanel.setPhasePanelEnabled(false);
		simplifierPanel.setSimplifierPanelEnabled(false);
		hotSolverPanel.setKeepSolverHotPanelEnabled(false);
		
		this.solverVisu = new JChartBasedSolverVisualisation(visuPreferences);

		updateWriter();
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
	}

	public void createChoixSolverPanel(){
		choixSolverPanel = new JPanel();

		choixSolverPanel.setName(CHOIX_SOLVER_PANEL);
		choixSolverPanel.setBorder(new CompoundBorder(new TitledBorder(null, choixSolverPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		choixSolverPanel.setLayout(new BorderLayout());

		choixSolver = new JLabel(CHOIX_SOLVER);
		updateListOfSolvers();


		optimisationModeCB = new JCheckBox(OPTMIZATION_MODE);
		optimisationModeCB.setSelected(optimizationMode);

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.add(choixSolver);
		tmpPanel1.add(listeSolvers);
		tmpPanel1.add(optimisationModeCB);

		optimisationModeCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				optimizationMode = optimisationModeCB.isSelected();
				log("use optimization mode: " + optimizationMode);
			}
		});

		startStopButton = new JButton(START);

		startStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(startStopButton.getText().equals(START)){
					//					launchSolver();
					launchSolverWithConfigs();
					pauseButton.setEnabled(true);
					setInstancePanelEnabled(false);
					restartPanel.setRestartPanelEnabled(true);
					rwPanel.setRWPanelEnabled(true);
					cleanPanel.setCleanPanelEnabled(true);
					cleanPanel.setCleanPanelOriginalStrategyEnabled(true);
					phasePanel.setPhasePanelEnabled(true);
					setChoixSolverPanelEnabled(false);
					simplifierPanel.setSimplifierPanelEnabled(true);
					hotSolverPanel.setKeepSolverHotPanelEnabled(true);
					startStopButton.setText(STOP);
					getThis().paintAll(getThis().getGraphics());
					frame.setActivateTracingEditableUnderCondition(false);
				}
				else {

					//assert solveurThread!=null;
					((ISolver)problem).expireTimeout();
					pauseButton.setEnabled(false);
					log("Asked the solver to stop");
					setInstancePanelEnabled(true);
					setChoixSolverPanelEnabled(true);
					//					setRestartPanelEnabled(false);
					//					setRWPanelEnabled(false);
					//					setCleanPanelEnabled(false);
					//					setPhasePanelEnabled(false);
					//					setSimplifierPanelEnabled(false);
					//					setKeepSolverHotPanelEnabled(false);
					startStopButton.setText(START);
					getThis().paintAll(getThis().getGraphics());
					frame.setActivateTracingEditable(true);

				}
			}
		});

		pauseButton = new JButton(PAUSE);
		pauseButton.setEnabled(false);

		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(pauseButton.getText().equals(PAUSE)){
					pauseButton.setText(RESUME);
					telecomStrategy.setInterrupted(true);
				}
				else{
					pauseButton.setText(PAUSE);
					telecomStrategy.setInterrupted(false);
				}

			}
		});


		JPanel tmpPanel2 = new JPanel();
		tmpPanel2.setLayout(new FlowLayout());
		tmpPanel2.add(startStopButton);
		tmpPanel2.add(pauseButton);

		solverLineParamLineRadio = new JRadioButton(SOLVER_LINE_PARAM_LINE_CONFIG);
		solverLineParamRemoteRadio = new JRadioButton(SOLVER_LINE_PARAM_REMOTE_CONFIG);
		solverListParamRemoteRadio = new JRadioButton(SOLVER_LIST_PARAM_REMOTE_CONFIG);
		solverListParamListRadio = new JRadioButton(SOLVER_LIST_PARAM_LIST_CONFIG);

		solverConfigGroup = new ButtonGroup();
		solverConfigGroup.add(solverLineParamLineRadio);
		solverConfigGroup.add(solverLineParamRemoteRadio);
		solverConfigGroup.add(solverListParamListRadio);
		solverConfigGroup.add(solverListParamRemoteRadio);

		chooseStartConfigLabel = new JLabel(CHOOSE_START_CONFIG);

		JPanel tmpPanel3 = new JPanel();
		tmpPanel3.setLayout(new BoxLayout(tmpPanel3, BoxLayout.Y_AXIS));

		tmpPanel3.add(chooseStartConfigLabel);
		tmpPanel3.add(solverLineParamLineRadio);
		tmpPanel3.add(solverLineParamRemoteRadio);
		tmpPanel3.add(solverListParamListRadio);
		tmpPanel3.add(solverListParamRemoteRadio);


		solverLineParamLineRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(solverLineParamLineRadio.isSelected()){
					startConfig = StartSolverEnum.SOLVER_LINE_PARAM_LINE;
				}
			}
		});

		solverLineParamRemoteRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(solverLineParamRemoteRadio.isSelected()){
					startConfig = StartSolverEnum.SOLVER_LINE_PARAM_REMOTE;
				}
			}
		});

		solverListParamListRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(solverListParamListRadio.isSelected()){
					startConfig = StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT;
				}
			}
		});

		solverListParamRemoteRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(solverListParamRemoteRadio.isSelected()){
					startConfig = StartSolverEnum.SOLVER_LIST_PARAM_REMOTE;
				}
			}
		});

		setChoixSolverPanelEnabled(true);

		if(solver==null){
			solverLineParamLineRadio.setEnabled(false);
			solverLineParamRemoteRadio.setEnabled(false);
		}

		if(firstStart){
			solverLineParamRemoteRadio.setEnabled(false);
			solverListParamRemoteRadio.setEnabled(false);
		}

		choixSolverPanel.add(tmpPanel1,BorderLayout.NORTH);
		choixSolverPanel.add(tmpPanel3,BorderLayout.CENTER);
		choixSolverPanel.add(tmpPanel2,BorderLayout.SOUTH);
	}

	public String getStartStopText(){
		return startStopButton.getText();
	}

	public void setOptimisationMode(boolean optimizationMode){
		this.optimizationMode = optimizationMode;
		optimisationModeCB.setSelected(optimizationMode);
	}

	public void launchSolverWithConfigs(){
		if(startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT)){
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

			telecomStrategy.setSolver(solver);
			telecomStrategy.setRestartStrategy(solver.getRestartStrategy());
			solver.setRestartStrategy(telecomStrategy);

			restartPanel.setCurrentRestart(telecomStrategy.getRestartStrategy().getClass().getSimpleName());

			IOrder order = solver.getOrder();

			double proba = 0;

			if(optimizationMode){
				if(order instanceof RandomWalkDecoratorObjective){
					randomWalk = (RandomWalkDecorator)order;
					proba = randomWalk.getProbability();
				}
				else if(order instanceof VarOrderHeapObjective){
					randomWalk = new RandomWalkDecoratorObjective((VarOrderHeapObjective) order, 0);
				}
			}
			else if(solver.getOrder() instanceof RandomWalkDecorator){
				randomWalk = (RandomWalkDecorator)order;
				proba = randomWalk.getProbability();
			}
			else{
				randomWalk = new RandomWalkDecorator((VarOrderHeap)order, 0);
			}

			randomWalk.setProbability(proba);
			rwPanel.setProba(proba);

			solver.setOrder(randomWalk);

			telecomStrategy.setPhaseSelectionStrategy(solver.getOrder().getPhaseSelectionStrategy());
			phasePanel.setPhaseListSelectedItem(telecomStrategy.getPhaseSelectionStrategy().getClass().getSimpleName());
			solver.getOrder().setPhaseSelectionStrategy(telecomStrategy);
			simplifierPanel.setSelectedSimplification(solver.getSimplifier().toString());
		}

		else if(startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_REMOTE)){
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

			telecomStrategy.setSolver(solver);

			solver.setRestartStrategy(telecomStrategy);
			solver.setOrder(randomWalk);
			solver.getOrder().setPhaseSelectionStrategy(telecomStrategy);

			restartPanel.hasClickedOnRestart();
			rwPanel.hasClickedOnApplyRW();
			phasePanel.hasClickedOnApplyPhase();
			simplifierPanel.hasClickedOnApplySimplification();
		}

		else if(startConfig.equals(StartSolverEnum.SOLVER_LINE_PARAM_LINE)){

			this.solver=Solvers.configureSolver(commandLines, this);

			telecomStrategy.setSolver(solver);
			telecomStrategy.setRestartStrategy(solver.getRestartStrategy());
			solver.setRestartStrategy(telecomStrategy);

			restartPanel.setCurrentRestart(telecomStrategy.getRestartStrategy().getClass().getSimpleName());

			IOrder order = solver.getOrder();

			double proba = 0;

			if(optimizationMode){
				if(order instanceof RandomWalkDecoratorObjective){
					randomWalk = (RandomWalkDecorator)order;
					proba = randomWalk.getProbability();
				}
				else if(order instanceof VarOrderHeapObjective){
					randomWalk = new RandomWalkDecoratorObjective((VarOrderHeapObjective) order, 0);
				}
			}
			else if(solver.getOrder() instanceof RandomWalkDecorator){
				randomWalk = (RandomWalkDecorator)order;
				proba = randomWalk.getProbability();
			}
			else{
				randomWalk = new RandomWalkDecorator((VarOrderHeap)order, 0);
			}

			randomWalk.setProbability(proba);
			rwPanel.setProba(proba);
			solver.setOrder(randomWalk);
			telecomStrategy.setPhaseSelectionStrategy(solver.getOrder().getPhaseSelectionStrategy());
			solver.getOrder().setPhaseSelectionStrategy(telecomStrategy);
			phasePanel.setPhaseListSelectedItem(telecomStrategy.getPhaseSelectionStrategy().getClass().getSimpleName());
			simplifierPanel.setSelectedSimplification(solver.getSimplifier().toString());
			
			phasePanel.repaint();
		}

		else if(startConfig.equals(StartSolverEnum.SOLVER_LINE_PARAM_REMOTE)){

			this.solver=Solvers.configureSolver(commandLines, this);

			solver.setRestartStrategy(telecomStrategy);
			solver.setOrder(randomWalk);
			solver.getOrder().setPhaseSelectionStrategy(telecomStrategy);

			restartPanel.hasClickedOnRestart();
			rwPanel.hasClickedOnApplyRW();
			phasePanel.hasClickedOnApplyPhase();
			simplifierPanel.hasClickedOnApplySimplification();
		}

		whereToWriteFiles = instancePath;

		if(ramdisk.length()>0){
			String[] instancePathSplit= instancePath.split("/");
			whereToWriteFiles = ramdisk+"/"+ instancePathSplit[instancePathSplit.length-1];
		}

		solver.setVerbose(true);
		initSearchListeners();
		solver.setLogger(this);
		reader = createReader(solver, instancePath);

		try{
			problem = reader.parseInstance(instancePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			log("Unsatisfiable (trivial)!");
		}

		boolean optimisation=false;
		if(reader instanceof PBInstanceReader){
			optimisation = ((PBInstanceReader)reader).hasObjectiveFunction();
			if(optimisation){
				problem = new OptToPBSATAdapter(new PseudoOptDecorator((IPBCDCLSolver)solver));
			}
		}

		log("# Started solver " + solver.getClass().getSimpleName());
		log("# on instance " + instancePath);
		log("# Optimisation = " + optimisation);
		log("# Restart strategy = " + telecomStrategy.getRestartStrategy().getClass().getSimpleName());
		log("# Random walk probability = " +randomWalk.getProbability());
		//log("# Number of conflicts before cleaning = " + nbConflicts);

		solveurThread = new Thread() {
			public void run() {
				//Thread thisThread = Thread.currentThread();
				//				if(shouldStop){
				//					System.out.println("coucou");
				//				}
				//				while(!shouldStop){
				try{
					stringWriter = new StringWriter();
					if(problem.isSatisfiable()){
						log("Satisfiable !");
						if(problem instanceof OptToPBSATAdapter){
							log(((OptToPBSATAdapter)problem).getCurrentObjectiveValue()+"");
							reader.decode(((OptToPBSATAdapter)problem).model(new PrintWriter(stringWriter)), new PrintWriter(stringWriter));
						}
						else
							reader.decode(problem.model(),new PrintWriter(stringWriter));
						log(stringWriter.toString());
					}
					else{
						log("Unsatisfiable !");
					}
				} catch (TimeoutException e) {
					log("Timeout, sorry!");      
				}
				//log("Solver has stopped");
				//				}
			}
		};
		solveurThread.start();

		if(isPlotActivated){
			solverVisu.setnVar(solver.nVars());
			startVisu();
		}
	}

	public void initSearchListeners(){
		List<SearchListener> listeners = new ArrayList<SearchListener>();

		if(isPlotActivated)
		{
			if(gnuplotBased){
				solverVisu = new GnuplotBasedSolverVisualisation(visuPreferences, solver.nVars(), instancePath, this);
				if(visuPreferences.isDisplayClausesEvaluation()){
					listeners.add(new LearnedTracing(new FileBasedVisualizationTool(whereToWriteFiles + "-learned")));
				}
				if(visuPreferences.isDisplayClausesSize()){
					listeners.add(new LearnedClausesSizeTracing(
							new FileBasedVisualizationTool(whereToWriteFiles + "-learned-clauses-size"), 
							new FileBasedVisualizationTool(whereToWriteFiles + "-learned-clauses-size-restart"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-learned-clauses-size-clean")));
				}
				if(visuPreferences.isDisplayConflictsDecision()){
					listeners.add(new ConflictLevelTracing(
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-level"), 
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-level-restart"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-level-clean")));
				}
				if(visuPreferences.isDisplayConflictsTrail()){
					listeners.add(new ConflictDepthTracing(new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-depth"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-depth-restart"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-depth-clean")));
				}

				if(visuPreferences.isDisplayDecisionIndexes()){
					listeners.add(new DecisionTracing(
							new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-pos"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-neg"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-restart"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-clean")));
				}

				if(visuPreferences.isDisplaySpeed()){
					listeners.add(new SpeedTracing(
							new FileBasedVisualizationTool(whereToWriteFiles + "-speed"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-speed-clean"), 
							new FileBasedVisualizationTool(whereToWriteFiles + "-speed-restart")));
				}
				if(visuPreferences.isDisplayVariablesEvaluation()){
					listeners.add(new HeuristicsTracing(new FileBasedVisualizationTool(whereToWriteFiles + "-heuristics")));
				}
			}

			else if(chartBased){

				if(solverVisu != null){
					solverVisu.end();
				}
				
				solverVisu = new JChartBasedSolverVisualisation(visuPreferences);

				((JChartBasedSolverVisualisation)solverVisu).setnVar(solver.nVars());
				if(visuPreferences.isDisplayClausesEvaluation()){
					listeners.add(new LearnedTracing(new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getClausesEvaluationTrace())));
				}
				if(visuPreferences.isDisplayClausesSize()){
					listeners.add(new LearnedClausesSizeTracing(
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getLearnedClausesSizeTrace()),
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getLearnedClausesSizeRestartTrace()),
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getLearnedClausesSizeCleanTrace())
							));
				}
				if(visuPreferences.isDisplayConflictsDecision()){
					listeners.add(new ConflictLevelTracing(
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getConflictLevelTrace()), 
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getConflictLevelRestartTrace()),
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getConflictLevelCleanTrace())));
				}
				if(visuPreferences.isDisplayConflictsTrail()){
					listeners.add(new ConflictDepthTracing(
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getConflictDepthTrace()),
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getConflictDepthRestartTrace()),
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getConflictDepthCleanTrace())));
				}
				if(visuPreferences.isDisplayDecisionIndexes()){
					listeners.add(new DecisionTracing(
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getPositiveDecisionTrace()),
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getNegativeDecisionTrace()),
							new ChartBasedVisualizationTool(new TraceComposite(
									((JChartBasedSolverVisualisation)solverVisu).getRestartPosDecisionTrace(),
									((JChartBasedSolverVisualisation)solverVisu).getRestartNegDecisionTrace())),
							new ChartBasedVisualizationTool(new TraceComposite(
									((JChartBasedSolverVisualisation)solverVisu).getCleanPosDecisionTrace(),
									((JChartBasedSolverVisualisation)solverVisu).getCleanNegDecisionTrace()))));
				}
				if(visuPreferences.isDisplaySpeed()){
					listeners.add(new SpeedTracing(
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getSpeedTrace()), 
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getSpeedCleanTrace()), 
							new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getSpeedRestartTrace())));
				}
				if(visuPreferences.isDisplayVariablesEvaluation()){
					listeners.add(new HeuristicsTracing(new ChartBasedVisualizationTool(((JChartBasedSolverVisualisation)solverVisu).getHeuristicsTrace())));
				}
			}

		}
		listeners.add(this);

		solver.setSearchListener(new MultiTracing(listeners));

	}

	
	public int getNVar(){
		if(solver!=null)
			return solver.nVars();
		return 0;
	}
	
	public void setPhaseSelectionStrategy(IPhaseSelectionStrategy phase){
		telecomStrategy.setPhaseSelectionStrategy(phase);
		log("Told the solver to apply a new phase strategy :" + phase.getClass().getSimpleName());
	}
	
	public void shouldRestartNow(){
		telecomStrategy.setHasClickedOnRestart(true);
	}
	
	public void setRestartStrategy(RestartStrategy strategy){
		telecomStrategy.setRestartStrategy(strategy);
		log("Set Restart to "+ strategy);
	}

	public RestartStrategy getRestartStrategy(){
		return telecomStrategy.getRestartStrategy();
	}
	
	public SearchParams getSearchParams(){
		return telecomStrategy.getSearchParams();
	}
	
	public void init(SearchParams params){
		telecomStrategy.init(params);
		log("Init restart with params");
	}
	
	public void setNbClausesAtWhichWeShouldClean(int nbConflicts){
		telecomStrategy.setNbClausesAtWhichWeShouldClean(nbConflicts);
		log("Changed number of conflicts before cleaning to " + nbConflicts);
	}
	
	public void setUseTelecomStrategyAsLearnedConstraintsDeletionStrategy(){
		telecomStrategy.setUseTelecomStrategyAsLearnedConstraintsDeletionStrategy(true);
		log("Solver now cleans clauses every " + cleanPanel.getCleanSliderValue() + " conflicts and bases evaluation of clauses on activity");
	}
	
	public void setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType type){
		solver.setLearnedConstraintsDeletionStrategy(telecomStrategy, type);
		log("Changed clauses evaluation type to " + type);
	}
	
	public LearnedConstraintsEvaluationType getLearnedConstraintsEvaluationType(){
		//TODO get the real evaluation !!
		return LearnedConstraintsEvaluationType.ACTIVITY;
	}
	
	public void shouldCleanNow(){
		log("Told the solver to clean");
		telecomStrategy.setHasClickedOnClean(true);
	}
	
	public void setKeepSolverHot(boolean keepHot){
		solver.setKeepSolverHot(keepHot);
		if(keepHot){
			log("Keep hot solver is now activated");
		}
		else{
			log("Keep hot solver is now desactivated");
		}
	}

	public boolean isGnuplotBased() {
		return gnuplotBased;
	}

	public void setGnuplotBased(boolean gnuplotBased) {
		this.gnuplotBased = gnuplotBased;
	}

	public boolean isChartBased() {
		return chartBased;
	}

	public void setChartBased(boolean chartBased) {
		this.chartBased = chartBased;
	}

	public boolean isPlotActivated() {
		return isPlotActivated;
	}

	public void setPlotActivated(boolean isPlotActivated) {
		this.isPlotActivated = isPlotActivated;
	}

	public void setRandomWalkProba(double proba){
		randomWalk.setProbability(proba);
		log("Set probability to " + proba);
	}
	
	public void setSimplifier(SimplificationType type){
		solver.setSimplifier(type);
		log("Told the solver to use " + type);
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

	public void log(String message){
		logsameline(message+"\n");
	}

	public void logsameline(String message){
		if(console!=null){
			console.append(message);
			console.setCaretPosition(console.getDocument().getLength() );
			console.repaint();
		}
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

	public void setInstancePanelEnabled(boolean enabled){
		instanceLabel.setEnabled(enabled);
		instancePathField.setEnabled(enabled);
		browseButton.setEnabled(enabled);
		instancePanel.repaint();
	}

	public void setChoixSolverPanelEnabled(boolean enabled){
		listeSolvers.setEnabled(enabled);
		choixSolver.setEnabled(enabled);
		solverLineParamLineRadio.setEnabled(enabled);
		solverLineParamRemoteRadio.setEnabled(enabled);
		solverListParamListRadio.setEnabled(enabled);
		solverListParamRemoteRadio.setEnabled(enabled);
		optimisationModeCB.setEnabled(enabled);
		// TODO regarder si le customized solver etait en mode optimisation ou pas
		choixSolverPanel.repaint();
	}

	public void setSolverVisualisation(SolverVisualisation visu){
		this.solverVisu = visu;
	}

	public void activateGnuplotTracing(boolean b){
		isPlotActivated=b;
		if(solver!=null)
			initSearchListeners();
	}
	
	public void startVisu(){
		solverVisu.start();
	}

	public void stopVisu(){
		solverVisu.end();
	}

	public VisuPreferences getGnuplotPreferences() {
		return visuPreferences;
	}

	public void setGnuplotPreferences(VisuPreferences gnuplotPreferences) {
		this.visuPreferences = gnuplotPreferences;
	}

	public DetailedCommandPanel getThis(){
		return this;
	}

	public ISolver getSolver(){
		return (ISolver)problem;
	}

	private long begin, end;
	private int propagationsCounter;
	
	private int conflictCounter;

	private PrintStream outSolutionFound;

	private void updateWriter() {
		try {
			outSolutionFound = new PrintStream(new FileOutputStream(whereToWriteFiles + "_solutions.dat"));
		} catch (FileNotFoundException e) {
			outSolutionFound = System.out;
		}

	}

	public void init(ISolverService solverService) {
		//		nVar = solverService.nVars();
		conflictCounter=0;
	}

	public void assuming(int p) {
	}

	public void propagating(int p, IConstr reason) {
		end = System.currentTimeMillis();
		if (end - begin >= 2000) {
			long tmp = (end - begin);
			//			index += tmp;
			

			cleanPanel.setSpeedLabeltext(propagationsCounter / tmp * 1000+"");
			
			begin = System.currentTimeMillis();
			propagationsCounter = 0;
		}
		propagationsCounter++;
	}

	public void backtracking(int p) {
	}

	public void adding(int p) {
	}

	public void learn(IConstr c) {
	}

	public void delete(int[] clause) {
	}

	public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
		conflictCounter++;
	}

	public void conflictFound(int p) {
	}

	public void solutionFound(int[] model) {
		//if(problem.)
		log("Found a solution !! ");
		logsameline(stringWriter.toString());
		stringWriter.getBuffer().delete(0, stringWriter.getBuffer().length());
		outSolutionFound.println(conflictCounter + "\t");
	}

	public void beginLoop() {
	}

	public void start() {
	}

	public void end(Lbool result) {
	}

	public void restarting() {
		end = System.currentTimeMillis();
		cleanPanel.setSpeedLabeltext(propagationsCounter / (end - begin)
				* 1000+"");
	}

	public void backjump(int backjumpLevel) {
	}

	public void cleaning() {
		end = System.currentTimeMillis();
		cleanPanel.setSpeedLabeltext(propagationsCounter / (end - begin) * 1000+"");
	}

	public class MyTabbedPane extends JTabbedPane {
		private static final long serialVersionUID = 1L;

		@Override
		public void setSelectedIndex(int index){
			if(this.getTabCount()==5){
				if(index==this.getTabCount()-1){
					//System.out.println("je suis lˆ");
					if(solver!=null && startStopButton.getText().equals(STOP)){
						String s = solver.toString();
						String res = solver.toString();
						int j=0;
						for(int i=0;i<s.length();i++){
							if(s.charAt(i)!='\n'){
								j++;
							}
							else{
								j=0;
							}
							if(j>80){
								res = new StringBuffer(res).insert(i, '\n').toString();
								j=0;
							}
						}
						textArea.setText(res);
						textArea.setEditable(false);
						textArea.repaint();
						aboutSolverPanel.paint(aboutSolverPanel.getGraphics());
						aboutSolverPanel.repaint();
					}
					else{
						textArea.setText("No solver is running at the moment");
						textArea.repaint();
						textArea.setEditable(false);
						aboutSolverPanel.paint(aboutSolverPanel.getGraphics());
						aboutSolverPanel.repaint();
					}

					//System.out.println(textArea.getText());
				}
			}

			super.setSelectedIndex(index);
		};
	} 

}
