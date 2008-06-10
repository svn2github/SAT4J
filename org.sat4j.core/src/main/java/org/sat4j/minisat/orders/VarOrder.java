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
package org.sat4j.minisat.orders;

import static org.sat4j.core.LiteralsUtils.neg;
import static org.sat4j.core.LiteralsUtils.var;

import java.io.PrintWriter;
import java.io.Serializable;

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;

/*
 * Created on 16 oct. 2003
 */

/**
 * @author leberre Heuristique du prouveur. Changement par rapport au MiniSAT
 *         original : la gestion activity est faite ici et non plus dans Solver.
 */
public class VarOrder<L extends ILits> implements Serializable, IOrder<L> {

    private static final long serialVersionUID = 1L;

    /**
     * Comparateur permettant de trier les variables
     */
    private static final double VAR_RESCALE_FACTOR = 1e-100;

    private static final double VAR_RESCALE_BOUND = 1 / VAR_RESCALE_FACTOR;

    /**
     * mesure heuristique de l'activite d'une variable.
     */
    protected double[] activity = new double[1];

    /**
     * Derniere variable choisie
     */
    protected int lastVar = 1;

    /**
     * Ordre des variables
     */
    protected int[] order = new int[1];

    private double varDecay = 1.0;

    /**
     * increment pour l'activite des variables.
     */
    private double varInc = 1.0;

    /**
     * position des variables
     */
    protected int[] varpos = new int[1];

    protected L lits;

    private long nullchoice = 0;

    protected IPhaseSelectionStrategy phaseStrategy;
    
    public VarOrder() {
        this(new PhaseInLastLearnedClauseSelectionStrategy());
    }
    
    public VarOrder(IPhaseSelectionStrategy strategy) {
        this.phaseStrategy = strategy;
    }
    
    /**
     * Change the selection strategy.
     * 
     * @param strategy
     */
    public void setPhaseSelectionStrategy(IPhaseSelectionStrategy strategy) {
        phaseStrategy = strategy;
    }
    
    public IPhaseSelectionStrategy getPhaseSelectionStrategy() {
        return phaseStrategy;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.IOrder#setLits(org.sat4j.minisat.core.ILits)
     */
    public void setLits(L lits) {
        this.lits = lits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.IOrder#select()
     */
    public int select() {
        assert lastVar > 0;
        for (int i = lastVar; i < order.length; i++) {
            assert i > 0;
            if (lits.isUnassigned(order[i])) {
                lastVar = i;
                if (activity[i] < 0.0001) {
                    // if (rand.nextDouble() <= RANDOM_WALK) {
                    // int randomchoice = rand.nextInt(order.length - i) + i;
                    // assert randomchoice >= i;
                    // if ((randomchoice > i)
                    // && lits.isUnassigned(order[randomchoice])) {
                    // randchoice++;
                    // return order[randomchoice];
                    // }
                    // }
                    nullchoice++;
                }
                return order[i];
            }
        }
        return ILits.UNDEFINED;
    }

    /**
     * Change la valeur de varDecay.
     * 
     * @param d
     *            la nouvelle valeur de varDecay
     */
    public void setVarDecay(double d) {
        varDecay = d;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.IOrder#undo(int)
     */
    public void undo(int x) {
        assert x > 0;
        assert x < order.length;
        int pos = varpos[x];
        if (pos < lastVar) {
            lastVar = pos;
        }
        assert lastVar > 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.IOrder#updateVar(int)
     */
    public void updateVar(int p) {
        assert p > 1;
        final int var = var(p);

        updateActivity(var);
        int i = varpos[var];
        for (; i > 1 // because there is nothing at i=0
                && (activity[var(order[i - 1])] < activity[var]); i--) {
            assert i > 1;
            // echange p avec son predecesseur
            final int orderpm1 = order[i - 1];
            assert varpos[var(orderpm1)] == i - 1;
            varpos[var(orderpm1)] = i;
            order[i] = orderpm1;
        }
        assert i >= 1;
        varpos[var] = i;
        order[i] = p;

        if (i < lastVar) {
            lastVar = i;
        }
    }

    protected void updateActivity(final int var) {
        if ((activity[var] += varInc) > VAR_RESCALE_BOUND) {
            varRescaleActivity();
        }
    }

    /**
     * 
     */
    public void varDecayActivity() {
        varInc *= varDecay;
    }

    /**
     * 
     */
    private void varRescaleActivity() {
        for (int i = 1; i < activity.length; i++) {
            activity[i] *= VAR_RESCALE_FACTOR;
        }
        varInc *= VAR_RESCALE_FACTOR;
    }

    public double varActivity(int p) {
        return activity[var(p)];
    }

    /**
     * 
     */
    public int numberOfInterestingVariables() {
        int cpt = 0;
        for (int i = 1; i < activity.length; i++) {
            if (activity[i] > 1.0) {
                cpt++;
            }
        }
        return cpt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.IOrder#init()
     */
    public void init() {
        int nlength = lits.nVars() + 1;
        int reallength = lits.realnVars() + 1;
        int[] nvarpos = new int[nlength];
        double[] nactivity = new double[nlength];
        int[] norder = new int[reallength];
        nvarpos[0] = -1;
        nactivity[0] = -1;
        norder[0] = ILits.UNDEFINED;
        for (int i = 1, j = 1; i < nlength; i++) {
            assert i > 0;
            assert i <= lits.nVars() : "" + lits.nVars() + "/" + i; //$NON-NLS-1$ //$NON-NLS-2$
            if (lits.belongsToPool(i)) {
                norder[j] = neg(lits.getFromPool(i)); // Looks a
                // promising
                // approach
                nvarpos[i] = j++;
            }
            nactivity[i] = 0.0;
        }
        varpos = nvarpos;
        activity = nactivity;
        order = norder;
        lastVar = 1;
    }

    /**
     * Affiche les litteraux dans l'ordre de l'heuristique, la valeur de
     * l'activite entre ().
     * 
     * @return les litteraux dans l'ordre courant.
     */
    @Override
    public String toString() {
        return "VSIDS like heuristics from MiniSAT using a sorted array"; //$NON-NLS-1$
    }

    public ILits getVocabulary() {
        return lits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.IOrder#printStat(java.io.PrintStream,
     *      java.lang.String)
     */
    public void printStat(PrintWriter out, String prefix) {
        out.println(prefix + "non guided choices\t" + nullchoice); //$NON-NLS-1$
        // out.println(prefix + "random choices\t" + randchoice);
    }

    public void assignLiteral(int p) {
        // do nothing       
    }

}
