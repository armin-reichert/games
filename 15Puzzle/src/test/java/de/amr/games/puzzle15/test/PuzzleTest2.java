package de.amr.games.puzzle15.test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.Test;

import de.amr.games.puzzle15.model.Puzzle15;
import de.amr.games.puzzle15.solver.Heuristics;
import de.amr.games.puzzle15.solver.Node;
import de.amr.games.puzzle15.solver.Solver;
import de.amr.games.puzzle15.solver.SolverAStar;
import de.amr.games.puzzle15.solver.SolverGivingUpException;

/**
 * https://codegolf.stackexchange.com/questions/6884/solve-the-15-puzzle-the-tile-sliding-puzzle
 *
 */
public class PuzzleTest2 {

	private void test(int... cells) {
		Puzzle15 puzzle = Puzzle15.of(cells);
		Solver solver = new SolverAStar(node -> Heuristics.manhattanDistFromOrdered(node.getPuzzle()),
				s -> s.getMaxQueueSize() > 1_000_000);
		List<Node> solution;
		try {
			solution = solver.solve(puzzle);
			System.out.println("Solution found, max queue size=" + solver.getMaxQueueSize());
			System.out.println(puzzle);
			System.out.println(solution.stream().map(Node::getDir).filter(Objects::nonNull).map(String::valueOf)
					.collect(Collectors.joining(" ")));
		} catch (SolverGivingUpException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test1() {
		// fast, may queue size = 18
		// DOWN DOWN DOWN LEFT UP UP UP LEFT DOWN DOWN DOWN LEFT UP UP UP
		test(5, 1, 7, 3, 9, 2, 11, 4, 13, 6, 15, 8, 0, 10, 14, 12);
	}

	@Test
	public void test2() {
		// slow
		test(2, 5, 13, 12, 1, 0, 3, 15, 9, 7, 14, 6, 10, 11, 8, 4);
	}

	@Test
	public void test3() {
		// slow, max queue size=161383
		// LEFT UP UP RIGHT RIGHT DOWN LEFT UP LEFT LEFT DOWN DOWN RIGHT RIGHT UP LEFT LEFT DOWN DOWN RIGHT
		// RIGHT UP RIGHT UP LEFT LEFT UP RIGHT DOWN DOWN RIGHT DOWN LEFT LEFT UP UP LEFT UP
		test(5, 2, 4, 8, 10, 0, 3, 14, 13, 6, 11, 12, 1, 15, 9, 7);
	}

	@Test
	public void test4() {
		// slow,
		test(11, 4, 12, 2, 5, 10, 3, 15, 14, 1, 6, 7, 0, 9, 8, 13);
	}

	@Test
	public void test5() {
		// slow
		test(5, 8, 7, 11, 1, 6, 12, 2, 9, 0, 13, 10, 14, 3, 4, 15);
	}
}
