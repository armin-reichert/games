package de.amr.games.puzzle15.solver;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import de.amr.games.puzzle15.model.Puzzle15;

/**
 * Breadth-First Search solver for 15-puzzle. Rather useless because of memory consumption.
 * 
 * @author Armin Reichert
 */
public class SolverBFS extends AbstractSolver {

	protected Queue<Node> frontier;
	protected final Set<Puzzle15> visited = new HashSet<>();

	public SolverBFS(Predicate<Solver> givingUpCondition) {
		super(givingUpCondition);
	}

	@Override
	protected int getFrontierSize() {
		return frontier.size();
	}

	protected void createFrontier(int initialCapacity) {
		frontier = new ArrayDeque<>(initialCapacity);
		updateMaxFrontierSize();
	}

	@Override
	protected void expandFrontier(Node node) {
		frontier.add(node);
		visited.add(node.getPuzzle());
		updateMaxFrontierSize();
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) throws SolverGivingUpException {
		startClock();
		createFrontier(1000);
		visited.clear();
		expandFrontier(new Node(puzzle));
		while (!frontier.isEmpty()) {
			maybeGiveUp();
			Node current = frontier.poll();
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			current.successors().filter(node -> !visited.contains(node.getPuzzle())).forEach(this::expandFrontier);
		}
		return Collections.emptyList();
	}

}