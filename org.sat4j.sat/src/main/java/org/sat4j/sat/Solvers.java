package org.sat4j.sat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.core.LearnedConstraintsEvaluationType;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.SimplificationType;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.pb.orders.RandomWalkDecoratorObjective;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.specs.ILogAble;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.DotSearchTracing;

public final class Solvers {

    private static final String SEPARATOR = "-------------------";
    private static final String CLASS = "class";
    private static final String OPB = "opb";
    private static final String CNF = "cnf";
    private static final String MAXSAT = "maxsat";
    private static final String OPT = "opt";
    private static final String FILENAME = "filename";
    private static final String NUMBER = "number";
    private static final String PROBLEM_WITH_COMPONENT = "Problem with component ";
    private static final String PB = "pb";
    private static final String MINISAT = "minisat";
    public static final String ORDERS = "ORDERS";
    public static final String LEARNING = "LEARNING";
    public static final String RESTARTS = "RESTARTS";
    public static final String PHASE = "PHASE";
    public static final String PARAMS = "PARAMS";
    public static final String SIMP = "SIMP";
    public static final String CLEANING = "CLEANING";

    private static final String PACKAGE_ORDERS = "org.sat4j.minisat.orders";
    private static final String PACKAGE_LEARNING = "org.sat4j.minisat.learning";
    private static final String PACKAGE_RESTARTS = "org.sat4j.minisat.restarts";
    private static final String PACKAGE_PHASE = "org.sat4j.minisat.orders";
    private static final String PACKAGE_PARAMS = "org.sat4j.minisat.core";

    private static final String RESTART_STRATEGY_NAME = "org.sat4j.minisat.core.RestartStrategy";
    private static final String ORDER_NAME = "org.sat4j.minisat.core.IOrder";
    private static final String LEARNING_NAME = "org.sat4j.minisat.core.LearningStrategy";
    private static final String PHASE_NAME = "org.sat4j.minisat.core.IPhaseSelectionStrategy";
    private static final String PARAMS_NAME = "org.sat4j.minisat.core.SearchParams";

    private static final Map<String, String> QUALIF = new HashMap<String, String>();
    static {
        QUALIF.put(ORDERS, PACKAGE_ORDERS);
        QUALIF.put(LEARNING, PACKAGE_LEARNING);
        QUALIF.put(RESTARTS, PACKAGE_RESTARTS);
        QUALIF.put(PHASE, PACKAGE_PHASE);
        QUALIF.put(PARAMS, PACKAGE_PARAMS);
    }

    private Solvers() {
    }

    protected static ICDCL configureFromString(String solverconfig,
            ICDCL theSolver, ILogAble logger) {
        assert theSolver != null;
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
        DataStructureFactory dsf = setupObject("DSF", pf, logger);
        if (dsf != null) {
            theSolver.setDataStructureFactory(dsf);
        }
        LearningStrategy learning = setupObject(LEARNING, pf, logger);
        if (learning != null) {
            theSolver.setLearningStrategy(learning);
            learning.setSolver(aSolver);
        }
        IOrder order = setupObject(ORDERS, pf, logger);
        if (order != null) {
            theSolver.setOrder(order);
        }
        IPhaseSelectionStrategy pss = setupObject(PHASE, pf, logger);
        if (pss != null) {
            theSolver.getOrder().setPhaseSelectionStrategy(pss);
        }
        RestartStrategy restarter = setupObject(RESTARTS, pf, logger);
        if (restarter != null) {
            theSolver.setRestartStrategy(restarter);
        }
        String simp = pf.getProperty(SIMP);
        if (simp != null) {
            logger.log("read " + simp);
            logger.log("configuring " + SIMP);
            theSolver.setSimplifier(SimplificationType.valueOf(simp));
        }
        SearchParams params = setupObject(PARAMS, pf, logger);
        if (params != null) {
            theSolver.setSearchParams(params);
        }
        String memory = pf.getProperty(CLEANING);
        if (memory != null) {
            try {
                logger.log("configuring "+CLEANING);
                LearnedConstraintsEvaluationType memoryType = LearnedConstraintsEvaluationType
                        .valueOf(memory);
                theSolver.setLearnedConstraintsDeletionStrategy(memoryType);
            } catch (IllegalArgumentException iae) {
                logger.log("wrong memory management setting: " + memory);
                showAvailableConstraintsCleaningStrategies(logger);
            }
        }
        return theSolver;
    }

