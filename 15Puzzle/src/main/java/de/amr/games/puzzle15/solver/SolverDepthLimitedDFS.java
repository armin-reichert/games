package de.amr.games.puzzle15.solver;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.amr.games.puzzle15.model.Puzzle15;

class SolverDepthLimitedDFS extends AbstractSolver {

	private Deque<Node> frontier;
	private final Set<Puzzle15> visited = new HashSet<>();
	private int maxDepth;

	public SolverDepthLimitedDFS(int maxDepth) {
		super(solver -> false);
		this.maxDepth = maxDepth;
		frontier = new ArrayDeque<>();
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) throws SolverGivingUpException {
		startClock();
		frontier.clear();
		visited.clear();
		maxDepth = 0;
		addToFrontier(new Node(puzzle));
		while (!frontier.isEmpty()) {
			maybeGiveUp();
			Node current = frontier.poll();
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			if (current.getDepth() <= maxDepth) {
				current.successors().filter(node -> !visited.contains(node.getPuzzle())).forEach(this::addToFrontier);
			}
		}
		return Collections.emptyList();
	}

	private void addToFrontier(Node node) {
		frontier.push(node);
		visited.add(node.getPuzzle());
		updateMaxFrontierSize();
	}

	@Override
	protected int getFrontierSize() {
		return frontier.size();
	}
}