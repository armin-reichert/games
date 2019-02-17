package de.amr.games.puzzle15.solver;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import de.amr.games.puzzle15.model.Puzzle15;

public interface Solver {

	static Predicate<Solver> frontierSizeMax(int size) {
		return solver -> solver.getMaxFrontierSize() > size;
	}

	static Predicate<Solver> runtimeSecMax(int seconds) {
		return solver -> solver.getRunningTime() > 1000 * seconds;
	}

	/**
	 * Runs this solver for the given puzzle.
	 * 
	 * @param puzzle
	 *                 puzzle to solve
	 * @return solution path or empty
	 * @throws SolverGivingUpException
	 */
	Optional<List<Node>> solve(Puzzle15 puzzle) throws SolverGivingUpException;

	int getMaxFrontierSize();

	long getRunningTime();
}
