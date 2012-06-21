package org.sat4j.sat;

import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.core.LearnedConstraintsEvaluationType;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.SimplificationType;

public interface SolverController {

	public int getNVar();
	
	public void setPhaseSelectionStrategy(IPhaseSelectionStrategy strategy);
	
	public void setLearnedDeletionStrategyTypeToSolver(LearnedConstraintsEvaluationType type);
	
	public void shouldRestartNow();
	
	public RestartStrategy getRestartStrategy();
	
	public void setRestartStrategy(RestartStrategy strategy);
	
	public SearchParams getSearchParams();
	
	public void init(SearchParams params);
	
	public void setNbClausesAtWhichWeShouldClean(int nbConflicts);
	
	public void setUseTelecomStrategyAsLearnedConstraintsDeletionStrategy();
	
	public void shouldCleanNow();
	
	public void setRandomWalkProba(double proba);
	
	public void setSimplifier(SimplificationType type);
}
