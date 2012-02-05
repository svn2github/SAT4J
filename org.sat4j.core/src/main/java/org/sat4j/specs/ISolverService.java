package org.sat4j.specs;

/**
 * The aim on that interface is to allow power users to communicate with the SAT
 * solver using Dimacs format. That way, there is no need to know the internals
 * of the solver.
 * 
 * @author leberre
 * @since 2.3.2
 */
public interface ISolverService {

	/**
	 * Ask the SAT solver to stop the search.
	 */
	void stop();

	/**
	 * Ask the SAT solver to backtrack. It is mandatory to provide a reason for
	 * backtracking, in terms of literals (which should be falsified under
	 * current assignment).
	 * 
	 * @param reason
	 *            a set of literals, in Dimacs format, currently falsified, i.e.
	 *            for (int l : reason) assert truthValue(l) == Lbool.FALSE
	 */
	void backtrack(int[] reason);

	/**
	 * To access the truth value of a specific literal under current assignment.
	 * 
	 * @param literal
	 *            a Dimacs literal, i.e. a non-zero integer.
	 * @return true or false if the literal is assigned, else undefined.
	 */
	Lbool truthValue(int literal);

	/**
	 * To access the current decision level
	 */
	int currentDecisionLevel();

	/**
	 * To access the literals propagated at a specific decision level.
	 * 
	 * @param decisionLevel
	 *            a decision level between 0 and #currentDecisionLevel()
	 */
	int[] getLiteralsPropagatedAt(int decisionLevel);

	/**
	 * Suggests to the SAT solver to branch next on the given literal.
	 * 
	 * @param l
	 *            a literal in Dimacs format.
	 */
	void suggestNextLiteralToBranchOn(int l);
}
