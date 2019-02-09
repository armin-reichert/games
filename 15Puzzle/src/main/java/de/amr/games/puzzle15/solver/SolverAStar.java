package de.amr.games.puzzle15.solver;

import static java.util.Comparator.comparingInt;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;

import de.amr.games.puzzle15.model.Dir;
import de.amr.games.puzzle15.model.Puzzle15;

/**
 * A* solver for 15-puzzle.
 * 
 * @author Armin Reichert
 */
public class SolverAStar implements Solver {

	private Function<Node, Integer> fnHeuristics;
	private PriorityQueue<Node> q;
	private Map<Node, Node> openList;
	private Set<Node> closedList;
	private int maxQueueSize;

	public SolverAStar(Function<Node, Integer> fnHeuristics) {
		this.fnHeuristics = fnHeuristics;
	}

	@Override
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	private void addToOpenList(Node node) {
		q.add(node);
		openList.put(node, node);
	}

	private void decreaseKey(Node node) {
		q.remove(node);
		q.add(node);
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) {
		q = new PriorityQueue<>(1000, comparingInt(Node::getScore));
		maxQueueSize = 0;
		openList = new HashMap<>();
		closedList = new HashSet<>();

		Node current = new Node(puzzle);
		current.setDistFromSource(0);
		current.setScore(fnHeuristics.apply(current)); // not necessary
		addToOpenList(current);

		while (!q.isEmpty()) {
			current = q.poll();
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			openList.remove(current);
			closedList.add(current);

			for (Dir dir : current.getPuzzle().possibleMoveDirs()) {
				Node successor = new Node(current.getPuzzle().move(dir));
				if (closedList.contains(successor)) {
					continue;
				}
				int tentative_dist = current.getDistFromSource() + 1;
				Node existing = openList.get(successor);
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
					addToOpenList(successor);
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