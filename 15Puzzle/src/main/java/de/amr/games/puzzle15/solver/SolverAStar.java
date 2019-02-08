package de.amr.games.puzzle15.solver;

import static de.amr.games.puzzle15.solver.Heuristics.manhattanDistFromOrdered;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import de.amr.games.puzzle15.model.Dir;
import de.amr.games.puzzle15.model.Puzzle15;

public class SolverAStar extends SolverBestFirstSearch {

	private Set<Node> open, closed;

	public SolverAStar(Function<Node, Integer> fnHeuristics) {
		super(fnHeuristics);
	}

	@Override
	protected void enqueue(Node node) {
		q.add(node);
		open.add(node);
	}

	private void decreaseKey(Node node) {
		q.remove(node);
		q.add(node);
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) {
		createQueue();
		open = new HashSet<>();
		closed = new HashSet<>();
		Node current = new Node(puzzle);
		current.setDistFromSource(0);
		current.setScore(manhattanDistFromOrdered(current.getPuzzle()));
		enqueue(current);
		while (!q.isEmpty()) {
			current = q.poll();
			open.remove(current);
			closed.add(current);
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			for (Dir dir : current.getPuzzle().possibleMoveDirs()) {
				Node child = new Node(current.getPuzzle().move(dir));
				if (closed.contains(child)) {
					continue;
				}
				int newDist = child.getDistFromSource() + 1;
				if (!open.contains(child) || newDist < child.getDistFromSource()) {
					child.setParent(current);
					child.setDistFromSource(newDist);
					child.setScore(child.getDistFromSource() + manhattanDistFromOrdered(child.getPuzzle()));
					if (open.contains(child)) {
						decreaseKey(child);
					} else {
						enqueue(child);
					}
				}
			}
			if (q.size() > maxQueueSize) {
				maxQueueSize = q.size();
			}
		}
		return Collections.emptyList();
	}
}