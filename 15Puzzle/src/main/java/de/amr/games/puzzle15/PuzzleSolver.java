package de.amr.games.puzzle15;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class PuzzleSolver {

	public static class Node {

		public final Puzzle15 puzzle;
		public final Dir dir;
		private Node parent;

		public Node(Puzzle15 puzzle, Dir dir, Node parent) {
			this.puzzle = puzzle;
			this.dir = dir;
			this.parent = parent;
		}

		@Override
		public String toString() {
			return dir == null ? puzzle.toString() : dir + "\n" + puzzle;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dir == null) ? 0 : dir.hashCode());
			result = prime * result + ((puzzle == null) ? 0 : puzzle.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (dir != other.dir)
				return false;
			if (puzzle == null) {
				if (other.puzzle != null)
					return false;
			} else if (!puzzle.equals(other.puzzle))
				return false;
			return true;
		}
	}

	private final Queue<Node> q = new ArrayDeque<>();
	private final Set<Node> visited = new HashSet<>();
	private int maxQueueSize;

	private void enqueue(Node node) {
		q.add(node);
		if (maxQueueSize < q.size()) {
			maxQueueSize++;
		}
	}

	private Node dequeue() {
		return q.poll();
	}

	public List<Node> solve(Puzzle15 puzzle) {
		maxQueueSize = 0;
		Node current = new Node(puzzle, null, null);
		enqueue(current);
		visited.add(current);
		while (!q.isEmpty()) {
			current = dequeue();
			if (current.puzzle.isSolved()) {
				System.out.println("Max Queue size: " + maxQueueSize);
				return solution(current);
			}
			Iterable<Dir> possibleDirs = current.puzzle.possibleMoveDirs()::iterator;
			for (Dir dir : possibleDirs) {
				Node child = new Node(current.puzzle.move(dir), dir, current);
				if (!visited.contains(child)) {
					enqueue(child);
					visited.add(child);
					maxQueueSize = Math.max(q.size(), maxQueueSize);
				}
			}
		}
		return Collections.emptyList();
	}

	private List<Node> solution(Node goal) {
		List<Node> solution = new LinkedList<>();
		Node current = goal;
		while (current != null) {
			solution.add(0, current);
			current = current.parent;
		}
		return solution;
	}
}