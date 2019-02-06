package de.amr.games.puzzle15;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	}

	public void solve(Puzzle puzzle) {
		Puzzle goal = new Puzzle(puzzle.size());
		Deque<Puzzle> q = new ArrayDeque<>();
		Set<Puzzle> visited = new HashSet<>();
		Map<Puzzle, Puzzle> parent = new HashMap<>();
		Map<Puzzle, Dir> parentDir = new HashMap<>();
		q.add(puzzle);
		visited.add(puzzle);
		while (!q.isEmpty()) {
			Puzzle current = q.poll();
			if (current.equals(goal)) {
				break;
			}
			current.possibleMoves().forEach(dir -> {
				Puzzle child = current.move(dir);
				if (!visited.contains(child)) {
					q.add(child);
					visited.add(child);
					parent.put(child, current);
					parentDir.put(child, dir);
				}
			});
		}
		List<Node> solution = new LinkedList<>();
		Puzzle p = goal;
		while (p != null) {
			solution.add(0, new Node(p, parentDir.get(p)));
			p = parent.get(p);
		}
		solution.forEach(node -> {
			System.out.println(node);
		});
		solution.stream().filter(node -> node.dir != null).forEach(node -> {
			System.out.println(node.dir);
		});
	}
}