    private static <T> T setupObject(String component, Properties pf,
            ILogAble logger) {
        try {
            String configline = pf.getProperty(component);
            String qualification = QUALIF.get(component);

            if (configline == null) {
                return null;
            }
            if (qualification != null) {
                logger.log("read " + qualification + "." + configline);
                if (configline.contains("Objective")
                        && qualification.contains(MINISAT)) {
                    qualification = qualification.replaceFirst(MINISAT, PB);
                }
                configline = qualification + "." + configline;
            }

            logger.log("configuring " + component);
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
                    logger.log(PROBLEM_WITH_COMPONENT + config[0] + " " + e);
                }
            }
            return comp;
        } catch (InstantiationException e) {
            logger.log(PROBLEM_WITH_COMPONENT + component + " " + e);
        } catch (IllegalAccessException e) {
            logger.log(PROBLEM_WITH_COMPONENT + component + " " + e);
        } catch (ClassNotFoundException e) {
            logger.log(PROBLEM_WITH_COMPONENT + component + " " + e);
        }
        return null;
    }

    public static Options createCLIOptions() {
        Options options = new Options();
        options.addOption(
                "l",
                "library",
                true,
                "specifies the name of the library used (if not present, the library depends on the file extension)");
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
        options.addOption("f", FILENAME, true,
                "specifies the file to use (in conjunction with -d for instance)");
        options.addOption("m", "mute", false, "Set launcher in silent mode");
        options.addOption("k", "kleast", true,
                "limit the search to models having at least k variables set to false");
        options.addOption("tr", "trace", false,
                "traces the behavior of the solver");
        options.addOption(OPT, "optimize", false,
                "uses solver in optimize mode instead of sat mode (default)");
        options.addOption("rw", "randomWalk", true,
                "specifies the random walk probability ");
        options.addOption("remote", "remoteControl", false,
                "launches remote control");
        options.addOption("r", "trace", false,
                "traces the behavior of the solver");
        options.addOption("H", "hot", false,
                "keep the solver hot (do not reset heuristics) when a model is found");
        options.addOption("y", "simplify", false,
                "simplify the set of clauses if possible");
        options.addOption("lo", "lower", false,
                "search solution by lower bounding instead of by upper bounding");
        options.addOption("e", "equivalence", false,
                "Use an equivalence instead of an implication for the selector variables");
        options.addOption("i", "incomplete", false,
                "incomplete mode for maxsat");
        options.addOption("n", "no solution line", false,
                "Do not display a solution line (useful if the solution is large)");
        Option op = options.getOption("l");
        op.setArgName("libname");
        op = options.getOption("s");
        op.setArgName("solvername");
        op = options.getOption("S");
        op.setArgName("solverStringDefinition");
        op = options.getOption("t");
        op.setArgName(NUMBER);
        op = options.getOption("T");
        op.setArgName(NUMBER);
        op = options.getOption("C");
        op.setArgName(NUMBER);
        op = options.getOption("k");
        op.setArgName(NUMBER);
        op = options.getOption("d");
        op.setArgName(FILENAME);
        op = options.getOption("f");
        op.setArgName(FILENAME);
        op = options.getOption("rw");
        op.setArgName(NUMBER);
        return options;
    }

    public static void stringUsage(ILogAble logger) {
        logger.log("Available building blocks: DSF, LEARNING, ORDERS, PHASE, RESTARTS, SIMP, PARAMS, CLEANING");
        logger.log("Example: -S RESTARTS=LubyRestarts/factor:512,LEARNING=MiniSATLearning");
    }

    public static boolean containsOptValue(String[] args) {
        Options options = createCLIOptions();
        try {
            CommandLine cmd = new PosixParser().parse(options, args);
            return cmd.hasOption(OPT);
        } catch (ParseException e) {
            return false;
        }
    }

    public static ICDCL configureSolver(String[] args, ILogAble logger) {
        Options options = createCLIOptions();
        if (args.length == 0) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("java -jar sat4j.jar", options, true);

            return null;
        }
        try {
            CommandLine cmd = new PosixParser().parse(options, args);

            boolean isModeOptimization = false;
            ASolverFactory<ISolver> factory;

            if (cmd.hasOption(OPT)) {
                isModeOptimization = true;
            }

            String filename = cmd.getOptionValue("f");

            String[] rargs = cmd.getArgs();
            if (filename == null && rargs.length > 0) {
                filename = rargs[0];
            }
            String unzipped = uncompressed(filename);

            String framework = cmd.getOptionValue("l"); //$NON-NLS-1$
            if (framework == null) {
                if (isModeOptimization) {
                    if (unzipped != null && unzipped.endsWith(CNF)) {
                        framework = MAXSAT;
                    } else {
                        framework = PB;
                    }
                } else if (unzipped != null && unzipped.endsWith(OPB)) {
                    framework = PB;
                } else {
                    framework = MINISAT;
                }
            }

            try {
                Class<?> clazz = Class
                        .forName("org.sat4j." + framework + ".SolverFactory"); //$NON-NLS-1$ //$NON-NLS-2$
                Class<?>[] params = {};
                Method m = clazz.getMethod("instance", params); //$NON-NLS-1$
                factory = (ASolverFactory<ISolver>) m.invoke(null, (Object[]) null);
            } catch (Exception e) { // DLB Findbugs warning ok
                logger.log("Wrong framework: " + framework
                        + ". Using minisat instead.");
                factory = org.sat4j.minisat.SolverFactory.instance();
            }

            ICDCL<?> asolver;
            if (cmd.hasOption("s")) {
                String solvername = cmd.getOptionValue("s");
                if (solvername == null) {
                    logger.log("No solver for option s. Launching default solver.");
                    logger.log("Available solvers: "
                            + Arrays.asList(factory.solverNames()));
                    asolver = (Solver<?>) factory.defaultSolver();
                } else {
                    asolver = (Solver<?>) factory.createSolverByName(solvername);
                }
            } else {
                asolver = (Solver<?>) factory.defaultSolver();
            }

            if (cmd.hasOption("S")) {
                String configuredSolver = cmd.getOptionValue("S");
                if (configuredSolver == null) {
                    stringUsage(logger);
                    return null;
                }
                asolver = Solvers.configureFromString(configuredSolver,
                        asolver, logger);
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

            if (cmd.hasOption("H")) {
                asolver.setKeepSolverHot(true);
            }

            if (cmd.hasOption("y")) {
                asolver.setDBSimplificationAllowed(true);
            }

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

            return asolver;
        } catch (ParseException e1) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("java -jar sat4j.jar", options, true);
            usage(logger);
        }
        return null;
    }

    public static String uncompressed(String filename) {
        if (filename != null
                && (filename.endsWith(".bz2") || filename.endsWith(".gz"))) {
            return filename.substring(0, filename.lastIndexOf('.'));
        }
        return filename;
    }

    public static void showAvailableRestarts(ILogAble logger) {
        List<String> classNames = new ArrayList<String>();
        List<String> resultRTSI = RTSI.find(RESTART_STRATEGY_NAME);
        Set<String> keySet;
        for (String name : resultRTSI) {
            if (!name.contains("Remote")) {
                try {
                    keySet = BeanUtils
                            .describe(
                                    Class.forName(
                                            Solvers.PACKAGE_RESTARTS + "."
                                                    + name).newInstance())
                            .keySet();
                    keySet.remove(CLASS);
                    if (keySet.size() > 0) {
                        classNames.add(name + keySet);
                    } else {
                        classNames.add(name);
                    }
                } catch (IllegalAccessException e) {
                    logger.log(e.getMessage());
                } catch (InvocationTargetException e) {
                    logger.log(e.getMessage());
                } catch (NoSuchMethodException e) {
                    logger.log(e.getMessage());
                } catch (InstantiationException e) {
                    logger.log(e.getMessage());
                } catch (ClassNotFoundException e) {
                    logger.log(e.getMessage());
                }
            }
        }
        logger.log("Available restart strategies (" + Solvers.RESTARTS + "): "
                + classNames);
    }

    public static void showAvailablePhase(ILogAble logger) {
        List<String> classNames = new ArrayList<String>();
        List<String> resultRTSI = RTSI.find(PHASE_NAME);
        Set<String> keySet;
        for (String name : resultRTSI) {
            if (!name.contains("Remote")) {
                try {

                    keySet = BeanUtils.describe(
                            Class.forName(PACKAGE_PHASE + "." + name)
                                    .newInstance()).keySet();
                    keySet.remove(CLASS);
                    if (keySet.size() > 0) {
                        classNames.add(name + keySet);
                    } else {
                        classNames.add(name);
                    }
                } catch (IllegalAccessException e) {
                    logger.log(e.getMessage());
                } catch (InvocationTargetException e) {
                    logger.log(e.getMessage());
                } catch (NoSuchMethodException e) {
                    logger.log(e.getMessage());
                } catch (InstantiationException e) {
                    logger.log(e.getMessage());
                } catch (ClassNotFoundException e) {
                    logger.log(e.getMessage());
                }
            }
        }
        logger.log("Available phase strategies (" + Solvers.PHASE + "): "
                + classNames);
    }

    public static void showAvailableLearning(ILogAble logger) {
        List<String> classNames = new ArrayList<String>();
        List<String> resultRTSI = RTSI.find(LEARNING_NAME);
        Set<String> keySet;
        for (String name : resultRTSI) {
            try {
                keySet = BeanUtils.describe(
                        Class.forName(PACKAGE_LEARNING + "." + name)
                                .newInstance()).keySet();
                keySet.remove(CLASS);
                if (keySet.size() > 0) {
                    classNames.add(name + keySet);
                } else {
                    classNames.add(name);
                }
            } catch (IllegalAccessException e) {
                logger.log(e.getMessage());
            } catch (InvocationTargetException e) {
                logger.log(e.getMessage());
            } catch (NoSuchMethodException e) {
                logger.log(e.getMessage());
            } catch (InstantiationException e) {
                classNames.add(name);
            } catch (ClassNotFoundException e) {
                logger.log(e.getMessage());
            } catch (NoClassDefFoundError cnfex) {
                logger.log(cnfex.getMessage());
            }
        }
        logger.log("Available learning (" + Solvers.LEARNING + "): "
                + classNames);
    }

    public static void showAvailableOrders(ILogAble logger) {
        List<String> classNames = new ArrayList<String>();
        List<String> resultRTSI = RTSI.find(ORDER_NAME);
        Set<String> keySet = null;
        for (String name : resultRTSI) {
            if (!name.contains("Remote")) {
                try {
                    if (name.contains("Objective")) {
                        String namePackage = Solvers.PACKAGE_ORDERS
                                .replaceFirst(MINISAT, PB);
                        keySet = BeanUtils.describe(
                                Class.forName(namePackage + "." + name)
                                        .newInstance()).keySet();
                    } else {
                        keySet = BeanUtils.describe(
                                Class.forName(
                                        Solvers.PACKAGE_ORDERS + "." + name)
                                        .newInstance()).keySet();
                    }
                    keySet.remove(CLASS);

                    if (keySet.size() > 0) {
                        classNames.add(name + keySet);
                    } else {
                        classNames.add(name);
                    }

                } catch (IllegalAccessException e) {
                    logger.log(e.getMessage());
                } catch (InvocationTargetException e) {
                    logger.log(e.getMessage());
                } catch (NoSuchMethodException e) {
                    logger.log(e.getMessage());
                } catch (InstantiationException e) {
                    logger.log(e.getMessage());
                } catch (ClassNotFoundException e) {
                    logger.log(e.getMessage());
                }
            }
        }
        logger.log("Available orders (" + Solvers.ORDERS + "): " + classNames);
    }

    public static void showParams(ILogAble logger) {

        Set<String> keySet = null;
        try {
            keySet = BeanUtils.describe(
                    Class.forName(PARAMS_NAME).newInstance()).keySet();
            keySet.remove(CLASS);
        } catch (IllegalAccessException e) {
            logger.log(e.getMessage());
        } catch (InvocationTargetException e) {
            logger.log(e.getMessage());
        } catch (NoSuchMethodException e) {
            logger.log(e.getMessage());
        } catch (InstantiationException e) {
            logger.log(e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.log(e.getMessage());
        }
        logger.log("Available search params (" + Solvers.PARAMS
                + "): [SearchParams" + keySet + "]");
    }

    public static void showSimplifiers(ILogAble logger) {
        logger.log("Available simplifiers ("+Solvers.SIMP+"): [NO_SIMPLIFICATION, SIMPLE_SIMPLIFICATION, EXPENSIVE_SIMPLIFICATION]");
    }

    public static void showAvailableConstraintsCleaningStrategies(
            ILogAble logger) {
        logger.log("Available learned constraints cleaning strategies ("+Solvers.CLEANING+"): "
                + Arrays.asList(LearnedConstraintsEvaluationType.values()));
    }

    public static <T extends ISolver> void showAvailableSolvers(
            ASolverFactory<T> afactory, ILogAble logger) {
        showAvailableSolvers(afactory, "", logger);
    }

    public static <T extends ISolver> void showAvailableSolvers(
            ASolverFactory<T> afactory, String framework, ILogAble logger) {
        if (afactory != null) {
            if (framework.length() > 0) {
                logger.log("Available solvers for " + framework + ": "); //$NON-NLS-1$
            } else {
                logger.log("Available solvers: "); //$NON-NLS-1$
            }
            String[] names = afactory.solverNames();
            for (String name : names) {
                logger.log(name);
            }
        }
    }

    public static void usage(ILogAble logger) {
        ASolverFactory<ISolver> factory;
        factory = org.sat4j.minisat.SolverFactory.instance();
        showAvailableSolvers(factory, "sat", logger);
        logger.log(SEPARATOR);
        factory = (ASolverFactory) org.sat4j.pb.SolverFactory.instance();
        showAvailableSolvers(factory, PB, logger);
        logger.log(SEPARATOR);
        factory = (ASolverFactory) org.sat4j.maxsat.SolverFactory.instance();
        showAvailableSolvers(factory, MAXSAT, logger);
        logger.log(SEPARATOR);
        showAvailableRestarts(logger);
        logger.log(SEPARATOR);
        showAvailableOrders(logger);
        logger.log(SEPARATOR);
        showAvailableLearning(logger);
        logger.log(SEPARATOR);
        showAvailablePhase(logger);
        logger.log(SEPARATOR);
        showParams(logger);
        logger.log(SEPARATOR);
        showSimplifiers(logger);
        logger.log(SEPARATOR);
        showAvailableConstraintsCleaningStrategies(logger);
        logger.log(SEPARATOR);
        stringUsage(logger);
    }

}
