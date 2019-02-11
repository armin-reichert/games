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
import java.util.function.Predicate;

import de.amr.games.puzzle15.model.Dir;
import de.amr.games.puzzle15.model.Puzzle15;

/**
 * A* solver for 15-puzzle.
 * 
 * @author Armin Reichert
 */
public class SolverAStar implements Solver {

	private Function<Node, Integer> fnHeuristics;
	private PriorityQueue<Node> frontier;
	private Map<Puzzle15, Node> openList;
	private Set<Puzzle15> closedList;
	private int maxQueueSize;
	private Predicate<Solver> givingUpCondition;
	private long startTime;

	public SolverAStar(Function<Node, Integer> fnHeuristics, Predicate<Solver> givingUpCondition) {
		this.fnHeuristics = fnHeuristics;
		this.givingUpCondition = givingUpCondition;
	}

	@Override
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	private void addToFrontier(Node node) {
		frontier.add(node);
		if (frontier.size() > maxQueueSize) {
			if (frontier.size() / 10_000 > maxQueueSize / 10_000) {
				System.out.println("Queue size=" + frontier.size());
			}
			maxQueueSize = frontier.size();
		}
		openList.put(node.getPuzzle(), node);
	}

	@Override
	public List<Node> solve(Puzzle15 puzzle) throws SolverGivingUpException {
		startTime = System.nanoTime();
		frontier = new PriorityQueue<>(1000, comparingInt(Node::getScore));
		maxQueueSize = 0;
		openList = new HashMap<>();
		closedList = new HashSet<>();

		addToFrontier(new Node(puzzle));

		while (!frontier.isEmpty()) {
			Node current = frontier.poll();
			if (current.getPuzzle().isOrdered()) {
				return solution(current);
			}
			openList.remove(current.getPuzzle());
			closedList.add(current.getPuzzle());

			Iterable<Dir> possibleDirs = current.getPuzzle().possibleMoveDirs()::iterator;
			for (Dir dir : possibleDirs) {
				Puzzle15 nextPuzzle = current.getPuzzle().move(dir);
				if (closedList.contains(nextPuzzle)) {
					continue;
				}
				int numMoves = current.getMovesSoFar() + 1;
				boolean revisited = openList.containsKey(nextPuzzle);
				if (revisited && numMoves >= openList.get(nextPuzzle).getMovesSoFar()) {
					continue;
				}
				Node next = new Node(nextPuzzle);
				next.setParent(current);
				next.setDir(dir);
				next.setMovesSoFar(numMoves);
				next.setScore(numMoves + fnHeuristics.apply(next));
				if (revisited) {
					// "decrease-key"
					frontier.remove(next); // removes the existing node from the queue!
					frontier.add(next);
				} else {
					addToFrontier(next);
				}
			}
			if (givingUpCondition.test(this)) {
				throw new SolverGivingUpException(
						"Queue size: " + frontier.size() + ", running (millis): " + runningTimeMillis());
			}
		}
		return Collections.emptyList();
	}

	@Override
	public long runningTimeMillis() {
		return (System.nanoTime() - startTime) / 1_000_000L;
	}
}