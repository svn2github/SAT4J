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
 * This panel contains buttons that control restart and clean on solver. It also
 * displays history of commands.
 * 
 * @author sroussel
 * 
 */
public class DetailedCommandPanel extends JPanel implements SolverController,
        SearchListener, ICDCLLogger {

    private static final long serialVersionUID = 1L;

    public static final EmptyBorder border5 = new EmptyBorder(5, 5, 5, 5);

    private String ramdisk;

    private RemoteControlStrategy telecomStrategy;
    private RandomWalkDecorator randomWalk;
    private ICDCL solver;
    private Reader reader;
    private IProblem problem;
    private boolean optimizationMode = false;

    private String[] commandLines;

    private boolean firstStart;

    // private JChartBasedSolverVisualisation visu;

    // private boolean useCustomizedSolver;
    private StartSolverEnum startConfig;

    private Thread solveurThread;

    private StringWriter stringWriter;

    private MyTabbedPane tabbedPane;

    private JPanel aboutSolverPanel;
    private JTextArea textArea;

    private JPanel instancePanel;
    private final static String INSTANCE_PANEL = "Instance";
    private JLabel instanceLabel;
    private final static String INSTANCE = "Path to instance: ";
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
    private final static String CHOIX_SOLVER = "Choose solver: ";
    private String selectedSolver;
    private JComboBox listeSolvers;

    private final static String OPTMIZATION_MODE = "Use optimization mode";
    private JCheckBox optimisationModeCB;

    // private JCheckBox useCustomizedSolverCB;
    // private final static String USE_CUSTOMIZED_SOLVER =
    // "Use customized solver";

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
    // private boolean isInterrupted;

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

    public DetailedCommandPanel(String filename, RemoteControlFrame frame) {
        this(filename, "", frame);
    }

    public DetailedCommandPanel(String filename, String ramdisk,
            RemoteControlFrame frame) {
        this(filename, ramdisk, null, frame);
    }

    public DetailedCommandPanel(String filename, String ramdisk, String[] args,
            RemoteControlFrame frame) {
        super();

        this.frame = frame;

        this.visuPreferences = new VisuPreferences();

        this.telecomStrategy = new RemoteControlStrategy(this);
        this.instancePath = filename;
        this.ramdisk = ramdisk;

        this.console = new JTextArea();

        this.commandLines = args;
        if (args.length > 0) {
            this.solver = Solvers.configureSolver(args, this);
        }

        this.isPlotActivated = false;

        if (this.solver != null) {
            this.startConfig = StartSolverEnum.SOLVER_LINE_PARAM_LINE;
        } else {
            this.startConfig = StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT;
        }

        this.firstStart = true;

        this.setPreferredSize(new Dimension(750, 800));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        createInstancePanel();
        createChoixSolverPanel();

        this.restartPanel = new RestartCommandComponent(RESTART_PANEL, this,
                this.telecomStrategy.getRestartStrategy().getClass()
                        .getSimpleName());
        this.rwPanel = new RandomWalkCommandComponent(RW_PANEL, this);
        this.cleanPanel = new CleanCommandComponent(CLEAN_PANEL, this);
        this.phasePanel = new PhaseCommandComponent(PHASE_PANEL, this,
                this.telecomStrategy.getPhaseSelectionStrategy().getClass()
                        .getSimpleName());
        this.simplifierPanel = new SimplifierCommandComponent(SIMPLIFIER_PANEL,
                this);
        this.hotSolverPanel = new HotSolverCommandComponent(HOT_SOLVER_PANEL,
                this);

        this.scrollPane = new JScrollPane(this.console);

        // scrollPane.setMinimumSize(new Dimension(100,100));
        this.scrollPane.setPreferredSize(new Dimension(400, 200));
        this.scrollPane.getVerticalScrollBar().setValue(
                this.scrollPane.getVerticalScrollBar().getMaximum());
        // scrollPane.setAutoscrolls(true);

        this.tabbedPane = new MyTabbedPane();

        JPanel solverBigPanel = new JPanel();
        solverBigPanel
                .setLayout(new BoxLayout(solverBigPanel, BoxLayout.Y_AXIS));
        solverBigPanel.add(this.instancePanel);
        solverBigPanel.add(this.choixSolverPanel);

        this.tabbedPane.addTab("Solver", null, solverBigPanel,
                "instance & solver options");

        JPanel restartBigPanel = new JPanel();
        restartBigPanel.setLayout(new BoxLayout(restartBigPanel,
                BoxLayout.Y_AXIS));
        restartBigPanel.add(this.restartPanel);
        // restartBigPanel.add(hotSolverPanel);

        this.tabbedPane.addTab("Restart", null, restartBigPanel,
                "restart strategy & options");

        JPanel rwPhaseBigPanel = new JPanel();
        rwPhaseBigPanel.setLayout(new BoxLayout(rwPhaseBigPanel,
                BoxLayout.Y_AXIS));
        rwPhaseBigPanel.add(this.rwPanel);
        rwPhaseBigPanel.add(this.phasePanel);
        rwPhaseBigPanel.add(this.hotSolverPanel);

        this.tabbedPane.addTab("Heuristics", null, rwPhaseBigPanel,
                "random walk and phase strategy");

        JPanel clausesBigPanel = new JPanel();
        clausesBigPanel.setLayout(new BoxLayout(clausesBigPanel,
                BoxLayout.Y_AXIS));
        clausesBigPanel.add(this.cleanPanel);
        clausesBigPanel.add(this.simplifierPanel);

        this.tabbedPane.addTab("Learned Constraints", null, clausesBigPanel,
                "deletion and simplification strategy");

        this.aboutSolverPanel = new JPanel();
        this.textArea = new JTextArea("No solver is running at the moment");
        this.textArea.setColumns(50);
        this.aboutSolverPanel.add(this.textArea);

        this.tabbedPane.addTab("About Solver", null, this.aboutSolverPanel,
                "information about solver");

        this.add(this.tabbedPane);
        this.add(this.scrollPane);

        this.restartPanel.setRestartPanelEnabled(false);
        this.rwPanel.setRWPanelEnabled(false);
        this.cleanPanel.setCleanPanelEnabled(false);
        this.phasePanel.setPhasePanelEnabled(false);
        this.simplifierPanel.setSimplifierPanelEnabled(false);
        this.hotSolverPanel.setKeepSolverHotPanelEnabled(false);

        this.solverVisu = new JChartBasedSolverVisualisation(
                this.visuPreferences);

        updateWriter();
    }

    public void createInstancePanel() {
        this.instancePanel = new JPanel();

        this.instancePanel.setName(INSTANCE_PANEL);
        this.instancePanel.setBorder(new CompoundBorder(new TitledBorder(null,
                this.instancePanel.getName(), TitledBorder.LEFT,
                TitledBorder.TOP), border5));

        this.instancePanel.setLayout(new BorderLayout(0, 0));

        this.instanceLabel = new JLabel(INSTANCE);
        this.instancePathField = new JTextField(20);
        this.instancePathField.setText(this.instancePath);

        this.instanceLabel.setLabelFor(this.instancePathField);

        JPanel tmpPanel1 = new JPanel();
        tmpPanel1.add(this.instanceLabel);
        tmpPanel1.add(this.instancePathField);

        this.browseButton = new JButton(BROWSE);

        this.browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openFileChooser();
            }
        });

        JPanel tmpPanel2 = new JPanel();
        tmpPanel2.add(this.browseButton);

        this.instancePanel.add(tmpPanel1, BorderLayout.CENTER);
    }

    public void createChoixSolverPanel() {
        this.choixSolverPanel = new JPanel();

        this.choixSolverPanel.setName(CHOIX_SOLVER_PANEL);
        this.choixSolverPanel.setBorder(new CompoundBorder(new TitledBorder(
                null, this.choixSolverPanel.getName(), TitledBorder.LEFT,
                TitledBorder.TOP), border5));

        this.choixSolverPanel.setLayout(new BorderLayout());

        this.choixSolver = new JLabel(CHOIX_SOLVER);
        updateListOfSolvers();

        this.optimisationModeCB = new JCheckBox(OPTMIZATION_MODE);
        this.optimisationModeCB.setSelected(this.optimizationMode);

        JPanel tmpPanel1 = new JPanel();
        tmpPanel1.add(this.choixSolver);
        tmpPanel1.add(this.listeSolvers);
        tmpPanel1.add(this.optimisationModeCB);

        this.optimisationModeCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DetailedCommandPanel.this.optimizationMode = DetailedCommandPanel.this.optimisationModeCB
                        .isSelected();
                log("use optimization mode: "
                        + DetailedCommandPanel.this.optimizationMode);
            }
        });

        this.startStopButton = new JButton(START);

        this.startStopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DetailedCommandPanel.this.startStopButton.getText().equals(
                        START)) {
                    // launchSolver();
                    launchSolverWithConfigs();
                    DetailedCommandPanel.this.pauseButton.setEnabled(true);
                    setInstancePanelEnabled(false);
                    DetailedCommandPanel.this.restartPanel
                            .setRestartPanelEnabled(true);
                    DetailedCommandPanel.this.rwPanel.setRWPanelEnabled(true);
                    DetailedCommandPanel.this.cleanPanel
                            .setCleanPanelEnabled(true);
                    DetailedCommandPanel.this.cleanPanel
                            .setCleanPanelOriginalStrategyEnabled(true);
                    DetailedCommandPanel.this.phasePanel
                            .setPhasePanelEnabled(true);
                    setChoixSolverPanelEnabled(false);
                    DetailedCommandPanel.this.simplifierPanel
                            .setSimplifierPanelEnabled(true);
                    DetailedCommandPanel.this.hotSolverPanel
                            .setKeepSolverHotPanelEnabled(true);
                    DetailedCommandPanel.this.startStopButton.setText(STOP);
                    getThis().paintAll(getThis().getGraphics());
                    DetailedCommandPanel.this.frame
                            .setActivateTracingEditableUnderCondition(false);
                } else {

                    // assert solveurThread!=null;
                    ((ISolver) DetailedCommandPanel.this.problem)
                            .expireTimeout();
                    DetailedCommandPanel.this.pauseButton.setEnabled(false);
                    log("Asked the solver to stop");
                    setInstancePanelEnabled(true);
                    setChoixSolverPanelEnabled(true);
                    // setRestartPanelEnabled(false);
                    // setRWPanelEnabled(false);
                    // setCleanPanelEnabled(false);
                    // setPhasePanelEnabled(false);
                    // setSimplifierPanelEnabled(false);
                    // setKeepSolverHotPanelEnabled(false);
                    DetailedCommandPanel.this.startStopButton.setText(START);
                    getThis().paintAll(getThis().getGraphics());
                    DetailedCommandPanel.this.frame
                            .setActivateTracingEditable(true);

                }
            }
        });

        this.pauseButton = new JButton(PAUSE);
        this.pauseButton.setEnabled(false);

        this.pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DetailedCommandPanel.this.pauseButton.getText().equals(
                        PAUSE)) {
                    DetailedCommandPanel.this.pauseButton.setText(RESUME);
                    DetailedCommandPanel.this.telecomStrategy
                            .setInterrupted(true);
                } else {
                    DetailedCommandPanel.this.pauseButton.setText(PAUSE);
                    DetailedCommandPanel.this.telecomStrategy
                            .setInterrupted(false);
                }

            }
        });

        JPanel tmpPanel2 = new JPanel();
        tmpPanel2.setLayout(new FlowLayout());
        tmpPanel2.add(this.startStopButton);
        tmpPanel2.add(this.pauseButton);

        this.solverLineParamLineRadio = new JRadioButton(
                SOLVER_LINE_PARAM_LINE_CONFIG);
        this.solverLineParamRemoteRadio = new JRadioButton(
                SOLVER_LINE_PARAM_REMOTE_CONFIG);
        this.solverListParamRemoteRadio = new JRadioButton(
                SOLVER_LIST_PARAM_REMOTE_CONFIG);
        this.solverListParamListRadio = new JRadioButton(
                SOLVER_LIST_PARAM_LIST_CONFIG);

        this.solverConfigGroup = new ButtonGroup();
        this.solverConfigGroup.add(this.solverLineParamLineRadio);
        this.solverConfigGroup.add(this.solverLineParamRemoteRadio);
        this.solverConfigGroup.add(this.solverListParamListRadio);
        this.solverConfigGroup.add(this.solverListParamRemoteRadio);

        this.chooseStartConfigLabel = new JLabel(CHOOSE_START_CONFIG);

        JPanel tmpPanel3 = new JPanel();
        tmpPanel3.setLayout(new BoxLayout(tmpPanel3, BoxLayout.Y_AXIS));

        tmpPanel3.add(this.chooseStartConfigLabel);
        tmpPanel3.add(this.solverLineParamLineRadio);
        tmpPanel3.add(this.solverLineParamRemoteRadio);
        tmpPanel3.add(this.solverListParamListRadio);
        tmpPanel3.add(this.solverListParamRemoteRadio);

        this.solverLineParamLineRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DetailedCommandPanel.this.solverLineParamLineRadio
                        .isSelected()) {
                    DetailedCommandPanel.this.startConfig = StartSolverEnum.SOLVER_LINE_PARAM_LINE;
                }
            }
        });

        this.solverLineParamRemoteRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DetailedCommandPanel.this.solverLineParamRemoteRadio
                        .isSelected()) {
                    DetailedCommandPanel.this.startConfig = StartSolverEnum.SOLVER_LINE_PARAM_REMOTE;
                }
            }
        });

        this.solverListParamListRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DetailedCommandPanel.this.solverListParamListRadio
                        .isSelected()) {
                    DetailedCommandPanel.this.startConfig = StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT;
                }
            }
        });

        this.solverListParamRemoteRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DetailedCommandPanel.this.solverListParamRemoteRadio
                        .isSelected()) {
                    DetailedCommandPanel.this.startConfig = StartSolverEnum.SOLVER_LIST_PARAM_REMOTE;
                }
            }
        });

        setChoixSolverPanelEnabled(true);

        if (this.solver == null) {
            this.solverLineParamLineRadio.setEnabled(false);
            this.solverLineParamRemoteRadio.setEnabled(false);
        }

        if (this.firstStart) {
            this.solverLineParamRemoteRadio.setEnabled(false);
            this.solverListParamRemoteRadio.setEnabled(false);
        }

        this.choixSolverPanel.add(tmpPanel1, BorderLayout.NORTH);
        this.choixSolverPanel.add(tmpPanel3, BorderLayout.CENTER);
        this.choixSolverPanel.add(tmpPanel2, BorderLayout.SOUTH);
    }

    public String getStartStopText() {
        return this.startStopButton.getText();
    }

    public void setOptimisationMode(boolean optimizationMode) {
        this.optimizationMode = optimizationMode;
        this.optimisationModeCB.setSelected(optimizationMode);
    }

    public void launchSolverWithConfigs() {
        if (this.startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT)) {
            this.selectedSolver = (String) this.listeSolvers.getSelectedItem();
            String[] partsSelectedSolver = this.selectedSolver.split("\\.");

            assert partsSelectedSolver.length == 2;
            assert partsSelectedSolver[0].equals(MINISAT_PREFIX)
                    || partsSelectedSolver[0].equals(PB_PREFIX);

            ASolverFactory factory;

            if (partsSelectedSolver[0].equals(MINISAT_PREFIX)) {
                factory = org.sat4j.minisat.SolverFactory.instance();
            } else {
                factory = org.sat4j.pb.SolverFactory.instance();
            }
            this.solver = (ICDCL) factory
                    .createSolverByName(partsSelectedSolver[1]);

            this.telecomStrategy.setSolver(this.solver);
            this.telecomStrategy.setRestartStrategy(this.solver
                    .getRestartStrategy());
            this.solver.setRestartStrategy(this.telecomStrategy);

            this.restartPanel.setCurrentRestart(this.telecomStrategy
                    .getRestartStrategy().getClass().getSimpleName());

            IOrder order = this.solver.getOrder();

            double proba = 0;

            if (this.optimizationMode) {
                if (order instanceof RandomWalkDecoratorObjective) {
                    this.randomWalk = (RandomWalkDecorator) order;
                    proba = this.randomWalk.getProbability();
                } else if (order instanceof VarOrderHeapObjective) {
                    this.randomWalk = new RandomWalkDecoratorObjective(
                            (VarOrderHeapObjective) order, 0);
                }
            } else if (this.solver.getOrder() instanceof RandomWalkDecorator) {
                this.randomWalk = (RandomWalkDecorator) order;
                proba = this.randomWalk.getProbability();
            } else {
                this.randomWalk = new RandomWalkDecorator((VarOrderHeap) order,
                        0);
            }

            this.randomWalk.setProbability(proba);
            this.rwPanel.setProba(proba);

            this.solver.setOrder(this.randomWalk);

            this.telecomStrategy.setPhaseSelectionStrategy(this.solver
                    .getOrder().getPhaseSelectionStrategy());
            this.phasePanel.setPhaseListSelectedItem(this.telecomStrategy
                    .getPhaseSelectionStrategy().getClass().getSimpleName());
            this.solver.getOrder().setPhaseSelectionStrategy(
                    this.telecomStrategy);
            this.simplifierPanel.setSelectedSimplification(this.solver
                    .getSimplifier().toString());
        }

        else if (this.startConfig
                .equals(StartSolverEnum.SOLVER_LIST_PARAM_REMOTE)) {
            this.selectedSolver = (String) this.listeSolvers.getSelectedItem();
            String[] partsSelectedSolver = this.selectedSolver.split("\\.");

            assert partsSelectedSolver.length == 2;
            assert partsSelectedSolver[0].equals(MINISAT_PREFIX)
                    || partsSelectedSolver[0].equals(PB_PREFIX);

            ASolverFactory factory;

            if (partsSelectedSolver[0].equals(MINISAT_PREFIX)) {
                factory = org.sat4j.minisat.SolverFactory.instance();
            } else {
                factory = org.sat4j.pb.SolverFactory.instance();
            }
            this.solver = (ICDCL) factory
                    .createSolverByName(partsSelectedSolver[1]);

            this.telecomStrategy.setSolver(this.solver);

            this.solver.setRestartStrategy(this.telecomStrategy);
            this.solver.setOrder(this.randomWalk);
            this.solver.getOrder().setPhaseSelectionStrategy(
                    this.telecomStrategy);

            this.restartPanel.hasClickedOnRestart();
            this.rwPanel.hasClickedOnApplyRW();
            this.phasePanel.hasClickedOnApplyPhase();
            this.simplifierPanel.hasClickedOnApplySimplification();
        }

        else if (this.startConfig
                .equals(StartSolverEnum.SOLVER_LINE_PARAM_LINE)) {

            this.solver = Solvers.configureSolver(this.commandLines, this);

            this.telecomStrategy.setSolver(this.solver);
            this.telecomStrategy.setRestartStrategy(this.solver
                    .getRestartStrategy());
            this.solver.setRestartStrategy(this.telecomStrategy);

            this.restartPanel.setCurrentRestart(this.telecomStrategy
                    .getRestartStrategy().getClass().getSimpleName());

            IOrder order = this.solver.getOrder();

            double proba = 0;

            if (this.optimizationMode) {
                if (order instanceof RandomWalkDecoratorObjective) {
                    this.randomWalk = (RandomWalkDecorator) order;
                    proba = this.randomWalk.getProbability();
                } else if (order instanceof VarOrderHeapObjective) {
                    this.randomWalk = new RandomWalkDecoratorObjective(
                            (VarOrderHeapObjective) order, 0);
                }
            } else if (this.solver.getOrder() instanceof RandomWalkDecorator) {
                this.randomWalk = (RandomWalkDecorator) order;
                proba = this.randomWalk.getProbability();
            } else {
                this.randomWalk = new RandomWalkDecorator((VarOrderHeap) order,
                        0);
            }

            this.randomWalk.setProbability(proba);
            this.rwPanel.setProba(proba);
            this.solver.setOrder(this.randomWalk);
            this.telecomStrategy.setPhaseSelectionStrategy(this.solver
                    .getOrder().getPhaseSelectionStrategy());
            this.solver.getOrder().setPhaseSelectionStrategy(
                    this.telecomStrategy);
            this.phasePanel.setPhaseListSelectedItem(this.telecomStrategy
                    .getPhaseSelectionStrategy().getClass().getSimpleName());
            this.simplifierPanel.setSelectedSimplification(this.solver
                    .getSimplifier().toString());

            this.phasePanel.repaint();
        }

        else if (this.startConfig
                .equals(StartSolverEnum.SOLVER_LINE_PARAM_REMOTE)) {

            this.solver = Solvers.configureSolver(this.commandLines, this);

            this.solver.setRestartStrategy(this.telecomStrategy);
            this.solver.setOrder(this.randomWalk);
            this.solver.getOrder().setPhaseSelectionStrategy(
                    this.telecomStrategy);

            this.restartPanel.hasClickedOnRestart();
            this.rwPanel.hasClickedOnApplyRW();
            this.phasePanel.hasClickedOnApplyPhase();
            this.simplifierPanel.hasClickedOnApplySimplification();
        }

        this.whereToWriteFiles = this.instancePath;

        if (this.ramdisk.length() > 0) {
            String[] instancePathSplit = this.instancePath.split("/");
            this.whereToWriteFiles = this.ramdisk + "/"
                    + instancePathSplit[instancePathSplit.length - 1];
        }

        this.solver.setVerbose(true);
        initSearchListeners();
        this.solver.setLogger(this);
        this.reader = createReader(this.solver, this.instancePath);

        try {
            this.problem = this.reader.parseInstance(this.instancePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ContradictionException e) {
            log("Unsatisfiable (trivial)!");
        }

        boolean optimisation = false;
        if (this.reader instanceof PBInstanceReader) {
            optimisation = ((PBInstanceReader) this.reader)
                    .hasObjectiveFunction();
            if (optimisation) {
                this.problem = new OptToPBSATAdapter(new PseudoOptDecorator(
                        (IPBCDCLSolver) this.solver));
            }
        }

        log("# Started solver " + this.solver.getClass().getSimpleName());
        log("# on instance " + this.instancePath);
        log("# Optimisation = " + optimisation);
        log("# Restart strategy = "
                + this.telecomStrategy.getRestartStrategy().getClass()
                        .getSimpleName());
        log("# Random walk probability = " + this.randomWalk.getProbability());
        // log("# Number of conflicts before cleaning = " + nbConflicts);

        this.solveurThread = new Thread() {
            @Override
            public void run() {
                // Thread thisThread = Thread.currentThread();
                // if(shouldStop){
                // System.out.println("coucou");
                // }
                // while(!shouldStop){
                try {
                    DetailedCommandPanel.this.stringWriter = new StringWriter();
                    if (DetailedCommandPanel.this.problem.isSatisfiable()) {
                        log("Satisfiable !");
                        if (DetailedCommandPanel.this.problem instanceof OptToPBSATAdapter) {
                            log(((OptToPBSATAdapter) DetailedCommandPanel.this.problem)
                                    .getCurrentObjectiveValue() + "");
                            DetailedCommandPanel.this.reader
                                    .decode(((OptToPBSATAdapter) DetailedCommandPanel.this.problem)
                                            .model(new PrintWriter(
                                                    DetailedCommandPanel.this.stringWriter)),
                                            new PrintWriter(
                                                    DetailedCommandPanel.this.stringWriter));
                        } else {
                            DetailedCommandPanel.this.reader
                                    .decode(DetailedCommandPanel.this.problem
                                            .model(),
                                            new PrintWriter(
                                                    DetailedCommandPanel.this.stringWriter));
                        }
                        log(DetailedCommandPanel.this.stringWriter.toString());
                    } else {
                        log("Unsatisfiable !");
                    }
                } catch (TimeoutException e) {
                    log("Timeout, sorry!");
                }
                // log("Solver has stopped");
                // }
            }
        };
        this.solveurThread.start();

        if (this.isPlotActivated) {
            this.solverVisu.setnVar(this.solver.nVars());
            startVisu();
        }
    }

    public void initSearchListeners() {
        List<SearchListener> listeners = new ArrayList<SearchListener>();

        if (this.isPlotActivated) {
            if (this.gnuplotBased) {
                this.solverVisu = new GnuplotBasedSolverVisualisation(
                        this.visuPreferences, this.solver.nVars(),
                        this.instancePath, this);
                if (this.visuPreferences.isDisplayClausesEvaluation()) {
                    listeners.add(new LearnedTracing(
                            new FileBasedVisualizationTool(
                                    this.whereToWriteFiles + "-learned")));
                }
                if (this.visuPreferences.isDisplayClausesSize()) {
                    listeners.add(new LearnedClausesSizeTracing(
                            new FileBasedVisualizationTool(
                                    this.whereToWriteFiles
                                            + "-learned-clauses-size"),
                            new FileBasedVisualizationTool(
                                    this.whereToWriteFiles
                                            + "-learned-clauses-size-restart"),
                            new FileBasedVisualizationTool(
                                    this.whereToWriteFiles
                                            + "-learned-clauses-size-clean")));
                }
                if (this.visuPreferences.isDisplayConflictsDecision()) {
                    listeners
                            .add(new ConflictLevelTracing(
                                    new FileBasedVisualizationTool(
                                            this.whereToWriteFiles
                                                    + "-conflict-level"),
                                    new FileBasedVisualizationTool(
                                            this.whereToWriteFiles
                                                    + "-conflict-level-restart"),
                                    new FileBasedVisualizationTool(
                                            this.whereToWriteFiles
                                                    + "-conflict-level-clean")));
                }
                if (this.visuPreferences.isDisplayConflictsTrail()) {
                    listeners
                            .add(new ConflictDepthTracing(
                                    new FileBasedVisualizationTool(
                                            this.whereToWriteFiles
                                                    + "-conflict-depth"),
                                    new FileBasedVisualizationTool(
                                            this.whereToWriteFiles
                                                    + "-conflict-depth-restart"),
                                    new FileBasedVisualizationTool(
                                            this.whereToWriteFiles
                                                    + "-conflict-depth-clean")));
                }

                if (this.visuPreferences.isDisplayDecisionIndexes()) {
                    listeners.add(new DecisionTracing(
                            new FileBasedVisualizationTool(
                                    this.whereToWriteFiles
                                            + "-decision-indexes-pos"),
                            new FileBasedVisualizationTool(
                                    this.whereToWriteFiles
                                            + "-decision-indexes-neg"),
                            new FileBasedVisualizationTool(
                                    this.whereToWriteFiles
                                            + "-decision-indexes-restart"),
                            new FileBasedVisualizationTool(
                                    this.whereToWriteFiles
                                            + "-decision-indexes-clean")));
                }

                if (this.visuPreferences.isDisplaySpeed()) {
                    listeners
                            .add(new SpeedTracing(
                                    new FileBasedVisualizationTool(
                                            this.whereToWriteFiles + "-speed"),
                                    new FileBasedVisualizationTool(
                                            this.whereToWriteFiles
                                                    + "-speed-clean"),
                                    new FileBasedVisualizationTool(
                                            this.whereToWriteFiles
                                                    + "-speed-restart")));
                }
                if (this.visuPreferences.isDisplayVariablesEvaluation()) {
                    listeners.add(new HeuristicsTracing(
                            new FileBasedVisualizationTool(
                                    this.whereToWriteFiles + "-heuristics")));
                }
            }

            else if (this.chartBased) {

                if (this.solverVisu != null) {
                    this.solverVisu.end();
                }

                this.solverVisu = new JChartBasedSolverVisualisation(
                        this.visuPreferences);

                ((JChartBasedSolverVisualisation) this.solverVisu)
                        .setnVar(this.solver.nVars());
                if (this.visuPreferences.isDisplayClausesEvaluation()) {
                    listeners
                            .add(new LearnedTracing(
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getClausesEvaluationTrace())));
                }
                if (this.visuPreferences.isDisplayClausesSize()) {
                    listeners
                            .add(new LearnedClausesSizeTracing(
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getLearnedClausesSizeTrace()),
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getLearnedClausesSizeRestartTrace()),
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getLearnedClausesSizeCleanTrace())));
                }
                if (this.visuPreferences.isDisplayConflictsDecision()) {
                    listeners
                            .add(new ConflictLevelTracing(
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getConflictLevelTrace()),
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getConflictLevelRestartTrace()),
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getConflictLevelCleanTrace())));
                }
                if (this.visuPreferences.isDisplayConflictsTrail()) {
                    listeners
                            .add(new ConflictDepthTracing(
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getConflictDepthTrace()),
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getConflictDepthRestartTrace()),
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getConflictDepthCleanTrace())));
                }
                if (this.visuPreferences.isDisplayDecisionIndexes()) {
                    listeners
                            .add(new DecisionTracing(
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getPositiveDecisionTrace()),
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getNegativeDecisionTrace()),
                                    new ChartBasedVisualizationTool(
                                            new TraceComposite(
                                                    ((JChartBasedSolverVisualisation) this.solverVisu)
                                                            .getRestartPosDecisionTrace(),
                                                    ((JChartBasedSolverVisualisation) this.solverVisu)
                                                            .getRestartNegDecisionTrace())),
                                    new ChartBasedVisualizationTool(
                                            new TraceComposite(
                                                    ((JChartBasedSolverVisualisation) this.solverVisu)
                                                            .getCleanPosDecisionTrace(),
                                                    ((JChartBasedSolverVisualisation) this.solverVisu)
                                                            .getCleanNegDecisionTrace()))));
                }
                if (this.visuPreferences.isDisplaySpeed()) {
                    listeners
                            .add(new SpeedTracing(
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getSpeedTrace()),
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getSpeedCleanTrace()),
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getSpeedRestartTrace())));
                }
                if (this.visuPreferences.isDisplayVariablesEvaluation()) {
                    listeners
                            .add(new HeuristicsTracing(
                                    new ChartBasedVisualizationTool(
                                            ((JChartBasedSolverVisualisation) this.solverVisu)
                                                    .getHeuristicsTrace())));
                }
            }

        }
        listeners.add(this);

        this.solver.setSearchListener(new MultiTracing(listeners));

    }

    public int getNVar() {
        if (this.solver != null) {
            return this.solver.nVars();
        }
        return 0;
    }

    public void setPhaseSelectionStrategy(IPhaseSelectionStrategy phase) {
        this.telecomStrategy.setPhaseSelectionStrategy(phase);
        log("Told the solver to apply a new phase strategy :"
                + phase.getClass().getSimpleName());
    }

    public void shouldRestartNow() {
        this.telecomStrategy.setHasClickedOnRestart(true);
    }

    public void setRestartStrategy(RestartStrategy strategy) {
        this.telecomStrategy.setRestartStrategy(strategy);
        log("Set Restart to " + strategy);
    }

    public RestartStrategy getRestartStrategy() {
        return this.telecomStrategy.getRestartStrategy();
    }

    public SearchParams getSearchParams() {
        return this.telecomStrategy.getSearchParams();
    }

    public void init(SearchParams params) {
        this.telecomStrategy.init(params);
        log("Init restart with params");
    }

    public void setNbClausesAtWhichWeShouldClean(int nbConflicts) {
        this.telecomStrategy.setNbClausesAtWhichWeShouldClean(nbConflicts);
        log("Changed number of conflicts before cleaning to " + nbConflicts);
    }

    public void setUseTelecomStrategyAsLearnedConstraintsDeletionStrategy() {
        this.telecomStrategy
                .setUseTelecomStrategyAsLearnedConstraintsDeletionStrategy(true);
        log("Solver now cleans clauses every "
                + this.cleanPanel.getCleanSliderValue()
                + " conflicts and bases evaluation of clauses on activity");
    }

    public void setLearnedDeletionStrategyTypeToSolver(
            LearnedConstraintsEvaluationType type) {
        this.solver.setLearnedConstraintsDeletionStrategy(this.telecomStrategy,
                type);
        log("Changed clauses evaluation type to " + type);
    }

    public LearnedConstraintsEvaluationType getLearnedConstraintsEvaluationType() {
        // TODO get the real evaluation !!
        return LearnedConstraintsEvaluationType.ACTIVITY;
    }

    public void shouldCleanNow() {
        log("Told the solver to clean");
        this.telecomStrategy.setHasClickedOnClean(true);
    }

    public void setKeepSolverHot(boolean keepHot) {
        this.solver.setKeepSolverHot(keepHot);
        if (keepHot) {
            log("Keep hot solver is now activated");
        } else {
            log("Keep hot solver is now desactivated");
        }
    }

    public boolean isGnuplotBased() {
        return this.gnuplotBased;
    }

    public void setGnuplotBased(boolean gnuplotBased) {
        this.gnuplotBased = gnuplotBased;
    }

    public boolean isChartBased() {
        return this.chartBased;
    }

    public void setChartBased(boolean chartBased) {
        this.chartBased = chartBased;
    }

    public boolean isPlotActivated() {
        return this.isPlotActivated;
    }

    public void setPlotActivated(boolean isPlotActivated) {
        this.isPlotActivated = isPlotActivated;
    }

    public void setRandomWalkProba(double proba) {
        this.randomWalk.setProbability(proba);
        log("Set probability to " + proba);
    }

    public void setSimplifier(SimplificationType type) {
        this.solver.setSimplifier(type);
        log("Told the solver to use " + type);
    }

    public List<String> getListOfSolvers() {
        ASolverFactory factory;

        List<String> result = new ArrayList<String>();

        factory = org.sat4j.minisat.SolverFactory.instance();
        for (String s : factory.solverNames()) {
            result.add(MINISAT_PREFIX + "." + s);
        }

        factory = org.sat4j.pb.SolverFactory.instance();
        for (String s : factory.solverNames()) {
            result.add(PB_PREFIX + "." + s);
        }

        Collections.sort(result);

        return result;
    }

    public List<String> getListOfPBSolvers() {
        ASolverFactory factory;

        List<String> result = new ArrayList<String>();

        factory = org.sat4j.pb.SolverFactory.instance();
        for (String s : factory.solverNames()) {
            result.add(PB_PREFIX + "." + s);
        }
        Collections.sort(result);

        return result;
    }

    public void log(String message) {
        logsameline(message + "\n");
    }

    public void logsameline(String message) {
        if (this.console != null) {
            this.console.append(message);
            this.console.setCaretPosition(this.console.getDocument()
                    .getLength());
            this.console.repaint();
        }
        this.repaint();
    }

    public void openFileChooser() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showDialog(this, "Choose instance");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            this.instancePath = file.getAbsolutePath();
            this.instancePathField.setText(this.instancePath);
            updateListOfSolvers();
        }
    }

    protected Reader createReader(ICDCL theSolver, String problemname) {
        if (theSolver instanceof IPBSolver) {
            return new PBInstanceReader((IPBSolver) theSolver);
        }
        return new InstanceReader(theSolver);
    }

    public void updateListOfSolvers() {
        if (this.instancePath.endsWith(".opb")) {
            this.listeSolvers = new JComboBox(getListOfPBSolvers().toArray());
            this.listeSolvers.setSelectedItem("pb.Default");
            this.selectedSolver = "pb.Default";
        } else {
            this.listeSolvers = new JComboBox(getListOfSolvers().toArray());
            this.listeSolvers.setSelectedItem("minisat.Default");
            this.selectedSolver = "minisat.Default";
        }
    }

    public void setInstancePanelEnabled(boolean enabled) {
        this.instanceLabel.setEnabled(enabled);
        this.instancePathField.setEnabled(enabled);
        this.browseButton.setEnabled(enabled);
        this.instancePanel.repaint();
    }

    public void setChoixSolverPanelEnabled(boolean enabled) {
        this.listeSolvers.setEnabled(enabled);
        this.choixSolver.setEnabled(enabled);
        this.solverLineParamLineRadio.setEnabled(enabled);
        this.solverLineParamRemoteRadio.setEnabled(enabled);
        this.solverListParamListRadio.setEnabled(enabled);
        this.solverListParamRemoteRadio.setEnabled(enabled);
        this.optimisationModeCB.setEnabled(enabled);
        // TODO regarder si le customized solver etait en mode optimisation ou
        // pas
        this.choixSolverPanel.repaint();
    }

    public void setSolverVisualisation(SolverVisualisation visu) {
        this.solverVisu = visu;
    }

    public void activateGnuplotTracing(boolean b) {
        this.isPlotActivated = b;
        if (this.solver != null) {
            initSearchListeners();
        }
    }

    public void startVisu() {
        this.solverVisu.start();
    }

    public void stopVisu() {
        this.solverVisu.end();
    }

    public VisuPreferences getGnuplotPreferences() {
        return this.visuPreferences;
    }

    public void setGnuplotPreferences(VisuPreferences gnuplotPreferences) {
        this.visuPreferences = gnuplotPreferences;
    }

    public DetailedCommandPanel getThis() {
        return this;
    }

    public ISolver getSolver() {
        return (ISolver) this.problem;
    }

    private long begin, end;
    private int propagationsCounter;

    private int conflictCounter;

    private PrintStream outSolutionFound;

    private void updateWriter() {
        try {
            this.outSolutionFound = new PrintStream(new FileOutputStream(
                    this.whereToWriteFiles + "_solutions.dat"));
        } catch (FileNotFoundException e) {
            this.outSolutionFound = System.out;
        }

    }

    public void init(ISolverService solverService) {
        // nVar = solverService.nVars();
        this.conflictCounter = 0;
    }

    public void assuming(int p) {
    }

    public void propagating(int p, IConstr reason) {
        this.end = System.currentTimeMillis();
        if (this.end - this.begin >= 2000) {
            long tmp = this.end - this.begin;
            // index += tmp;

            this.cleanPanel.setSpeedLabeltext(this.propagationsCounter / tmp
                    * 1000 + "");

            this.begin = System.currentTimeMillis();
            this.propagationsCounter = 0;
        }
        this.propagationsCounter++;
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
        this.conflictCounter++;
    }

    public void conflictFound(int p) {
    }

    public void solutionFound(int[] model) {
        // if(problem.)
        log("Found a solution !! ");
        logsameline(this.stringWriter.toString());
        this.stringWriter.getBuffer().delete(0,
                this.stringWriter.getBuffer().length());
        this.outSolutionFound.println(this.conflictCounter + "\t");
    }

    public void beginLoop() {
    }

    public void start() {
    }

    public void end(Lbool result) {
    }

    public void restarting() {
        this.end = System.currentTimeMillis();
        this.cleanPanel.setSpeedLabeltext(this.propagationsCounter
                / (this.end - this.begin) * 1000 + "");
    }

    public void backjump(int backjumpLevel) {
    }

    public void cleaning() {
        this.end = System.currentTimeMillis();
        this.cleanPanel.setSpeedLabeltext(this.propagationsCounter
                / (this.end - this.begin) * 1000 + "");
    }

    public class MyTabbedPane extends JTabbedPane {
        private static final long serialVersionUID = 1L;

        @Override
        public void setSelectedIndex(int index) {
            if (this.getTabCount() == 5) {
                if (index == this.getTabCount() - 1) {
                    // System.out.println("je suis l�");
                    if (DetailedCommandPanel.this.solver != null
                            && DetailedCommandPanel.this.startStopButton
                                    .getText().equals(STOP)) {
                        String s = DetailedCommandPanel.this.solver.toString();
                        String res = DetailedCommandPanel.this.solver
                                .toString();
                        int j = 0;
                        for (int i = 0; i < s.length(); i++) {
                            if (s.charAt(i) != '\n') {
                                j++;
                            } else {
                                j = 0;
                            }
                            if (j > 80) {
                                res = new StringBuffer(res).insert(i, '\n')
                                        .toString();
                                j = 0;
                            }
                        }
                        DetailedCommandPanel.this.textArea.setText(res);
                        DetailedCommandPanel.this.textArea.setEditable(false);
                        DetailedCommandPanel.this.textArea.repaint();
                        DetailedCommandPanel.this.aboutSolverPanel
                                .paint(DetailedCommandPanel.this.aboutSolverPanel
                                        .getGraphics());
                        DetailedCommandPanel.this.aboutSolverPanel.repaint();
                    } else {
                        DetailedCommandPanel.this.textArea
                                .setText("No solver is running at the moment");
                        DetailedCommandPanel.this.textArea.repaint();
                        DetailedCommandPanel.this.textArea.setEditable(false);
                        DetailedCommandPanel.this.aboutSolverPanel
                                .paint(DetailedCommandPanel.this.aboutSolverPanel
                                        .getGraphics());
                        DetailedCommandPanel.this.aboutSolverPanel.repaint();
                    }

                    // System.out.println(textArea.getText());
                }
            }

            super.setSelectedIndex(index);
        };
    }

}
