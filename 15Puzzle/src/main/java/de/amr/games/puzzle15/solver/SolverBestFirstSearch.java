package de.amr.games.puzzle15.solver;

import static java.util.Comparator.comparingInt;

import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Best-First Search puzzle solver.
 * 
 * <p>
 * Always expands the node with the smallest heuristic cost (e.g. the estimated number of moves
 * leading to the target).
 * 
 * @author Armin Reichert
 *
 */
public class SolverBestFirstSearch extends SolverBFS {

	private Function<Node, Integer> fnHeuristics;

	public SolverBestFirstSearch(Function<Node, Integer> fnHeuristics, Predicate<Solver> givingUpCondition) {
		super(givingUpCondition);
		this.fnHeuristics = fnHeuristics;
	}

	@Override
	protected void createQueue(int initialCapacity) {
		q = new PriorityQueue<>(initialCapacity, comparingInt(Node::getScore));
		maxQueueSize = 0;
	}

	@Override
	protected void expand(Node node) {
		node.setScore(fnHeuristics.apply(node));
		super.expand(node);
	}
}