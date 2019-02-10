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
	private Map<Puzzle15, Node> openList;
	private Set<Puzzle15> closedList;
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
		if (q.size() > maxQueueSize) {
			if (q.size() / 10_000 > maxQueueSize / 10_000) {
				System.out.println("Queue size=" + q.size());
			}
			maxQueueSize = q.size();
		}
		openList.put(node.getPuzzle(), node);
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
		current.setDistFromSource(0); // not necessary
		current.setScore(fnHeuristics.apply(current)); // not necessary
		addToOpenList(current);

		while (!q.isEmpty()) {
			current = q.poll();
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			openList.remove(current.getPuzzle());
			closedList.add(current.getPuzzle());

			Iterable<Dir> possibleDirs = current.getPuzzle().possibleMoveDirs()::iterator;
			for (Dir dir : possibleDirs) {
				Node successor = new Node(current.getPuzzle().move(dir));
				if (closedList.contains(successor.getPuzzle())) {
					continue;
				}
				int tentative_dist = current.getDistFromSource() + 1;
				boolean alreadyInOpenList = openList.containsKey(successor.getPuzzle());
				if (alreadyInOpenList && tentative_dist >= openList.get(successor.getPuzzle()).getDistFromSource()) {
					continue;
				}
				successor.setDir(dir);
				successor.setParent(current);
				successor.setDistFromSource(tentative_dist);
				successor.setScore(tentative_dist + fnHeuristics.apply(successor));
				if (alreadyInOpenList) {
					decreaseKey(successor);
				} else {
					addToOpenList(successor);
				}
			}
		}
		return Collections.emptyList();
	}
}