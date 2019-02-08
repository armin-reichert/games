package de.amr.games.puzzle15.solver;

import static java.util.Comparator.comparingInt;

import java.util.PriorityQueue;
import java.util.function.Function;

public class PuzzleSolverBestFirstSearch extends PuzzleSolverBFS {

	private Function<Node, Integer> fnHeuristics;

	public PuzzleSolverBestFirstSearch(Function<Node, Integer> fnHeuristics) {
		this.fnHeuristics = fnHeuristics;
	}

	@Override
	protected void createQueue() {
		q = new PriorityQueue<>(comparingInt(Node::getScore));
		maxQueueSize = 0;
	}

	@Override
	protected void enqueue(Node node) {
		node.setScore(fnHeuristics.apply(node));
		q.add(node);
		if (maxQueueSize < q.size()) {
			maxQueueSize++;
			if (maxQueueSize % 1000 == 0) {
				System.out.println("Queue size reached " + maxQueueSize);
			}
		}
	}
}