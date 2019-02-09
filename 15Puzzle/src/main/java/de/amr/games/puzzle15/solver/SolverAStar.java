package de.amr.games.puzzle15.solver;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.amr.games.puzzle15.model.Dir;
import de.amr.games.puzzle15.model.Puzzle15;

public class SolverAStar extends SolverBestFirstSearch {

	private Set<Node> closed;
	private Map<Node, Node> open;

	public SolverAStar(Function<Node, Integer> fnHeuristics) {
		super(fnHeuristics);
	}

	@Override
	protected void enqueue(Node node) {
		q.add(node);
		open.put(node, node);
	}

	private void decreaseKey(Node node) {
		q.remove(node);
		q.add(node);
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) {
		createQueue(1000);
		open = new HashMap<>();
		closed = new HashSet<>();
		Node current = new Node(puzzle);
		current.setDistFromSource(0);
		// current.setScore(fnHeuristics.apply(current));
		enqueue(current);
		while (!q.isEmpty()) {
			current = q.poll();
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			open.remove(current);
			closed.add(current);
			// expand current node
			for (Dir dir : current.getPuzzle().possibleMoveDirs()) {
				Node successor = new Node(current.getPuzzle().move(dir));
				if (closed.contains(successor)) {
					continue;
				}
				int tentative_dist = current.getDistFromSource() + 1;
				Node existing = open.get(successor);
				if (existing != null && tentative_dist >= existing.getDistFromSource()) {
					continue;
				}
				successor.setDir(dir);
				successor.setParent(current);
				successor.setDistFromSource(tentative_dist);
				successor.setScore(tentative_dist + fnHeuristics.apply(successor));
				if (existing != null) {
					decreaseKey(successor);
				} else {
					enqueue(successor);
				}
			}
			if (q.size() > maxQueueSize) {
				if (q.size() / 10_000 > maxQueueSize / 10_000) {
					System.out.println("Queue size=" + q.size());
				}
				maxQueueSize = q.size();
			}
		}
		return Collections.emptyList();
	}
}