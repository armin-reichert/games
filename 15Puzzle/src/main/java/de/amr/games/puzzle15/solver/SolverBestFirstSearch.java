package de.amr.games.puzzle15.solver;

import static java.util.Comparator.comparingInt;

import java.util.PriorityQueue;
import java.util.function.Function;

public class SolverBestFirstSearch extends SolverBFS {

	protected Function<Node, Integer> fnHeuristics;

	public SolverBestFirstSearch(Function<Node, Integer> fnHeuristics) {
		this.fnHeuristics = fnHeuristics;
	}

	@Override
	protected void createQueue(int initialCapacity) {
		q = new PriorityQueue<>(initialCapacity, comparingInt(Node::getScore));
	}

	@Override
	protected void enqueue(Node node) {
		node.setScore(fnHeuristics.apply(node));
		q.add(node);
	}
}