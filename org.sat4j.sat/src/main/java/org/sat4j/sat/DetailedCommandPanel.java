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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.DefaultComboBoxModel;
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
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.maxsat.reader.MSInstanceReader;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.core.LearnedConstraintsEvaluationType;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.SimplificationType;
import org.sat4j.minisat.core.SolverStats;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.pb.ConstraintRelaxingPseudoOptDecorator;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.orders.RandomWalkDecoratorObjective;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.pb.tools.ClausalConstraintsDecorator;
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
import org.sat4j.specs.ILogAble;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.RandomAccessModel;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ClausalCardinalitiesDecorator;
import org.sat4j.tools.ConflictDepthTracing;
import org.sat4j.tools.ConflictLevelTracing;
import org.sat4j.tools.DecisionTracing;
import org.sat4j.tools.FileBasedVisualizationTool;
import org.sat4j.tools.HeuristicsTracing;
import org.sat4j.tools.LearnedClausesSizeTracing;
import org.sat4j.tools.LearnedTracing;
import org.sat4j.tools.MultiTracing;
import org.sat4j.tools.SpeedTracing;
import org.sat4j.tools.encoding.EncodingStrategy;
import org.sat4j.tools.encoding.Policy;

/**
 * 
 * This panel contains buttons that control restart and clean on solver. It also
 * displays history of commands.
 * 
 * @author sroussel
 * 
 */
