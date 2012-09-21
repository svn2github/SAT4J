package org.sat4j.tools;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.sat4j.core.LiteralsUtils;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.Counter;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;

public class StatisticsSolver implements ISolver {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Number of constraints in the problem
     */
    private int expectedNumberOfConstraints;

    /**
     * Number of declared vars (max var id)
     */
    private int nbvars;

    /**
     * Size of the constraints for each occurrence of each var for each polarity
     */
    private IVecInt[] sizeoccurrences;

    private int allpositive;

    private int allnegative;

    private int horn;

    private int dualhorn;

    /**
     * Distribution of clauses size
     */
    private final Map<Integer, Counter> sizes = new HashMap<Integer, Counter>();

    public int[] model() {
        throw new UnsupportedOperationException(
                "That solver only compute statistics");
    }

    public boolean model(int var) {
        throw new UnsupportedOperationException(
                "That solver only compute statistics");
    }

    public int[] primeImplicant() {
        throw new UnsupportedOperationException(
                "That solver only compute statistics");
    }

    public boolean primeImplicant(int p) {
        throw new UnsupportedOperationException(
                "That solver only compute statistics");
    }

    public boolean isSatisfiable() throws TimeoutException {
        throw new TimeoutException("That solver only compute statistics");
    }

    public boolean isSatisfiable(IVecInt assumps, boolean globalTimeout)
            throws TimeoutException {
        throw new TimeoutException("That solver only compute statistics");
    }

    public boolean isSatisfiable(boolean globalTimeout) throws TimeoutException {
        throw new TimeoutException("That solver only compute statistics");
    }

    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        throw new TimeoutException("That solver only compute statistics");
    }

    public int[] findModel() throws TimeoutException {
        throw new TimeoutException("That solver only compute statistics");
    }

    public int[] findModel(IVecInt assumps) throws TimeoutException {
        throw new TimeoutException("That solver only compute statistics");
    }

    public int nConstraints() {
        return expectedNumberOfConstraints;
    }

    public int newVar(int howmany) {
        this.nbvars = howmany;
        sizeoccurrences = new IVecInt[(howmany + 1) << 1];
        return howmany;
    }

    public int nVars() {
        return this.nbvars;
    }

    @Deprecated
    public void printInfos(PrintWriter out, String prefix) {

    }

    public void printInfos(PrintWriter out) {
    }

    @Deprecated
    public int newVar() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public int nextFreeVarId(boolean reserve) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void registerLiteral(int p) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void setExpectedNumberOfClauses(int nb) {
        this.expectedNumberOfConstraints = nb;
    }

    public IConstr addClause(IVecInt literals) throws ContradictionException {
        int size = literals.size();
        Counter counter = sizes.get(size);
        if (counter == null) {
            counter = new Counter(0);
            sizes.put(size, counter);
        }
        counter.inc();
        IVecInt list;
        int x, p;
        int pos = 0, neg = 0;
        for (IteratorInt it = literals.iterator(); it.hasNext();) {
            x = it.next();
            if (x > 0) {
                pos++;
            } else {
                neg++;
            }
            p = LiteralsUtils.toInternal(x);
            list = sizeoccurrences[p];
            if (list == null) {
                list = new VecInt();
                sizeoccurrences[p] = list;
            }
            list.push(size);
        }
        if (neg == 0) {
            allpositive++;
        } else if (pos == 0) {
            allnegative++;
        } else if (pos == 1) {
            horn++;
        } else if (neg == 1) {
            dualhorn++;
        }
        return null;
    }

    public IConstr addBlockingClause(IVecInt literals)
            throws ContradictionException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public boolean removeConstr(IConstr c) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public boolean removeSubsumedConstr(IConstr c) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void addAllClauses(IVec<IVecInt> clauses)
            throws ContradictionException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public IConstr addExactly(IVecInt literals, int n)
            throws ContradictionException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void setTimeout(int t) {
    }

    public void setTimeoutOnConflicts(int count) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void setTimeoutMs(long t) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public int getTimeout() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public long getTimeoutMs() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void expireTimeout() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void reset() {
    }

    @Deprecated
    public void printStat(PrintStream out, String prefix) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Deprecated
    public void printStat(PrintWriter out, String prefix) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void printStat(PrintWriter out) {
        int realNumberOfVariables = 0;
        int realNumberOfLiterals = 0;
        int minOccV = Integer.MAX_VALUE;
        int maxOccV = Integer.MIN_VALUE;
        int sumV = 0;
        int sizeL, sizeV;
        int minOccL = Integer.MAX_VALUE;
        int maxOccL = Integer.MIN_VALUE;
        int sumL = 0;
        IVecInt list;
        int max = sizeoccurrences.length - 1;
        for (int i = 1; i < max; i += 2) {
            sizeV = 0;
            for (int k = 0; k < 2; k++) {
                list = sizeoccurrences[i + k];
                if (list != null) {
                    realNumberOfLiterals++;
                    sizeL = list.size();
                    sizeV += sizeL;
                    if (minOccL > sizeL) {
                        minOccL = sizeL;
                    }
                    if (sizeL > maxOccL) {
                        maxOccL = sizeL;
                    }
                    sumL += sizeL;
                }
            }
            if (sizeV > 0) {
                realNumberOfVariables++;
                if (minOccV > sizeV) {
                    minOccV = sizeV;
                }
                if (sizeV > maxOccV) {
                    maxOccV = sizeV;
                }
                sumV += sizeV;
            }

        }
        System.out.println("c Distribution of constraints size:");
        int nbclauses = 0;
        for (Map.Entry<Integer, Counter> entry : sizes.entrySet()) {
            System.out.printf("c %d => %d\n", entry.getKey(), entry.getValue()
                    .getValue());
            nbclauses += entry.getValue().getValue();
        }

        System.out
                .printf("c Real number of variables, literals, number of clauses ");
        System.out.printf("variable occurrences (min/max/avg) ");
        System.out.printf("literals occurrences (min/max/avg) ");
        System.out
                .println("Specific clauses: #positive  #negative #horn  #dualhorn #remaining #total");

        System.out.printf("c %d %d %d %d %d %d %d %d %d ",
                realNumberOfVariables, realNumberOfLiterals, nbclauses,
                minOccV, maxOccV, sumV / realNumberOfVariables, minOccL,
                maxOccL, sumL / realNumberOfLiterals);
        System.out.printf("%d %d %d %d %d\n", allpositive, allnegative, horn,
                dualhorn, nbclauses - allpositive - allnegative - horn
                        - dualhorn);
    }

    public Map<String, Number> getStat() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public String toString(String prefix) {
        return prefix + "Statistics about the benchmarks";
    }

    public void clearLearntClauses() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void setDBSimplificationAllowed(boolean status) {
    }

    public boolean isDBSimplificationAllowed() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public <S extends ISolverService> void setSearchListener(
            SearchListener<S> sl) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public <S extends ISolverService> SearchListener<S> getSearchListener() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public boolean isVerbose() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void setVerbose(boolean value) {
    }

    public void setLogPrefix(String prefix) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public String getLogPrefix() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public IVecInt unsatExplanation() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public int[] modelWithInternalVariables() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public int realNumberOfVariables() {
        return nbvars;
    }

    public boolean isSolverKeptHot() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void setKeepSolverHot(boolean keepHot) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public ISolver getSolvingEngine() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}
