package de.amr.games.puzzle15.solver;

import java.util.Collections;
import java.util.List;

import de.amr.games.puzzle15.model.Puzzle15;

public class SolverIDDFS implements Solver {

	static final int MAX_DEPTH = 50;

	private SolverDepthLimitedDFS dls;

	@Override
	public List<Node> solve(Puzzle15 puzzle) throws SolverGivingUpException {
		for (int depth = 0; depth <= MAX_DEPTH; ++depth) {
			dls = new SolverDepthLimitedDFS(depth);
			List<Node> solution = dls.solve(puzzle);
			if (!solution.isEmpty()) {
				return solution;
			}
		}
		return Collections.emptyList();
	}

	@Override
	public int getMaxFrontierSize() {
		return dls != null ? dls.getMaxFrontierSize() : 0;
	}

	@Override
	public long getRunningTime() {
		return dls != null ? dls.getRunningTime() : 0;
	}
}
