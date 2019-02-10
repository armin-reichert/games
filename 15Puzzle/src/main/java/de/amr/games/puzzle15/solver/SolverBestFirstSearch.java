package de.amr.games.puzzle15.solver;

import static java.util.Comparator.comparingInt;

import java.util.PriorityQueue;
import java.util.function.Function;

/**
 * Best-First Search.
 * 
 * <p>
 * Always expands the node with the smallest heuristics cost (e.g. estimated remaining distance to
 * the target).
 * 
 * @author Armin Reichert
 *
 */
public class SolverBestFirstSearch extends SolverBFS {

	protected Function<Node, Integer> fnHeuristics;

	public SolverBestFirstSearch(Function<Node, Integer> fnHeuristics) {
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