package de.amr.games.puzzle15.solver;

import java.util.List;
import java.util.function.Predicate;

import de.amr.games.puzzle15.model.Puzzle15;

public interface Solver {

	static Predicate<Solver> queueSizeOver(int size) {
		return solver -> solver.getMaxFrontierSize() > size;
	}

	static Predicate<Solver> runtimeOver(int millis) {
		return solver -> solver.getRunningTime() > millis;
	}

	/**
	 * Runs this solver for the given puzzle.
	 * 
	 * @param puzzle
	 *                 puzzle to solve
	 * @return solution path or empty list
	 * @throws SolverGivingUpException
	 */
	List<Node> solve(Puzzle15 puzzle) throws SolverGivingUpException;

	int getMaxFrontierSize();

	long getRunningTime();
}
