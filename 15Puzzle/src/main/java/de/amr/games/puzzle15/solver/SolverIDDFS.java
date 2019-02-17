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
		System.out.print("Running DLS of depth: ");
		for (int depth = 0; depth <= MAX_DEPTH; ++depth) {
			System.out.print(" " + depth);
			dls = new SolverDepthLimitedDFS(depth);
			Optional<List<Node>> solution = dls.solve(puzzle);
			maxFrontierSize = Math.max(maxFrontierSize, dls.getMaxFrontierSize());
			totalRuntime += dls.getRunningTime();
			if (solution.isPresent()) {
				System.out.println();
				return solution;
			}
		}
		System.out.println();
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