package de.amr.games.puzzle15.solver;

import static java.util.Comparator.comparingInt;

import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * (Greedy) Best-First Search puzzle solver.
 * 
 * <p>
 * Always expands the node with the smallest cost (e.g. the estimated number of moves leading to the
 * target). Does not take the length of the path to the current node into account. Runs fast but
 * doesn't find the optimal solution in general.
 * 
 * @author Armin Reichert
 */
public class SolverBestFirstSearch extends SolverBFS {

	private Function<Node, Integer> fnNodeCost;

	public SolverBestFirstSearch(Function<Node, Integer> fnNodeCost, Predicate<Solver> givingUpCondition) {
		super(givingUpCondition);
		this.fnNodeCost = fnNodeCost;
	}

	@Override
	protected void createFrontier(int initialCapacity) {
		frontier = new PriorityQueue<>(initialCapacity, comparingInt(Node::getScore));
		resetMaxFrontierSize();
	}

	@Override
	protected void expandFrontier(Node node) {
		node.setScore(fnNodeCost.apply(node));
		super.expandFrontier(node);
	}
}