public class DetailedCommandPanel extends JPanel implements SolverController,
        SearchListener<ISolverService>, ILogAble {

    private static final String EXACTLY_1 = "Exactly 1:";
    private static final String EXACTLY_K = "Exactly K:";
    private static final String AT_MOST_1 = "At Most 1:";
    private static final String AT_MOST_K = "At Most K:";

    private Policy encodingPolicy;

    private static final long serialVersionUID = 1L;

    public static final EmptyBorder BORDER5 = new EmptyBorder(5, 5, 5, 5);

    private String ramdisk;

    private RemoteControlStrategy telecomStrategy;
    private RandomWalkDecorator randomWalk;
    private ISolver solver;
    private Reader reader;
    private IProblem problem;
    private ProblemType problemType;
    private boolean optimizationMode;
    private boolean equivalenceMode;
    private boolean lowerMode;

    private String[] commandLines;

    private boolean firstStart;

    private StartSolverEnum startConfig;

    private StringWriter stringWriter;

    private JPanel aboutSolverPanel;
    private JTextArea textArea;

    private JPanel instancePanel;
    private static final String INSTANCE_PANEL = "Instance";
    private JLabel instanceLabel;
    private static final String INSTANCE = "Path to instance: ";
    private JTextField instancePathField;
    private String instancePath;
    private JButton browseButton;
    private static final String BROWSE = "Browse";

    private String whereToWriteFiles;

    private static final String MINISAT_PREFIX = "minisat";
    private static final String PB_PREFIX = "pb";
    private static final String MAXSAT_PREFIX = "maxsat";
    private JPanel choixSolverPanel;
    private static final String CHOIX_SOLVER_PANEL = "Solver";
    private JLabel choixSolver;
    private static final String CHOIX_SOLVER = "Choose solver: ";
    private JComboBox listeSolvers;

    private static final String OPTMIZATION_MODE = "Optimization problem";
    private JCheckBox optimisationModeCB;

    private static final String EQUIVALENCE = "Use equivalence instead of implication";
    private JCheckBox equivalenceCB;

    private static final String LOWER = "Search solution by lower bounding instead of upper bounding";
    private JCheckBox lowerCB;

    private JComboBox atMostKCB;
    private JComboBox atMost1CB;
    private JComboBox exactlyKCB;
    private JComboBox exactly1CB;

    private JRadioButton solverLineParamLineRadio;
    private JRadioButton solverLineParamRemoteRadio;
    private JRadioButton solverListParamListRadio;
    private JRadioButton solverListParamRemoteRadio;

    private static final String SOLVER_LINE_PARAM_LINE_CONFIG = "Start customized solver as given in command line";
    private static final String SOLVER_LINE_PARAM_REMOTE_CONFIG = "Start customized solver as given in command line with configuration given in the remote";
    private static final String SOLVER_LIST_PARAM_LIST_CONFIG = "Start solver as chosen in list with its default configuration";
    private static final String SOLVER_LIST_PARAM_REMOTE_CONFIG = "Start solver as chosen in list with configuration given in the remote";

    private static final String CHOOSE_START_CONFIG = "Choose start configuration : ";

    private JButton startStopButton;
    private static final String START = "Start";
    private static final String STOP = "Stop";

    private JButton pauseButton;
    private static final String PAUSE = "Pause";
    private static final String RESUME = "Resume";

    private static final String RESTART_PANEL = "Restart strategy";
    private RestartCommandComponent restartPanel;

    private static final String RW_PANEL = "Random Walk";
    private RandomWalkCommandComponent rwPanel;

    private static final String CLEAN_PANEL = "Learned Constraint Deletion Strategy";
    private CleanCommandComponent cleanPanel;

    private PhaseCommandComponent phasePanel;
    private static final String PHASE_PANEL = "Phase Strategy";

    private SimplifierCommandComponent simplifierPanel;
    private static final String SIMPLIFIER_PANEL = "Simplification strategy";

    private HotSolverCommandComponent hotSolverPanel;
    private static final String HOT_SOLVER_PANEL = "Hot solver";

    private JTextArea console;

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

        this(filename, ramdisk, new String[0], frame);
    }

    public DetailedCommandPanel(String filename, String ramdisk, String[] args,
            RemoteControlFrame frame) {
        super();

        this.encodingPolicy = new Policy();

        this.frame = frame;

        this.visuPreferences = new VisuPreferences();

        this.telecomStrategy = new RemoteControlStrategy(this);
        this.instancePath = filename;
        this.ramdisk = ramdisk;

        this.console = new JTextArea();

        this.commandLines = args.clone();
        if (args.length > 0) {
            this.solver = Solvers.configureSolver(args, this);
            this.optimizationMode = Solvers.containsOptValue(args);
        }

        this.equivalenceMode = false;
        this.lowerMode = false;

        this.isPlotActivated = false;

        if (this.solver != null) {
            this.startConfig = StartSolverEnum.SOLVER_LINE_PARAM_LINE;
        } else {
            this.startConfig = StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT;
        }

        this.firstStart = true;

        this.setPreferredSize(new Dimension(800, 800));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        createInstancePanel();
        createChoixSolverPanel();

        this.restartPanel = new RestartCommandComponent(RESTART_PANEL, this,
                this.telecomStrategy.getRestartStrategy().getClass()
                        .getSimpleName(), this);
        this.rwPanel = new RandomWalkCommandComponent(RW_PANEL, this);
        this.cleanPanel = new CleanCommandComponent(CLEAN_PANEL, this);
        this.phasePanel = new PhaseCommandComponent(PHASE_PANEL, this,
                this.telecomStrategy.getPhaseSelectionStrategy().getClass()
                        .getSimpleName());
        this.simplifierPanel = new SimplifierCommandComponent(SIMPLIFIER_PANEL,
                this);
        this.hotSolverPanel = new HotSolverCommandComponent(HOT_SOLVER_PANEL,
                this);

        JScrollPane scrollPane = new JScrollPane(this.console);

        scrollPane.setPreferredSize(new Dimension(400, 200));
        scrollPane.getVerticalScrollBar().setValue(
                scrollPane.getVerticalScrollBar().getMaximum());

        MyTabbedPane tabbedPane = new MyTabbedPane();

        JPanel solverBigPanel = new JPanel();
        solverBigPanel
                .setLayout(new BoxLayout(solverBigPanel, BoxLayout.Y_AXIS));
        solverBigPanel.add(this.instancePanel);
        solverBigPanel.add(this.choixSolverPanel);

        tabbedPane.addTab("Solver", null, solverBigPanel,
                "instance & solver options");

        JPanel restartBigPanel = new JPanel();
        restartBigPanel.setLayout(new GridBagLayout());

        GridBagConstraints cRestart = new GridBagConstraints();
        cRestart.anchor = GridBagConstraints.PAGE_START;
        cRestart.fill = GridBagConstraints.HORIZONTAL;
        cRestart.weightx = 1;
        cRestart.weighty = 1;

        restartBigPanel.add(this.restartPanel, cRestart);

        tabbedPane.addTab("Restart", null, restartBigPanel,
                "restart strategy & options");

        JPanel rwPhaseBigPanel = new JPanel();
        rwPhaseBigPanel.setLayout(new GridBagLayout());

        GridBagConstraints cP = new GridBagConstraints();
        cP.anchor = GridBagConstraints.PAGE_START;
        cP.fill = GridBagConstraints.HORIZONTAL;
        cP.weightx = 1;
        cP.weighty = 1;

        JPanel tmpPhasePanel = new JPanel();
        tmpPhasePanel.setLayout(new GridBagLayout());
        GridBagConstraints cPhase = new GridBagConstraints();
        cPhase.fill = GridBagConstraints.HORIZONTAL;
        cPhase.weightx = 1;
        cPhase.weighty = .2;

        tmpPhasePanel.add(this.rwPanel, cPhase);

        cPhase.gridy = 1;
        cPhase.weighty = .2;
        tmpPhasePanel.add(this.phasePanel, cPhase);

        cPhase.gridy = 2;
        tmpPhasePanel.add(this.hotSolverPanel, cPhase);

        rwPhaseBigPanel.add(tmpPhasePanel, cP);

        tabbedPane.addTab("Heuristics", null, rwPhaseBigPanel,
                "random walk and phase strategy");

        JPanel clausesBigPanel = new JPanel();
        clausesBigPanel.setLayout(new GridBagLayout());

        GridBagConstraints cC = new GridBagConstraints();
        cC.anchor = GridBagConstraints.PAGE_START;
        cC.fill = GridBagConstraints.HORIZONTAL;
        cC.weightx = 1;
        cC.weighty = 1;

        JPanel tmpClausesPanel = new JPanel();
        tmpClausesPanel.setLayout(new GridBagLayout());
        GridBagConstraints cClauses = new GridBagConstraints();
        cClauses.fill = GridBagConstraints.HORIZONTAL;
        cClauses.weightx = 1;
        cClauses.weighty = .2;

        tmpClausesPanel.add(this.cleanPanel, cClauses);

        cClauses.gridy = 1;
        tmpClausesPanel.add(this.simplifierPanel, cClauses);

        clausesBigPanel.add(tmpClausesPanel, cC);

        tabbedPane.addTab("Learned Constraints", null, clausesBigPanel,
                "deletion and simplification strategy");

        this.aboutSolverPanel = new JPanel();
        this.textArea = new JTextArea("No solver is running at the moment");
        this.textArea.setColumns(50);
        this.aboutSolverPanel.add(this.textArea);

        tabbedPane.addTab("About Solver", null, this.aboutSolverPanel,
                "information about solver");

        this.add(tabbedPane);
        this.add(scrollPane);

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

    private void createInstancePanel() {
        this.instancePanel = new JPanel();

        this.instancePanel.setName(INSTANCE_PANEL);
        this.instancePanel.setBorder(new CompoundBorder(new TitledBorder(null,
                this.instancePanel.getName(), TitledBorder.LEFT,
                TitledBorder.TOP), BORDER5));

        this.instancePanel.setLayout(new BorderLayout(0, 0));

        this.instanceLabel = new JLabel(INSTANCE);
        this.instancePathField = new JTextField(20);
        this.instancePathField.setText(this.instancePath);

        this.instanceLabel.setLabelFor(this.instancePathField);

        this.browseButton = new JButton(BROWSE);

        this.browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openFileChooser();
                updateListOfSolvers();
            }
        });

        this.optimisationModeCB = new JCheckBox(OPTMIZATION_MODE);
        this.optimisationModeCB.setSelected(this.optimizationMode);

        this.equivalenceCB = new JCheckBox(EQUIVALENCE);
        this.equivalenceCB.setSelected(this.equivalenceMode);

        this.lowerCB = new JCheckBox(LOWER);
        this.lowerCB.setSelected(this.lowerMode);

        JPanel tmpPanel11 = new JPanel();
        tmpPanel11.add(this.instanceLabel);
        tmpPanel11.add(this.instancePathField);
        tmpPanel11.add(this.browseButton);

        JPanel tmpPanel12 = new JPanel();
        tmpPanel12.setLayout(new BoxLayout(tmpPanel12, BoxLayout.Y_AXIS));
        tmpPanel12.add(this.optimisationModeCB);
        tmpPanel12.add(this.equivalenceCB);
        tmpPanel12.add(this.lowerCB);

        this.optimisationModeCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setOptimisationMode(optimisationModeCB.isSelected());
                log("use optimization mode: "
                        + DetailedCommandPanel.this.optimizationMode);
                updateListOfSolvers();
            }
        });

        this.equivalenceCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                equivalenceMode = equivalenceCB.isSelected();
            }
        });

        this.lowerCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lowerMode = lowerCB.isSelected();
            }
        });

        instancePanel.setLayout(new BoxLayout(instancePanel, BoxLayout.Y_AXIS));

        instancePanel.add(tmpPanel11);
        instancePanel.add(tmpPanel12);
    }

    private void createChoixSolverPanel() {
        this.choixSolverPanel = new JPanel();

        this.choixSolverPanel.setName(CHOIX_SOLVER_PANEL);
        this.choixSolverPanel.setBorder(new CompoundBorder(new TitledBorder(
                null, this.choixSolverPanel.getName(), TitledBorder.LEFT,
                TitledBorder.TOP), BORDER5));

        this.choixSolverPanel.setLayout(new BoxLayout(choixSolverPanel,
                BoxLayout.Y_AXIS));

        this.choixSolver = new JLabel(CHOIX_SOLVER);

        this.listeSolvers = new JComboBox();

        updateListOfSolvers();

        JPanel tmpPanel1 = new JPanel();
        tmpPanel1.add(this.choixSolver);
        tmpPanel1.add(this.listeSolvers);

        this.startStopButton = new JButton(START);

        this.startStopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                manageStartStopButton();
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

        JPanel tmpPanel = new JPanel();
        tmpPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        tmpPanel.setName("Cardinality Constraints Encodings");
        tmpPanel.setBorder(new CompoundBorder(new TitledBorder(null, tmpPanel
                .getName(), TitledBorder.LEFT, TitledBorder.TOP), BORDER5));

        JLabel atMostKLabel = new JLabel(AT_MOST_K);
        atMostKCB = new JComboBox(new DefaultComboBoxModel(getListOfEncodings(
                AT_MOST_K).toArray()));

        JLabel atMost1Label = new JLabel(AT_MOST_1);
        atMost1CB = new JComboBox(new DefaultComboBoxModel(getListOfEncodings(
                AT_MOST_1).toArray()));

        JLabel exactlyKLabel = new JLabel(EXACTLY_K);
        exactlyKCB = new JComboBox(new DefaultComboBoxModel(getListOfEncodings(
                EXACTLY_K).toArray()));

        JLabel exactly1Label = new JLabel(EXACTLY_1);
        exactly1CB = new JComboBox(new DefaultComboBoxModel(getListOfEncodings(
                EXACTLY_1).toArray()));

        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.2;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_END;

        tmpPanel.add(atMostKLabel, c);

        c.gridx = 0;
        c.gridy = 1;
        tmpPanel.add(atMost1Label, c);

        c.gridx = 2;
        c.gridy = 1;
        tmpPanel.add(exactly1Label, c);

        c.gridx = 2;
        c.gridy = 0;
        tmpPanel.add(exactlyKLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0.8;
        c.gridx = 1;
        c.gridy = 0;
        tmpPanel.add(atMostKCB, c);

        c.gridx = 3;
        c.gridy = 0;
        tmpPanel.add(exactlyKCB, c);

        c.gridx = 1;
        c.gridy = 1;
        tmpPanel.add(atMost1CB, c);

        c.gridx = 3;
        c.gridy = 1;
        tmpPanel.add(exactly1CB, c);

        JPanel tmpPanel2 = new JPanel();
        tmpPanel2.setLayout(new GridBagLayout());

        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.LINE_START;
        c2.fill = GridBagConstraints.NONE;
        c2.weightx = 1;
        c2.gridx = 0;

        tmpPanel2.setName(CHOOSE_START_CONFIG);
        tmpPanel2.setBorder(new CompoundBorder(new TitledBorder(null, tmpPanel2
                .getName(), TitledBorder.LEFT, TitledBorder.TOP), BORDER5));

        this.solverLineParamLineRadio = new JRadioButton(
                SOLVER_LINE_PARAM_LINE_CONFIG);
        this.solverLineParamRemoteRadio = new JRadioButton(
                SOLVER_LINE_PARAM_REMOTE_CONFIG);
        this.solverListParamRemoteRadio = new JRadioButton(
                SOLVER_LIST_PARAM_REMOTE_CONFIG);
        this.solverListParamListRadio = new JRadioButton(
                SOLVER_LIST_PARAM_LIST_CONFIG);

        ButtonGroup solverConfigGroup = new ButtonGroup();
        solverConfigGroup.add(this.solverLineParamLineRadio);
        solverConfigGroup.add(this.solverLineParamRemoteRadio);
        solverConfigGroup.add(this.solverListParamListRadio);
        solverConfigGroup.add(this.solverListParamRemoteRadio);

        this.solverListParamListRadio.setSelected(true);
        this.startConfig = StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT;

        c2.gridy = 0;
        tmpPanel2.add(this.solverLineParamLineRadio, c2);
        c2.gridy = 1;
        tmpPanel2.add(this.solverLineParamRemoteRadio, c2);
        c2.gridy = 2;
        tmpPanel2.add(this.solverListParamListRadio, c2);
        c2.gridy = 3;
        tmpPanel2.add(this.solverListParamRemoteRadio, c2);

        JPanel tmpPanel3 = new JPanel();
        tmpPanel3.setLayout(new FlowLayout());
        tmpPanel3.add(this.startStopButton);
        tmpPanel3.add(this.pauseButton);

        this.choixSolverPanel.add(tmpPanel1);
        this.choixSolverPanel.add(tmpPanel);
        this.choixSolverPanel.add(tmpPanel2);
        this.choixSolverPanel.add(tmpPanel3);

        atMostKCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                encodingPolicy.setAtMostKEncoding((EncodingStrategy) atMostKCB
                        .getSelectedItem());
            }
        });

        atMost1CB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                encodingPolicy
                        .setAtMostOneEncoding((EncodingStrategy) atMost1CB
                                .getSelectedItem());
            }
        });

        exactlyKCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                encodingPolicy
                        .setExactlyKEncoding((EncodingStrategy) exactlyKCB
                                .getSelectedItem());
            }
        });

        exactly1CB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                encodingPolicy
                        .setExactlyOneEncoding((EncodingStrategy) exactly1CB
                                .getSelectedItem());
            }
        });

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

    }

    public void manageStartStopButton() {
        if (DetailedCommandPanel.this.startStopButton.getText().equals(START)) {
            launchSolverWithConfigs();
            DetailedCommandPanel.this.pauseButton.setEnabled(true);
            setInstancePanelEnabled(false);
            DetailedCommandPanel.this.restartPanel.setRestartPanelEnabled(true);
            DetailedCommandPanel.this.rwPanel.setRWPanelEnabled(true);
            DetailedCommandPanel.this.cleanPanel.setCleanPanelEnabled(true);
            DetailedCommandPanel.this.cleanPanel
                    .setCleanPanelOriginalStrategyEnabled(true);
            DetailedCommandPanel.this.phasePanel.setPhasePanelEnabled(true);
            setChoixSolverPanelEnabled(false);
            DetailedCommandPanel.this.simplifierPanel
                    .setSimplifierPanelEnabled(true);
            DetailedCommandPanel.this.hotSolverPanel
                    .setKeepSolverHotPanelEnabled(true);
            DetailedCommandPanel.this.startStopButton.setText(STOP);
            solverListParamListRadio.setSelected(true);
            startConfig = StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT;
            getThis().paintAll(getThis().getGraphics());
            DetailedCommandPanel.this.frame
                    .setActivateTracingEditableUnderCondition(false);
            frame.setActivateRadioTracing(false);
        } else {

            ((ISolver) DetailedCommandPanel.this.problem).expireTimeout();
            DetailedCommandPanel.this.pauseButton.setEnabled(false);
            log("Asked the solver to stop");
            setInstancePanelEnabled(true);
            setChoixSolverPanelEnabled(true);
            DetailedCommandPanel.this.startStopButton.setText(START);
            getThis().paintAll(getThis().getGraphics());
            DetailedCommandPanel.this.frame.setActivateTracingEditable(true);
            frame.setActivateRadioTracing(true);
        }
    }

    public String getStartStopText() {
        return this.startStopButton.getText();
    }

    public void setOptimisationMode(boolean optimizationMode) {
        this.optimizationMode = optimizationMode;
        this.optimisationModeCB.setSelected(optimizationMode);
    }

    public void launchSolverWithConfigs() {
        ICDCL<?> cdclSolver;
        ASolverFactory<?> factory;
        String[] partsSelectedSolver;
        IOrder order;
        double proba;

        String selectedSolver;

        switch (startConfig) {
        case SOLVER_LIST_PARAM_REMOTE:
            selectedSolver = (String) this.listeSolvers.getSelectedItem();
            partsSelectedSolver = selectedSolver.split("\\.");

            assert partsSelectedSolver.length == 2;
            assert partsSelectedSolver[0].equals(MINISAT_PREFIX)
                    || partsSelectedSolver[0].equals(PB_PREFIX)
                    || partsSelectedSolver[0].equals(MAXSAT_PREFIX);

            if (partsSelectedSolver[0].equals(MINISAT_PREFIX)) {
                factory = org.sat4j.minisat.SolverFactory.instance();
            } else if (partsSelectedSolver[0].equals(PB_PREFIX)) {
                factory = org.sat4j.pb.SolverFactory.instance();
            } else {
                factory = org.sat4j.maxsat.SolverFactory.instance();
            }
            this.solver = (ICDCL<?>) factory
                    .createSolverByName(partsSelectedSolver[1]);

            cdclSolver = (ICDCL<?>) this.solver.getSolvingEngine();

            this.telecomStrategy.setSolver(cdclSolver);

            cdclSolver.setRestartStrategy(this.telecomStrategy);
            cdclSolver.setOrder(this.randomWalk);
            cdclSolver.getOrder().setPhaseSelectionStrategy(
                    this.telecomStrategy);

            this.restartPanel.hasClickedOnRestart();
            this.rwPanel.hasClickedOnApplyRW();
            this.phasePanel.hasClickedOnApplyPhase();
            this.simplifierPanel.hasClickedOnApplySimplification();
            break;

        case SOLVER_LINE_PARAM_LINE:
            this.solver = Solvers.configureSolver(this.commandLines, this);

            cdclSolver = (ICDCL<?>) this.solver.getSolvingEngine();

            this.telecomStrategy.setSolver(cdclSolver);
            this.telecomStrategy.setRestartStrategy(cdclSolver
                    .getRestartStrategy());
            cdclSolver.setRestartStrategy(this.telecomStrategy);

            this.restartPanel.setCurrentRestart(this.telecomStrategy
                    .getRestartStrategy().getClass().getSimpleName());

            order = cdclSolver.getOrder();

            proba = 0;

            if (this.optimizationMode) {
                if (order instanceof RandomWalkDecoratorObjective) {
                    this.randomWalk = (RandomWalkDecorator) order;
                    proba = this.randomWalk.getProbability();
                } else if (order instanceof VarOrderHeapObjective) {
                    this.randomWalk = new RandomWalkDecoratorObjective(
                            (VarOrderHeapObjective) order, 0);
                }
            } else if (cdclSolver.getOrder() instanceof RandomWalkDecorator) {
                this.randomWalk = (RandomWalkDecorator) order;
                proba = this.randomWalk.getProbability();
            } else {
                this.randomWalk = new RandomWalkDecorator((VarOrderHeap) order,
                        0);
            }

            this.randomWalk.setProbability(proba);
            this.rwPanel.setProba(proba);
            cdclSolver.setOrder(this.randomWalk);
            this.telecomStrategy.setPhaseSelectionStrategy(cdclSolver
                    .getOrder().getPhaseSelectionStrategy());
            cdclSolver.getOrder().setPhaseSelectionStrategy(
                    this.telecomStrategy);
            this.phasePanel.setPhaseListSelectedItem(this.telecomStrategy
                    .getPhaseSelectionStrategy().getClass().getSimpleName());
            this.simplifierPanel.setSelectedSimplification(cdclSolver
                    .getSimplifier().toString());

            this.phasePanel.repaint();
            break;

        case SOLVER_LINE_PARAM_REMOTE:
            this.solver = Solvers.configureSolver(this.commandLines, this);

            cdclSolver = (ICDCL<?>) this.solver.getSolvingEngine();

            cdclSolver.setRestartStrategy(this.telecomStrategy);
            cdclSolver.setOrder(this.randomWalk);
            cdclSolver.getOrder().setPhaseSelectionStrategy(
                    this.telecomStrategy);

            this.restartPanel.hasClickedOnRestart();
            this.rwPanel.hasClickedOnApplyRW();
            this.phasePanel.hasClickedOnApplyPhase();
            this.simplifierPanel.hasClickedOnApplySimplification();
            break;

        default:
            selectedSolver = (String) this.listeSolvers.getSelectedItem();
            partsSelectedSolver = selectedSolver.split("\\.");

            assert partsSelectedSolver.length == 2;
            assert partsSelectedSolver[0].equals(MINISAT_PREFIX)
                    || partsSelectedSolver[0].equals(PB_PREFIX)
                    || partsSelectedSolver[0].equals(MAXSAT_PREFIX);

            if (partsSelectedSolver[0].equals(MINISAT_PREFIX)) {
                factory = org.sat4j.minisat.SolverFactory.instance();
            } else if (partsSelectedSolver[0].equals(PB_PREFIX)) {
                factory = org.sat4j.pb.SolverFactory.instance();
            } else {
                factory = org.sat4j.maxsat.SolverFactory.instance();
            }

            this.solver = factory.createSolverByName(partsSelectedSolver[1]);

            cdclSolver = (ICDCL<?>) this.solver.getSolvingEngine();

            this.telecomStrategy.setSolver(cdclSolver);
            this.telecomStrategy.setRestartStrategy(cdclSolver
                    .getRestartStrategy());
            cdclSolver.setRestartStrategy(this.telecomStrategy);

            this.restartPanel.setCurrentRestart(this.telecomStrategy
                    .getRestartStrategy().getClass().getSimpleName());

            order = cdclSolver.getOrder();

            proba = 0;

            if (this.optimizationMode) {
                if (order instanceof RandomWalkDecoratorObjective) {
                    this.randomWalk = (RandomWalkDecorator) order;
                    proba = this.randomWalk.getProbability();
                } else if (order instanceof VarOrderHeapObjective) {
                    this.randomWalk = new RandomWalkDecoratorObjective(
                            (VarOrderHeapObjective) order, 0);
                }
            } else if (cdclSolver.getOrder() instanceof RandomWalkDecorator) {
                this.randomWalk = (RandomWalkDecorator) order;
                proba = this.randomWalk.getProbability();
            } else {
                this.randomWalk = new RandomWalkDecorator((VarOrderHeap) order,
                        0);
            }

            this.randomWalk.setProbability(proba);
            this.rwPanel.setProba(proba);

            cdclSolver.setOrder(this.randomWalk);

            this.telecomStrategy.setPhaseSelectionStrategy(cdclSolver
                    .getOrder().getPhaseSelectionStrategy());
            this.phasePanel.setPhaseListSelectedItem(this.telecomStrategy
                    .getPhaseSelectionStrategy().getClass().getSimpleName());
            cdclSolver.getOrder().setPhaseSelectionStrategy(
                    this.telecomStrategy);
            this.simplifierPanel.setSelectedSimplification(cdclSolver
                    .getSimplifier().toString());
            break;
        }

        this.whereToWriteFiles = this.instancePath;

        if (this.ramdisk.length() > 0) {
            String[] instancePathSplit = this.instancePath.split("/");
            this.whereToWriteFiles = this.ramdisk + "/"
                    + instancePathSplit[instancePathSplit.length - 1];
        }

        this.solver.setVerbose(true);
        initSearchListeners();
        cdclSolver.setLogger(this);

        try {
            switch (problemType) {
            case PB_OPT:
                this.solver = new ClausalConstraintsDecorator(
                        (IPBSolver) this.solver, this.encodingPolicy);
                if (lowerMode) {
                    this.solver = new ConstraintRelaxingPseudoOptDecorator(
                            (IPBSolver) solver);
                } else {
                    this.solver = new PseudoOptDecorator((IPBSolver) solver);
                }

                this.reader = createReader(this.solver, this.instancePath);
                this.problem = this.reader.parseInstance(this.instancePath);
                this.problem = new OptToPBSATAdapter(
                        (IOptimizationProblem) this.problem);
                break;
            case CNF_MAXSAT:
            case WCNF_MAXSAT:
                this.solver = new ClausalConstraintsDecorator(
                        (IPBSolver) this.solver, this.encodingPolicy);
                this.solver = new WeightedMaxSatDecorator(
                        (IPBSolver) this.solver, equivalenceMode);

                this.reader = createReader(this.solver, this.instancePath);
                this.problem = this.reader.parseInstance(this.instancePath);

                if (lowerMode) {
                    this.problem = new ConstraintRelaxingPseudoOptDecorator(
                            (WeightedMaxSatDecorator) this.problem);
                } else {
                    this.problem = new PseudoOptDecorator(
                            (WeightedMaxSatDecorator) this.problem, false,
                            false);
                }

                this.problem = new OptToPBSATAdapter(
                        (IOptimizationProblem) this.problem);
                break;
            case PB_SAT:
                this.solver = new ClausalConstraintsDecorator(
                        (IPBSolver) this.solver, this.encodingPolicy);
                this.reader = createReader(this.solver, this.instancePath);
                this.problem = this.reader.parseInstance(this.instancePath);
                break;
            case CNF_SAT:
            default:
                this.solver = new ClausalCardinalitiesDecorator<ISolver>(
                        this.solver, this.encodingPolicy);
                this.reader = createReader(this.solver, this.instancePath);
                this.problem = this.reader.parseInstance(this.instancePath);
                break;
            }

        } catch (FileNotFoundException e) {
            log(e.getMessage());
        } catch (ParseFormatException e) {
            log(e.getMessage());
        } catch (IOException e) {
            log(e.getMessage());
        } catch (ContradictionException e) {
            log("Unsatisfiable (trivial)!");
            return;
        }

        log("# Started solver "
                + this.solver.getSolvingEngine().getClass().getSimpleName());
        log("# on instance " + this.instancePath);
        log("# Optimisation = " + this.optimizationMode);
        log("# Restart strategy = "
                + this.telecomStrategy.getRestartStrategy().getClass()
                        .getSimpleName());
        log("# Random walk probability = " + this.randomWalk.getProbability());
        log("# variables : " + this.solver.nVars());

        Thread solveurThread = new Thread() {
            @Override
            public void run() {

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
            }
        };
        solveurThread.start();

        if (this.isPlotActivated) {
            this.solverVisu.setnVar(this.solver.nVars());
            startVisu();
        }
    }

    public void initSearchListeners() {
        List<SearchListener<ISolverService>> listeners = new ArrayList<SearchListener<ISolverService>>();

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

        this.solver.setSearchListener(new MultiTracing<ISolverService>(listeners));

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

    public SolverStats getSolverStats() {
        return this.telecomStrategy.getSolverStats();
    }

    public void init(SearchParams params, SolverStats stats) {
        this.telecomStrategy.init(params, stats);
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
        ((ICDCL<?>) this.solver.getSolvingEngine())
                .setLearnedConstraintsDeletionStrategy(this.telecomStrategy,
                        type);
        log("Changed clauses evaluation type to " + type);
    }

    public LearnedConstraintsEvaluationType getLearnedConstraintsEvaluationType() {
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
        ((ICDCL<?>) this.solver.getSolvingEngine()).setSimplifier(type);
        log("Told the solver to use " + type);
    }

    public List<String> getListOfSolvers() {
        ASolverFactory<?> factory;

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

        factory = org.sat4j.maxsat.SolverFactory.instance();
        for (String s : factory.solverNames()) {
            result.add(MAXSAT_PREFIX + "." + s);
        }

        Collections.sort(result);

        return result;
    }

    public List<String> getListOfSatSolvers() {
        ASolverFactory<?> factory;

        List<String> result = new ArrayList<String>();

        factory = org.sat4j.minisat.SolverFactory.instance();
        for (String s : factory.solverNames()) {
            result.add(MINISAT_PREFIX + "." + s);
        }
        Collections.sort(result);

        return result;
    }

    public List<String> getListOfPBSolvers() {
        ASolverFactory<?> factory;

        List<String> result = new ArrayList<String>();

        factory = org.sat4j.pb.SolverFactory.instance();
        for (String s : factory.solverNames()) {
            result.add(PB_PREFIX + "." + s);
        }
        Collections.sort(result);

        return result;
    }

    public List<String> getListOfMaxsatSolvers() {
        ASolverFactory<?> factory;

        List<String> result = new ArrayList<String>();

        factory = org.sat4j.pb.SolverFactory.instance();
        for (String s : factory.solverNames()) {
            result.add(MAXSAT_PREFIX + "." + s);
        }
        Collections.sort(result);

        return result;
    }

    public List<EncodingStrategy> getListOfEncodings(String typeOfConstraint) {
        List<EncodingStrategy> v = new ArrayList<EncodingStrategy>();

        v.add(EncodingStrategy.NATIVE);

        if (typeOfConstraint.equals(AT_MOST_K)
                || typeOfConstraint.equals(AT_MOST_1)) {
            v.add(EncodingStrategy.BINARY);
            v.add(EncodingStrategy.BINOMIAL);
            v.add(EncodingStrategy.COMMANDER);
        }
        if (typeOfConstraint.equals(AT_MOST_K)) {
            v.add(EncodingStrategy.SEQUENTIAL);
        }
        if (typeOfConstraint.equals(AT_MOST_1)
                || typeOfConstraint.equals(EXACTLY_1)) {
            v.add(EncodingStrategy.LADDER);
        }
        if (typeOfConstraint.equals(AT_MOST_1)) {
            v.add(EncodingStrategy.PRODUCT);
        }

        return v;
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

    protected Reader createReader(ISolver theSolver, String problemname) {
        InstanceReader instance = new InstanceReader(theSolver);
        switch (problemType) {
        case CNF_MAXSAT:
        case WCNF_MAXSAT:
            instance = new MSInstanceReader((WeightedMaxSatDecorator) theSolver);
            break;
        case PB_OPT:
        case PB_SAT:
            instance = new PBInstanceReader((IPBSolver) theSolver);
            break;
        case CNF_SAT:
            instance = new InstanceReader(theSolver);
            break;
        }

        return instance;
    }

    public void updateListOfSolvers() {
        List<String> theList = new ArrayList<String>();
        String defaultSolver = "";

        if (instancePath == null || instancePath.length() == 0) {
            theList = getListOfSolvers();
            defaultSolver = "minisat.Default";
            problemType = ProblemType.CNF_SAT;
            equivalenceCB.setEnabled(false);
            lowerCB.setEnabled(false);
        } else if (instancePath.endsWith(".cnf")) {
            optimisationModeCB.setEnabled(true);
            if (optimizationMode) {
                theList.addAll(getListOfMaxsatSolvers());
                theList.addAll(getListOfPBSolvers());
                defaultSolver = "maxsat.Default";
                equivalenceCB.setEnabled(true);
                lowerCB.setEnabled(true);
                problemType = ProblemType.CNF_MAXSAT;
                log("cnf file + opt => pb/maxsat solvers");
            } else {
                theList.addAll(getListOfSatSolvers());
                theList.addAll(getListOfPBSolvers());
                defaultSolver = "minisat.Default";
                log("cnf file + non opt => sat/pb solvers");
                problemType = ProblemType.CNF_SAT;
                equivalenceCB.setEnabled(false);
                lowerCB.setEnabled(false);
            }
        } else if (instancePath.endsWith(".opb")) {
            optimisationModeCB.setEnabled(true);
            theList.addAll(getListOfPBSolvers());
            defaultSolver = "pb.Default";
            if (optimizationMode) {
                problemType = ProblemType.PB_OPT;
                equivalenceCB.setEnabled(true);
                lowerCB.setEnabled(true);
            } else {
                problemType = ProblemType.PB_SAT;
                equivalenceCB.setEnabled(false);
                lowerCB.setEnabled(false);
            }
            log("opb file => pb solvers");
        } else if (instancePath.endsWith(".wcnf")) {
            equivalenceCB.setEnabled(true);
            lowerCB.setEnabled(true);
            theList.addAll(getListOfMaxsatSolvers());
            theList.addAll(getListOfPBSolvers());
            defaultSolver = "maxsat.Default";
            optimisationModeCB.setSelected(true);
            optimisationModeCB.setEnabled(false);
            problemType = ProblemType.WCNF_MAXSAT;
            log("wcnf file => pb/maxsat solvers");
        }
        this.listeSolvers.setModel(new DefaultComboBoxModel(theList.toArray()));
        this.listeSolvers.setSelectedItem(defaultSolver);
        this.choixSolverPanel.repaint();
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
        this.conflictCounter = 0;
    }

    public void assuming(int p) {
    }

    public void propagating(int p) {
        this.end = System.currentTimeMillis();
        if (this.end - this.begin >= 2000) {
            long tmp = this.end - this.begin;

            this.cleanPanel.setSpeedLabeltext(this.propagationsCounter / tmp
                    * 1000 + "");

            this.begin = System.currentTimeMillis();
            this.propagationsCounter = 0;
        }
        this.propagationsCounter++;
    }

    public void enqueueing(int p, IConstr reason) {        
    }
    
    public void backtracking(int p) {
    }

    public void adding(int p) {
    }

    public void learn(IConstr c) {
    }

    public void delete(IConstr c) {
    }

    public void learnUnit(int p) {        
    }
    
    public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
        this.conflictCounter++;
    }

    public void conflictFound(int p) {
    }

    public void solutionFound(int[] model,RandomAccessModel lazyModel) {
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
        if (this.end != this.begin) {
            this.cleanPanel.setSpeedLabeltext(this.propagationsCounter
                    / (this.end - this.begin) * 1000 + "");
        }
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
            if (this.getTabCount() == 5 && index == 4) {
                if (DetailedCommandPanel.this.solver != null
                        && DetailedCommandPanel.this.startStopButton.getText()
                                .equals(STOP)) {
                    String s = DetailedCommandPanel.this.solver.toString();
                    String res = DetailedCommandPanel.this.solver.toString();
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
            }
            super.setSelectedIndex(index);
        };
    }

}
