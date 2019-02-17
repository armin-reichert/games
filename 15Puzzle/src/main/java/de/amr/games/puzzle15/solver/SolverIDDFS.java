package de.amr.games.puzzle15.solver;

import java.util.List;
import java.util.Optional;

import de.amr.games.puzzle15.model.Puzzle15;

public class SolverIDDFS implements Solver {

	static final int MAX_DEPTH = 50;

	private SolverDepthLimitedDFS dls;
	private int maxFrontierSize;
	private long totalRuntime;

	@Override
	public Optional<List<Node>> solve(Puzzle15 puzzle) throws SolverGivingUpException {
		maxFrontierSize = 0;
		totalRuntime = 0;
		for (int depth = 0; depth <= MAX_DEPTH; ++depth) {
			dls = new SolverDepthLimitedDFS(depth);
			System.out.println("Running DLS including depth " + depth);
			Optional<List<Node>> solution = dls.solve(puzzle);
			maxFrontierSize = Math.max(maxFrontierSize, dls.getMaxFrontierSize());
			totalRuntime += dls.getRunningTime();
			if (solution.isPresent()) {
				return solution;
			}
		}
		return Optional.empty();
	}

	@Override
	public int getMaxFrontierSize() {
		return maxFrontierSize;
	}

	@Override
	public long getRunningTime() {
		return totalRuntime;
	}
}