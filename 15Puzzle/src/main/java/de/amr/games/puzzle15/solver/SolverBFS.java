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
 * Breadth-First Search solver for 15-puzzle.
 * 
 * @author Armin Reichert
 */
public class SolverBFS implements Solver {

	protected Queue<Node> q;
	protected int maxQueueSize;
	protected final Set<Puzzle15> visited = new HashSet<>();
	protected Predicate<Solver> givingUpCondition;

	public SolverBFS(Predicate<Solver> givingUpCondition) {
		this.givingUpCondition = givingUpCondition;
	}

	protected void createQueue(int initialCapacity) {
		q = new ArrayDeque<>(initialCapacity);
		maxQueueSize = 0;
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) throws SolverGivingUpException {
		createQueue(1000);
		visited.clear();
		expand(new Node(puzzle));
		while (!q.isEmpty()) {
			Node current = q.poll();
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			current.successors().filter(node -> !visited.contains(node.getPuzzle())).forEach(this::expand);
			if (givingUpCondition.test(this)) {
				throw new SolverGivingUpException("Queue size: " + q.size());
			}
		}
		return Collections.emptyList();
	}

	protected void expand(Node node) {
		q.add(node);
		maxQueueSize = Math.max(q.size(), maxQueueSize);
		visited.add(node.getPuzzle());
	}

	@Override
	public int getMaxQueueSize() {
		return maxQueueSize;
	}
}