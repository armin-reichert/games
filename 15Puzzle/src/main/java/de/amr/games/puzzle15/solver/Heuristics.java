package de.amr.games.puzzle15.solver;

import static de.amr.games.puzzle15.model.Puzzle15.col;
import static de.amr.games.puzzle15.model.Puzzle15.row;

import de.amr.games.puzzle15.model.Puzzle15;

public class Heuristics {

	public static int manhattanDistFromOrdered(Puzzle15 puzzle) {
		int dist = 0;
		for (int row = 0; row < 4; ++row) {
			for (int col = 0; col < 4; ++col) {
				byte number = puzzle.get(row, col);
				if (number != 0) {
					byte orderedIndex = (byte) (number - 1);
					dist += Math.abs(row(orderedIndex) - row) + Math.abs(col(orderedIndex) - col);
				}
			}
		}
		return dist;
	}

	public static int manhattan(Node node) {
		return manhattanDistFromOrdered(node.getPuzzle());
	}
}