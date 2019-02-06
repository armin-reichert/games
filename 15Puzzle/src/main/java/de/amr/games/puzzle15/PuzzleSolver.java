package de.amr.games.puzzle15;

import static java.util.stream.Collectors.toList;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class PuzzleSolver {

	public static class Node {

		public Puzzle puzzle;
		public Dir dir;

		public Node(Puzzle puzzle, Dir dir) {
			this.puzzle = puzzle;
			this.dir = dir;
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

	private final Map<Node, Node> parent = new HashMap<>();

	public List<Node> solve(Puzzle puzzle) {

		Queue<Node> q = new ArrayDeque<>();
		Set<Puzzle> visited = new HashSet<>();
		Puzzle orderedPuzzle = new Puzzle(puzzle.size());

		Node current = new Node(puzzle, null);
		q.add(current);
		visited.add(current.puzzle);

		while (!q.isEmpty()) {
			current = q.poll();
			System.out.println("Queue size: " + q.size());
			if (current.puzzle.equals(orderedPuzzle)) {
				return solution(current);
			}
			for (Dir dir : current.puzzle.possibleMoveDirs().collect(toList())) {
				Node child = new Node(current.puzzle.move(dir), dir);
				if (!visited.contains(child.puzzle)) {
					q.add(child);
					visited.add(child.puzzle);
					parent.put(child, current);
					System.out.println("Queue size: " + q.size());
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
			current = parent.get(current);
		}
		return solution;
	}
}
