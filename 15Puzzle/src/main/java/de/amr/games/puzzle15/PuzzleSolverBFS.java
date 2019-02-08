package de.amr.games.puzzle15;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class PuzzleSolverBFS implements PuzzleSolver {

	protected Queue<Node> q;
	protected Set<Node> visited;
	protected int maxQueueSize;

	protected void enqueue(Node node) {
		q.add(node);
		if (maxQueueSize < q.size()) {
			maxQueueSize++;
		}
	}

	protected Node dequeue() {
		return q.poll();
	}

	protected void createQueue() {
		q = new ArrayDeque<>();
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) {
		createQueue();
		visited = new HashSet<>();
		maxQueueSize = 0;
		Node current = new Node(puzzle, null, null);
		enqueue(current);
		visited.add(current);
		while (!q.isEmpty()) {
			current = dequeue();
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			Iterable<Dir> possibleDirs = current.getPuzzle().possibleMoveDirs()::iterator;
			for (Dir dir : possibleDirs) {
				Node child = new Node(current.getPuzzle().move(dir), dir, current);
				if (!visited.contains(child)) {
					enqueue(child);
					visited.add(child);
					maxQueueSize = Math.max(q.size(), maxQueueSize);
				}
			}
		}
		return Collections.emptyList();
	}

	protected List<Node> solution(Node goal) {
		List<Node> solution = new LinkedList<>();
		for (Node current = goal; current != null; current = current.getParent()) {
			solution.add(0, current);
		}
		return solution;
	}

	@Override
	public int getMaxQueueSize() {
		return maxQueueSize;
	}
}