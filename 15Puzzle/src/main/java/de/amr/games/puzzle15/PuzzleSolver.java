package de.amr.games.puzzle15;

import java.util.List;

public interface PuzzleSolver {

	List<Node> solve(Puzzle15 puzzle);
	
	int getMaxQueueSize();
}
