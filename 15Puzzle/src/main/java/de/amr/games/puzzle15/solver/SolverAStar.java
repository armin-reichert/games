package de.amr.games.puzzle15.solver;

import static java.util.Comparator.comparingInt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import de.amr.games.puzzle15.model.Dir;
import de.amr.games.puzzle15.model.Puzzle15;

/**
 * A*-based solver for 15-puzzle. Finds the optimal solution but needs much memory and runs rather
 * slowly.
 * 
 * @author Armin Reichert
 */
public class SolverAStar extends AbstractSolver {

	private final Function<Node, Integer> fnHeuristicNodeCost;
	private final PriorityQueue<Node> frontier;
	private final Map<Puzzle15, Node> openList;
	private final Set<Puzzle15> closedList;

	public SolverAStar(Function<Node, Integer> fnHeuristicNodeCost, Predicate<Solver> givingUpCondition) {
		super(givingUpCondition);
		this.fnHeuristicNodeCost = fnHeuristicNodeCost;
		frontier = new PriorityQueue<>(1000, comparingInt(Node::getScore));
		openList = new HashMap<>();
		closedList = new HashSet<>();
	}

	@Override
	protected int getFrontierSize() {
		return frontier.size();
	}

	protected void expandFrontier(Node node) {
		frontier.add(node);
		openList.put(node.getPuzzle(), node);
		updateMaxFrontierSize();
	}

	@Override
	public Optional<List<Node>> solve(Puzzle15 puzzle) throws SolverGivingUpException {
		startClock();
		resetMaxFrontierSize();

		frontier.clear();
		openList.clear();
		closedList.clear();

		expandFrontier(new Node(puzzle));

		while (!frontier.isEmpty()) {
			maybeGiveUp();
			Node current = frontier.poll();
			if (current.getPuzzle().isOrdered()) {
				return Optional.of(solution(current));
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
				next.setScore(numMoves + fnHeuristicNodeCost.apply(next));
				if (revisited) {
					// "decrease-key"
					frontier.remove(next); // removes the existing node from the queue!
					frontier.add(next);
				} else {
					expandFrontier(next);
				}
			}
		}
		return Optional.empty();
	}
}