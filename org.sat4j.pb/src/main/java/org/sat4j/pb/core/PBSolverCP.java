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
* Based on the pseudo boolean algorithms described in:
* A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
* Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
* Volume 24, Issue 3, March 2005 Page(s): 305 - 317
* 
* and 
* Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
* Framework. Ph.D. Dissertation, University of Oregon.
*******************************************************************************/
package org.sat4j.pb.core;

import org.sat4j.core.Vec;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.AssertingClauseGenerator;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.Pair;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.pb.constraints.pb.ConflictMap;
import org.sat4j.pb.constraints.pb.IConflict;
import org.sat4j.pb.constraints.pb.PBConstr;
import org.sat4j.specs.IVec;

/**
 * @author parrain To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PBSolverCP<L extends ILits> extends PBSolver<L> {

    private static final long serialVersionUID = 1L;
    
    /**
     * @param acg
     * @param learner
     * @param dsf
     */
    public PBSolverCP(AssertingClauseGenerator acg, LearningStrategy<L,PBDataStructureFactory<L>> learner,
            PBDataStructureFactory<L> dsf, IOrder<L> order) {
        super(acg, learner, dsf, new SearchParams(1.5, 100), order,new MiniSATRestarts());
    }

    public PBSolverCP(AssertingClauseGenerator acg, LearningStrategy<L,PBDataStructureFactory<L>> learner, PBDataStructureFactory<L> dsf, SearchParams params, IOrder<L> order, RestartStrategy restarter) {
        super(acg, learner, dsf, params, order, restarter);
    }

    public PBSolverCP(AssertingClauseGenerator acg, LearningStrategy<L,PBDataStructureFactory<L>> learner, PBDataStructureFactory<L> dsf, SearchParams params, IOrder<L> order) {
        super(acg, learner, dsf, params, order,new MiniSATRestarts());
    }

     
    @Override
    public void analyze(Constr myconfl, Pair results) {
        int litImplied = trail.last();
        int currentLevel = voc.getLevel(litImplied);
        IConflict confl = chooseConflict((PBConstr)myconfl, currentLevel);
        initExplanation();
        buildExplanation(litImplied,myconfl);
        assert confl.slackConflict().signum() < 0;
        while (!confl.isAssertive(currentLevel)) {
            PBConstr constraint = (PBConstr)voc.getReason(litImplied);
            buildExplanation(litImplied,constraint);
            // result of the resolution is in the conflict (confl)
            confl.resolve(constraint, litImplied, this);
            assert confl.slackConflict().signum() <= 0;
            // implication trail is reduced
            if (trail.size() == 1)
                break;
            undoOne();
            //assert decisionLevel() >= 0;
            if (decisionLevel() == 0)
            	break;
            litImplied = trail.last();
            if (voc.getLevel(litImplied) != currentLevel) {
                trailLim.pop();
                confl.updateSlack(voc.getLevel(litImplied));
            }
            assert voc.getLevel(litImplied) <= currentLevel;
            currentLevel = voc.getLevel(litImplied);
            assert confl.slackIsCorrect(currentLevel);
            assert currentLevel == decisionLevel();
            assert litImplied > 1;
        }
    	assert confl.isAssertive(currentLevel) || trail.size() == 1 || decisionLevel() == 0;

        assert currentLevel == decisionLevel();
        undoOne();

        // necessary informations to build a PB-constraint
        // are kept from the conflict
        if ((confl.size()==0) || ((decisionLevel() == 0 || trail.size() == 0) && confl.slackConflict().signum() < 0)){ 
        	results.reason = null;
        	results.backtrackLevel= -1;
        	return;
        }
        
        // assertive PB-constraint is build and referenced
        PBConstr resConstr = (PBConstr) dsfactory
              .createUnregisteredPseudoBooleanConstraint(confl);
        
        results.reason = resConstr;
        
        // the conflict give the highest decision level for the backtrack 
        // (which is less than current level) 
    	// assert confl.isAssertive(currentLevel);
        if (decisionLevel() == 0 || trail.size() == 0) 
        	results.backtrackLevel = -1;
        else
        	results.backtrackLevel =  confl.getBacktrackLevel(currentLevel);
    }

    @Override
    protected void analyzeAtRootLevel(Constr myconfl) {
        // first literal implied in the conflict
        int litImplied = trail.last();
        initExplanation();
        buildExplanation(litImplied,myconfl);
        while (!trail.isEmpty()) {
            PBConstr constraint = (PBConstr)voc.getReason(litImplied);
            if (constraint != null)
            	buildExplanation(litImplied,constraint);
            trail.pop();
            if (!trail.isEmpty())
            	litImplied = trail.last();
        }
    }

    IConflict chooseConflict(PBConstr myconfl, int level) {
        return ConflictMap.createConflict(myconfl, level);
    }

    @Override
    public String toString(String prefix) {
        return prefix + "Cutting planes based inference ("
                + this.getClass().getName() + ")\n" + super.toString(prefix);
    }
    
    private IVec<String> conflictVariables = new Vec<String>();
    private IVec<String> conflictConstraints = new Vec<String>();
    
    void initExplanation(){
    	conflictVariables.clear();
    	conflictConstraints.clear();
    }
    
    void buildExplanation(int lit, Constr c){
    	if ((listOfVariables != null) && (listOfVariables.contains(lit>>1))){
    			conflictVariables.push(Lits.toString(lit));
    			conflictConstraints.push(c.toString());
    	}
    }

    @Override
	public String getExplanation(){
    	if (!conflictVariables.isEmpty()){
    		assert conflictVariables.size() == conflictConstraints.size();
    		StringBuffer sb = new StringBuffer();
    		sb.append("Variables and constraints involved in unsatisfiability : \n");
    		for(int i = 0; i<conflictVariables.size();i++){
    			sb.append("Var : "+conflictVariables.get(i));
    			sb.append(" - Constr : "+conflictConstraints.get(i));
    			sb.append("\n");
    		}
    		return sb.toString();
    	}
    	return "";
    }
}
