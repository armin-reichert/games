package de.amr.games.puzzle15.solver;

import static java.util.Comparator.comparingInt;

import java.util.function.ToIntFunction;

public class SolverHillClimbing extends SolverDepthLimitedDFS {

	private ToIntFunction<Node> h;

	public SolverHillClimbing() {
		super(Integer.MAX_VALUE);
		h = Heuristics::manhattan;
	}

	@Override
	protected void expand(Node node) {
		/*@formatter:off*/
		node.successors()
			.filter(succ -> !visited.contains(succ.getPuzzle()))
			.sorted(comparingInt(h).reversed())
			.forEach(this::addToFrontier);
		/*@formatter:on*/
	}
}