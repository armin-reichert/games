package de.amr.games.puzzle15.test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import de.amr.games.puzzle15.model.Puzzle15;
import de.amr.games.puzzle15.solver.Heuristics;
import de.amr.games.puzzle15.solver.Node;
import de.amr.games.puzzle15.solver.Solver;
import de.amr.games.puzzle15.solver.SolverAStar;
import de.amr.games.puzzle15.solver.SolverGivingUpException;
import de.amr.games.puzzle15.solver.SolverHillClimbing;
import de.amr.games.puzzle15.solver.SolverIDDFS;

/**
 * https://codegolf.stackexchange.com/questions/6884/solve-the-15-puzzle-the-tile-sliding-puzzle
 *
 */
public class PuzzleSolverTest {

	private void test(Solver solver, int... cells) {
		if (cells.length != 16) {
			throw new IllegalArgumentException("Illegal number of cells" + cells.length);
		}
		Puzzle15 puzzle = Puzzle15.of(cells);
		System.out.println(puzzle);
		try {
			Optional<List<Node>> solution = solver.solve(puzzle);
			if (!solution.isPresent()) {
				System.out.println("No solution found");
			} else {
				System.out.println(String.format("Solution length=%d, max queue size=%,d", solution.get().size(),
						solver.getMaxFrontierSize()));
				System.out.println(solution.get().stream().map(Node::getDir).filter(Objects::nonNull).map(String::valueOf)
						.collect(Collectors.joining(" ")));
				System.out.println();
			}
		} catch (SolverGivingUpException e) {
			e.printStackTrace();
		}
	}

	private void testAStar(int... cells) {
		System.out.println("A* solver:");
		test(new SolverAStar(node -> Heuristics.manhattanDistFromOrdered(node.getPuzzle()),
				s -> s.getMaxFrontierSize() > 200_000), cells);
	}

	private void testHillClimbing(int... cells) {
		System.out.println("Hill-climbing solver:");
		test(new SolverHillClimbing(), cells);
	}

	private void testIDDFS(int... cells) {
		System.out.println("IDDFS solver:");
		test(new SolverIDDFS(), cells);
	}

	@Test
	public void test1() {
		// DOWN DOWN DOWN LEFT UP UP UP LEFT DOWN DOWN DOWN LEFT UP UP UP

		// A*: fast, Solution length=16, max queue size=18
		testAStar(5, 1, 7, 3, 9, 2, 11, 4, 13, 6, 15, 8, 0, 10, 14, 12);

		// IDDFS: medium, Solution length=26, max queue size=34
		testIDDFS(5, 1, 7, 3, 9, 2, 11, 4, 13, 6, 15, 8, 0, 10, 14, 12);

		// Hill-climbing: fast, Solution length=16, max queue size=18
		testHillClimbing(5, 1, 7, 3, 9, 2, 11, 4, 13, 6, 15, 8, 0, 10, 14, 12);
	}

	@Test
	public void test2() {
		testAStar(2, 5, 13, 12, 1, 0, 3, 15, 9, 7, 14, 6, 10, 11, 8, 4);
		testHillClimbing(2, 5, 13, 12, 1, 0, 3, 15, 9, 7, 14, 6, 10, 11, 8, 4);
	}

	@Test
	public void test3() {
		// LEFT UP UP RIGHT RIGHT DOWN LEFT UP LEFT LEFT DOWN DOWN RIGHT RIGHT UP LEFT LEFT DOWN DOWN RIGHT
		// RIGHT UP RIGHT UP LEFT LEFT UP RIGHT DOWN DOWN RIGHT DOWN LEFT LEFT UP UP LEFT UP

		// A*: slow, Solution length=39, max queue size=161.383
		testAStar(5, 2, 4, 8, 10, 0, 3, 14, 13, 6, 11, 12, 1, 15, 9, 7);

		// IDDFS: very slow, out of memory
//		testIDDFS(5, 2, 4, 8, 10, 0, 3, 14, 13, 6, 11, 12, 1, 15, 9, 7);

		testHillClimbing(5, 2, 4, 8, 10, 0, 3, 14, 13, 6, 11, 12, 1, 15, 9, 7);
	}

	@Test
	public void test4() {
		testAStar(11, 4, 12, 2, 5, 10, 3, 15, 14, 1, 6, 7, 0, 9, 8, 13);
	}

	@Test
	public void test5() {

		// A*:
		testAStar(5, 8, 7, 11, 1, 6, 12, 2, 9, 0, 13, 10, 14, 3, 4, 15);
	}
}