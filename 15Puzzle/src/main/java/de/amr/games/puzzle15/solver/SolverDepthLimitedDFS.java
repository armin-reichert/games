package de.amr.games.puzzle15.solver;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.amr.games.puzzle15.model.Puzzle15;

public class SolverDepthLimitedDFS extends AbstractSolver {

	private final Deque<Node> frontier;
	private final Set<Puzzle15> visited;
	private int maxDepth;

	public SolverDepthLimitedDFS(int maxDepth) {
		super(solver -> false);
		frontier = new ArrayDeque<>();
		visited = new HashSet<>();
		this.maxDepth = maxDepth;
	}

	@Override
	public Optional<List<Node>> solve(Puzzle15 puzzle) throws SolverGivingUpException {
		startClock();
		frontier.clear();
		visited.clear();
		addToFrontier(new Node(puzzle));
		while (!frontier.isEmpty()) {
			maybeGiveUp();
			Node current = frontier.poll();
//			System.out.println("Polling node " + current);
			if (current.getPuzzle().isOrdered()) {
				return Optional.of(solution(current));
			}
			if (current.getDepth() < maxDepth) {
				current.successors().filter(succ -> !visited.contains(succ.getPuzzle())).forEach(this::addToFrontier);
			}
		}
		return Optional.empty();
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