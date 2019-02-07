package de.amr.games.puzzle15;

import static de.amr.games.puzzle15.Puzzle15.col;
import static de.amr.games.puzzle15.Puzzle15.row;
import static java.util.Comparator.comparingInt;

import java.util.PriorityQueue;

public class PuzzleSolverBestFirstSearch extends PuzzleSolverBFS {

	private static int manhattanDistFromOrdered(Puzzle15 puzzle) {
		int dist = 0;
		for (byte row = 0; row < 4; ++row) {
			for (byte col = 0; col < 4; ++col) {
				byte number = puzzle.get(row, col);
				dist += Math.abs(row(number) - row) + Math.abs(col(number) - col);
			}
		}
		return dist;
	}

	@Override
	protected void createQueue() {
		q = new PriorityQueue<>(comparingInt(Node::getScore));
	}

	@Override
	protected void enqueue(Node node) {
		node.setScore(manhattanDistFromOrdered(node.getPuzzle()));
		q.add(node);
		if (maxQueueSize < q.size()) {
			maxQueueSize++;
		}
		System.out.println("Enqueued node with score " + node.getScore());
	}
}