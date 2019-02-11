package de.amr.games.puzzle15.solver;

import java.util.LinkedList;
import java.util.List;

import de.amr.games.puzzle15.model.Puzzle15;

public interface Solver {

	List<Node> solve(Puzzle15 puzzle) throws SolverGivingUpException;

	default List<Node> solution(Node goal) {
		List<Node> solution = new LinkedList<>();
		for (Node current = goal; current != null; current = current.getParent()) {
			solution.add(0, current);
		}
		return solution;
	}

	int getMaxQueueSize();
	
	long runningTimeMillis();
}
