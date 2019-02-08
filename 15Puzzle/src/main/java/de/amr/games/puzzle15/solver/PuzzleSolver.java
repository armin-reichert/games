package de.amr.games.puzzle15.solver;

import java.util.List;

import de.amr.games.puzzle15.model.Puzzle15;

public interface PuzzleSolver {

	List<Node> solve(Puzzle15 puzzle);
	
	int getMaxQueueSize();
}
