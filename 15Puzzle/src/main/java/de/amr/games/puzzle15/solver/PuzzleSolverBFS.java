package de.amr.games.puzzle15.solver;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.amr.games.puzzle15.model.Dir;
import de.amr.games.puzzle15.model.Puzzle15;

public class PuzzleSolverBFS implements PuzzleSolver {

	protected Queue<Node> q;
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
		maxQueueSize = 0;
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) {
		createQueue();
		Set<Node> visited = new HashSet<>();
		Node current = new Node(puzzle);
		visited.add(current);
		enqueue(current);
		while (!q.isEmpty()) {
			current = dequeue();
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			for (Dir dir : current.getPuzzle().possibleMoveDirs()) {
				Node child = new Node(current.getPuzzle().move(dir));
				if (!visited.contains(child)) {
					child.setDir(dir);
					child.setParent(current);
					visited.add(child);
					enqueue(child);
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