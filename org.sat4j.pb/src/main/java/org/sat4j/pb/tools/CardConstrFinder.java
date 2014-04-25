package org.sat4j.pb.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.MixedDataStructureDanielWLConciseBinary;
import org.sat4j.minisat.core.Solver;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.IPBSolverService;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SearchListenerAdapter;

public class CardConstrFinder implements Iterator<AtLeastCard> {

    private final IPBSolver coSolver;

    private BitSet propagated = null;

    private final Map<BitSet, BitSet> implied = new HashMap<BitSet, BitSet>();

    private final SearchListener<ISolverService> oldListener;

    private final SortedSet<IVecInt> clauses = new TreeSet<IVecInt>(
            new ClauseSizeComparator());

    private final Map<Integer, List<BitSet>> atLeastCardCache = new HashMap<Integer, List<BitSet>>();

    private final Map<BitSet, Integer> atLeastCardDegree = new HashMap<BitSet, Integer>();

    private Iterator<BitSet> cardIt;

    private final int maxCardDegree = Integer.MAX_VALUE - 1;

    private int initNumberOfClauses;

    private BitSet zeroProps = null;

    private boolean printCards = false;

    private boolean shouldDisplayStatus = false;

    private Set<Integer> authorizedExtLits = null;

    @SuppressWarnings("unchecked")
    public CardConstrFinder(IPBSolver coSolver) {
        this.coSolver = coSolver;
        ((Solver<MixedDataStructureDanielWLConciseBinary>) coSolver)
                .setDataStructureFactory(new MixedDataStructureDanielWLConciseBinary());
        this.coSolver.setTimeoutOnConflicts(Integer.MAX_VALUE);
        this.oldListener = this.coSolver.getSearchListener();
        this.coSolver.setSearchListener(new CardConstrFinderListener(this));
    }

    public void forget() {
        this.coSolver.setSearchListener(this.oldListener);
    }

    public void addClause(IVecInt clause) {
        IVecInt copy = new VecInt(clause.size());
        clause.copyTo(copy);
        this.clauses.add(copy);
    }

