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

/**
 * This class is used to launch the SAT solvers from the command line. It is
 * compliant with the SAT competition (www.satcompetition.org) I/O format. The
 * launcher is to be used as follows:
 * 
 * <pre>
 *                [solvername] filename [key=value]*
 * </pre>
 * 
 * If no solver name is given, then the default solver of the solver factory is
 * used (@see org.sat4j.core.ASolverFactory#defaultSolver()).
 * 
 * @author sroussel
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.AbstractLauncher;
import org.sat4j.ExitCode;
import org.sat4j.core.ASolverFactory;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.SimplificationType;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.core.IPBCDCLSolver;
import org.sat4j.pb.orders.RandomWalkDecoratorObjective;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ConflictDepthTracing;
import org.sat4j.tools.ConflictLevelTracing;
import org.sat4j.tools.DecisionTracing;
import org.sat4j.tools.DotSearchTracing;
import org.sat4j.tools.LearnedClausesSizeTracing;
import org.sat4j.tools.MultiTracing;

public class Lanceur extends AbstractLauncher {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String CURRENT_OPTIMUM_VALUE_PREFIX = "o "; //$NON-NLS-1$

	private final static String PACKAGE_ORDERS = "org.sat4j.minisat.orders";
	private final static String PACKAGE_LEARNING = "org.sat4j.minisat.learning";
	private final static String PACKAGE_RESTARTS = "org.sat4j.minisat.restarts";
	private final static String PACKAGE_PHASE = "org.sat4j.minisat.orders";
	private final static String PACKAGE_PARAMS = "org.sat4j.minisat.core";

	private final static String ORDERS = "ORDERS";
	private final static String LEARNING = "LEARNING";
	private final static String RESTARTS = "RESTARTS";
	private final static String PHASE = "PHASE";
	private final static String PARAMS = "PARAMS";
	private final static String SIMP = "SIMP";

	private final static String RESTART_STRATEGY_NAME = "org.sat4j.minisat.core.RestartStrategy";
	private final static String ORDER_NAME = "org.sat4j.minisat.core.IOrder";
	private final static String LEARNING_NAME = "org.sat4j.minisat.core.LearningStrategy";
	private final static String PHASE_NAME = "org.sat4j.minisat.core.IPhaseSelectionStrategy";
	private final static String PARAMS_NAME = "org.sat4j.minisat.core.SearchParams";

	private final static Map<String, String> qualif = new HashMap<String, String>();
	static {
		qualif.put(ORDERS, PACKAGE_ORDERS);
		qualif.put(LEARNING, PACKAGE_LEARNING);
		qualif.put(RESTARTS, PACKAGE_RESTARTS);
		qualif.put(PHASE, PACKAGE_PHASE);
		qualif.put(PARAMS, PACKAGE_PARAMS);
	}
	private boolean incomplete = false;

	private boolean isModeOptimization = false;

	private IProblem problem;
	private ICDCL cdclSolver;

	private boolean modeTracing = false;

	private boolean launchRemoteControl;

	static AbstractLauncher lanceur;

	public static void main(final String[] args) {
		lanceur = new Lanceur();
		lanceur.run(args);

	}

	protected ASolverFactory<ISolver> factory;

	private String filename;

	private int k = -1;

	@SuppressWarnings("nls")
	private Options createCLIOptions() {
		Options options = new Options();
		options.addOption("l", "library", true,
				"specifies the name of the library used (minisat by default)");
		options.addOption("s", "solver", true,
				"specifies the name of a prebuilt solver from the library");
		options.addOption("S", "Solver", true,
				"setup a solver using a solver config string");
		options.addOption("t", "timeout", true,
				"specifies the timeout (in seconds)");
		options.addOption("T", "timeoutms", true,
				"specifies the timeout (in milliseconds)");
		options.addOption("C", "conflictbased", false,
				"conflict based timeout (for deterministic behavior)");
		options.addOption("d", "dot", true,
				"creates a sat4j.dot file in current directory representing the search");
		options.addOption("f", "filename", true,
				"specifies the file to use (in conjunction with -d for instance)");
		options.addOption("m", "mute", false, "Set launcher in silent mode");
		options.addOption("k", "kleast", true,
				"limit the search to models having at least k variables set to false");
		options.addOption("r", "trace", false,
				"traces the behavior of the solver");
		options.addOption("opt", "optimize", false,
				"uses solver in optimize mode instead of sat mode (default)");
		options.addOption("rw", "randomWalk", true,
				"specifies the random walk probability ");
		options.addOption("remote", "remoteControl", false,
				"launches remote control");
		options.addOption("H", "hot", false,
				"keep the solver hot (do not reset heuristics) when a model is found");
		options.addOption("y", "simplify", false,
				"simplify the set of clauses is possible");
		Option op = options.getOption("l");
		op.setArgName("libname");
		op = options.getOption("s");
		op.setArgName("solvername");
		op = options.getOption("S");
		op.setArgName("solverStringDefinition");
		op = options.getOption("t");
		op.setArgName("number");
		op = options.getOption("T");
		op.setArgName("number");
		op = options.getOption("C");
		op.setArgName("number");
		op = options.getOption("k");
		op.setArgName("number");
		op = options.getOption("d");
		op.setArgName("filename");
		op = options.getOption("f");
		op.setArgName("filename");
		op = options.getOption("r");
		op.setArgName("searchlistener");
		op = options.getOption("rw");
		op.setArgName("number");
		return options;
	}

	/**
	 * Configure the solver according to the command line parameters.
	 * 
	 * @param args
	 *            the command line
	 * @return a solver properly configured.
	 */
	@SuppressWarnings({ "nls", "unchecked" })
	@Override
	protected ICDCL configureSolver(String[] args) {
		Options options = createCLIOptions();
		if (args.length == 0) {
			HelpFormatter helpf = new HelpFormatter();
			helpf.printHelp("java -jar sat4j.jar", options, true);

			// log("Available solvers: "
			// + Arrays.asList(factory.solverNames()));
			// showAvailableLearning();
			// showAvailableOrders();
			// showAvailablePhase();
			// showAvailableRestarts();
			return null;
		}
		try {
			CommandLine cmd = new PosixParser().parse(options, args);

			if (cmd.hasOption("opt")) {
				isModeOptimization = true;
			}

			String framework = cmd.getOptionValue("l"); //$NON-NLS-1$
			if (isModeOptimization) {
				framework = "pb";
			} else if (framework == null) { //$NON-NLS-1$
				framework = "minisat";
			}

			try {
				Class<?> clazz = Class
						.forName("org.sat4j." + framework + ".SolverFactory"); //$NON-NLS-1$ //$NON-NLS-2$
				Class<?>[] params = {};
				Method m = clazz.getMethod("instance", params); //$NON-NLS-1$
				factory = (ASolverFactory) m.invoke(null, (Object[]) null);
			} catch (Exception e) { // DLB Findbugs warning ok
				log("Wrong framework: " + framework
						+ ". Using minisat instead.");
				factory = org.sat4j.minisat.SolverFactory.instance();
			}

			ICDCL asolver;
			if (cmd.hasOption("s")) {
				String solvername = cmd.getOptionValue("s");
				if (solvername == null) {
					log("No solver for option s. Launching default solver.");
					log("Available solvers: "
							+ Arrays.asList(factory.solverNames()));
					asolver = (Solver) factory.defaultSolver();
				} else {
					asolver = (Solver) factory.createSolverByName(solvername);
				}
			} else {
				asolver = (Solver) factory.defaultSolver();
			}

			if (cmd.hasOption("S")) {
				String configuredSolver = cmd.getOptionValue("S");
				if (configuredSolver == null) {
					stringUsage();
					return null;
				}
				asolver = configureFromString(configuredSolver, asolver);
			}

			if (cmd.hasOption("rw")) {
				double proba = Double.parseDouble(cmd.getOptionValue("rw"));
				IOrder order = asolver.getOrder();
				if (isModeOptimization
						&& order instanceof VarOrderHeapObjective) {
					order = new RandomWalkDecoratorObjective(
							(VarOrderHeapObjective) order, proba);
				} else {
					order = new RandomWalkDecorator((VarOrderHeap) order, proba);
				}
				asolver.setOrder(order);
			}

			launchRemoteControl = (cmd.hasOption("remote"));

			String timeout = cmd.getOptionValue("t");
			if (timeout == null) {
				timeout = cmd.getOptionValue("T");
				if (timeout != null) {
					asolver.setTimeoutMs(Long.parseLong(timeout));
				}
			} else {
				if (cmd.hasOption("C")) {
					asolver.setTimeoutOnConflicts(Integer.parseInt(timeout));
				} else {
					asolver.setTimeout(Integer.parseInt(timeout));
				}
			}
			filename = cmd.getOptionValue("f");

			if (cmd.hasOption("d")) {
				String dotfilename = null;
				if (filename != null) {
					dotfilename = cmd.getOptionValue("d");
				}
				if (dotfilename == null) {
					dotfilename = "sat4j.dot";
				}
				asolver.setSearchListener(new DotSearchTracing(dotfilename,
						null));
			}

			if (cmd.hasOption("m")) {
				setSilent(true);
			}

			if (cmd.hasOption("H")) {
				asolver.setKeepSolverHot(true);
			}

			if (cmd.hasOption("k")) {
				Integer myk = Integer.valueOf(cmd.getOptionValue("k"));
				if (myk != null) {
					k = myk.intValue();
				}
			}

			if (cmd.hasOption("y")) {
				asolver.setDBSimplificationAllowed(true);
			}
			
			if (isModeOptimization) {
				assert asolver instanceof IPBSolver;
				problem = new PseudoOptDecorator((IPBCDCLSolver) asolver);
			}

			int others = 0;
			String[] rargs = cmd.getArgs();
			if (filename == null && rargs.length > 0) {
				filename = rargs[others++];
			}

			if (cmd.hasOption("r")) {
				modeTracing = true;
				asolver.setSearchListener(new MultiTracing(
						new ConflictLevelTracing(filename + "-conflict-level"),
						new DecisionTracing(filename + "-decision-indexes"),
						new LearnedClausesSizeTracing(filename
								+ "-learned-clauses-size"),
						new ConflictDepthTracing(filename + "-conflict-depth")));
			}

			// use remaining data to configure the solver
			while (others < rargs.length) {
				String[] param = rargs[others].split("="); //$NON-NLS-1$
				assert param.length == 2;
				log("setting " + param[0] + " to " + param[1]); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					BeanUtils.setProperty(asolver, param[0], param[1]);
				} catch (Exception e) {
					log("Cannot set parameter : " //$NON-NLS-1$
							+ args[others]);
				}
				others++;
			}

			getLogWriter().println(asolver.toString(COMMENT_PREFIX)); //$NON-NLS-1$
			return asolver;
		} catch (ParseException e1) {
			HelpFormatter helpf = new HelpFormatter();
			helpf.printHelp("java -jar sat4j.jar", options, true);
			usage();
		}
		return null;
	}

	@Override
	protected Reader createReader(ISolver theSolver, String problemname) {
		if (theSolver instanceof IPBSolver) {
			return new PBInstanceReader((IPBSolver) theSolver);
		}
		return new InstanceReader(theSolver);
	}

	@Override
	public void displayLicense() {
		super.displayLicense();
		log("This software uses some libraries from the Jakarta Commons project. See jakarta.apache.org for details."); //$NON-NLS-1$
	}

	@Override
	public void usage() {
		factory = org.sat4j.minisat.SolverFactory.instance();
		// log("SAT");
		showAvailableSolvers(factory, "sat");
		log("-------------------");
		factory = (ASolverFactory) org.sat4j.pb.SolverFactory.instance();
		// log("PB");
		showAvailableSolvers(factory, "pb");
		showAvailableRestarts();
		showAvailableOrders();
		showAvailableLearning();
		showAvailablePhase();
		showParams();
		showSimplifiers();
		stringUsage();

	}

	@Override
	protected String getInstanceName(String[] args) {
		return filename;
	}

	@SuppressWarnings("unchecked")
	private final ICDCL configureFromString(String solverconfig, ICDCL theSolver) {
		// AFAIK, there is no easy way to solve parameterized problems
		// when building the solver at runtime.
		StringTokenizer stk = new StringTokenizer(solverconfig, ",");
		Properties pf = new Properties();
		String token;
		String[] couple;
		while (stk.hasMoreElements()) {
			token = stk.nextToken();
			couple = token.split("=");
			pf.setProperty(couple[0], couple[1]);
		}

		Solver aSolver = (Solver) theSolver;
		DataStructureFactory dsf = setupObject("DSF", pf);
		if (dsf != null) {
			theSolver.setDataStructureFactory(dsf);
		}
		LearningStrategy learning = setupObject(LEARNING, pf);
		if (learning != null) {
			theSolver.setLearner(learning);
			learning.setSolver(aSolver);
		}
		IOrder order = setupObject(ORDERS, pf);
		if (order != null) {
			theSolver.setOrder(order);
		}
		IPhaseSelectionStrategy pss = setupObject(PHASE, pf);
		if (pss != null) {
			theSolver.getOrder().setPhaseSelectionStrategy(pss);
		}
		RestartStrategy restarter = setupObject(RESTARTS, pf);
		if (restarter != null) {
			theSolver.setRestartStrategy(restarter);
		}
		String simp = pf.getProperty(SIMP);
		if (simp != null) {
			log("read " + simp);
			log("configuring " + SIMP);
			theSolver.setSimplifier(SimplificationType.valueOf(simp));
		}
		SearchParams params = setupObject(PARAMS, pf);
		if (params != null) {
			theSolver.setSearchParams(params);
		}
		String memory = pf.getProperty("MEMORY");
		if ("GLUCOSE".equalsIgnoreCase(memory)) {
			log("configuring MEMORY");
			theSolver.setLearnedConstraintsDeletionStrategy(aSolver.glucose);
		}
		return theSolver;
	}

	private void stringUsage() {
		log("Available building blocks: DSF, LEARNING, ORDERS, PHASE, RESTARTS, SIMP, PARAMS");
		log("Example: -S RESTARTS=LubyRestarts/factor:512,LEARNING=MiniSATLearning");
	}

	@SuppressWarnings("unchecked")
	private final <T> T setupObject(String component, Properties pf) {
		try {
			String configline = pf.getProperty(component);
			String qualification = qualif.get(component);

			if (configline == null) {
				return null;
			}
			if (qualification != null) {
				log("read " + qualification + "." + configline);
				if (configline.contains("Objective")
						&& qualification.contains("minisat")) {
					// log(qualification);
					qualification = qualification.replaceFirst("minisat", "pb");
				}
				configline = qualification + "." + configline;
			}

			log("configuring " + component);
			String[] config = configline.split("/");
			T comp = (T) Class.forName(config[0]).newInstance();
			for (int i = 1; i < config.length; i++) {
				String[] param = config[i].split(":"); //$NON-NLS-1$
				assert param.length == 2;
				try {
					// Check first that the property really exists
					BeanUtils.getProperty(comp, param[0]);
					BeanUtils.setProperty(comp, param[0], param[1]);
				} catch (Exception e) {
					log("Problem with component " + config[0] + " " + e);
				}
			}
			return comp;
		} catch (InstantiationException e) {
			log("Problem with component " + component + " " + e);
		} catch (IllegalAccessException e) {
			log("Problem with component " + component + " " + e);
		} catch (ClassNotFoundException e) {
			log("Problem with component " + component + " " + e);
		}
		return null;
	}

	@Override
	protected IProblem readProblem(String problemname)
			throws FileNotFoundException, ParseFormatException, IOException,
			ContradictionException {
		ISolver theSolver = (ISolver) super.readProblem(problemname);
		if (k > 0) {
			IVecInt literals = new VecInt();
			for (int i = 1; i <= theSolver.nVars(); i++) {
				literals.push(-i);
			}
			theSolver.addAtLeast(literals, k);
			log("Limiting solutions to those having at least " + k
					+ " variables assigned to false");
		}
		return theSolver;
	}

	@Override
	protected void solve(IProblem problem) throws TimeoutException {
		if (isModeOptimization) {
			boolean isSatisfiable = false;

			IOptimizationProblem optproblem = (IOptimizationProblem) problem;

			try {
				while (optproblem.admitABetterSolution()) {
					if (!isSatisfiable) {
						if (optproblem.nonOptimalMeansSatisfiable()) {
							setExitCode(ExitCode.SATISFIABLE);
							if (optproblem.hasNoObjectiveFunction()) {
								return;
							}
							log("SATISFIABLE"); //$NON-NLS-1$
						} else if (incomplete) {
							setExitCode(ExitCode.UPPER_BOUND);
						}
						isSatisfiable = true;
						log("OPTIMIZING..."); //$NON-NLS-1$
					}
					log("Got one! Elapsed wall clock time (in seconds):" //$NON-NLS-1$
							+ (System.currentTimeMillis() - getBeginTime())
							/ 1000.0);
					getLogWriter().println(
							CURRENT_OPTIMUM_VALUE_PREFIX
									+ optproblem.getObjectiveValue());
					optproblem.discardCurrentSolution();
				}
				if (isSatisfiable) {
					setExitCode(ExitCode.OPTIMUM_FOUND);
				} else {
					setExitCode(ExitCode.UNSATISFIABLE);
				}
			} catch (ContradictionException ex) {
				assert isSatisfiable;
				setExitCode(ExitCode.OPTIMUM_FOUND);
			}
		} else {
			exitCode = problem.isSatisfiable() ? ExitCode.SATISFIABLE
					: ExitCode.UNSATISFIABLE;
		}
	}

	protected void displayResult() {
		if (isModeOptimization) {
			displayAnswer();

			log("Total wall clock time (in seconds): " //$NON-NLS-1$
					+ (System.currentTimeMillis() - getBeginTime()) / 1000.0);
		} else {
			super.displayResult();
		}
	}

	protected void displayAnswer() {
		if (solver == null)
			return;
		System.out.flush();
		PrintWriter out = getLogWriter();
		out.flush();
		solver.printStat(out, COMMENT_PREFIX);
		solver.printInfos(out, COMMENT_PREFIX);
		ExitCode exitCode = getExitCode();
		out.println(ANSWER_PREFIX + exitCode);
		if (exitCode == ExitCode.SATISFIABLE
				|| exitCode == ExitCode.OPTIMUM_FOUND
				|| (incomplete && exitCode == ExitCode.UPPER_BOUND)) {
			out.print(SOLUTION_PREFIX);
			getReader().decode(problem.model(), out);
			out.println();
			if (isModeOptimization) {
				IOptimizationProblem optproblem = (IOptimizationProblem) problem;
				if (!optproblem.hasNoObjectiveFunction()) {
					log("objective function=" + optproblem.getObjectiveValue()); //$NON-NLS-1$
				}
			}
		}
	}

	protected void showAvailableRestarts() {
		Vector<String> classNames = new Vector<String>();
		Vector<String> resultRTSI = RTSI.find(RESTART_STRATEGY_NAME);
		Set<String> keySet;
		for (String name : resultRTSI) {
			if (!name.contains("Remote")) {
				try {
					keySet = BeanUtils.describe(
							Class.forName(PACKAGE_RESTARTS + "." + name)
									.newInstance()).keySet();
					keySet.remove("class");
					if (keySet.size() > 0) {
						classNames.add(name + keySet);
					} else {
						classNames.add(name);
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		log("Available restart strategies (" + RESTARTS + "): " + classNames);
	}

	protected void showAvailablePhase() {
		Vector<String> classNames = new Vector<String>();
		Vector<String> resultRTSI = RTSI.find(PHASE_NAME);
		Set<String> keySet;
		for (String name : resultRTSI) {
			if (!name.contains("Remote")) {
				try {

					keySet = BeanUtils.describe(
							Class.forName(PACKAGE_PHASE + "." + name)
									.newInstance()).keySet();
					keySet.remove("class");
					if (keySet.size() > 0) {
						classNames.add(name + keySet);
					} else {
						classNames.add(name);
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		log("Available phase strategies (" + PHASE + "): " + classNames);
	}

	protected void showAvailableLearning() {
		Vector<String> classNames = new Vector<String>();
		Vector<String> resultRTSI = RTSI.find(LEARNING_NAME);
		Set<String> keySet;
		for (String name : resultRTSI) {
			try {
				keySet = BeanUtils.describe(
						Class.forName(PACKAGE_LEARNING + "." + name)
								.newInstance()).keySet();
				keySet.remove("class");
				if (keySet.size() > 0) {
					classNames.add(name + keySet);
				} else {
					classNames.add(name);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				classNames.add(name);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoClassDefFoundError cnfex) {
				// System.out.println("Warning : no classDefFoundError : " +
				// classname);
			}
		}
		log("Available learning (" + LEARNING + "): " + classNames);
	}

	protected void showAvailableOrders() {
		Vector<String> classNames = new Vector<String>();
		Vector<String> resultRTSI = RTSI.find(ORDER_NAME);
		Set<String> keySet = null;
		for (String name : resultRTSI) {
			if (!name.contains("Remote")) {
				try {
					if (name.contains("Objective")) {
						String namePackage = PACKAGE_ORDERS.replaceFirst(
								"minisat", "pb");
						keySet = BeanUtils.describe(
								Class.forName(namePackage + "." + name)
										.newInstance()).keySet();
					} else {
						keySet = BeanUtils.describe(
								Class.forName(PACKAGE_ORDERS + "." + name)
										.newInstance()).keySet();
					}
					keySet.remove("class");

					if (keySet.size() > 0) {
						classNames.add(name + keySet);
					} else {
						classNames.add(name);
					}

				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					// classNames.add(name);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		log("Available orders (" + ORDERS + "): " + classNames);
	}

	protected void showParams() {

		Set<String> keySet = null;
		try {
			keySet = BeanUtils.describe(
					Class.forName(PARAMS_NAME).newInstance()).keySet();
			keySet.remove("class");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		log("Available search params (" + PARAMS + "): [SearchParams" + keySet
				+ "]");
	}

	protected void showSimplifiers() {
		log("Available simplifiers : [NO_SIMPLIFICATION, SIMPLE_SIMPLIFICATION, EXPENSIVE_SIMPLIFICATION]");
	}

	@Override
	public void run(String[] args) {
		try {
			displayHeader();
			cdclSolver = configureSolver(args);
			solver = cdclSolver;
			if (solver == null) {
				usage();
				return;
			}
			if (!silent)
				solver.setVerbose(true);
			String instanceName = getInstanceName(args);
			if (instanceName == null) {
				usage();
				return;
			}
			beginTime = System.currentTimeMillis();
			if (!launchRemoteControl) {
				readProblem(instanceName);
				try {
					if (problem != null) {
						solve(problem);
					} else {
						solve(solver);
					}
				} catch (TimeoutException e) {
					log("timeout"); //$NON-NLS-1$
				}
				System.exit(lanceur.getExitCode().value());
			} else {
				RemoteControlFrame frame = new RemoteControlFrame(filename, "",
						cdclSolver);
				frame.activateGnuplotTracing(modeTracing);
			}
		} catch (FileNotFoundException e) {
			System.err.println("FATAL " + e.getLocalizedMessage());
		} catch (IOException e) {
			System.err.println("FATAL " + e.getLocalizedMessage());
		} catch (ContradictionException e) {
			exitCode = ExitCode.UNSATISFIABLE;
			log("(trivial inconsistency)"); //$NON-NLS-1$
		} catch (ParseFormatException e) {
			System.err.println("FATAL " + e.getLocalizedMessage());
		}
	}

}
