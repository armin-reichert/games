package de.amr.games.puzzle15.solver;

import static de.amr.games.puzzle15.solver.Heuristics.manhattanDistFromOrdered;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import de.amr.games.puzzle15.model.Dir;
import de.amr.games.puzzle15.model.Puzzle15;

public class PuzzleSolverAStar implements PuzzleSolver {

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
		q = new PriorityQueue<>(Comparator.comparingInt(Node::getScore));
		maxQueueSize = 0;
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) {
		createQueue();
		Set<Node> open = new HashSet<>();
		Set<Node> closed = new HashSet<>();
		Node current = new Node(puzzle);
		current.setDistFromSource(0);
		current.setScore(manhattanDistFromOrdered(current.getPuzzle()));
		open.add(current);
		enqueue(current);
		while (!q.isEmpty()) {
			current = dequeue();
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
					if (!open.contains(child)) {
						open.add(child);
						enqueue(child);
					} else {
						q.remove(child);
						q.add(child);
					}
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