    public void rissPreprocessing(String rissLocation, String instance) {
        this.initNumberOfClauses = this.clauses.size();
        int status = -1;
        System.out.println("c executing riss subprocess");
        try {
            // Process p = Runtime.getRuntime().exec(
            // rissLocation + " -findCard -card_print -card_noLim "
            // + instance);
            // Process p = Runtime
            // .getRuntime()
            // .exec(rissLocation
            // +
            // " -findCard -card_print -no-card_amt -no-card_amo -no-card_sub -no-card_twoProd -no-card_merge -card_noLim "
            // + instance);
            Process p = Runtime
                    .getRuntime()
                    .exec(rissLocation
                            + " -findCard -card_print -no-card_semCard -card_noLim "
                            + instance);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("c "))
                    continue;
                IVecInt lits = new VecInt();
                String[] words = line.split(" +");
                try {
                    for (int i = 0; i < words.length - 2; ++i)
                        lits.push(Integer.valueOf(words[i]));
                    int degree = Integer.valueOf(words[words.length - 1]);
                    System.out.println("c riss extracted: "
                            + new AtMostCard(lits, degree));
                    storeAtMostCard(lits, degree);
                } catch (Exception e) {
                    System.err.println("WARNING: read \"" + line
                            + "\" from Riss subprocess");
                }
            }
            reader.close();
            status = p.waitFor();
            System.out.println("c riss process exited with status " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Iterator<IVecInt> clIt = this.clauses.iterator(); clIt.hasNext();) {
            IVecInt clause = clIt.next();
            if (clause.size() <= this.maxCardDegree + 1) {
                BitSet atLeastLits = new BitSet(clause.size());
                for (IteratorInt it = clause.iterator(); it.hasNext();)
                    atLeastLits.set(it.next()
                            + this.coSolver.realNumberOfVariables());
                if (clauseInFoundCard(atLeastLits)) {
                    clIt.remove();
                }
            }
        }
        this.cardIt = this.atLeastCardDegree.keySet().iterator();
    }

    public void searchCards() {
        this.initNumberOfClauses = this.clauses.size();
        System.out.println("c " + this.clauses.size() + " clauses to process");
        int cpt = 0;
        Timer timerStatus = new Timer();
        timerStatus.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                shouldDisplayStatus = true;
            }
        }, 30 * 1000, 30 * 1000);
        for (Iterator<IVecInt> clIt = this.clauses.iterator(); clIt.hasNext();) {
            IVecInt clause = clIt.next();
            if (clause.size() <= this.maxCardDegree + 1) {
                BitSet atLeastLits = new BitSet(clause.size());
                for (IteratorInt it = clause.iterator(); it.hasNext();) {
                    int nextLit = it.next();
                    atLeastLits.set(nextLit
                            + this.coSolver.realNumberOfVariables());
                }
                if (!clauseInFoundCard(atLeastLits)) {
                    BitSet cardFound = searchCardFromAtLeastOneCard(atLeastLits);
                    if (cardFound != null) {
                        clIt.remove();
                    }
                } else {
                    clIt.remove();
                }
            }
            ++cpt;
            if (this.shouldDisplayStatus) {
                System.out.println("c processed " + cpt + "/"
                        + this.initNumberOfClauses + " clauses");
                this.shouldDisplayStatus = false;
            }
        }
        timerStatus.cancel();
        this.cardIt = this.atLeastCardDegree.keySet().iterator();
    }

    public IVecInt searchCardFromClause(IVecInt clause) {
        BitSet atLeastLits = new BitSet(clause.size());
        for (IteratorInt it = clause.iterator(); it.hasNext();)
            atLeastLits.set(it.next() + this.coSolver.realNumberOfVariables());
        BitSet cardFound = searchCardFromAtLeastOneCard(atLeastLits);
        IVecInt atMostLits = new VecInt(cardFound.cardinality());
        for (int from = 0; (from = cardFound.nextSetBit(from)) != -1; ++from) {
            atMostLits.push(from - this.coSolver.realNumberOfVariables());
        }
        return atMostLits;
    }

    private BitSet searchCardFromAtLeastOneCard(BitSet atLeastLits) {
        BitSet atMostLits = new BitSet(atLeastLits.cardinality());
        int from = 0;
        int cur;
        while ((cur = atLeastLits.nextSetBit(from)) != -1) {
            int negBit = 2 * this.coSolver.realNumberOfVariables() - cur;
            assert negBit != this.coSolver.realNumberOfVariables();
            atMostLits.set(negBit);
            from = cur + 1;
        }
        int atMostDegree = atLeastLits.cardinality() - 1;
        Set<Integer> newLits = expendAtMostCard(atMostLits, atMostDegree);
        for (Integer lit : newLits) {
            atLeastLits.set(-lit + this.coSolver.realNumberOfVariables());
        }
        if (newLits.isEmpty())
            return null;
        storeAtLeastCard(atLeastLits, atLeastLits.cardinality() - atMostDegree);
        if (this.printCards)
            System.out.println("c newConstr: "
                    + new AtMostCard(atMostLits, atMostDegree, -this.coSolver
                            .realNumberOfVariables()));
        return atMostLits;
    }

    private boolean clauseInFoundCard(BitSet atLeastLits) {
        List<BitSet> storedCards = this.atLeastCardCache.get(atLeastLits
                .nextSetBit(0) - this.coSolver.realNumberOfVariables());
        if (storedCards == null) {
            return false;
        }
        // if clause is subsumed by a card, a card contain all the clause
        // literals
        if (storedCards != null) {
            for (BitSet storedCard : storedCards) {
                BitSet clauseClone = (BitSet) atLeastLits.clone();
                clauseClone.andNot(storedCard);
                if (clauseClone.isEmpty()) {
                    // L>=d dominates L'>=d' iff |L\L'| <= d-d'
                    BitSet intersection = ((BitSet) storedCard.clone());
                    intersection.andNot(atLeastLits);
                    if (intersection.cardinality() <= this.atLeastCardDegree
                            .get(storedCard) - 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void storeAtLeastCard(BitSet atLeastLits, int atLeastDegree) {
        int from = 0;
        int cur;
        while ((cur = atLeastLits.nextSetBit(from)) != -1) {
            List<BitSet> cardsList = this.atLeastCardCache.get(cur
                    - this.coSolver.realNumberOfVariables());
            if (cardsList == null) {
                cardsList = new LinkedList<BitSet>();
                this.atLeastCardCache.put(
                        cur - this.coSolver.realNumberOfVariables(), cardsList);
            }
            cardsList.add(atLeastLits);
            from = cur + 1;
        }
        this.atLeastCardDegree.put(atLeastLits, atLeastDegree);
    }

    private void storeAtMostCard(IVecInt lits, int degree) {
        BitSet bs = new BitSet();
        for (IteratorInt it = lits.iterator(); it.hasNext();) {
            int lit = it.next();
            bs.set(this.coSolver.realNumberOfVariables() - lit);
        }
        storeAtLeastCard(bs, lits.size() - degree);
    }

    private Set<Integer> expendAtMostCard(BitSet atMostLits, int degree) {
        Set<Integer> res = new HashSet<Integer>();
        BitSet candidates = computeInitialCandidates(atMostLits, degree);
        if (candidates == null || candidates.isEmpty())
            return res;
        int from = 0;
        int cur;
        while ((cur = candidates.nextSetBit(from)) != -1) {
            from = cur + 1;
            // if a candidate is the negation of a literal, forget it
            // this literal may not be set by the solver (improvement needed)
            if (atMostLits.get(2 * this.coSolver.realNumberOfVariables() - cur))
                continue;
            res.add(-(cur - this.coSolver.realNumberOfVariables()));
            // after adding the new literal to the card, we need to compute the
            // remaining candidates
            refineCandidates(atMostLits, degree, cur, candidates);
            atMostLits.set(2 * this.coSolver.realNumberOfVariables() - cur);
        }
        return res;
    }

    private void refineCandidates(BitSet atMostLits, int degree,
            int newLitInCard, BitSet candidates) {
        if (degree == 1) {
            BitSet newLit = new BitSet(1);
            newLit.set(2 * this.coSolver.realNumberOfVariables() - newLitInCard);
            BitSet implied = impliedBy(newLit);
            candidates.and(implied);
        } else {
            CombinationIterator combIt = new CombinationIterator(degree - 1,
                    atMostLits);
            while (combIt.hasNext()) {
                BitSet comb = combIt.nextBitSet();
                comb.set(2 * this.coSolver.realNumberOfVariables()
                        - newLitInCard);
                candidates.and(impliedBy(comb));
                if (candidates.isEmpty())
                    break;
            }
        }
    }

    private BitSet computeInitialCandidates(BitSet atMostLits, int degree) {
        BitSet candidates = null;
        CombinationIterator combIt = new CombinationIterator(degree, atMostLits);
        while (combIt.hasNext()) {
            BitSet nextBitSet = combIt.nextBitSet();
            BitSet implied = impliedBy(nextBitSet);
            if (candidates == null) {
                candidates = implied;
            } else {
                candidates.and(implied);
            }
            if (candidates.isEmpty())
                return candidates;
        }
        return candidates;
    }

    private BitSet impliedBy(BitSet lits) {
        if (this.zeroProps == null) {
            this.zeroProps = new BitSet(0);
            this.zeroProps = impliedBy(new BitSet(0));
            System.out.println("c " + zeroProps.cardinality()
                    + " literals propagated at decision level 0");
        }
        BitSet cached = this.implied.get(lits);
        if (cached != null)
            return cached;
        IVecInt litVec = new VecInt(this.zeroProps.cardinality()
                + lits.cardinality());
        int from = 0;
        int cur;
        while ((cur = lits.nextSetBit(from)) != -1) {
            litVec.push(cur - this.coSolver.realNumberOfVariables());
            from = cur + 1;
        }
        this.propagated = new BitSet();
        try {
            this.coSolver.isSatisfiable(litVec);
        } catch (TimeoutException e) {
        }
        this.propagated.andNot(this.zeroProps);
        this.implied.put(lits, this.propagated);
        return this.propagated;
    }

    public Set<IVecInt> remainingClauses() {
        return this.clauses;
    }

    public int initNumberOfClauses() {
        return this.initNumberOfClauses;
    }

    public void setAuthorizedExtLits(IVecInt lits) {
        this.authorizedExtLits = new HashSet<Integer>();
        for (IteratorInt it = lits.iterator(); it.hasNext();)
            this.authorizedExtLits.add(it.next());
    }

    private class CardConstrFinderListener extends
            SearchListenerAdapter<IPBSolverService> {

        private static final long serialVersionUID = 1L;
        private final CardConstrFinder ccf;

        private CardConstrFinderListener(CardConstrFinder ccf) {
            this.ccf = ccf;
        }

        @Override
        public void propagating(int p) {
            if (Math.abs(p) > ccf.coSolver.realNumberOfVariables()) {
                System.out.println();
                assert Math.abs(p) <= ccf.coSolver.realNumberOfVariables();
            }
            if (authorizedExtLits != null && !authorizedExtLits.contains(p))
                return;
            ccf.propagated.set(p + ccf.coSolver.realNumberOfVariables());
        }

        @Override
        public void beginLoop() {
            ccf.coSolver.expireTimeout();
        }
    }

    public boolean hasNext() {
        boolean res = cardIt.hasNext();
        if (!res)
            this.cardIt = this.atLeastCardDegree.keySet().iterator();
        return res;
    }

    public AtLeastCard next() {
        BitSet next = cardIt.next();
        return new AtLeastCard(next, this.atLeastCardDegree.get(next),
                -this.coSolver.realNumberOfVariables());
    }

    public void remove() {
        cardIt.remove();
    }

    private class ClauseSizeComparator implements Comparator<IVecInt> {

        public int compare(IVecInt o1, IVecInt o2) {
            int sizeDif = o1.size() - o2.size();
            return sizeDif != 0 ? sizeDif : -1;
        }

    }

    public void setPrintCards(boolean b) {
        this.printCards = b;
    }

}
